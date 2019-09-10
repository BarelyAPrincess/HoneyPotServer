package com.marchnetworks.app.service;

import com.marchnetworks.app.core.OsgiManager;
import com.marchnetworks.app.dao.AppDAO;
import com.marchnetworks.app.data.App;
import com.marchnetworks.app.data.AppIdentityParser;
import com.marchnetworks.app.data.AppParseException;
import com.marchnetworks.app.data.AppStatus;
import com.marchnetworks.app.data.AppXmlDescriptor;
import com.marchnetworks.app.data.TestApp;
import com.marchnetworks.app.events.AppEvent;
import com.marchnetworks.app.events.AppEventType;
import com.marchnetworks.app.events.AppStateEvent;
import com.marchnetworks.app.model.AppEntity;
import com.marchnetworks.app.task.AppInstallTask;
import com.marchnetworks.audit.data.AuditEventNameEnum;
import com.marchnetworks.audit.data.AuditView;
import com.marchnetworks.audit.events.AuditEvent;
import com.marchnetworks.command.api.app.AppCoreService;
import com.marchnetworks.command.api.initialization.InitializationListener;
import com.marchnetworks.command.api.uninstall.UninstallService;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.app.AppException;
import com.marchnetworks.command.common.app.AppExceptionTypeEnum;
import com.marchnetworks.command.common.scheduling.TaskScheduler;
import com.marchnetworks.command.common.user.UserException;
import com.marchnetworks.command.common.user.data.MemberView;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.utils.CommonUtils;
import com.marchnetworks.common.utils.CompressionUtils;
import com.marchnetworks.common.utils.DateUtils;
import com.marchnetworks.common.utils.ServerUtils;
import com.marchnetworks.license.LicenseService;
import com.marchnetworks.license.model.AppType;
import com.marchnetworks.license.model.ApplicationIdentityToken;
import com.marchnetworks.management.topology.ResourceTopologyServiceIF;
import com.marchnetworks.management.user.UserService;
import com.marchnetworks.server.event.AppEventService;
import com.marchnetworks.server.event.EventRegistry;

