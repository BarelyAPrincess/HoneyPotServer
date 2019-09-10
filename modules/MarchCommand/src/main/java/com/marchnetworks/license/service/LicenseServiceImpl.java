package com.marchnetworks.license.service;

import com.marchnetworks.audit.data.AuditEventNameEnum;
import com.marchnetworks.audit.data.AuditView;
import com.marchnetworks.audit.data.AuditView.Builder;
import com.marchnetworks.audit.events.AuditEvent;
import com.marchnetworks.command.api.initialization.BeforeInitializationListener;
import com.marchnetworks.command.api.license.LicenseCoreService;
import com.marchnetworks.command.api.rest.DeviceManagementConstants;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.device.data.DeviceView;
import com.marchnetworks.command.common.license.data.AppLicenseInfo;
import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.common.crypto.CryptoUtils;
import com.marchnetworks.common.system.ServerParameterStoreServiceIF;
import com.marchnetworks.common.transport.datamodel.DeviceException;
import com.marchnetworks.license.Crypto;
import com.marchnetworks.license.LicenseService;
import com.marchnetworks.license.ServerLicenseBO;
import com.marchnetworks.license.exception.LicenseException;
import com.marchnetworks.license.exception.LicenseExceptionType;
import com.marchnetworks.license.model.DeviceLicenseInfo;
import com.marchnetworks.license.model.License;
import com.marchnetworks.license.model.LicenseImport;
import com.marchnetworks.license.model.LicenseType;
import com.marchnetworks.license.model.ServerLicenseImport;
import com.marchnetworks.license.model.ServerLicenseInfo;
import com.marchnetworks.license.model.ServerLicenseType;
import com.marchnetworks.license.serverId.Criterion;
import com.marchnetworks.license.serverId.ServerId;
import com.marchnetworks.license.serverId.ServerId_v1;
import com.marchnetworks.license.serverId.criteria.UID;
import com.marchnetworks.management.instrumentation.DeviceRegistry;
import com.marchnetworks.management.instrumentation.DeviceService;
import com.marchnetworks.management.instrumentation.events.ServerIdHashEvent;
import com.marchnetworks.management.instrumentation.model.DeviceMBean;
import com.marchnetworks.server.event.EventRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LicenseServiceImpl implements LicenseService, LicenseCoreService, BeforeInitializationListener
{
	private static Logger LOG = LoggerFactory.getLogger( LicenseServiceImpl.class );

	private static final String PARAM_SERVERID = "serverId";

	private static final String SERVERID_CREATEDBY = "ces";

	public static final int GRACE_REBOOT_DECREMENT = 1;

	public static final int GRACE_PERIOD = 360;

	protected ServerLicenseBO serverLicenseBO;

	protected AppLicenseService appLicenseService;

	protected EventRegistry eventRegistry;

	protected DeviceRegistry deviceRegistry;

	protected DeviceService deviceService;
	protected ServerParameterStoreServiceIF parameterStore;
	protected static Crypto m_Crypto;
	protected static ServerId m_ServerId;

	public void beforeAppInitialized()
	{
		m_Crypto = new Crypto();
		initServerId();
		serverLicenseBO.start();
		appLicenseService.start();
	}

	protected void initServerId()
	{
		String gv = null;
		ServerId_v1 sidUID = new ServerId_v1();
		ServerId_v1 sid = new ServerId_v1();

		LOG.debug( "Loading serverId.." );
		m_ServerId = new ServerId_v1();

		gv = parameterStore.getParameterValue( "serverId" );
		if ( gv != null )
		{
			try
			{
				sidUID.load( Crypto.stringBase64ToByte( gv ), m_Crypto );
			}
			catch ( Exception e )
			{
				LOG.error( "Failure loading existing ServerId" );
			}
		}
		else
		{
			LOG.info( "No ServerId found" );
		}

		if ( !sidUID.isLoaded() )
		{
			LOG.info( "Generating new ServerId" );
			try
			{
				sid.generate( "ces" );
				m_ServerId = sid;

				String sSid = getServerId();
				parameterStore.storeParameter( "serverId", sSid );
				LOG.info( "New ServerId generated and saved successfully" );
			}
			catch ( Exception e )
			{
				LOG.error( "Critical error creating/saving serverId: ", e );
			}
		}
		else
		{
			try
			{
				Criterion c = sidUID.getCriterion( "UUID" );
				sid.generate( ( UID ) c, "ces", sidUID.getDate() );
				m_ServerId = sid;
			}
			catch ( Exception e )
			{
				LOG.error( "Critical error refreshing serverId: ", e );
			}
		}
	}

	public boolean isLicenseMatch( byte[] serverId )
	{
		try
		{
			ServerId sid = new ServerId_v1();
			sid.load( serverId, m_Crypto );
			return m_ServerId.IsSameServer( sid );
		}
		catch ( LicenseException e )
		{
		}
		return false;
	}

	public boolean validate( String content, byte[] signature ) throws Exception
	{
		if ( ( content == null ) || ( signature == null ) )
		{
			return false;
		}

		byte[] bHashedContent = Crypto.cmdHash( content );

		byte[] bDecryptSig = m_Crypto.cmdDecryptPub( signature );

		return Crypto.isByteArrayEqual( bHashedContent, bDecryptSig );
	}

	public String getServerId() throws Exception
	{
		return CommonAppUtils.byteToBase64( m_ServerId.export( m_Crypto ) );
	}

	public String getHashedServerId() throws Exception
	{
		String base64RawServerId = getServerId();

		byte[] SHA1_data = CryptoUtils.sha1( base64RawServerId.getBytes() );

		return CommonAppUtils.byteToBase64( SHA1_data );
	}

	public int loadParameter( String parameter )
	{
		int Result = -1;

		try
		{
			String gv = parameterStore.getParameterValue( parameter );
			if ( gv != null )
				Result = m_Crypto.importDecryptInt( gv );
		}
		catch ( Exception localException )
		{
			// Ignore
		}

		return Result;
	}

	public void saveParameter( int parameter, String parameterName )
	{
		try
		{
			parameterStore.storeParameter( parameterName, m_Crypto.exportEncryptInt( parameter ) );
		}
		catch ( Exception e )
		{
			LOG.warn( "Error saving <licensing> state: ", e );
		}
	}

	public boolean checkAppLicense( String appId )
	{
		return appLicenseService.checkAppLicense( appId );
	}

	public boolean isIdentifiedAndLicensedSession()
	{
		return appLicenseService.isIdentifiedAndLicensedSession();
	}

	public Collection<ServerLicenseInfo> getAllLicenseInfo() throws LicenseException
	{
		Collection<ServerLicenseInfo> result = new ArrayList( LicenseType.values().length );

		for ( ServerLicenseType slt : serverLicenseBO.getServerLicenses() )
		{
			int inUse = serverLicenseBO.getAllocated( slt.getType() );
			result.add( slt.toInfo( inUse ) );
		}
		return result;
	}

	public void importLicense( String license ) throws LicenseException
	{
		LOG.info( "Starting license import.." );

		ServerLicenseImport sli = ServerLicenseImport.parseServerLicenseImport( license );

		try
		{
			if ( !validate( sli.getContentString(), sli.getSignature() ) )
				throw new LicenseException( "License failed validation", LicenseExceptionType.LICENSE_VALIDATION_FAILED );
		}
		catch ( Exception e )
		{
			throw new LicenseException( "Server Error validating license: ", LicenseExceptionType.LICENSE_VALIDATION_FAILED, e );
		}

		if ( sli.requiresServerId() )
		{
			ServerId_v1 sid = new ServerId_v1();
			try
			{
				sid.load( sli.getServerId(), m_Crypto );
			}
			catch ( Exception e )
			{
				throw new LicenseException( "Corrupt serverId", LicenseExceptionType.LICENSE_SERVERID_NOT_FOUND );
			}
			if ( !sid.IsSameServer( m_ServerId ) )
			{
				throw new LicenseException( "License is meant for a different server", LicenseExceptionType.LICENSE_WRONG_SERVER );
			}
		}

		serverLicenseBO.importLicense( sli );

		appLicenseService.importLicense( sli );

		for ( LicenseImport importedLicense : sli.getLicenseImportList() )
		{
			auditLicenseImport( importedLicense );
		}
	}

	public void allocateForRegistration( Long deviceId ) throws LicenseException
	{
		DeviceMBean device = deviceRegistry.getDevice( deviceId.toString() );
		if ( device == null )
		{
			throw new LicenseException( "Could not find deviceId=" + deviceId, LicenseExceptionType.NOT_FOUND );
		}

		LicenseType t = getLicenseTypeRequired( device );

		if ( t == null )
		{
			return;
		}
		int required = getMinCountRequired( device, t );

		int total = serverLicenseBO.getTotal( t );
		int allocated = serverLicenseBO.getAllocated( t );

		if ( LOG.isDebugEnabled() )
			LOG.debug( "Available check: Total=" + total + " Allocated=" + allocated + " Required= " + required );
		if ( allocated + required > total )
		{
			throw new LicenseException( "No " + t.toString() + " license available", t == LicenseType.RECORDER ? LicenseExceptionType.LICENSE_COUNT_RECORDER : LicenseExceptionType.LICENSE_COUNT_CHANNEL );
		}

		LOG.info( "Allocating " + required + " " + t + " licenses to registering deviceId=" + deviceId );
		setDeviceLicense( deviceId, t, required );
	}

	public static LicenseType getLicenseTypeRequired( DeviceMBean device )
	{
		String m = device.getManufacturer();
		String f = device.getFamily();

		if ( DeviceManagementConstants.isVMSDevice( m, f ) )
			return LicenseType.CHANNEL;
		if ( DeviceManagementConstants.isNVRDevice( m, f ) )
			return LicenseType.RECORDER;
		if ( DeviceManagementConstants.isR5Device( m, f ) )
			return LicenseType.RECORDER;
		if ( DeviceManagementConstants.isExtractorDevice( m, f ) )
		{
			return null;
		}
		LOG.warn( "Unknown device: ManufacturerId=" + m + " FamilyId=" + f + ". Using Recorder license type as default requirement." );
		return LicenseType.RECORDER;
	}

	public static int getMinCountRequired( DeviceMBean d, LicenseType t )
	{
		if ( t == LicenseType.RECORDER )
		{
			return 1;
		}
		Integer i = d.getChannelsInUse();
		if ( i == null )
		{
			return 0;
		}
		return i.intValue();
	}

	public static int getMinCountRequired( DeviceView d, LicenseType t )
	{
		if ( t == LicenseType.RECORDER )
		{
			return 1;
		}
		Integer i = d.getChannelsInUse();
		if ( i == null )
		{
			return 0;
		}
		return i.intValue();
	}

	public void setDeviceLicense( Long deviceId, LicenseType type, int count ) throws LicenseException
	{
		LOG.info( "setDeviceLicense DeviceId={} Type={} Count={}", new Object[] {deviceId, type, Integer.valueOf( count )} );
		serverLicenseBO.setDeviceLicense( deviceId, type, count );

		auditDeviceLicense( deviceId.toString(), type.name(), String.valueOf( count ) );
	}

	public DeviceLicenseInfo getDeviceLicense( Long deviceId )
	{
		return serverLicenseBO.getDeviceLicense( deviceId );
	}

	public Collection<DeviceLicenseInfo> getAllDeviceLicense( LicenseType type ) throws LicenseException
	{
		return serverLicenseBO.getAllDeviceLicense( type );
	}

	public License getLicense( String licenseId ) throws LicenseException
	{
		return appLicenseService.getLicense( licenseId );
	}

	public List<Resource> filterAppResources( String appId, Long deviceResourceId, List<Resource> resources ) throws LicenseException
	{
		return appLicenseService.filterAppResources( appId, deviceResourceId, resources );
	}

	public License[] getLicenses() throws LicenseException
	{
		return appLicenseService.getLicenses();
	}

	public void removeLicense( String licenseId ) throws LicenseException
	{
		appLicenseService.removeLicense( licenseId );
	}

	public void setLicenseResources( String licenseId, Long[] resources ) throws LicenseException
	{
		appLicenseService.setLicenseResources( licenseId, resources );
	}

	public List<AppLicenseInfo> getAppLicenseInfo( String appId )
	{
		return appLicenseService.getAppLicenseInfo( appId );
	}

	public void processDeviceUnregistered( String deviceId )
	{
		appLicenseService.processDeviceUnregistered( deviceId );
	}

	public boolean checkAnalyticsLicense( Long resourceId, String deviceId )
	{
		return appLicenseService.checkAnalyticsLicense( resourceId );
	}

	protected void auditLicenseImport( LicenseImport license )
	{
		if ( CommonAppUtils.getUsernameFromSecurityContext() != null )
		{
			AuditView av = new Builder( AuditEventNameEnum.LICENSE_IMPORT.getName() ).addDetailsPair( "type", license.getLicenseTypeName() ).addDetailsPair( "expiration_type", license.getExpiry().name() ).addDetailsPair( "count", String.valueOf( license.getCount() ) ).build();

			if ( license.isTrialLicense() )
			{
				av.addDetailsPair( "valid_from", String.valueOf( license.getStart().getTime() ) );
				av.addDetailsPair( "valid_to", String.valueOf( license.getEnd().getTime() ) );
			}
			eventRegistry.sendEventAfterTransactionCommits( new AuditEvent( av ) );
		}
	}

	protected void auditDeviceLicense( String deviceId, String licenseType, String count )
	{
		if ( CommonAppUtils.getUsernameFromSecurityContext() != null )
		{
			AuditView av = new Builder( AuditEventNameEnum.LICENSE_ALLOCATE.getName() ).addRootDeviceToAudit( deviceId.toString(), true ).addDetailsPair( "type", licenseType ).addDetailsPair( "count", count ).build();

			eventRegistry.sendEventAfterTransactionCommits( new AuditEvent( av ) );
		}
	}

	public void allocateForTestDevice( DeviceMBean device ) throws LicenseException
	{
		LicenseType t = getLicenseTypeRequired( device );
		int required = getMinCountRequired( device, t );

		int total = serverLicenseBO.getTotal( t );
		int allocated = serverLicenseBO.getAllocated( t );

		if ( allocated + required > total )
		{
			throw new LicenseException( "No " + t.toString() + " license available", t == LicenseType.RECORDER ? LicenseExceptionType.LICENSE_COUNT_RECORDER : LicenseExceptionType.LICENSE_COUNT_CHANNEL );
		}

		serverLicenseBO.setTestDeviceLicense( Long.valueOf( Long.parseLong( device.getDeviceId() ) ), t, required );
	}

	public void sendServerId( Long deviceId, ServerIdHashEvent event )
	{
		String serverId = null;
		try
		{
			serverId = getHashedServerId();
		}
		catch ( Exception e1 )
		{
			LOG.error( "Hashed Server ID could not be generated for device [Device ID: {}]", deviceId );
		}
		if ( serverId != null )
		{
			try
			{
				deviceService.sendServerId( deviceId, serverId, event, false );
			}
			catch ( DeviceException e )
			{
				LOG.error( "Could not send the Hashed Server ID to device with ID: {}", deviceId );
			}
		}
	}

	public void setEventRegistry( EventRegistry er )
	{
		eventRegistry = er;
	}

	public void setServerLicenseBO( ServerLicenseBO serverLicenseBO )
	{
		this.serverLicenseBO = serverLicenseBO;
	}

	public void setDeviceRegistry( DeviceRegistry dr )
	{
		deviceRegistry = dr;
	}

	public void setAppLicenseService( AppLicenseService appLicenseService )
	{
		this.appLicenseService = appLicenseService;
	}

	public void setServerParameterStore( ServerParameterStoreServiceIF parameterStore )
	{
		this.parameterStore = parameterStore;
	}

	public void setDeviceService( DeviceService deviceService )
	{
		this.deviceService = deviceService;
	}
}
