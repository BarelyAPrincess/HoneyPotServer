package com.marchnetworks.license.service;

import com.marchnetworks.app.service.AppManager;
import com.marchnetworks.audit.data.AuditEventNameEnum;
import com.marchnetworks.audit.data.AuditView;
import com.marchnetworks.audit.data.AuditView.Builder;
import com.marchnetworks.audit.events.AuditEvent;
import com.marchnetworks.com.util.AuditLogDetailsHelper;
import com.marchnetworks.command.api.alert.AlertDefinitionEnum;
import com.marchnetworks.command.api.initialization.InitializationListener;
import com.marchnetworks.command.api.query.Criteria;
import com.marchnetworks.command.api.rest.DeviceManagementConstants;
import com.marchnetworks.command.api.security.CommandAuthenticationDetails;
import com.marchnetworks.command.common.CollectionUtils;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.device.data.VideoEncoderView;
import com.marchnetworks.command.common.license.data.AppLicenseInfo;
import com.marchnetworks.command.common.topology.ResourceRootType;
import com.marchnetworks.command.common.topology.TopologyException;
import com.marchnetworks.command.common.topology.data.ChannelResource;
import com.marchnetworks.command.common.topology.data.DeviceResource;
import com.marchnetworks.command.common.topology.data.GenericResource;
import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.command.common.transport.data.Pair;
import com.marchnetworks.common.crypto.CryptoException;
import com.marchnetworks.common.crypto.CryptoUtils;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.utils.CommonUtils;
import com.marchnetworks.common.utils.DateUtils;
import com.marchnetworks.health.input.AlertInput;
import com.marchnetworks.health.input.ServerAlertInput;
import com.marchnetworks.health.service.HealthServiceIF;
import com.marchnetworks.license.LicenseService;
import com.marchnetworks.license.dao.AppLicenseDAO;
import com.marchnetworks.license.event.AppLicenseEvent;
import com.marchnetworks.license.event.AppLicenseEventType;
import com.marchnetworks.license.event.LicenseEvent;
import com.marchnetworks.license.event.LicenseEventType;
import com.marchnetworks.license.event.LicenseInvalidEvent;
import com.marchnetworks.license.exception.LicenseException;
import com.marchnetworks.license.exception.LicenseExceptionType;
import com.marchnetworks.license.model.AppLicenseEntity;
import com.marchnetworks.license.model.AppLicenseImport;
import com.marchnetworks.license.model.AppLicenseType;
import com.marchnetworks.license.model.AppType;
import com.marchnetworks.license.model.ApplicationIdentityToken;
import com.marchnetworks.license.model.Expiry;
import com.marchnetworks.license.model.License;
import com.marchnetworks.license.model.LicenseFeature;
import com.marchnetworks.license.model.LicenseStatus;
import com.marchnetworks.license.model.ServerLicenseImport;
import com.marchnetworks.management.topology.ResourceTopologyServiceIF;
import com.marchnetworks.server.event.EventRegistry;
import com.marchnetworks.server.event.StateCacheService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class AppLicenseServiceImpl implements AppLicenseService, InitializationListener
{
	private static final Logger LOG = LoggerFactory.getLogger( AppLicenseServiceImpl.class );

	private static final int EXPIRY_WARNING_THRESHOLD = 7;

	private static final String GRACE_REMAINING_PARAM = "device_data";
	private AppLicenseDAO appLicenseDAO;
	private LicenseService licenseService;
	private AppManager appManager;
	private HealthServiceIF healthService;
	private EventRegistry eventRegistry;
	private StateCacheService stateCacheService;
	private ResourceTopologyServiceIF topologyService;
	private static List<String> failedLicenses = new ArrayList();

	public void onAppInitialized()
	{
		migrateSearchlightLicenses();
	}

	public void importLicense( ServerLicenseImport license ) throws LicenseException
	{
		List<AppLicenseImport> appLicenses = license.getAppLicenses();
		String previousLicenseId = null;

		for ( AppLicenseImport appLicense : appLicenses )
		{
			LOG.info( "Importing App License, App name:" + appLicense.getIdentity().getName() );

			Date today = new Date();
			if ( appLicense.getExpiry() == Expiry.TRIAL )
			{
				if ( today.after( appLicense.getEnd() ) )
				{
					throw new LicenseException( "Trial license is expired", LicenseExceptionType.LICENSE_EXPIRED );
				}
				if ( today.before( appLicense.getStart() ) )
				{
					throw new LicenseException( "Trial license is not valid yet", LicenseExceptionType.LICENSE_NOT_YET_VALID );
				}
			}

			if ( BlacklistedApps.isBlacklisted( appLicense.getIdentity().getId() ) )
			{
				throw new LicenseException( "License is for an App that is no longer supported", LicenseExceptionType.LICENSE_NOT_SUPPORTED );
			}

			ApplicationIdentityToken appIdentity = appLicense.getIdentity();
			List<AppLicenseEntity> appLicenseEntities = appLicenseDAO.findAllByAppId( appIdentity.getId() );

			AppLicenseEntity appLicenseEntity = null;

			for ( AppLicenseEntity entity : appLicenseEntities )
			{
				if ( appLicense.getType().isGeneric() )
				{

					if ( ( entity.getType() == appLicense.getType() ) && ( entity.getFeature().equals( appLicense.getFeature() ) ) )
					{
						appLicenseEntity = entity;
						break;
					}

				}
				else if ( !entity.getType().isGeneric() )
				{
					appLicenseEntity = entity;
					break;
				}
			}

			if ( appLicenseEntity == null )
			{
				LOG.info( "Importing new App License" );
				appLicenseEntity = new AppLicenseEntity();
				appLicenseEntity.readFromImport( appLicense );
				appLicenseDAO.create( appLicenseEntity );

			}
			else
			{
				if ( appLicenseEntity.getLicenseIds().contains( appLicense.getLicenseId() ) )
				{
					LOG.info( "License to import is already loaded " );
					throw new LicenseException( "License already loaded", LicenseExceptionType.LICENSE_ALREADY_ADDED );
				}
				previousLicenseId = appLicenseEntity.getLicenseId();

				if ( ( appLicense.getType() != AppLicenseType.APP_FULL_ACCESS ) && ( appLicenseEntity.getType() != AppLicenseType.APP_FULL_ACCESS ) && ( !appLicense.getType().equals( appLicenseEntity.getType() ) ) )
				{
					String error = "New license type " + appLicense.getType() + " not compatible with existing type " + appLicenseEntity.getType();
					LOG.info( error );
					throw new LicenseException( error, LicenseExceptionType.LICENSE_TYPE_INCOMPATIBLE );
				}

				LicenseInvalidEvent existing;
				if ( failedLicenses.contains( appLicenseEntity.getIdentity().getId() ) )
				{
					LOG.info( "Importing over failed license" );

					if ( appLicenseEntity.getAllocated() > appLicense.getCount() )
					{
						String error = "New license count insufficient for existing allocation";
						LOG.info( error );
						throw new LicenseException( error, LicenseExceptionType.LICENSE_COUNT_APP );
					}
					appLicenseEntity.readFromImport( appLicense );

					failedLicenses.remove( appLicenseEntity.getIdentity().getId() );

					if ( failedLicenses.isEmpty() )
					{
						LOG.info( "No more failed licenses, resetting App grace period" );
						licenseService.saveParameter( -1, "device_data" );

						stateCacheService.removeFromCache( new LicenseInvalidEvent( 0, true ) );
					}
					else
					{
						String name = appLicenseEntity.getName();
						existing = ( LicenseInvalidEvent ) stateCacheService.getCachedEvent( new LicenseInvalidEvent() );
						if ( name.equals( existing.getLicenseName() ) )
						{
							List<AppLicenseEntity> existingLicenses = appLicenseDAO.findAllExcludeId( appLicenseEntity.getId() );
							for ( AppLicenseEntity existingLicense : existingLicenses )
							{
								if ( existingLicense.isFailed() )
								{
									existing.setLicenseName( existingLicense.getName() );
									break;
								}

							}

						}
					}
				}
				else if ( appLicenseEntity.getExpiry() == Expiry.TRIAL )
				{
					LOG.info( "Importing over existing trial App License" );

					if ( appLicenseEntity.getAllocated() > appLicense.getCount() )
					{
						String error = "New license count insufficient for existing allocation";
						LOG.info( error );
						throw new LicenseException( error, LicenseExceptionType.LICENSE_COUNT_APP );
					}

					appLicenseEntity.readFromImport( appLicense );
				}
				else
				{
					LOG.info( "Importing over existing permanent App License" );

					if ( appLicense.getExpiry() == Expiry.TRIAL )
					{
						String error = "Cannot add trial license to existing permanent license";
						LOG.info( error );
						throw new LicenseException( error, LicenseExceptionType.LICENSE_TRIAL_OVER_PERMANENT );
					}
					appLicenseEntity.addFromImport( appLicense );
				}
			}

			if ( !appLicenseEntity.isOpen() )
			{
				appLicenseEntity.setServerId( license.getServerId() );
			}

			setInitialState( appLicenseEntity );

			appLicenseEntity.update();

			if ( ( appLicenseEntity.getStatus() != LicenseStatus.EXPIRING ) && ( appLicenseEntity.getLastWarningDay() != -1L ) )
			{
				AlertInput alert = new ServerAlertInput( AlertDefinitionEnum.LICENSE_EXPIRING, previousLicenseId, AlertInput.pairsToString( getAppLicenseInfo( appLicenseEntity ) ), "", false );

				healthService.processHealthAlert( alert );
				appLicenseEntity.setLastWarningDay( -1L );
			}

			if ( previousLicenseId != null )
			{
				LicenseEvent event = new LicenseEvent( LicenseEventType.REMOVED, previousLicenseId );
				eventRegistry.sendEventAfterTransactionCommits( event );
			}

			if ( appLicenseEntity.getIdentity().getAppType() == AppType.APP )
			{
				getAppManager().notifyAppLicenseChange( appLicenseEntity.getAppId(), true );
			}

			sendLicenseEvent( true, appLicenseEntity );
		}
	}

	public void start()
	{
		int graceRemaining = licenseService.loadParameter( "device_data" );
		boolean sendInvalidEvent = false;
		String failedLicenseName = null;

		if ( graceRemaining > 0 )
		{
			LOG.info( "App grace period remaining " + graceRemaining / 24 );
			graceRemaining--;
			if ( graceRemaining < 0 )
			{
				graceRemaining = 0;
			}
		}

		List<AppLicenseEntity> appLicenses = appLicenseDAO.findAll();

		for ( AppLicenseEntity appLicense : appLicenses )
		{
			setInitialState( appLicense );

			if ( ( !appLicense.isOpen() ) && ( !licenseService.isLicenseMatch( appLicense.getServerId() ) ) )
			{
				failedLicenses.add( appLicense.getIdentity().getId() );
				if ( graceRemaining == -1 )
				{
					LOG.info( "Starting App grace period" );
					graceRemaining = 360;
				}

				if ( appLicense.getStatus() != LicenseStatus.EXPIRED )
				{
					sendInvalidEvent = true;
					if ( failedLicenseName == null )
					{
						failedLicenseName = appLicense.getName();
					}
					if ( graceRemaining == 0 )
					{
						LOG.info( "Failed license going to failed: " + appLicense );
						appLicense.setStatus( LicenseStatus.FAILED );
					}
					else
					{
						LOG.info( "Failed license going to failgrace: " + appLicense );
						appLicense.setStatus( LicenseStatus.FAILGRACE );
					}
				}
			}
			appLicense.update();
		}

		if ( failedLicenses.isEmpty() )
		{
			if ( graceRemaining >= 0 )
			{
				LOG.info( "Resetting App grace period" );
				graceRemaining = -1;
			}
		}
		else if ( sendInvalidEvent )
		{
			LicenseInvalidEvent licenseInvalidEvent = new LicenseInvalidEvent( getGraceDaysRemaining( graceRemaining ), failedLicenseName );
			stateCacheService.putIntoCache( licenseInvalidEvent );
		}
		licenseService.saveParameter( graceRemaining, "device_data" );
	}

	public void checkGraceExpire()
	{
		int graceRemaining = licenseService.loadParameter( "device_data" );

		if ( graceRemaining > 0 )
		{
			graceRemaining--;

			if ( graceRemaining == 0 )
			{
				doGraceExpire();
			}

			LicenseInvalidEvent existing = ( LicenseInvalidEvent ) stateCacheService.getCachedEvent( new LicenseInvalidEvent() );
			existing.setRemainingDays( getGraceDaysRemaining( graceRemaining ) );

			licenseService.saveParameter( graceRemaining, "device_data" );
		}
	}

	private void doGraceExpire()
	{
		LOG.info( "App grace period expired" );
		List<AppLicenseEntity> appLicenses = appLicenseDAO.findAll();
		for ( AppLicenseEntity appLicense : appLicenses )
		{
			if ( appLicense.getStatus() == LicenseStatus.FAILGRACE )
			{
				LOG.info( "Setting App license " + appLicense + " to failed state" );
				appLicense.setStatus( LicenseStatus.FAILED );

				if ( appLicense.getIdentity().getAppType() == AppType.APP )
				{
					getAppManager().notifyAppLicenseChange( appLicense.getAppId(), false );
				}

				sendLicenseEvent( true, appLicense );

				appLicense.update();
			}
		}
	}

	public void checkExpiredLicenses()
	{
		List<AppLicenseEntity> appLicenses = appLicenseDAO.findAll();
		for ( AppLicenseEntity appLicense : appLicenses )
		{
			if ( appLicense.getExpiry() == Expiry.TRIAL )
			{

				long remainingDays = getDaysRemaining( appLicense );

				if ( remainingDays <= 7L )
				{
					long lastWarningDay = appLicense.getLastWarningDay();

					if ( lastWarningDay != remainingDays )
					{
						LOG.info( "Sending warning for remaining days:" + remainingDays + ", " + appLicense );
						AlertInput alert = new ServerAlertInput( AlertDefinitionEnum.LICENSE_EXPIRING, appLicense.getLicenseId(), AlertInput.pairsToString( getAppLicenseInfo( appLicense ) ), Long.toString( remainingDays ), true );

						healthService.processHealthAlert( alert );
						appLicense.setLastWarningDay( remainingDays );

						LicenseStatus status = appLicense.getStatus();
						if ( ( status == LicenseStatus.OK ) && ( remainingDays != 0L ) )
						{
							appLicense.setStatus( LicenseStatus.EXPIRING );

							LicenseEvent event = new LicenseEvent( LicenseEventType.UPDATED, appLicense.getLicenseId() );
							eventRegistry.sendEventAfterTransactionCommits( event );
						}
					}
				}

				if ( ( remainingDays == 0L ) && ( appLicense.getStatus() != LicenseStatus.EXPIRED ) )
				{
					LOG.info( "License expired " + appLicense );

					appLicense.setStatus( LicenseStatus.EXPIRED );

					if ( appLicense.getIdentity().getAppType() == AppType.APP )
					{
						getAppManager().notifyAppLicenseChange( appLicense.getAppId(), false );
					}

					sendLicenseEvent( true, appLicense );
				}
				appLicense.update();
			}
		}
	}

	public boolean checkAppLicense( String appId )
	{
		if ( PreLicensedApps.isPreLicensed( appId ) )
		{
			return true;
		}
		List<AppLicenseEntity> appLicenseEntities = appLicenseDAO.findAllByAppId( appId );
		for ( AppLicenseEntity appLicenseEntity : appLicenseEntities )
		{
			if ( ( appLicenseEntity.getStatus() != LicenseStatus.EXPIRED ) && ( appLicenseEntity.getStatus() != LicenseStatus.FAILED ) )
			{
				return true;
			}
		}
		LOG.warn( "App license for App id " + appId + " not found or invalid when checking if license valid" );
		return false;
	}

	public boolean isIdentifiedAndLicensedSession()
	{
		CommandAuthenticationDetails sessionDetails = CommonUtils.getAuthneticationDetails();
		if ( ( sessionDetails == null ) || ( !sessionDetails.isIdentified() ) )
		{
			return false;
		}

		return checkAppLicense( sessionDetails.getAppId() );
	}

	public byte[] getIdentitySignature( String appId )
	{
		byte[] signature = PreLicensedApps.getSignature( appId );
		if ( signature != null )
		{
			return signature;
		}

		AppLicenseEntity appLicenseEntity = appLicenseDAO.findOneByAppId( appId );
		if ( appLicenseEntity == null )
		{
			return null;
		}
		String accessCode = appLicenseEntity.getIdentity().getAccessCode();
		try
		{
			return CryptoUtils.decrypt( accessCode );
		}
		catch ( CryptoException e )
		{
		}
		return null;
	}

	public License getLicense( String licenseId ) throws LicenseException
	{
		AppLicenseEntity appLicenseEntity = appLicenseDAO.findByLicenseId( licenseId );
		if ( appLicenseEntity == null )
		{
			String error = "License queried with licenseId " + licenseId + " does not exist";
			LOG.info( error );
			throw new LicenseException( error, LicenseExceptionType.NOT_FOUND );
		}

		Set<Long> resources = filterUserResources( appLicenseEntity );

		License result = appLicenseEntity.toDataObject( resources );
		return result;
	}

	public List<Resource> filterAppResources( String appId, Long deviceResourceId, List<Resource> resources ) throws LicenseException
	{
		if ( resources.isEmpty() )
		{
			return resources;
		}

		if ( !PreLicensedApps.isPreLicensed( appId ) )
		{
			License license = getLicenseByAppId( appId );
			List<Long> appResources = Arrays.asList( license.getResources() );
			Iterator<Resource> iterator;
			if ( license.getType() == AppLicenseType.APP_CHANNEL_ACCESS )
			{
				for ( iterator = resources.iterator(); iterator.hasNext(); )
				{
					Resource resource = ( Resource ) iterator.next();
					if ( ( ( resource instanceof ChannelResource ) ) && ( !appResources.contains( resource.getId() ) ) )
					{
						iterator.remove();
					}

				}
			}
			else if ( license.getType() == AppLicenseType.APP_RECORDER_ACCESS )
			{
				if ( !appResources.contains( deviceResourceId ) )
				{
					return Collections.emptyList();
				}
			}
		}

		return resources;
	}

	private License getLicenseByAppId( String appId ) throws LicenseException
	{
		AppLicenseEntity appLicenseEntity = null;
		List<AppLicenseEntity> appLicenseEntities = appLicenseDAO.findAllByAppId( appId );

		if ( appLicenseEntities.isEmpty() )
		{
			String error = "License queried with appId " + appId + " does not exist";
			LOG.info( error );
			throw new LicenseException( error, LicenseExceptionType.NOT_FOUND );
		}

		for ( AppLicenseEntity entity : appLicenseEntities )
		{
			if ( entity.getType() != AppLicenseType.APP_GENERIC_ACCESS )
			{
				appLicenseEntity = entity;
			}
		}

		License result = null;
		if ( appLicenseEntity != null )
		{
			Set<Long> resources = appLicenseEntity.getResources();
			result = appLicenseEntity.toDataObject( resources );
		}
		return result;
	}

	public License[] getLicenses() throws LicenseException
	{
		List<AppLicenseEntity> appLicenses = appLicenseDAO.findAll();
		License[] result = new License[appLicenses.size()];
		for ( int i = 0; i < appLicenses.size(); i++ )
		{
			AppLicenseEntity appLicense = ( AppLicenseEntity ) appLicenses.get( i );

			Set<Long> resources = filterUserResources( appLicense );

			License dataObject = appLicense.toDataObject( resources );
			result[i] = dataObject;
		}
		return result;
	}

	private Set<Long> filterUserResources( AppLicenseEntity appLicenseEntity ) throws LicenseException
	{
		Set<Long> resources = null;
		if ( appLicenseEntity.getType() != AppLicenseType.APP_FULL_ACCESS )
		{
			List<Long> userResources = getResourcesForUser( appLicenseEntity );
			resources = new LinkedHashSet( appLicenseEntity.getResources() );
			resources.retainAll( userResources );
		}
		return resources;
	}

	public void removeLicense( String licenseId ) throws LicenseException
	{
		AppLicenseEntity appLicenseEntity = appLicenseDAO.findByLicenseId( licenseId );
		if ( appLicenseEntity == null )
		{
			String error = "License to be removed with licenseId " + licenseId + " does not exist";
			LOG.info( error );
			throw new LicenseException( error, LicenseExceptionType.NOT_FOUND );
		}

		LOG.info( "Removing license " + appLicenseEntity );

		Set<Long> resources = filterUserResources( appLicenseEntity );
		auditAppLicense( AuditEventNameEnum.LICENSE_REMOVE, appLicenseEntity.toDataObject( resources ), new Long[0] );

		LicenseStatus status = appLicenseEntity.getStatus();
		if ( ( appLicenseEntity.getExpiry() == Expiry.TRIAL ) || ( status == LicenseStatus.FAILED ) || ( status == LicenseStatus.FAILGRACE ) || ( appLicenseEntity.getIdentity().getAppType() == AppType.COMMAND_API ) )
		{

			if ( appLicenseEntity.getIdentity().getAppType() == AppType.APP )
			{
				getAppManager().notifyAppLicenseChange( appLicenseEntity.getAppId(), false );
			}

			appLicenseDAO.delete( appLicenseEntity );

			if ( failedLicenses.contains( appLicenseEntity.getIdentity().getId() ) )
			{
				failedLicenses.remove( appLicenseEntity.getIdentity().getId() );
			}

			sendLicenseEvent( false, appLicenseEntity );
		}
		else
		{
			String error = "Can not remove license for licenseId:" + licenseId + ", expiry:" + appLicenseEntity.getExpiry() + ", status:" + status;
			LOG.info( error );
			throw new LicenseException( error, LicenseExceptionType.NOT_SUPPORTED );
		}
	}

	public void processDeviceUnregistered( String deviceId )
	{
		Long resourceId = getTopologyService().getResourceIdByDeviceId( deviceId );
		if ( resourceId == null )
		{
			return;
		}
		DeviceResource device = getTopologyService().getDeviceResource( resourceId );
		if ( device == null )
		{
			LOG.warn( "DeviceResource {} not found.", resourceId );
			return;
		}
		if ( !device.isRootDevice() )
		{
			return;
		}
		List<Resource> channels = device.createFilteredResourceList( new Class[] {ChannelResource.class} );

		List<Long> removedIds = new ArrayList();
		removedIds.add( resourceId );
		for ( Resource channel : channels )
		{
			removedIds.add( channel.getId() );
		}

		List<AppLicenseEntity> licenses = appLicenseDAO.findAll();
		for ( AppLicenseEntity license : licenses )
		{
			Set<Long> resources = license.getResources();
			if ( resources.removeAll( removedIds ) )
			{
				license.update();

				LicenseEvent event = new LicenseEvent( LicenseEventType.UPDATED, license.getLicenseId() );
				eventRegistry.sendEventAfterTransactionCommits( event );
			}
		}
	}

	public void processChannelRemoved( String deviceId, String channelId )
	{
		Long resourceId = getTopologyService().getChannelResourceId( deviceId, channelId );

		List<AppLicenseEntity> licenses = appLicenseDAO.findAll();
		for ( AppLicenseEntity license : licenses )
		{
			Set<Long> resources = license.getResources();
			if ( resources.remove( resourceId ) )
			{
				license.update();

				LicenseEvent event = new LicenseEvent( LicenseEventType.UPDATED, license.getLicenseId() );
				eventRegistry.sendEventAfterTransactionCommits( event );
			}
		}
	}

	public void processGenericResourceRemoved( GenericResource resource )
	{
		List<AppLicenseEntity> licenses = appLicenseDAO.findAll();
		for ( AppLicenseEntity license : licenses )
		{
			Set<Long> resources = license.getResources();
			if ( resources.remove( resource.getId() ) )
			{
				license.update();

				LicenseEvent event = new LicenseEvent( LicenseEventType.UPDATED, license.getLicenseId() );
				eventRegistry.sendEventAfterTransactionCommits( event );
			}
		}
	}

	public void setLicenseResources( String licenseId, Long[] resources ) throws LicenseException
	{
		AppLicenseEntity appLicenseEntity = appLicenseDAO.findByLicenseId( licenseId );
		if ( appLicenseEntity == null )
		{
			String error = "License to set resources for with licenseId " + licenseId + " does not exist";
			LOG.info( error );
			throw new LicenseException( error, LicenseExceptionType.NOT_FOUND );
		}

		if ( CollectionUtils.containsDuplicates( resources ) )
		{
			String error = "Resources to set for licenseId " + licenseId + " contain duplicates";
			LOG.info( error );
			throw new LicenseException( error, LicenseExceptionType.LICENSE_RESOURCE_ERROR );
		}

		if ( appLicenseEntity.getType() == AppLicenseType.APP_FULL_ACCESS )
		{
			String error = "Can not set resources for " + appLicenseEntity.getType() + " App License type";
			LOG.info( error );
			throw new LicenseException( error, LicenseExceptionType.LICENSE_RESOURCE_ERROR );
		}

		List<Long> userResourceIds = getResourcesForUser( appLicenseEntity );

		List<Long> resourcesList = Arrays.asList( resources );

		if ( !userResourceIds.containsAll( resourcesList ) )
		{
			String error = "User " + CommonAppUtils.getUsernameFromSecurityContext() + " attempting to set disallowed Resources for App License: " + resourcesList;
			LOG.info( error );
			throw new LicenseException( error, LicenseExceptionType.LICENSE_RESOURCE_ERROR );
		}

		List<Long> removedResources = CollectionUtils.difference( userResourceIds, resourcesList );

		Set<Long> masterList = appLicenseEntity.getResources();
		masterList.addAll( resourcesList );
		masterList.removeAll( removedResources );

		if ( masterList.size() > appLicenseEntity.getCount() )
		{
			String error = "Can not set " + masterList.size() + " resources for licenseId:" + licenseId + ", count " + appLicenseEntity.getCount();
			LOG.info( error );
			throw new LicenseException( error, LicenseExceptionType.LICENSE_COUNT_APP );
		}

		appLicenseEntity.setResources( masterList );
		appLicenseEntity.update();

		sendLicenseEvent( true, appLicenseEntity );

		auditAppLicense( AuditEventNameEnum.LICENSE_UPDATE, appLicenseEntity.toDataObject( masterList ), resources );
	}

	public List<AppLicenseInfo> getAppLicenseInfo( String appId )
	{
		List<AppLicenseInfo> result = new ArrayList();
		List<AppLicenseEntity> appLicenseEntities = appLicenseDAO.findAllByAppId( appId );
		for ( AppLicenseEntity appLicenseEntity : appLicenseEntities )
		{
			if ( appLicenseEntity.getType() == AppLicenseType.APP_GENERIC_ACCESS )
			{
				result.add( appLicenseEntity.toAppLicenseInfo() );
			}
		}
		return result;
	}

	public boolean checkAnalyticsLicense( Long resourceId )
	{
		ChannelResource channel = null;
		DeviceResource device = null;
		try
		{
			channel = ( ChannelResource ) getTopologyService().getResource( resourceId );
			String deviceId = channel.getChannelView().getDeviceId();
			device = getTopologyService().getDeviceResourceByDeviceId( deviceId );
		}
		catch ( TopologyException e )
		{
			return false;
		}

		boolean isThirdParty = false;
		if ( device.isRootDevice() )
		{

			VideoEncoderView[] video = channel.getChannelView().getVideo();
			for ( VideoEncoderView view : video )
			{
				if ( DeviceManagementConstants.isThirdPartyIdentifier( view.getCodecPrvData() ) )
				{
					isThirdParty = true;
					break;
				}
			}
		}
		else
		{
			isThirdParty = DeviceManagementConstants.isThirdPartyCamera( device.getDeviceView().getManufacturer(), device.getDeviceView().getModelName() );
		}

		if ( isThirdParty )
		{
			List<AppLicenseEntity> appLicenseEntities = appLicenseDAO.findAllDetached();
			for ( AppLicenseEntity appLicenseEntity : appLicenseEntities )
			{
				if ( ( appLicenseEntity.getType() == AppLicenseType.APP_GENERIC_CHANNEL_ACCESS ) && ( appLicenseEntity.getFeature().equals( LicenseFeature.THIRD_PARTY_ANALYTICS.getName() ) ) && ( appLicenseEntity.getResources().contains( resourceId ) ) )
				{
					return true;
				}
			}

			return false;
		}
		return true;
	}

	private void sendLicenseEvent( boolean isUpdate, AppLicenseEntity appLicenseEntity )
	{
		LicenseEvent event = new LicenseEvent( isUpdate ? LicenseEventType.UPDATED : LicenseEventType.REMOVED, appLicenseEntity.getLicenseId() );
		eventRegistry.sendEventAfterTransactionCommits( event );

		if ( appLicenseEntity.getType() == AppLicenseType.APP_GENERIC_ACCESS )
		{
			AppLicenseEvent appEvent = new AppLicenseEvent( isUpdate ? AppLicenseEventType.UPDATED : AppLicenseEventType.REMOVED, appLicenseEntity.toAppLicenseInfo() );
			eventRegistry.sendEventAfterTransactionCommits( appEvent );
		}
	}

	private List<Long> getResourcesForUser( AppLicenseEntity appLicenseEntity ) throws LicenseException
	{
		ResourceRootType root = ResourceRootType.SYSTEM;
		AppLicenseType type = appLicenseEntity.getType();
		Class<?> resourceClass;
		if ( ( type == AppLicenseType.APP_CHANNEL_ACCESS ) || ( type == AppLicenseType.APP_GENERIC_CHANNEL_ACCESS ) )
		{
			resourceClass = ChannelResource.class;
		}
		else
		{
			if ( type == AppLicenseType.APP_RECORDER_ACCESS )
				resourceClass = DeviceResource.class;
			else if ( type == AppLicenseType.APP_GENERIC_ACCESS )
			{
				resourceClass = GenericResource.class;
				root = ResourceRootType.SYSTEM_LOGICAL;
			}
			else
			{
				String error = "Can not get resources for " + type + " App License type";
				LOG.info( error );
				throw new LicenseException( error, LicenseExceptionType.LICENSE_TYPE_INCOMPATIBLE );
			}
		}

		List<Resource> userResources;

		try
		{
			String username = CommonAppUtils.getUsernameFromSecurityContext();
			userResources = getTopologyService().getResourcesForUser( username, root, new Criteria( resourceClass ), true );
		}
		catch ( TopologyException e )
		{
			String error = "Error getting Resources for App License, Exception:" + e.getMessage();
			LOG.info( error );
			throw new LicenseException( error, LicenseExceptionType.LICENSE_RESOURCE_ERROR );
		}

		List<Long> userResourceIds = new ArrayList();
		for ( Resource resource : userResources )
		{
			if ( ( resource instanceof DeviceResource ) )
			{
				DeviceResource deviceResource = ( DeviceResource ) resource;
				if ( !deviceResource.isRootDevice() )
				{
					continue;
				}
			}
			else if ( ( resource instanceof GenericResource ) )
			{
				GenericResource genericResource = ( GenericResource ) resource;
				if ( !CollectionUtils.containsIgnoreCase( appLicenseEntity.getResourceTypes(), genericResource.getType() ) )
				{
					continue;
				}
			}
			userResourceIds.add( resource.getId() );
		}
		return userResourceIds;
	}

	private void setInitialState( AppLicenseEntity appLicense )
	{
		if ( appLicense.getExpiry() == Expiry.TRIAL )
		{
			long days = getDaysRemaining( appLicense );
			if ( ( days <= 7L ) && ( days > 0L ) )
			{
				appLicense.setStatus( LicenseStatus.EXPIRING );
				return;
			}
			if ( days == 0L )
			{
				appLicense.setStatus( LicenseStatus.EXPIRED );
				return;
			}
		}
		appLicense.setStatus( LicenseStatus.OK );
	}

	private long getDaysRemaining( AppLicenseEntity appLicense )
	{
		long differenceMicros = appLicense.getEnd() - DateUtils.getCurrentUTCTimeInMicros();
		long remainingDays = ( long ) Math.ceil( differenceMicros / 8.64E10D );

		if ( remainingDays < 0L )
			remainingDays = 0L;

		return remainingDays;
	}

	private int getGraceDaysRemaining( int hours )
	{
		return ( int ) Math.ceil( hours / 24.0D );
	}

	private Pair[] getAppLicenseInfo( AppLicenseEntity appLicense )
	{
		Pair[] info = new Pair[2];
		Pair p = new Pair();
		p.setName( "licenseType" );
		p.setValue( appLicense.getType().toString() );
		info[0] = p;
		p = new Pair();
		p.setName( "appName" );
		p.setValue( appLicense.getIdentity().getName() );
		info[1] = p;
		return info;
	}

	protected void auditAppLicense( AuditEventNameEnum auditEvent, License appLicense, Long[] resources )
	{
		if ( CommonAppUtils.getUsernameFromSecurityContext() != null )
		{
			AuditView.Builder auditBuilder = new AuditView.Builder( auditEvent.getName() ).addDetailsPair( "type", appLicense.getType().name() ).addDetailsPair( "app", appLicense.getAppIdentity().getName() ).addDetailsPair( "app_id", appLicense.getAppIdentity().getId() );

			if ( ( resources != null ) && ( resources.length > 0 ) )
			{
				StringBuilder sb = new StringBuilder();
				for ( int i = 0; i < resources.length; )
				{
					Long resourceId = resources[i];
					sb.append( AuditLogDetailsHelper.findResourcePath( resourceId ) );
					i++;
					if ( i != resources.length )
					{
						sb.append( "," );
					}
				}
				auditBuilder.addDetailsPair( "resources", sb.toString() );
				auditBuilder.addDetailsPair( "count", String.valueOf( resources.length ) );
			}
			eventRegistry.sendEventAfterTransactionCommits( new AuditEvent( auditBuilder.build() ) );
		}
	}

	public void migrateSearchlightLicenses()
	{
		List<AppLicenseEntity> appLicenses = appLicenseDAO.findAll();
		for ( AppLicenseEntity appLicense : appLicenses )
		{
			if ( appLicense.getAppId().equals( AppIds.SEARCHLIGHT_4_0.getId() ) )
			{
				migrateSearchlightLicense( appLicense );
			}
		}
	}

	private void migrateSearchlightLicense( AppLicenseEntity appLicense )
	{
		LOG.info( "Migrating Searchlight " + appLicense.getFeature() + " 4.0.x license to 4.1" );

		appLicense.setAppId( AppIds.SEARCHLIGHT_4_1.getId() );
		appLicense.setResourceTypes( Arrays.asList( new String[] {"STORE", "BANK"} ) );
		appLicense.clearResources();

		appLicense.setIdentity( AppIds.SEARCHLIGHT_4_1 );
		appLicense.update();
	}

	public void setAppLicenseDAO( AppLicenseDAO appLicenseDAO )
	{
		this.appLicenseDAO = appLicenseDAO;
	}

	public void setLicenseService( LicenseService licenseService )
	{
		this.licenseService = licenseService;
	}

	private AppManager getAppManager()
	{
		if ( appManager == null )
		{
			appManager = ( ( AppManager ) ApplicationContextSupport.getBean( "appManager" ) );
		}
		return appManager;
	}

	public void setHealthService( HealthServiceIF healthService )
	{
		this.healthService = healthService;
	}

	public void setEventRegistry( EventRegistry eventRegistry )
	{
		this.eventRegistry = eventRegistry;
	}

	public void setStateCacheService( StateCacheService stateCacheService )
	{
		this.stateCacheService = stateCacheService;
	}

	public ResourceTopologyServiceIF getTopologyService()
	{
		if ( topologyService == null )
		{
			topologyService = ( ( ResourceTopologyServiceIF ) ApplicationContextSupport.getBean( "resourceTopologyService_internal" ) );
		}
		return topologyService;
	}
}