import org.apache.commons.io.FileUtils;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AppManagerImpl implements AppManager, AppCoreService, InitializationListener
{
	private static final Logger LOG = LoggerFactory.getLogger( AppManagerImpl.class );

	private static String serverVersion;

	private OsgiManager osgiManager;
	private AppDAO appDAO;
	private EventRegistry eventRegistry;
	private LicenseService licenseService;
	private ResourceTopologyServiceIF topologyService;
	private TaskScheduler taskScheduler;
	private AppEventService appEventService;
	private UserService userService;
	private Object appInitialization = new Object();
	private boolean appInitialized = false;

	public void onAppInitialized()
	{
		serverVersion = ServerUtils.getServerMajorMinorVersion();

		List<AppEntity> apps = appDAO.findAll();

		for ( AppEntity app : apps )
		{
			if ( CommonUtils.compareVersions( serverVersion, app.getMinimumCESVersion() ) < 0 )
			{
				app.setStatus( AppStatus.NOTSUPPORTED );
				LOG.info( "App to be started " + app + " requires minimum CES version " + app.getMinimumCESVersion() + " and is not supported on CES version " + serverVersion );
			}
			else
			{
				if ( app.getStatus() == AppStatus.NOTSUPPORTED )
				{
					app.setStatus( AppStatus.INSTALLED );
					LOG.info( "App " + app + " previously unsupported is now supported" );
				}

				if ( !AppCompatability.isCompatible( app.getGuid(), app.getVersion() ) )
				{
					app.setStatus( AppStatus.UPGRADE_REQUIRED );
					LOG.info( "App to be started " + app + " requires minimum App version " + AppCompatability.getMinimumVersion( app.getGuid() ) + " to run on CES version " + serverVersion );
				}
				else
				{
					boolean licenseValid = true;
					if ( app.requiresLicense() )
					{
						licenseValid = licenseService.checkAppLicense( app.getGuid() );
					}
					AppStatus status = app.getStatus();

					if ( ( !licenseValid ) && ( ( status == AppStatus.RUNNING ) || ( status == AppStatus.INSTALLED ) ) )
					{
						app.setStatus( AppStatus.UNLICENSED );
						LOG.info( "App " + app + " is no longer licensed" );
					}
					else if ( ( licenseValid ) && ( status == AppStatus.RUNNING ) && ( app.hasServerFile() ) )
					{
						try
						{
							String appFile = app.getAppServerFile();
							long bundleId = getOsgiManager().installBundle( appFile );
							app.setBundleId( Long.valueOf( bundleId ) );
						}
						catch ( BundleException e )
						{
							LOG.error( "Could not install server App bundle " + app + " on CES startup", e );
						}
					}
					else if ( ( licenseValid ) && ( app.getStatus() == AppStatus.UNLICENSED ) )
					{
						LOG.info( "App " + app + " previously unlicensed is now licensed" );
						app.setStatus( AppStatus.INSTALLED );
					}
				}
			}
		}
		for ( AppEntity app : apps )
		{
			if ( ( app.getStatus() == AppStatus.RUNNING ) && ( app.hasServerFile() ) )
			{
				LOG.info( "Starting App " + app );
				synchronized ( appInitialization )
				{
					appInitialized = false;
					long startTime = System.currentTimeMillis();
					try
					{
						getOsgiManager().startBundle( app.getBundleId().longValue() );
						appInitialization.wait( 240000L );
					}
					catch ( InterruptedException e )
					{
						LOG.error( "Interrupted while waiting for app " + app + " to start" );
					}
					catch ( BundleException e )
					{
						LOG.error( "Could not start server App bundle " + app + " on CES startup", e );
					}
					long total = System.currentTimeMillis() - startTime;
					if ( !appInitialized )
					{
						LOG.error( "App " + app + " was not able to start, startup time " + total + " ms." );
					}
					else
					{
						LOG.info( "App " + app.getIdentity().getName() + " started in " + total + " ms." );
					}
				}
			}
		}

		File builtInDir = new File( AppConstants.BUILT_IN_APP_DIRECTORY );
		if ( !builtInDir.exists() )
		{
			return;
		}
		File[] files = builtInDir.listFiles();
		for ( File file : files )
		{
			if ( ( file.isFile() ) && ( CommonUtils.getFileExtension( file.getName() ).equals( "zip" ) ) )
			{
				String filePath = file.getPath();
				ApplicationIdentityToken identity = null;
				try
				{
					identity = getIdentityFromFile( filePath );
				}
				catch ( AppParseException e )
				{
					LOG.error( "Could not read Built-In App " + filePath + ", Error: " + e.getMessage() );
					continue;
				}
				if ( identity.getAppType() == AppType.BUILT_IN_APP )
				{
					boolean appInstalled = false;
					for ( AppEntity app : apps )
					{
						if ( app.getGuid().equals( identity.getId() ) )
						{
							appInstalled = true;
							break;
						}
					}

					if ( !appInstalled )
					{
						taskScheduler.executeNow( new AppInstallTask( filePath ) );
					}
				}
				else
				{
					LOG.warn( "Non Built-In App found in:" + filePath );
				}
			}
		}
	}

	public void appInitializationComplete( String appId, boolean success )
	{
		synchronized ( appInitialization )
		{
			appInitialized = success;
			if ( !success )
			{
				getAppEventService().processAppStopped( appId );
			}
			appInitialization.notify();
		}
	}

	public String getAppDirectory( String appId ) throws AppException
	{
		AppEntity appEntity = appDAO.findByGuid( appId );

		if ( appEntity == null )
		{
			String error = "App " + appId + " was not found when getting Directory";
			LOG.error( error );
			throw new AppException( AppExceptionTypeEnum.APP_QUERY_ERROR, error );
		}

		String appDirectory = appEntity.getAppFolder();
		return appDirectory;
	}

	public String install( String zipFile ) throws AppException
	{
		AppInformation appInformation = readAppInformation( zipFile );
		AppXmlDescriptor descriptor = appInformation.getDescriptor();
		ApplicationIdentityToken identity = appInformation.getIdentity();

		AppEntity existingApp = appDAO.findByGuid( identity.getId() );
		if ( existingApp != null )
		{
			String error = "App in package " + zipFile + " has guid " + identity.getId() + " which already exists in another App:" + existingApp;
			LOG.error( error );
			throw new AppException( AppExceptionTypeEnum.APP_INSTALL_ERROR, error );
		}

		AppEntity appEntity = new AppEntity();

		LOG.info( "Installing App, name:" + identity.getName() );

		appEntity.setIdentity( identity );
		appEntity.setVersion( descriptor.getVersion() );
		appEntity.setMinimumCESVersion( descriptor.getMinimumCESVersion() );
		appEntity.setTargetSDKVersion( descriptor.getTargetSDKVersion() );
		appEntity.setInstalledTime( DateUtils.getCurrentUTCTimeInMillis() );

		appEntity.setServerFile( descriptor.getServerFile() );
		appEntity.setClientFile( descriptor.getClientFile() );

		if ( CommonUtils.compareVersions( serverVersion, appEntity.getMinimumCESVersion() ) < 0 )
		{
			LOG.info( "App to be installed " + identity.getName() + " requires minimum CES " + appEntity.getMinimumCESVersion() + " and is not supported on CES " + serverVersion );
			appEntity.setStatus( AppStatus.NOTSUPPORTED );
		}
		else if ( !AppCompatability.isCompatible( appEntity.getGuid(), appEntity.getVersion() ) )
		{
			LOG.info( "App to be installed " + appEntity + " requires minimum App version " + AppCompatability.getMinimumVersion( appEntity.getGuid() ) + " to run on CES version " + serverVersion );
			appEntity.setStatus( AppStatus.UPGRADE_REQUIRED );

		}
		else if ( ( appEntity.requiresLicense() ) && ( !licenseService.checkAppLicense( appEntity.getGuid() ) ) )
		{
			appEntity.setStatus( AppStatus.UNLICENSED );
		}
		else
		{
			appEntity.setStatus( AppStatus.INSTALLED );
		}

		appDAO.create( appEntity );

		String outputDirectory = appEntity.getAppFolder();

		if ( !CompressionUtils.unzipFilesToDisk( zipFile, outputDirectory, new String[] {appEntity.getServerFile(), appEntity.getClientFile()} ) )
		{
			File directory = new File( outputDirectory );
			try
			{
				FileUtils.deleteDirectory( directory );
			}
			catch ( IOException e )
			{
				LOG.error( "Could not clean up App folder on failed installation: " + outputDirectory, e );
			}
			String error = "Could not unzip server and client files in App package " + zipFile;
			LOG.error( error );
			throw new AppException( AppExceptionTypeEnum.APP_INSTALL_ERROR, error );
		}

		AppEvent event = new AppEvent( AppEventType.INSTALLED, appEntity.getGuid() );
		eventRegistry.sendEventAfterTransactionCommits( event );

		auditApp( appEntity.toDataObject(), AuditEventNameEnum.APPS_INSTALLED );
		return appEntity.getGuid();
	}

	public void start( String id ) throws AppException
	{
		AppEntity appEntity = appDAO.findByGuid( id );

		if ( appEntity == null )
		{
			String error = "App " + id + " was not found when starting";
			LOG.error( error );
			throw new AppException( AppExceptionTypeEnum.APP_START_ERROR, error );
		}

		if ( appEntity.getStatus() == AppStatus.RUNNING )
		{
			String error = "App " + appEntity + " was already started";
			LOG.error( error );
			throw new AppException( AppExceptionTypeEnum.APP_START_ERROR, error );
		}

		if ( appEntity.getStatus() == AppStatus.NOTSUPPORTED )
		{
			String error = "App " + appEntity + " is not supported";
			LOG.error( error );
			throw new AppException( AppExceptionTypeEnum.APP_START_ERROR, error );
		}

		if ( appEntity.getStatus() == AppStatus.UPGRADE_REQUIRED )
		{
			String error = "App " + appEntity + " requires upgrade";
			LOG.error( error );
			throw new AppException( AppExceptionTypeEnum.APP_START_ERROR, error );
		}

		if ( ( appEntity.requiresLicense() ) && ( ( appEntity.getStatus() == AppStatus.UNLICENSED ) || ( !licenseService.checkAppLicense( appEntity.getGuid() ) ) ) )
		{
			String error = "App " + appEntity + " is not licensed and can not be started";
			LOG.error( error );
			throw new AppException( AppExceptionTypeEnum.APP_LICENSE_ERROR, error );
		}

		try
		{
			LOG.info( "Starting App " + appEntity );
			if ( appEntity.hasServerFile() )
			{
				String appFile = appEntity.getAppServerFile();
				long bundleId = getOsgiManager().installBundle( appFile );
				appEntity.setBundleId( Long.valueOf( bundleId ) );
				getOsgiManager().startBundle( appEntity.getBundleId().longValue() );
			}

			appEntity.setStartedTime( DateUtils.getCurrentUTCTimeInMillis() );
			appEntity.setStatus( AppStatus.RUNNING );

			AppStateEvent event = new AppStateEvent( appEntity.getGuid(), AppStatus.RUNNING, appEntity.getStartedTime() );
			eventRegistry.sendEventAfterTransactionCommits( event );

			auditApp( appEntity.toDataObject(), AuditEventNameEnum.APPS_START );
		}
		catch ( BundleException e )
		{
			if ( appEntity.getBundleId() != null )
			{
				try
				{
					getOsgiManager().uninstallBundle( appEntity.getBundleId().longValue() );
				}
				catch ( BundleException be )
				{
					LOG.error( "Error while cleaning up failed App start " + appEntity, be );
				}
			}
			String error = "Could not start App " + appEntity + ": " + e.getMessage();
			LOG.error( error, e );
			throw new AppException( AppExceptionTypeEnum.APP_START_ERROR, error );
		}
	}

	public void restart( String id ) throws AppException
	{
		stop( id );
		start( id );

		try
		{
			Thread.sleep( 5000L );
		}
		catch ( InterruptedException e )
		{
			LOG.info( "Interrupted while waiting for App Restart. Details:", e.getMessage() );
		}
	}

	public void stop( String id )
	{
		stop( id, AppStatus.INSTALLED );
	}

	private void stop( String id, AppStatus newState ) throws AppException
	{
		AppEntity appEntity = appDAO.findByGuid( id );

		if ( appEntity == null )
		{
			String error = "App id " + id + " was not found when stopping";
			LOG.error( error );
			throw new AppException( AppExceptionTypeEnum.APP_STOP_ERROR, error );
		}

		if ( appEntity.getStatus() != AppStatus.RUNNING )
		{
			String error = "App " + appEntity + " was already stopped";
			LOG.error( error );
			throw new AppException( AppExceptionTypeEnum.APP_STOP_ERROR, error );
		}
		try
		{
			LOG.info( "Stopping App: " + appEntity );
			if ( appEntity.hasServerFile() )
			{
				getOsgiManager().uninstallBundle( appEntity.getBundleId().longValue() );
			}

			appEntity.setStartedTime( 0L );
			appEntity.setStatus( newState );

			if ( newState != null )
			{
				LOG.info( "New App state after stopping: " + newState );

				AppStateEvent event = new AppStateEvent( appEntity.getGuid(), newState );
				eventRegistry.send( event );

				auditApp( appEntity.toDataObject(), AuditEventNameEnum.APPS_STOP );
			}
		}
		catch ( BundleException e )
		{
			String error = "Could not stop App " + appEntity + ": " + e.getMessage();
			LOG.error( error, e );
			throw new AppException( AppExceptionTypeEnum.APP_STOP_ERROR, error );
		}
	}

	public void uninstall( String id ) throws AppException
	{
		AppEntity appEntity = appDAO.findByGuid( id );

		if ( appEntity == null )
		{
			String error = "App id " + id + " was not found when uninstalling";
			LOG.error( error );
			throw new AppException( AppExceptionTypeEnum.APP_UNINSTALL_ERROR, error );
		}

		if ( appEntity.getIdentity().getAppType() == AppType.BUILT_IN_APP )
		{
			String error = "App id " + id + " is Built-In and may not be uninstalled";
			LOG.error( error );
			throw new AppException( AppExceptionTypeEnum.APP_UNINSTALL_ERROR, error );
		}

		UninstallService uninstallService = ( UninstallService ) getOsgiManager().getService( UninstallService.class, id );

		if ( uninstallService == null )
		{
			LOG.info( "There is no UninstallService registered for App id " + id );
		}
		else
		{
			uninstallService.onUninstall();
		}

		if ( appEntity.getStatus() == AppStatus.RUNNING )
		{
			stop( id, null );
		}

		LOG.info( "Uninstalling App: " + appEntity );

		String appDirectory = appEntity.getAppFolder();
		File directory = new File( appDirectory );
		try
		{
			FileUtils.deleteDirectory( directory );
		}
		catch ( IOException e )
		{
			String error = "Could not clean up App folder " + appDirectory + " when uninstalling";
			LOG.error( error, e );
			throw new AppException( AppExceptionTypeEnum.APP_UNINSTALL_ERROR, error );
		}

		appDAO.delete( appEntity );

		AppEvent event = new AppEvent( AppEventType.UNINSTALLED, appEntity.getGuid() );
		eventRegistry.sendEventAfterTransactionCommits( event );

		auditApp( appEntity.toDataObject(), AuditEventNameEnum.APPS_UNINSTALLED );
	}

	public void upgrade( String zipFile, String appId ) throws AppException
	{
		AppInformation appInformation = readAppInformation( zipFile );
		AppXmlDescriptor descriptor = appInformation.getDescriptor();
		ApplicationIdentityToken identity = appInformation.getIdentity();

		if ( ( appId != null ) && ( !appId.equals( identity.getId() ) ) )
		{
			String error = "Wrong App selected for upgrade, id " + appId + " is different from uploaded App " + identity.getId();
			LOG.error( error );
			throw new AppException( AppExceptionTypeEnum.APP_UPGRADE_ERROR, error );
		}

		AppEntity existingApp = appDAO.findByGuid( identity.getId() );
		if ( existingApp == null )
		{
			String error = "Can not upgrade App in package " + zipFile + " with guid " + identity.getId() + " because it does not exist";
			LOG.error( error );
			throw new AppException( AppExceptionTypeEnum.APP_UPGRADE_ERROR, error );
		}

		LOG.info( "Upgrading App, name:" + identity.getName() );

		if ( ( CommonUtils.compareVersions( serverVersion, descriptor.getMinimumCESVersion() ) < 0 ) || ( !AppCompatability.isCompatible( appId, descriptor.getVersion() ) ) )
		{
			String error = "App to be upgraded " + identity.getName() + " requires minimum CES " + descriptor.getMinimumCESVersion() + " and is not supported on CES " + serverVersion;
			LOG.error( error );
			throw new AppException( AppExceptionTypeEnum.APP_UPGRADE_ERROR, error );
		}

		if ( CommonUtils.compareVersions( descriptor.getVersion(), existingApp.getVersion() ) != 1 )
		{
			String error = "App " + identity.getName() + " can not be upgraded from version " + existingApp.getVersion() + " to version " + descriptor.getVersion();
			LOG.error( error );
			throw new AppException( AppExceptionTypeEnum.APP_UPGRADE_ERROR, error );
		}

		existingApp.setVersion( descriptor.getVersion() );
		existingApp.setMinimumCESVersion( descriptor.getMinimumCESVersion() );
		existingApp.setTargetSDKVersion( descriptor.getTargetSDKVersion() );
		existingApp.setInstalledTime( DateUtils.getCurrentUTCTimeInMillis() );
		existingApp.setUpgraded( true );

		existingApp.setServerFile( descriptor.getServerFile() );
		existingApp.setClientFile( descriptor.getClientFile() );

		AppStatus previousStatus = existingApp.getStatus();

		if ( previousStatus == AppStatus.RUNNING )
		{
			stop( existingApp.getGuid(), AppStatus.INSTALLED );
		}
		else if ( previousStatus == AppStatus.UPGRADE_REQUIRED )
		{
			existingApp.setStatus( AppStatus.INSTALLED );
		}

		String appDirectory = existingApp.getAppFolder();

		if ( !CompressionUtils.unzipFilesToDisk( zipFile, appDirectory, new String[] {existingApp.getServerFile(), existingApp.getClientFile()} ) )
		{
			String error = "Could not unzip server and client files in App package " + zipFile;
			LOG.error( error );
			throw new AppException( AppExceptionTypeEnum.APP_UPGRADE_ERROR, error );
		}

		AppEvent event = new AppEvent( AppEventType.UPGRADED, existingApp.getGuid() );
		eventRegistry.sendEventAfterTransactionCommits( event );

		auditApp( existingApp.toDataObject(), AuditEventNameEnum.APPS_UPGRADED );

		if ( ( previousStatus == AppStatus.RUNNING ) || ( ( existingApp.getStatus() == AppStatus.INSTALLED ) && ( AppCompatability.isCompatible( existingApp.getGuid(), existingApp.getVersion() ) ) ) )
		{
			start( existingApp.getGuid() );
		}
	}

	public App[] getApps() throws AppException
	{
		String username = CommonAppUtils.getUsernameFromSecurityContext();

		List<AppEntity> appList = new ArrayList();

		if ( username == null )
		{
			appList = appDAO.findAll();
			App[] result = new App[appList.size()];
			for ( int i = 0; i < appList.size(); i++ )
			{
				result[i] = ( ( AppEntity ) appList.get( i ) ).toDataObject();
			}
			return result;
		}

		MemberView member;

		try
		{
			member = userService.getUser( username );
		}
		catch ( UserException e )
		{
			throw new AppException( AppExceptionTypeEnum.APP_QUERY_ERROR, e.getMessage(), e );
		}

		for ( String appGuid : member.getAssembledAppRights() )
		{
			appList.add( appDAO.findByGuid( appGuid ) );
		}

		App[] result = new App[appList.size()];
		for ( int i = 0; i < appList.size(); i++ )
		{
			result[i] = ( ( AppEntity ) appList.get( i ) ).toDataObject();
		}
		return result;
	}

	public App[] getAllApps()
	{
		List<AppEntity> appList = new ArrayList();

		appList = appDAO.findAll();
		App[] result = new App[appList.size()];
		for ( int i = 0; i < appList.size(); i++ )
		{
			result[i] = ( ( AppEntity ) appList.get( i ) ).toDataObject();
		}
		return result;
	}

	public App getApp( String id ) throws AppException
	{
		AppEntity appEntity = appDAO.findByGuid( id );
		if ( appEntity == null )
		{
			String error = "App id " + id + " was not found when querying";
			LOG.error( error );
			throw new AppException( AppExceptionTypeEnum.APP_QUERY_ERROR, error );
		}
		return appEntity.toDataObject();
	}

	public String getClientFile( String id )
	{
		AppEntity appEntity = appDAO.findByGuid( id );
		if ( appEntity == null )
		{
			String error = "App id " + id + " was not found when querying for client file";
			LOG.error( error );
			throw new AppException( AppExceptionTypeEnum.APP_QUERY_ERROR, error );
		}
		if ( !appEntity.hasClientFile() )
		{
			String error = "App id " + id + " does not have a client file";
			LOG.error( error );
			throw new AppException( AppExceptionTypeEnum.APP_QUERY_ERROR, error );
		}
		if ( appEntity.getStatus() == AppStatus.NOTSUPPORTED )
		{
			String error = "App id " + id + " is not supported";
			LOG.error( error );
			throw new AppException( AppExceptionTypeEnum.APP_QUERY_ERROR, error );
		}
		if ( appEntity.getStatus() == AppStatus.UPGRADE_REQUIRED )
		{
			String error = "App id " + id + " requires upgrade";
			LOG.error( error );
			throw new AppException( AppExceptionTypeEnum.APP_QUERY_ERROR, error );
		}

		if ( ( appEntity.requiresLicense() ) && ( !licenseService.checkAppLicense( appEntity.getGuid() ) ) )
		{
			String error = "App " + appEntity + " is not licensed and client download is not allowed";
			LOG.error( error );
			throw new AppException( AppExceptionTypeEnum.APP_LICENSE_ERROR, error );
		}
		return appEntity.getAppClientFile();
	}

	public void notifyAppLicenseChange( String id, boolean licensed )
	{
		AppEntity appEntity = appDAO.findByGuid( id );
		if ( ( appEntity != null ) && ( !appEntity.requiresLicense() ) )
		{
			return;
		}

		if ( appEntity != null )
		{

			if ( licensed )
			{
				if ( appEntity.getStatus() == AppStatus.UNLICENSED )
				{
					appEntity.setStatus( AppStatus.INSTALLED );

					AppStateEvent event = new AppStateEvent( appEntity.getGuid(), AppStatus.INSTALLED );
					eventRegistry.sendEventAfterTransactionCommits( event );
				}

			}
			else if ( appEntity.getStatus() != AppStatus.UNLICENSED )
			{

				if ( appEntity.getStatus() == AppStatus.RUNNING )
				{
					try
					{
						stop( appEntity.getGuid(), AppStatus.UNLICENSED );
					}
					catch ( AppException e )
					{
						LOG.error( "Could not stop App " + appEntity + " after it became unlicensed" );
					}
				}
				else if ( appEntity.getStatus() == AppStatus.INSTALLED )
				{
					appEntity.setStatus( AppStatus.UNLICENSED );

					AppStateEvent event = new AppStateEvent( appEntity.getGuid(), AppStatus.UNLICENSED );
					eventRegistry.sendEventAfterTransactionCommits( event );
				}
			}
		}
	}

	public List<TestApp> getAvailableApps()
	{
		List<TestApp> result = new ArrayList();
		List<AppEntity> appList = appDAO.findAll();

		File appDir = new File( AppConstants.APP_TEST_DIRECTORY );

		if ( !appDir.exists() )
		{
			boolean folderCreated = false;
			folderCreated = appDir.mkdir();

			if ( !folderCreated )
			{
				throw new AppException( AppExceptionTypeEnum.APP_QUERY_ERROR, "/test folder could not be created. Create it in the 'apps' folder" );
			}
		}

		File[] files = appDir.listFiles();
		for ( File file : files )
		{
			if ( ( file.isFile() ) && ( CommonUtils.getFileExtension( file.getName() ).equals( "zip" ) ) )
			{
				boolean installed = isAppInstalled( file.getPath(), appList );
				TestApp testApp = new TestApp( file.getName(), installed );
				result.add( testApp );
			}
		}

		return result;
	}

	public String getAppName( String id )
	{
		App app = getApp( id );
		return app.getIdentity().getName();
	}

	public List<String> getAppGuids()
	{
		List<String> appGuids = new ArrayList();

		for ( AppEntity app : appDAO.findAll() )
		{
			appGuids.add( app.getGuid() );
		}

		return appGuids;
	}

	public Integer getCurrentVersion( String appId )
	{
		AppEntity appEntity = appDAO.findByGuid( appId );
		return appEntity.getDatabaseVersion();
	}

	public void setDatabaseVersion( String appId, Integer databaseVersion )
	{
		AppEntity appEntity = appDAO.findByGuid( appId );
		appEntity.setDatabaseVersion( databaseVersion.intValue() );
	}

	public boolean upgraded( String appId )
	{
		AppEntity appEntity = appDAO.findByGuid( appId );
		if ( appEntity.isUpgraded() )
		{
			return true;
		}
		return false;
	}

	protected void auditApp( App auditedApp, AuditEventNameEnum auditEvent )
	{
		if ( CommonAppUtils.getUsernameFromSecurityContext() != null )
		{
			AuditView audit = new AuditView.Builder( auditEvent.getName() ).addDetailsPair( "app_name", auditedApp.getIdentity().getName() ).addDetailsPair( "app_version", auditedApp.getVersion() ).build();

			eventRegistry.sendEventAfterTransactionCommits( new AuditEvent( audit ) );
		}
	}

	private AppInformation readAppInformation( String zipFile ) throws AppException
	{
		byte[] descriptorBytes = CompressionUtils.unzipFileToBytes( zipFile, "app.xml" );

		if ( descriptorBytes == null )
		{
			String error = "Could not read app.xml in App package " + zipFile;
			LOG.error( error );
			throw new AppException( AppExceptionTypeEnum.APP_INSTALL_ERROR, error );
		}

		AppXmlDescriptor descriptor = new AppXmlDescriptor();
		try
		{
			descriptor.parsefromBytes( descriptorBytes );
		}
		catch ( AppParseException e )
		{
			String error = "Could not parse app.xml in App package " + zipFile + ", Reason:" + e.getMessage();
			LOG.error( error );
			throw new AppException( AppExceptionTypeEnum.APP_INSTALL_ERROR, error );
		}

		byte[] identityBytes = CompressionUtils.unzipFileToBytes( zipFile, descriptor.getIdentityFile() );
		if ( identityBytes == null )
		{
			String error = "Could not read " + descriptor.getIdentityFile() + " in App package " + zipFile;
			LOG.error( error );
			throw new AppException( AppExceptionTypeEnum.APP_INSTALL_ERROR, error );
		}

		ApplicationIdentityToken identity;

		try
		{
			identity = AppIdentityParser.parseIdentity( identityBytes );
		}
		catch ( AppParseException e )
		{
			String error = "Could not parse " + descriptor.getIdentityFile() + " in App package " + zipFile + " " + e.getMessage();
			LOG.error( error );
			throw new AppException( AppExceptionTypeEnum.APP_INSTALL_ERROR, error );
		}

		AppInformation result = new AppInformation( descriptor, identity );
		return result;
	}

	private boolean isAppInstalled( String filename, List<AppEntity> apps )
	{
		ApplicationIdentityToken identity = null;
		try
		{
			identity = getIdentityFromFile( filename );
		}
		catch ( AppParseException e )
		{
			return false;
		}

		for ( AppEntity app : apps )
		{
			if ( app.getGuid().equals( identity.getId() ) )
			{
				return true;
			}
		}
		return false;
	}

	private ApplicationIdentityToken getIdentityFromFile( String file ) throws AppParseException
	{
		byte[] bytes = CompressionUtils.unzipFileToBytes( file, "app.xml" );

		if ( bytes == null )
		{
			throw new AppParseException( "Could not read app.xml in App package " + file );
		}

		AppXmlDescriptor descriptor = new AppXmlDescriptor();
		descriptor.parsefromBytes( bytes );

		bytes = CompressionUtils.unzipFileToBytes( file, descriptor.getIdentityFile() );

		if ( bytes == null )
		{
			throw new AppParseException( "Could not read " + descriptor.getIdentityFile() + " in App package " + file );
		}

		ApplicationIdentityToken identity = AppIdentityParser.parseIdentity( bytes );
		return identity;
	}

	public void setAppDAO( AppDAO appDAO )
	{
		this.appDAO = appDAO;
	}

	public void setEventRegistry( EventRegistry eventRegistry )
	{
		this.eventRegistry = eventRegistry;
	}

	public void setLicenseService( LicenseService licenseService )
	{
		this.licenseService = licenseService;
	}

	public void setTaskScheduler( TaskScheduler taskScheduler )
	{
		this.taskScheduler = taskScheduler;
	}

	public ResourceTopologyServiceIF getTopologyService()
	{
		if ( topologyService == null )
		{
			topologyService = ( ( ResourceTopologyServiceIF ) ApplicationContextSupport.getBean( "resourceTopologyService_internal" ) );
		}
		return topologyService;
	}

	public OsgiManager getOsgiManager()
	{
		if ( osgiManager == null )
		{
			osgiManager = ( ( OsgiManager ) ApplicationContextSupport.getBean( "osgiManager" ) );
		}
		return osgiManager;
	}

	public AppEventService getAppEventService()
	{
		if ( appEventService == null )
		{
			appEventService = ( ( AppEventService ) ApplicationContextSupport.getBean( "appEventCoreService" ) );
		}
		return appEventService;
	}

	public void setUserService( UserService userService )
	{
		this.userService = userService;
	}
}
