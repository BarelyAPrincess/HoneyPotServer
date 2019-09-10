package com.marchnetworks.management.instrumentation;

import com.marchnetworks.audit.data.AuditEventNameEnum;
import com.marchnetworks.audit.data.AuditView;
import com.marchnetworks.audit.data.AuditView.Builder;
import com.marchnetworks.audit.events.AuditEvent;
import com.marchnetworks.command.api.alert.AlertDefinitionEnum;
import com.marchnetworks.command.api.initialization.InitializationListener;
import com.marchnetworks.command.api.metrics.input.BucketCounterInput;
import com.marchnetworks.command.api.query.Criteria;
import com.marchnetworks.command.api.query.Restrictions;
import com.marchnetworks.command.api.rest.DeviceManagementConstants;
import com.marchnetworks.command.api.topology.TopologyCoreService;
import com.marchnetworks.command.common.CollectionUtils;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.HttpUtils;
import com.marchnetworks.command.common.device.DeviceEventsEnum;
import com.marchnetworks.command.common.device.data.ChannelView;
import com.marchnetworks.command.common.device.data.ConnectState;
import com.marchnetworks.command.common.device.data.DeviceView;
import com.marchnetworks.command.common.device.data.MassRegistrationInfo;
import com.marchnetworks.command.common.device.data.RegistrationStatus;
import com.marchnetworks.command.common.scheduling.TaskScheduler;
import com.marchnetworks.command.common.topology.ResourceAssociationType;
import com.marchnetworks.command.common.topology.TopologyException;
import com.marchnetworks.command.common.topology.data.ChannelResource;
import com.marchnetworks.command.common.topology.data.DeviceResource;
import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.command.common.topology.data.ResourceAssociation;
import com.marchnetworks.command.common.topology.data.ResourcePathNode;
import com.marchnetworks.command.common.transport.data.GenericValue;
import com.marchnetworks.command.common.transport.data.Timestamp;
import com.marchnetworks.common.config.ConfigProperty;
import com.marchnetworks.common.device.DeletedDevice;
import com.marchnetworks.common.diagnostics.metrics.MetricsTypes;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.common.event.EventTypesEnum;
import com.marchnetworks.common.event.StateCacheable;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.transport.datamodel.DeviceException;
import com.marchnetworks.common.types.DeviceExceptionTypes;
import com.marchnetworks.common.utils.CommonUtils;
import com.marchnetworks.common.utils.DateUtils;
import com.marchnetworks.common.utils.ServerUtils;
import com.marchnetworks.health.input.DeviceAlertInput;
import com.marchnetworks.health.service.HealthServiceIF;
import com.marchnetworks.license.LicenseService;
import com.marchnetworks.license.exception.LicenseException;
import com.marchnetworks.license.exception.LicenseExceptionType;
import com.marchnetworks.license.model.LicenseType;
import com.marchnetworks.management.config.service.ConfigService;
import com.marchnetworks.management.config.service.ConfigurationException;
import com.marchnetworks.management.config.service.DeviceConfigDescriptor;
import com.marchnetworks.management.firmware.service.FirmwareService;
import com.marchnetworks.management.instrumentation.adaptation.DeviceEventHandlerScheduler;
import com.marchnetworks.management.instrumentation.dao.ChannelDAO;
import com.marchnetworks.management.instrumentation.dao.DeletedDeviceDAO;
import com.marchnetworks.management.instrumentation.dao.DeviceDAO;
import com.marchnetworks.management.instrumentation.data.DeviceSubscriptionType;
import com.marchnetworks.management.instrumentation.data.RegistrationAuditEnum;
import com.marchnetworks.management.instrumentation.data.RegistrationAuditInfo;
import com.marchnetworks.management.instrumentation.data.RegistrationState;
import com.marchnetworks.management.instrumentation.events.AbstractDeviceEvent;
import com.marchnetworks.management.instrumentation.events.ChildDeviceRegistrationEvent;
import com.marchnetworks.management.instrumentation.events.DeviceChannelAddedEvent;
import com.marchnetworks.management.instrumentation.events.DeviceChannelRemovedEvent;
import com.marchnetworks.management.instrumentation.events.DeviceConnectionStateChangeEvent;
import com.marchnetworks.management.instrumentation.events.DeviceConnectionStateEvent;
import com.marchnetworks.management.instrumentation.events.DeviceIpChangedEvent;
import com.marchnetworks.management.instrumentation.events.DeviceRegistrationEvent;
import com.marchnetworks.management.instrumentation.events.DeviceRestartEvent;
import com.marchnetworks.management.instrumentation.events.DeviceStateReconciliationEvent;
import com.marchnetworks.management.instrumentation.events.DeviceStatisticsStateEvent;
import com.marchnetworks.management.instrumentation.events.DeviceSystemChangedEvent;
import com.marchnetworks.management.instrumentation.events.GenericDeviceStateEvent;
import com.marchnetworks.management.instrumentation.events.ServerIdHashEvent;
import com.marchnetworks.management.instrumentation.events.TerritoryAwareDeviceEvent;
import com.marchnetworks.management.instrumentation.model.Channel;
import com.marchnetworks.management.instrumentation.model.CompositeDevice;
import com.marchnetworks.management.instrumentation.model.CompositeDeviceMBean;
import com.marchnetworks.management.instrumentation.model.Device;
import com.marchnetworks.management.instrumentation.model.DeviceMBean;
import com.marchnetworks.management.instrumentation.pooling.DeferredEvent;
import com.marchnetworks.management.instrumentation.pooling.DeferredEventPool;
import com.marchnetworks.management.instrumentation.registration.DeviceRegistrationScheduler;
import com.marchnetworks.management.instrumentation.subscription.DeviceSubscriptionManager;
import com.marchnetworks.management.instrumentation.task.DeviceTimeDeltaUpdater;
import com.marchnetworks.management.topology.ResourceTopologyServiceIF;
import com.marchnetworks.monitoring.metrics.event.MetricsEvent;
import com.marchnetworks.security.device.DeviceSessionHolderService;
import com.marchnetworks.server.communications.soap.PortInitializerTask;
import com.marchnetworks.server.communications.transport.datamodel.AlarmEntryCloseRecord;
import com.marchnetworks.server.communications.transport.datamodel.AlarmSource;
import com.marchnetworks.server.communications.transport.datamodel.AlertConfig;
import com.marchnetworks.server.communications.transport.datamodel.AlertEntry;
import com.marchnetworks.server.communications.transport.datamodel.ChannelDetails;
import com.marchnetworks.server.communications.transport.datamodel.ConfigurationEnvelope;
import com.marchnetworks.server.communications.transport.datamodel.DeviceDetails;
import com.marchnetworks.server.communications.transport.datamodel.GenericParameter;
import com.marchnetworks.server.event.EventListener;
import com.marchnetworks.server.event.EventRegistry;
import com.marchnetworks.server.event.StateCacheService;
import com.marchnetworks.shared.config.CommonConfiguration;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceServiceImpl implements DeviceService, EventListener, InitializationListener
{
	private static final Logger LOG = LoggerFactory.getLogger( DeviceServiceImpl.class );

	private DeviceRegistry deviceRegistry;

	private DeviceRegistrationScheduler registrationScheduler;
	private DeviceSubscriptionManager subscriptionManager;
	private DeviceDAO deviceDAO;
	private ChannelDAO channelDAO;
	private DeletedDeviceDAO deletedDeviceDAO;
	private StateCacheService stateCacheService;
	private CommonConfiguration configuration;
	private DeviceCapabilityService deviceCapabilityService;
	private DeviceSessionHolderService deviceSessionHolderService;
	private DeviceEventHandlerScheduler deviceEventHandlerScheduler;
	private static Map<String, String> deviceSettingsMap = new HashMap();
	private static String SERVER_ADDRESSES_LIST = "server.addresses";
	private static String AGENT_SETTINGS_LIST = "agent.params";

	public void onAppInitialized()
	{
		List<String> serverAddresses = ServerUtils.getServerAddressList();
		deviceSettingsMap.put( SERVER_ADDRESSES_LIST, String.valueOf( serverAddresses.hashCode() ) );

		Map<String, String> agentSettings = getAgentSettings();
		if ( !agentSettings.isEmpty() )
		{
			deviceSettingsMap.put( AGENT_SETTINGS_LIST, String.valueOf( agentSettings.hashCode() ) );
		}

		List<DeviceResource> devices = getTopologyService().getAllDeviceResources();
		List<String> urls = new ArrayList( devices.size() );
		for ( DeviceResource device : devices )
		{
			String url = device.getDeviceView().getRegistrationAddress();
			urls.add( url );

			DeviceStateReconciliationEvent deviceStateReconciliationEvent = new DeviceStateReconciliationEvent( device.getDeviceId() );
			getDeferredEventPool().add( device.getDeviceId(), new DeferredEvent( deviceStateReconciliationEvent, ConnectState.ONLINE.toString(), 172800000L ) );
		}

		PortInitializerTask task = new PortInitializerTask( urls );
		getTaskScheduler().executeNow( task );
	}

	public void process( Event event )
	{
		if ( ( event instanceof DeviceConnectionStateChangeEvent ) )
		{
			DeviceConnectionStateChangeEvent stateChangeEvent = ( DeviceConnectionStateChangeEvent ) event;
			if ( stateChangeEvent.getConnectState() == ConnectState.ONLINE )
			{
				checkDeviceGlobalSettings( stateChangeEvent.getDeviceId() );
			}
		}
		else if ( ( event instanceof DeviceRestartEvent ) )
		{
			DeviceRestartEvent restartEvent = ( DeviceRestartEvent ) event;
			checkDeviceGlobalSettings( restartEvent.getDeviceId() );
		}
		else if ( ( event instanceof DeviceStateReconciliationEvent ) )
		{
			DeviceStateReconciliationEvent deviceEvent = ( DeviceStateReconciliationEvent ) event;
			fetchDeviceStateEvents( deviceEvent.getDeviceId() );
		}
	}

	public String getListenerName()
	{
		return DeviceServiceImpl.class.getSimpleName();
	}

	private CompositeDeviceMBean addCompositeDevice( String deviceAddress, String stationId )
	{
		CompositeDevice ret = new CompositeDevice();

		ret.setAddress( deviceAddress );
		ret.setTimeCreated( Calendar.getInstance() );
		deviceDAO.create( ret );

		if ( CommonAppUtils.isNullOrEmptyString( stationId ) )
		{
			stationId = generateStationId( ret.getDeviceId() );
		}
		ret.setStationId( stationId );

		transitRegistrationStatus( ret, RegistrationStatus.INITIAL );

		deviceRegistry.putConnectState( ret.getDeviceId(), ConnectState.ONLINE );

		return ret;
	}

	private String generateStationId( String deviceId )
	{
		int index = 0;
		Long deviceIdLong = Long.valueOf( Long.parseLong( deviceId ) );
		for ( ; ; )
		{
			String targetStationId = "NVR-" + ( deviceIdLong.longValue() + index );
			Criteria criteria = new Criteria( DeviceResource.class );
			criteria.add( Restrictions.eq( "deviceView.parentDeviceId", null ) );
			criteria.add( Restrictions.eq( "deviceView.stationId", targetStationId ) );

			if ( getTopologyService().getFirstResource( criteria ) == null )
			{
				return targetStationId;
			}
			index += 1;
		}
	}

	private RemoteCompositeDeviceOperations preSetUpForRegisterDevice( DeviceResource deviceResource, Map<String, Object> registrationParams )
	{
		deviceResource.getDeviceView().setAdditionalDeviceRegistrationInfo( registrationParams );
		deviceResource.getDeviceView().setEventSubscriptionId( null );

		if ( ( String ) registrationParams.get( "deviceAdress" ) != null )
		{
			deviceResource.getDeviceView().setRegistrationAddress( ( String ) registrationParams.get( "deviceAdress" ) );
		}

		RemoteCompositeDeviceOperations adaptor = ( RemoteCompositeDeviceOperations ) getDeviceAdaptorFactory().getDeviceAdaptor( deviceResource );
		return adaptor;
	}

	private String registerDevice( DeviceResource deviceResource, String deviceId, DeviceSubscriptionType subscriptionType, RemoteCompositeDeviceOperations adaptor, String stationId ) throws DeviceException
	{
		String subscriptionId = null;
		String securityToken = ( String ) deviceResource.getDeviceView().getAdditionalDeviceRegistrationInfo().get( "globalSecurityToken" );

		if ( securityToken != null )
		{
			String session = adaptor.getSessionIdWithESMToken( securityToken );
			deviceResource.getDeviceView().getAdditionalDeviceRegistrationInfo().put( "securityToken", session );
		}

		RegistrationState registrationState = null;
		try
		{
			registrationState = adaptor.retrieveRegistrationState();
			LOG.debug( "Registration state for device {}:{}", deviceId );
		}
		catch ( DeviceException ex )
		{
			LOG.info( " Error while retrieving registration state from device {}:{}", deviceId, ex.getDetailedErrorMessage() );
			throw ex;
		}

		if ( registrationState.isRegistered() )
		{
			LOG.debug( " device {}: already registered with Server: {}", deviceId, registrationState.getServerAddress() );
		}

		if ( isRegisteredByThisServer( registrationState ) )
		{
			LOG.debug( "Device already registered with this host {}.", registrationState.getServerAddress() );

			String rsDeviceID = registrationState.getDeviceId();
			if ( !deviceId.equals( rsDeviceID ) )
			{
				long deviceCreationTime = registrationState.getDeviceCreateTime();
				Device foundDevice = ( Device ) deviceRegistry.getDeviceByTime( deviceCreationTime );

				if ( foundDevice != null )
				{

					RegistrationStatus currentStatus = foundDevice.getRegistrationStatus();
					if ( ( currentStatus == RegistrationStatus.REGISTERED ) || ( currentStatus == RegistrationStatus.PENDING_REGISTRATION ) || ( currentStatus == RegistrationStatus.ERROR_REGISTRATION ) || ( currentStatus == RegistrationStatus.MARKED_FOR_REPLACEMENT ) || ( currentStatus == RegistrationStatus.ERROR_REPLACEMENT ) )
					{

						DeviceException dex = new DeviceException( "Device already registered with id " + registrationState.getDeviceId(), DeviceExceptionTypes.DEVICE_ALREADY_REGISTERED_WITH_THIS_SERVER );
						throw dex;
					}
				}
			}
		}
		else if ( isRegisteredByAnotherServer( registrationState ) )
		{
			LOG.debug( "Device already registered with another host {}.", registrationState.getServerAddress() );

			DeviceException de = new DeviceException( "Device already registered with another host", DeviceExceptionTypes.DEVICE_REGISTERED_WITH_ANOTHER_SERVER );
			throw de;
		}
		try
		{
			subscriptionId = adaptor.register( subscriptionType );
		}
		catch ( DeviceException ex )
		{
			throw ex;
		}

		if ( !CommonAppUtils.isNullOrEmptyString( stationId ) )
		{
			try
			{
				GenericParameter parameter = GenericParameter.newGenericParameter( "system.station.id", stationId );
				adaptor.setParamValues( new GenericParameter[] {parameter} );
				LOG.info( "Set station id {} to device {} ", stationId, deviceId );
			}
			catch ( DeviceException ex )
			{
				LOG.info( "Set station id {} to device failed ! ", stationId );
			}
		}
		else
		{
			LOG.error( "Device stationid is null ! " );
		}

		return subscriptionId;
	}

	public void beginRegister( String deviceId )
	{
		CompositeDevice device = null;
		try
		{
			device = ( CompositeDevice ) deviceDAO.findById( deviceId );
		}
		catch ( ClassCastException cce )
		{
			LOG.warn( "Device {} is not a Root Device. Registration Aborted.", deviceId );
		}

		if ( device == null )
		{
			LOG.warn( "Device {} not found. Device was unregistered. Registration Aborted.", deviceId );
			transitRegistrationError( device, RegistrationStatus.ERROR_REGISTRATION, new DeviceException( "Device not found", DeviceExceptionTypes.DEVICE_NOT_FOUND ) );
			return;
		}

		transitRegistrationStatus( device, RegistrationStatus.PENDING_REGISTRATION );
	}

	public void register( String deviceId, Map<String, Object> registrationParams )
	{
		long start = System.currentTimeMillis();
		CompositeDevice device = null;
		String subscriptionId = null;

		DeviceResource deviceResource = getTopologyService().getDeviceResourceByDeviceId( deviceId );

		Map<String, Object> tempRegistrationParams = registrationParams != null ? new HashMap( registrationParams ) : deviceResource.getDeviceView().getAdditionalDeviceRegistrationInfo();
		boolean isMassRegister = tempRegistrationParams.get( "isMassRegister" ) != null;
		try
		{
			RemoteCompositeDeviceOperations adaptor = null;
			DeviceDetails deviceDetails = null;
			String registrationAddress = ( String ) tempRegistrationParams.get( "deviceAdress" );
			try
			{
				adaptor = preSetUpForRegisterDevice( deviceResource, tempRegistrationParams );
				adaptor.getDeviceInfo( true, false );
			}
			catch ( DeviceException dex )
			{
				String autoDetectedAddress = ( String ) tempRegistrationParams.get( "autodetectedaddress" );

				if ( ( autoDetectedAddress == null ) || ( autoDetectedAddress.equals( ( String ) registrationParams.get( "deviceAdress" ) ) ) )
				{
					throw dex;
				}

				tempRegistrationParams.put( "deviceAdress", autoDetectedAddress );
				adaptor = preSetUpForRegisterDevice( deviceResource, tempRegistrationParams );

				registrationAddress = autoDetectedAddress;
			}

			subscriptionId = registerDevice( deviceResource, deviceId, DeviceSubscriptionType.FULL_EVENTS, adaptor, deviceResource.getDeviceView().getStationId() );
			deviceDetails = adaptor.getDeviceDetailsInfo();

			if ( deviceCapabilityService.isCapabilityEnabled( Long.parseLong( deviceId ), "register.2" ) )
			{
				Map<String, String> deviceGlobalSettings = new HashMap();
				deviceResource.getDeviceView().setGlobalSettings( deviceGlobalSettings );
				deviceGlobalSettings.put( SERVER_ADDRESSES_LIST, deviceSettingsMap.get( SERVER_ADDRESSES_LIST ) );

				if ( pushDeviceGlobalSetting( deviceResource, AGENT_SETTINGS_LIST ) )
				{
					deviceGlobalSettings.put( AGENT_SETTINGS_LIST, deviceSettingsMap.get( AGENT_SETTINGS_LIST ) );
				}
			}

			device = ( CompositeDevice ) deviceDAO.findById( deviceId );
			setRegisteredDeviceDetailsInfo( device, deviceDetails, adaptor );
			transitRegistrationStatus( device, RegistrationStatus.REGISTERED, isMassRegister );

			device.setAddress( registrationAddress );
			device.setTimeDelta( deviceResource.getDeviceView().getTimeDelta() );
			device.setDeviceEventSubscriptionId( subscriptionId );
			device.setEventSubscriptionPrefixes( subscriptionManager.getDeviceEventSubscriptionsByType( DeviceSubscriptionType.FULL_EVENTS ) );
			device.initializeDeviceEventSequenceId();
			device.setGlobalSettings( deviceResource.getDeviceView().getGlobalSettings() );
			device.setNotifyInterval( Integer.valueOf( InstrumentationSettings.DEVICE_NOTIFY_INTERVAL ) );

			getEventRegistry().sendEventAfterTransactionCommits( new MetricsEvent( new BucketCounterInput( MetricsTypes.DEVICE_REGISTRATION.getName(), "success" ) ) );
			LOG.info( "Device {} registered in {} ms.", deviceId, Long.valueOf( System.currentTimeMillis() - start ) );
		}
		catch ( DeviceException ex )
		{
			updateRegistrationStatus( deviceId, RegistrationStatus.ERROR_REGISTRATION, ex );
			if ( ex.isCommunicationError() )
			{
				LOG.info( "Could not register {} in {} ms due to communication problem, Exception: {}", new Object[] {deviceId, Long.valueOf( System.currentTimeMillis() - start ), ex.getMessage()} );
			}
			else
			{
				LOG.warn( "Could not register {} in {} ms. Error: {}", new Object[] {deviceId, Long.valueOf( System.currentTimeMillis() - start ), ex.getMessage()} );
			}
			getEventRegistry().sendEventAfterTransactionCommits( new MetricsEvent( new BucketCounterInput( MetricsTypes.DEVICE_REGISTRATION.getName(), "failure" ) ) );
			LOG.debug( "Could not register.", ex );
		}
	}

	private void setRegisteredDeviceDetailsInfo( CompositeDevice device, DeviceDetails deviceDetails, RemoteCompositeDeviceOperations adaptor ) throws DeviceException
	{
		device.setPartialDeviceInfo( deviceDetails );

		LicenseService licenseService = ( LicenseService ) ApplicationContextSupport.getBean( "licenseService_internal" );
		try
		{
			licenseService.allocateForRegistration( Long.valueOf( device.getDeviceId() ) );
		}
		catch ( LicenseException e )
		{
			adaptor.unregister();
			device.setDeviceEventSubscriptionId( null );

			LicenseExceptionType faultCode = e.getFaultCode();
			DeviceException de;

			if ( ( faultCode == LicenseExceptionType.LICENSE_COUNT_RECORDER ) || ( faultCode == LicenseExceptionType.LICENSE_COUNT_CHANNEL ) )
			{
				DeviceExceptionTypes errorType = DeviceExceptionTypes.NO_AVAILABLE_LICENSE_RECORDER;
				LicenseType licenseType = LicenseType.RECORDER;
				if ( faultCode == LicenseExceptionType.LICENSE_COUNT_CHANNEL )
				{
					errorType = DeviceExceptionTypes.NO_AVAILABLE_LICENSE_CHANNEL;
					licenseType = LicenseType.CHANNEL;
				}
				de = new DeviceException( "Server does not have available license for " + licenseType.toString() + " license type", errorType );
			}
			else
			{

				if ( faultCode == LicenseExceptionType.LICENSE_EXPIRED )
				{
					de = new DeviceException( e.getMessage(), DeviceExceptionTypes.SOFT_LICENSE_EXPIRED );
				}
				else
				{
					if ( faultCode == LicenseExceptionType.LICENSE_COUNT_FROZEN )
					{
						de = new DeviceException( e.getMessage(), DeviceExceptionTypes.LICENSE_STATE_FROZEN );
					}
					else
						de = new DeviceException( "Failed to update internal License store to Device", DeviceExceptionTypes.COULD_NOT_ASSIGN_SOFT_LICENSE );
				}
			}
			throw de;
		}

		device.setDeviceInfoFromTransport( deviceDetails );

		for ( ChannelDetails channel : deviceDetails.getDeviceChannels() )
		{
			addChannelToDevice( device, channel );
		}
		device.consolidateDevices( deviceDetails );

		device.getChildDevices().clear();
		for ( DeviceDetails childDeviceDetail : deviceDetails.getChildDevices() )
		{

			if ( !childDeviceDetail.getDeviceChannels().isEmpty() )
			{

				boolean hasEnabledChannel = false;
				for ( ChannelDetails channelDetail : childDeviceDetail.getDeviceChannels() )
				{
					if ( ChannelDetails.isChannelEnabled( channelDetail ) )
					{
						hasEnabledChannel = true;
						break;
					}
				}
				if ( hasEnabledChannel )
				{

					Device child = addChildDevice( device, childDeviceDetail );

					for ( ChannelDetails channel : childDeviceDetail.getDeviceChannels() )
					{
						addChannelToDevice( child, channel );
					}

					transitRegistrationStatus( child, RegistrationStatus.REGISTERED );
				}
			}
		}
	}

	public RegistrationAuditInfo registerDeviceResource( String deviceAddress, String stationId, Map<String, Object> registrationParams ) throws DeviceException
	{
		Criteria criteria = new Criteria( DeviceResource.class );
		criteria.add( Restrictions.eq( "deviceView.registrationAddress", deviceAddress ) );
		DeviceResource device = ( DeviceResource ) getTopologyService().getFirstResource( criteria );

		RegistrationAuditInfo registrationAuditInfo = new RegistrationAuditInfo();

		if ( !CommonAppUtils.isNullOrEmptyString( stationId ) )
		{
			criteria.clear();
			criteria.add( Restrictions.eq( "deviceView.stationId", stationId ) );
			DeviceResource matchedStationIdDevice = ( DeviceResource ) getTopologyService().getFirstResource( criteria );

			if ( matchedStationIdDevice != null )
			{
				RegistrationStatus status = matchedStationIdDevice.getDeviceView().getRegistrationStatus();
				if ( ( device != null ) && ( !device.getDeviceId().equals( matchedStationIdDevice.getDeviceId() ) ) )
				{
					throw new DeviceException( "Address " + deviceAddress + " for device id " + matchedStationIdDevice.getDeviceId() + " to replace already exists", DeviceExceptionTypes.DEVICE_ALREADY_REGISTERED_WITH_THIS_SERVER );
				}

				if ( ( status == RegistrationStatus.MARKED_FOR_REPLACEMENT ) || ( status == RegistrationStatus.ERROR_REPLACEMENT ) )
				{
					LOG.info( "Replacing device {}, address {}", matchedStationIdDevice.getDeviceId(), deviceAddress );
					scheduleDeviceReplacement( matchedStationIdDevice, deviceAddress, registrationParams );
					registrationAuditInfo.setDeviceId( matchedStationIdDevice.getDeviceId() );
					registrationAuditInfo.setRegistrationAuditEnum( RegistrationAuditEnum.REPLACEMENT );
				}
				else
				{
					if ( status == RegistrationStatus.ERROR_REGISTRATION )
					{
						throw new DeviceException( "Device address " + deviceAddress + " is already registered on this server", DeviceExceptionTypes.DEVICE_ALREADY_REGISTERED_WITH_THIS_SERVER );
					}

					throw new DeviceException( "Device id " + matchedStationIdDevice.getDeviceId() + ", status " + status + " must be marked for replacement to replace", DeviceExceptionTypes.DEVICE_NOT_MARKED_FOR_REPLACEMENT );
				}
				return registrationAuditInfo;
			}
		}

		boolean addNewDevice = true;
		if ( device != null )
		{
			if ( RegistrationStatus.ERROR_REGISTRATION == device.getDeviceView().getRegistrationStatus() )
			{
				addNewDevice = false;
			}
			else if ( !RegistrationStatus.isRegisteredStatus( device.getDeviceView().getRegistrationStatus() ) )
			{
				throw new DeviceException( "Device address " + deviceAddress + " is in a " + device.getDeviceView().getRegistrationStatus() + " status. Won't proceed with registration", DeviceExceptionTypes.DEVICE_ALREADY_REGISTERED_WITH_THIS_SERVER );
			}
		}

		if ( addNewDevice )
		{
			CompositeDeviceMBean registeredCompositeDeviceMBean = addCompositeDevice( deviceAddress, stationId );
			LOG.info( "Registering Device id {}, address {}", registeredCompositeDeviceMBean.getDeviceId(), deviceAddress );
			registrationAuditInfo.setDeviceId( registeredCompositeDeviceMBean.getDeviceId() );
		}
		else
		{
			registrationAuditInfo.setDeviceId( device.getDeviceId() );
			LOG.info( "Attempting to register Device again id {}, address {}", device.getDeviceId(), deviceAddress );
		}

		registrationParams.put( "useTrusted", Boolean.TRUE );
		registrationParams.put( "deviceAdress", deviceAddress );

		registrationAuditInfo.setRegistrationAuditEnum( RegistrationAuditEnum.REGISTRATION );
		return registrationAuditInfo;
	}

	public void replacementConfigFailed( String deviceId )
	{
		DeviceException de = new DeviceException( "Replace device configuration apply failed", DeviceExceptionTypes.DEVICE_REPLACE_CONFIG_APPLY_ERROR );
		updateRegistrationStatus( deviceId, RegistrationStatus.ERROR_REPLACEMENT, de );
		LOG.info( "device {} is in error_replacement status", deviceId );
	}

	public void replacementConfigApplied( DeviceMBean dev ) throws DeviceException
	{
		CompositeDevice device = ( CompositeDevice ) dev;
		transitRegistrationStatus( device, RegistrationStatus.REGISTERED );
		DeviceResource deviceResource = getTopologyService().getDeviceResourceByDeviceId( device.getDeviceId() );
		LOG.info( "device {} serial: {} back to registered status", device.getDeviceId(), deviceResource.getDeviceView().getSerial() );
		device.setEventSubscriptionPrefixes( subscriptionManager.getDeviceEventSubscriptionsByType( DeviceSubscriptionType.FULL_EVENTS ) );
		deviceResource.getDeviceView().setDeviceEventSubscriptionPrefixes( subscriptionManager.getDeviceEventSubscriptionsByType( DeviceSubscriptionType.FULL_EVENTS ) );
		subscriptionManager.setDeviceEventSubscriptionsType( device.getDeviceId(), DeviceSubscriptionType.FULL_EVENTS );
		modifyDeviceSubscription( device.getDeviceId(), DeviceSubscriptionType.FULL_EVENTS );
	}

	public void markForReplacement( String deviceId, Boolean markForReplacement ) throws DeviceException
	{
		CompositeDevice device = ( CompositeDevice ) deviceRegistry.getDevice( deviceId );
		String serialFromSnapShot = null;
		DeviceConfigDescriptor deviceConfigDescriptor = null;

		if ( device == null )
		{
			throw new DeviceException( "Device not found", DeviceExceptionTypes.DEVICE_NOT_FOUND );
		}
		try
		{
			if ( !getConfigurationService().isDeviceConfigSnapShotExist( deviceId ) )
			{
				throw new DeviceException( "The device configuration does not exist", DeviceExceptionTypes.DEVICE_CONFIGURATION_SNAPSHOT_NOT_EXISTS );
			}
			deviceConfigDescriptor = getConfigurationService().getDeviceConfigByDeviceId( deviceId );
			serialFromSnapShot = deviceConfigDescriptor.getDeviceSerial();
		}
		catch ( ConfigurationException e )
		{
			LOG.warn( "Device {} check configuration exception ...", deviceId );
			throw new DeviceException( "The device configuration does not exist", DeviceExceptionTypes.DEVICE_CONFIGURATION_SNAPSHOT_NOT_EXISTS );
		}

		if ( CommonAppUtils.isNullOrEmptyString( device.getStationId() ) )
		{
			device.setStationId( generateStationId( deviceId ) );
		}

		if ( markForReplacement.booleanValue() )
		{
			if ( !CommonUtils.isReplaceableDevice( device.getFamily(), device.getModel(), device.getSoftwareVersion() ) )
			{
				LOG.debug( "The device {} cannot be replaced, maybe the software version is lower than the minimum version for mark for replacement", device.getAddress() );
				throw new DeviceException( "The device software version is lower than the minimum version for mark for replacement", DeviceExceptionTypes.DEVICE_FIRMWARE_VERSION_TOO_LOW );
			}
			try
			{
				if ( CommonAppUtils.isNullOrEmptyString( serialFromSnapShot ) )
				{
					getConfigurationService().updateConfigSnapShotSerial( device );
					LOG.info( "Update Device {} configuration snapShot serial number to {} ", deviceId, device.getSerial() );
				}
			}
			catch ( ConfigurationException e )
			{
				LOG.warn( "Failed to update Device {} configuration snapshot ...", deviceId );
				throw new DeviceException( "The device configuration could not be updated", DeviceExceptionTypes.DEVICE_CONFIGURATION_SNAPSHOT_NOT_EXISTS );
			}

			LOG.info( "Device {} marked for replacement  ", deviceId );
			transitRegistrationStatus( device, RegistrationStatus.MARKED_FOR_REPLACEMENT );
		}
		else
		{
			LOG.info( "Device {} undo mark for replacement  ", deviceId );
			DeviceResource deviceResource = getTopologyService().getDeviceResourceByDeviceId( device.getDeviceId() );

			if ( !device.getSerial().equalsIgnoreCase( serialFromSnapShot ) )
			{
				device.setDeviceEventSubscriptionId( null );
				deviceResource.getDeviceView().setEventSubscriptionId( null );
				device.setSerial( serialFromSnapShot );
				deviceResource.getDeviceView().setSerial( serialFromSnapShot );

				device.setFamily( deviceConfigDescriptor.getDeviceFamily() );
				deviceResource.getDeviceView().setFamily( deviceConfigDescriptor.getDeviceFamily() );
				device.setModel( deviceConfigDescriptor.getDeviceModel() );
				deviceResource.getDeviceView().setModel( deviceConfigDescriptor.getDeviceModel() );
				device.setSoftwareVersion( deviceConfigDescriptor.getFirmwareVersion() );
				deviceResource.getDeviceView().setSoftwareVersion( deviceConfigDescriptor.getFirmwareVersion() );
				LOG.info( "Update the Device: " + deviceId + " serial to: " + serialFromSnapShot + " and softwareVersion to: " + deviceConfigDescriptor.getFirmwareVersion() );
			}

			device.setRegistrationStatus( RegistrationStatus.REGISTERED );
			deviceResource.getDeviceView().setRegistrationStatus( RegistrationStatus.REGISTERED );
			removeCachedRegistrationState( deviceId );
			try
			{
				getTopologyService().updateResource( deviceResource );
			}
			catch ( TopologyException e )
			{
				LOG.error( "Device {} undo mark for replacement update resource error.", deviceId );
			}
		}
	}

	public void replaceDevice( String deviceId, Map<String, Object> registrationParams )
	{
		String updateAddress = ( String ) registrationParams.get( "deviceAdress" );

		DeviceResource deviceResource = getTopologyService().getDeviceResourceByDeviceId( deviceId );
		RemoteCompositeDeviceOperations adaptor = preSetUpForRegisterDevice( deviceResource, registrationParams );

		DeviceDetails deviceDetails;

		try
		{
			deviceDetails = adaptor.retrieveInfo();
		}
		catch ( DeviceException de )
		{
			if ( DeviceExceptionTypes.UNKNOWN.equals( de.getDetailedErrorType() ) )
			{
				DeviceException ex = new DeviceException( de.getMessage(), DeviceExceptionTypes.DEVICE_REPLACE_UNKNOWN_ERROR );
				updateRegistrationStatus( deviceId, RegistrationStatus.ERROR_REPLACEMENT, ex );
				LOG.error( "replacement error for device {} due to the retrieveInfo exception :{} ", deviceId, ex.getDetailedErrorType() );
			}
			else
			{
				updateRegistrationStatus( deviceId, RegistrationStatus.ERROR_REPLACEMENT, de );
				LOG.error( "replacement error for device {} due to the retrieveInfo exception :{} ", deviceId, de.getDetailedErrorType() );
			}
			return;
		}

		DeviceConfigDescriptor deviceConfigDescriptor = null;
		String falimyIdFromDeviceDetails = Integer.toString( deviceDetails.getFamilyId() );
		String modelIdFromdeviceDetails = Integer.toString( deviceDetails.getModelId() );
		if ( !CommonUtils.isReplaceableModel( falimyIdFromDeviceDetails, modelIdFromdeviceDetails ) )
		{
			DeviceException de = new DeviceException( "The NVR model error, replacement abort", DeviceExceptionTypes.DEVICE_REPLACE_MODEL_ERROR );
			updateRegistrationStatus( deviceId, RegistrationStatus.ERROR_REPLACEMENT, de );
			LOG.error( "replacement error for device: {} due to the device model exception:{} ", deviceId, de.getMessage() );
			return;
		}
		try
		{
			deviceConfigDescriptor = getConfigurationService().getDeviceConfigByDeviceId( deviceId );
			if ( !DeviceManagementConstants.replacementDeviceModelCheck( falimyIdFromDeviceDetails, modelIdFromdeviceDetails, deviceConfigDescriptor.getDeviceFamily(), deviceConfigDescriptor.getDeviceModel() ) )
			{
				DeviceException de = new DeviceException( "The NVR model error, replacement abort", DeviceExceptionTypes.DEVICE_REPLACE_MODEL_ERROR );
				updateRegistrationStatus( deviceId, RegistrationStatus.ERROR_REPLACEMENT, de );
				LOG.error( "replacement error for device {} due to the device model exception: {} ", deviceId, de.getMessage() );
				return;
			}
		}
		catch ( ConfigurationException e )
		{
			LOG.error( "Device {} Apply replacement exception and aborted...", deviceId );
			DeviceException de = new DeviceException( "The device configuration snapshot not exists, replacement abort", DeviceExceptionTypes.DEVICE_CONFIGURATION_SNAPSHOT_NOT_EXISTS );
			updateRegistrationStatus( deviceId, RegistrationStatus.ERROR_REPLACEMENT, de );
			LOG.error( "replacement error for device {} due to the Configuration Exception :{} ", deviceId, de.getMessage() );
			return;
		}
		String subscriptionId = null;
		try
		{
			subscriptionId = registerDevice( deviceResource, deviceId, DeviceSubscriptionType.EVENTS_FOR_REPLACEMENT, adaptor, deviceResource.getDeviceView().getStationId() );
		}
		catch ( DeviceException ex )
		{
			updateRegistrationStatus( deviceId, RegistrationStatus.ERROR_REPLACEMENT, ex );
			LOG.error( "replacement error for device {} due to the register Device exception :{} ", deviceId, ex.getMessage() );
			return;
		}
		CompositeDevice device = null;
		try
		{
			device = ( CompositeDevice ) deviceDAO.findById( deviceId );
		}
		catch ( ClassCastException cce )
		{
			LOG.error( "Device {} is not a Root Device. replace Device Aborted.", deviceId );
			return;
		}

		if ( device == null )
		{
			LOG.error( "Device {} not found. Device was unregistered. replace Device Aborted.", deviceId );
			return;
		}

		transitRegistrationStatus( device, RegistrationStatus.PENDING_REPLACEMENT );
		LOG.info( "device {} is in pending replacement status", deviceId );

		device.setAddress( updateAddress );
		device.setDeviceSystemInfoFromTransport( deviceDetails );

		LOG.info( "device {} address update to {} ", deviceId, updateAddress );
		device.setTimeDelta( deviceResource.getDeviceView().getTimeDelta() );
		device.setDeviceEventSubscriptionId( subscriptionId );
		device.setEventSubscriptionPrefixes( subscriptionManager.getDeviceEventSubscriptionsByType( DeviceSubscriptionType.EVENTS_FOR_REPLACEMENT ) );
		device.initializeDeviceEventSequenceId();

		checkVersionandApplyConfig( device, deviceDetails );
	}

	public void scheduleRetryReplacement( String deviceId ) throws DeviceException
	{
		registrationScheduler.scheduleRetryReplacement( deviceId );
	}

	public void resumeReplacement( DeviceMBean dev )
	{
		CompositeDevice device = ( CompositeDevice ) dev;

		LOG.info( " Resume the replacement for device {} ", device.getDeviceId() );
		DeviceResource deviceResource = getTopologyService().getDeviceResourceByDeviceId( device.getDeviceId() );
		RemoteCompositeDeviceOperations adaptor = getDeviceAdaptorFactory().getDeviceAdaptor( deviceResource );
		DeviceDetails deviceDetails;

		try
		{
			deviceDetails = adaptor.retrieveInfo();

			if ( CommonAppUtils.isNullOrEmptyString( deviceDetails.getStationId() ) )
			{
				LOG.warn( "The replacing device {} is now in {} but missing the CES station ID. Set the station ID {}!!!", new Object[] {device.getDeviceId(), deviceDetails.getSwVersion(), device.getStationId()} );
				GenericParameter parameter = GenericParameter.newGenericParameter( "system.station.id", device.getStationId() );
				adaptor.setParamValues( new GenericParameter[] {parameter} );
			}
		}
		catch ( DeviceException de )
		{
			DeviceException ex = new DeviceException( de.getMessage(), DeviceExceptionTypes.DEVICE_REPLACE_RETRY_FAIL );
			transitRegistrationError( device, RegistrationStatus.ERROR_REPLACEMENT, ex );
			LOG.error( "replacement error for device {} due to the retrieveInfo exception :{} ", device.getDeviceId(), de.getMessage() );
			return;
		}

		checkVersionandApplyConfig( device, deviceDetails );
	}

	public void retryReplaceDevice( String deviceId )
	{
		LOG.info( " retry replacement for device {} ", deviceId );

		DeviceResource deviceResource = getTopologyService().getDeviceResourceByDeviceId( deviceId );
		RemoteCompositeDeviceOperations adaptor = ( RemoteCompositeDeviceOperations ) getDeviceAdaptorFactory().getDeviceAdaptor( deviceResource );

		String[] eventPreFixes = subscriptionManager.getDeviceEventSubscriptionsByType( DeviceSubscriptionType.EVENTS_FOR_REPLACEMENT );
		deviceResource.getDeviceView().setDeviceEventSubscriptionPrefixes( eventPreFixes );
		subscriptionManager.setDeviceEventSubscriptionsType( deviceId, DeviceSubscriptionType.EVENTS_FOR_REPLACEMENT );

		CompositeDevice device = null;

		try
		{
			device = ( CompositeDevice ) deviceDAO.findById( deviceId );
		}
		catch ( ClassCastException cce )
		{
			LOG.error( "Device {} is not a Root Device. retryReplaceDevice Aborted.", deviceId );
			return;
		}

		if ( device == null )
		{
			LOG.error( "Device {} not found.  retryReplaceDevice Aborted.", deviceId );
			return;
		}

		DeviceDetails deviceDetails;

		try
		{
			adaptor.modifyEventSubscription( deviceResource.getDeviceView().getEventSubscriptionId(), eventPreFixes );
			deviceDetails = adaptor.retrieveInfo();
		}
		catch ( DeviceException de )
		{
			DeviceException ex = new DeviceException( de.getMessage(), DeviceExceptionTypes.DEVICE_REPLACE_RETRY_FAIL );
			transitRegistrationError( device, RegistrationStatus.ERROR_REPLACEMENT, ex );
			LOG.error( "retry replacement error for device {} due to the retrieveInfo exception :{} ", deviceId, de.getMessage() );
			return;
		}

		device.setDeviceSystemInfoFromTransport( deviceDetails );

		device.setEventSubscriptionPrefixes( subscriptionManager.getDeviceEventSubscriptionsByType( DeviceSubscriptionType.EVENTS_FOR_REPLACEMENT ) );
		transitRegistrationStatus( device, RegistrationStatus.PENDING_REPLACEMENT );
		LOG.info( "device {} is in pending replacement status", deviceId );

		checkVersionandApplyConfig( device, deviceDetails );
	}

	public void handleReplaceFirmwareCheckFailure( DeviceMBean dev )
	{
		CompositeDevice device = ( CompositeDevice ) dev;
		LOG.info( " replacement device {} firmware check failed. ", device.getDeviceId() );
		DeviceException de = new DeviceException( "The device SW version is lower than the version of the configuration", DeviceExceptionTypes.DEVICE_FIRMWARE_VERSION_TOO_LOW );
		transitRegistrationError( device, RegistrationStatus.ERROR_REPLACEMENT, de );
	}

	private void checkVersionandApplyConfig( CompositeDevice device, DeviceDetails deviceDetails )
	{
		String deviceSoftwareVersion = deviceDetails.getSwVersion();
		DeviceConfigDescriptor deviceConfigDescriptor = null;
		String falimyIdFromDeviceDetails = Integer.toString( deviceDetails.getFamilyId() );
		String modelIdFromdeviceDetails = Integer.toString( deviceDetails.getModelId() );
		try
		{
			deviceConfigDescriptor = getConfigurationService().getDeviceConfigByDeviceId( device.getDeviceId() );

			String softwareVersionOfSanpShot = deviceConfigDescriptor.getFirmwareVersion();
			String softwareVersionofTargetUpgrade = null;
			softwareVersionofTargetUpgrade = getFirmwareService().getDefaultFirmwareVersion( falimyIdFromDeviceDetails, modelIdFromdeviceDetails );

			if ( ( softwareVersionofTargetUpgrade == null ) || ( ( softwareVersionofTargetUpgrade != null ) && ( CommonUtils.compareVersions( softwareVersionofTargetUpgrade, deviceSoftwareVersion ) <= 0 ) ) )
			{

				if ( CommonUtils.compareVersions( deviceSoftwareVersion, softwareVersionOfSanpShot ) < 0 )
				{
					LOG.warn( "The device SW version is lower than the version of the configuration" );
					handleReplaceFirmwareCheckFailure( device );
				}
			}
			else
			{
				if ( CommonUtils.compareVersions( softwareVersionofTargetUpgrade, softwareVersionOfSanpShot ) < 0 )
				{
					LOG.warn( "The device Target SW version is lower than the version of the configuration" );
					handleReplaceFirmwareCheckFailure( device );
					return;
				}

				LOG.info( "The device target firmware upgrade is enabled, waiting for the target upgrade complete ..." );
				return;
			}

			getConfigurationService().applyReplacement( device.getDeviceId() );
		}
		catch ( ConfigurationException e )
		{
			LOG.warn( "Device {} Apply replacement exception and aborted...", device.getDeviceId() );
		}
	}

	private void scheduleDeviceReplacement( DeviceResource resource, String deviceAddress, Map<String, Object> registrationParams )
	{
		registrationParams.put( "deviceAdress", deviceAddress );
		registrationParams.put( "useTrusted", Boolean.TRUE );

		registrationScheduler.scheduleSerialDeviceReplacement( resource.getDeviceId(), registrationParams );
	}

	public void retryRegistration( String deviceId, String deviceAddress, Map<String, Object> registrationParams ) throws DeviceException
	{
		CompositeDevice device = ( CompositeDevice ) deviceRegistry.getDevice( deviceId );

		if ( device == null )
		{
			LOG.warn( "Device {} not found when re-registering.", deviceAddress );
			throw new DeviceException( "Device not found", DeviceExceptionTypes.DEVICE_NOT_FOUND );
		}

		if ( ( !device.getRegistrationStatus().equals( RegistrationStatus.ERROR_REGISTRATION ) ) && ( !device.getRegistrationStatus().equals( RegistrationStatus.UNREGISTERED ) ) )
		{
			LOG.warn( "Device {} is aready registered .", deviceAddress );
			throw new DeviceException( "Device already Registered or with pending registration tasks.", DeviceExceptionTypes.DEVICE_ALREADY_REGISTERED_WITH_THIS_SERVER );
		}

		registrationParams.put( "deviceAdress", deviceAddress );
		registrationParams.put( "useTrusted", Boolean.TRUE );
		registrationParams.put( "stationId", device.getStationId() );

		registrationScheduler.scheduleSerialDeviceAddressUpdate( deviceId, deviceAddress );
		registrationScheduler.scheduleSerialDeviceRegistration( deviceId, registrationParams );
	}

	public void scheduleDeviceUnregistration( DeviceResource deviceResource )
	{
		registrationScheduler.scheduleDeviceUnregistration( deviceResource, null );

		EventRegistry eventRegistry = getEventRegistry();
		Builder auditViewBuilder = new Builder( AuditEventNameEnum.DEVICE_UNREGISTRATION.getName() );
		auditViewBuilder.addResourceId( deviceResource.getId() );
		AuditView auditView = auditViewBuilder.build();
		eventRegistry.sendEventAfterTransactionCommits( new AuditEvent( auditView ) );
	}

	public void unregister( DeviceResource deviceResource ) throws DeviceException
	{
		CompositeDevice rootDevice = null;
		try
		{
			rootDevice = ( CompositeDevice ) deviceDAO.findById( deviceResource.getDeviceId() );
		}
		catch ( ClassCastException cce )
		{
			LOG.warn( "Device {} is not a Root Device. Registration Aborted.", deviceResource.getDeviceId() );
		}
		if ( rootDevice == null )
		{
			LOG.warn( "Device {} not found. Device was unregistered. Registration Aborted.", deviceResource.getDeviceId() );
			throw new DeviceException( "Device not found", DeviceExceptionTypes.DEVICE_NOT_FOUND );
		}

		RegistrationStatus currentRegStatus = rootDevice.getRegistrationStatus();
		if ( ( currentRegStatus == RegistrationStatus.REGISTERED ) || ( currentRegStatus == RegistrationStatus.ERROR_REPLACEMENT ) )
		{
			try
			{
				RemoteCompositeDeviceOperations adaptor = ( RemoteCompositeDeviceOperations ) getDeviceAdaptorFactory().getDeviceAdaptor( deviceResource );
				adaptor.unregister();
			}
			catch ( DeviceException ex )
			{
				LOG.warn( "Failed to unregister with Device {}. Proceeding with unregistration. {}", new Object[] {deviceResource.getDeviceId(), ex} );
			}
		}

		Collection<Device> values = rootDevice.getChildDevices().values();
		for ( Device childDevice : values )
		{
			transitRegistrationStatus( childDevice, RegistrationStatus.UNREGISTERED );
		}
		transitRegistrationStatus( rootDevice, RegistrationStatus.UNREGISTERED );

		getEventRegistry().sendEventAfterTransactionCommits( new MetricsEvent( new BucketCounterInput( MetricsTypes.DEVICE_UNREGISTRATION.getName(), "success" ) ) );
	}

	public ConfigurationEnvelope retrieveConfiguration( String deviceId ) throws DeviceException
	{
		DeviceResource device = getTopologyService().getDeviceResourceByDeviceId( deviceId );
		if ( device == null )
		{
			throw new DeviceException( "Device " + deviceId + " not found when retrieving configuration", DeviceExceptionTypes.DEVICE_NOT_FOUND );
		}

		if ( device.getDeviceView().getParentDeviceId() != null )
		{
			RemoteCompositeDeviceOperations adaptor = ( RemoteCompositeDeviceOperations ) getDeviceAdaptorFactory().getDeviceAdaptor( getTopologyService().getDeviceResourceByDeviceId( device.getDeviceView().getParentDeviceId() ) );

			String channelId = null;
			try
			{
				List<String> channelIds = ( ( TopologyCoreService ) getTopologyService() ).getChannelIdsFromDevice( device.getId() );
				if ( ( channelIds != null ) && ( channelIds.size() > 0 ) )
				{
					channelId = ( String ) channelIds.get( channelIds.size() - 1 );
				}
			}
			catch ( TopologyException e )
			{
				LOG.debug( "Failed to get channel Ids for device: {}", device.getDeviceId(), e );
			}
			return adaptor.retrieveChildDeviceConfiguration( channelId );
		}
		RemoteDeviceOperations adaptor = getDeviceAdaptorFactory().getDeviceAdaptor( device );
		return adaptor.retrieveConfiguration();
	}

	public String retrieveConfigurationHash( String deviceId ) throws DeviceException
	{
		DeviceResource device = getTopologyService().getDeviceResourceByDeviceId( deviceId );
		if ( device == null )
		{
			throw new DeviceException( "Device " + deviceId + " not found when retrieving configuration hash", DeviceExceptionTypes.DEVICE_NOT_FOUND );
		}

		if ( device.getDeviceView().isR5() )
		{
			return null;
		}

		RemoteDeviceOperations adaptor = getDeviceAdaptorFactory().getDeviceAdaptor( device );
		return adaptor.retrieveConfigurationHash();
	}

	public String configure( String deviceId, byte[] configuration, String configSnapshotID ) throws DeviceException
	{
		Device device = deviceDAO.findByIdEagerDetached( deviceId );
		if ( device == null )
		{
			throw new DeviceException( "Device " + deviceId + " not found when configuring", DeviceExceptionTypes.DEVICE_NOT_FOUND );
		}

		if ( device.getParentDevice() != null )
		{
			return getDeviceUpgradeTaskDispatcher().configure( device.getParentDevice(), device, configuration, configSnapshotID );
		}
		return getDeviceUpgradeTaskDispatcher().configure( ( CompositeDevice ) device, null, configuration, configSnapshotID );
	}

	public String upgrade( String deviceId, String version, String fileName, InputStream fileContent, String key ) throws DeviceException
	{
		Device device = deviceDAO.findByIdEagerDetached( deviceId );
		if ( device == null )
		{
			throw new DeviceException( "Device " + deviceId + " not found when upgrading", DeviceExceptionTypes.DEVICE_NOT_FOUND );
		}

		if ( device.getParentDevice() != null )
		{
			return getDeviceUpgradeTaskDispatcher().upgrade( device.getParentDevice(), device, version, fileName, fileContent, key );
		}
		return getDeviceUpgradeTaskDispatcher().upgrade( ( CompositeDevice ) device, ( Device ) null, version, fileName, fileContent, key );
	}

	public String upgrade( String deviceId, List<String> channelDeviceIds, String version, String fileName, InputStream fileContent, String key ) throws DeviceException
	{
		Device device = deviceDAO.findByIdEagerDetached( deviceId );

		if ( device == null )
		{
			throw new DeviceException( "Device " + deviceId + " not found when upgrading", DeviceExceptionTypes.DEVICE_NOT_FOUND );
		}

		Map<String, String> deviceChannelIds = new HashMap();
		for ( String channelDeviceId : channelDeviceIds )
		{
			String channelId = getTopologyService().getFirstChannelIdFromDevice( channelDeviceId );
			if ( channelId == null )
			{
				LOG.warn( "Channel ID from device ID " + channelDeviceId + " not found when upgrading!" );
			}
			else
			{
				deviceChannelIds.put( channelDeviceId, channelId );
			}
		}
		return getDeviceUpgradeTaskDispatcher().upgrade( ( CompositeDevice ) device, deviceChannelIds, version, fileName, fileContent, key );
	}

	public void startUpdateDeviceAddress( String deviceId, String deviceAddress ) throws DeviceException
	{
		if ( !CommonUtils.validateIpAddress( deviceAddress ) )
		{
			throw new DeviceException( "Device Address is invalid", DeviceExceptionTypes.INVALID_DEVICE_ADDRESS );
		}

		registrationScheduler.scheduleSerialDeviceAddressUpdate( deviceId, deviceAddress );
		subscriptionManager.fetchEvents( deviceId );
	}

	private void updateDeviceAddressByDevice( Device device, String deviceAddress )
	{
		if ( ( device instanceof CompositeDevice ) )
		{
			LOG.info( "Updating device's DeviceId:{} registration address from {} to {}.", device.getDeviceId(), new Object[] {device.getAddress(), deviceAddress} );
			device.setAddress( deviceAddress );
			if ( device.getRegistrationStatus() == RegistrationStatus.REGISTERED )
			{
				String deviceResourceId = getTopologyService().getDeviceResourceByDeviceId( device.getDeviceId() ).getId().toString();
				DeviceIpChangedEvent deviceIpChangedEvent = new DeviceIpChangedEvent( deviceResourceId, device.getDeviceId(), deviceAddress );
				getEventRegistry().sendEventAfterTransactionCommits( deviceIpChangedEvent );
			}
		}
	}

	public void updateDeviceAddress( String deviceId, String deviceAddress )
	{
		if ( !deviceAddress.contains( ":" ) )
		{
			StringBuilder sb = new StringBuilder( deviceAddress );
			sb.append( ":" );
			sb.append( "443" );
			deviceAddress = sb.toString();
		}

		if ( ( deviceDAO.updateDeviceAddressByDeviceId( deviceId, deviceAddress ).intValue() == 1 ) && ( getTopologyService().getDeviceResourceByDeviceId( deviceId ).getDeviceView().getRegistrationStatus() == RegistrationStatus.REGISTERED ) )
		{

			LOG.info( "Updating device DeviceId:{} registration address to {}.", deviceId, deviceAddress );
			String deviceResourceId = getTopologyService().getDeviceResourceByDeviceId( deviceId ).getId().toString();
			DeviceIpChangedEvent deviceIpChangedEvent = new DeviceIpChangedEvent( deviceResourceId, deviceId, deviceAddress );
			getEventRegistry().sendEventAfterTransactionCommits( deviceIpChangedEvent );
		}
	}

	public String convertToDeviceIdFromChannelId( String deviceId, String channelId )
	{
		Device dev = ( Device ) deviceRegistry.getDevice( deviceId );
		return dev.convertToDeviceIdFromChannelId( channelId );
	}

	public DeletedDevice createDeletedDevice( String deviceId )
	{
		Device dev = ( Device ) deviceRegistry.getDevice( deviceId );

		List<ResourcePathNode> pathNodes = getTopologyService().getDeviceResourcePath( dev.getDeviceId() );

		if ( pathNodes == null )
		{
			return null;
		}

		List<String> path = new ArrayList( 1 );
		String pathString = "";

		for ( int i = pathNodes.size() - 1; i >= 0; i-- )
		{
			ResourcePathNode node = ( ResourcePathNode ) pathNodes.get( i );
			path.add( node.getId().toString() );
			pathString = pathString + node.getName();
			if ( i > 0 )
			{
				pathString = pathString + "/";
			}
		}

		DeletedDevice result = deletedDeviceDAO.findByPathAndDevice( pathString, dev );

		if ( result == null )
		{
			result = new DeletedDevice( path, pathString, dev.getAddress(), dev.getManufacturer(), dev.getManufacturerName(), dev.getModel(), dev.getModelName(), dev.getMacAddress(), dev.getSerial(), dev.getName(), dev.getSoftwareVersion(), dev.getHardwareVersion(), dev.getFamily(), dev.getFamilyName() );

			deletedDeviceDAO.create( result );
		}
		return result;
	}

	public String findChannelNameFromId( String deviceId, String channelId )
	{
		String result = null;
		Device dev = ( Device ) deviceRegistry.getDevice( deviceId );
		if ( dev != null )
		{
			Channel channel = dev.getChannelFromDevice( channelId );
			if ( channel != null )
			{
				result = channel.getName();
			}
		}
		return result;
	}

	public String findRootDevice( String deviceId )
	{
		Device dev = ( Device ) deviceRegistry.getDevice( deviceId );
		if ( dev != null )
		{
			String parentId = dev.getParentDeviceId();
			if ( parentId != null )
			{
				return parentId;
			}
			return dev.getDeviceId();
		}
		return null;
	}

	public void removeDeletedDevices( List<Long> referencedDeletedDevices )
	{
		List<DeletedDevice> deletedDevices = deletedDeviceDAO.findAll();

		int total = deletedDevices.size();
		int unreferenced = total - referencedDeletedDevices.size();
		if ( unreferenced > 0 )
		{
			LOG.info( "Purging " + unreferenced + " unreferenced deleted devices from total of " + total );

			for ( DeletedDevice d : deletedDevices )
			{
				if ( !referencedDeletedDevices.contains( d.getId() ) )
				{
					deletedDeviceDAO.delete( d );
				}
			}
		}
	}

	public List<String> findChannelIdsFromDevice( String deviceId )
	{
		List<String> channelIds = new ArrayList();

		Device dev = ( Device ) deviceRegistry.getDevice( deviceId );
		if ( dev != null )
		{
			for ( Channel channel : dev.getChannels().values() )
			{
				channelIds.add( channel.getChannelId() );
			}
		}
		return channelIds;
	}

	public void resubscribeDevice( String deviceId, Map<String, Object> extraInfo )
	{
		long start = System.currentTimeMillis();

		DeviceResource device = getTopologyService().getDeviceResourceByDeviceId( deviceId );
		if ( device == null )
		{
			LOG.info( "DeviceId={} remote address {} not found on topology cache. Aborting device subsription task.", deviceId, ( String ) extraInfo.get( "deviceRemoteAddress" ) );

			return;
		}

		if ( RegistrationStatus.MARKED_FOR_REPLACEMENT.equals( device.getDeviceView().getRegistrationStatus() ) )
		{
			LOG.warn( "Ignore the restart(resubscribeDevice) event for device {} since it is already marked for replacement", deviceId );
			return;
		}

		String addressFromRequest = null;
		boolean remoteAddressVerified = false;

		device.getDeviceView().setAdditionalDeviceRegistrationInfo( extraInfo );
		String deviceRequestRemoteAddress = ( String ) extraInfo.get( "deviceRemoteAddress" );
		if ( deviceRequestRemoteAddress != null )
		{
			long deviceRequestTimestamp = ( ( Long ) extraInfo.get( "deviceTimestamp" ) ).longValue();
			addressFromRequest = getRemoteDeviceAddress( device, deviceRequestRemoteAddress );
			try
			{
				remoteAddressVerified = testRemoteDeviceAddress( device, addressFromRequest, deviceRequestTimestamp );
				RemoteCompositeDeviceOperations adaptor = ( RemoteCompositeDeviceOperations ) getDeviceAdaptorFactory().getDeviceAdaptor( device );

				DeviceDetails deviceDetails = adaptor.retrieveInfo();
				LOG.debug( "Checking device serial number. current device: {} remote device: {} ", device.getDeviceView().getSerial(), deviceDetails.getSerial() );
				if ( !deviceDetails.getSerial().equalsIgnoreCase( device.getDeviceView().getSerial() ) )
				{
					LOG.warn( "Device serial number does not match. current serial: {} remote device serial: {} ", new Object[] {device.getDeviceView().getSerial(), deviceDetails.getSerial()} );
					return;
				}
			}
			catch ( DeviceException de )
			{
				LOG.warn( "Failed to communicate with device {} due to error: {}. Aborting device subscription task.", device.getDeviceId(), de.getMessage() );

				if ( !DeviceManagementConstants.isMobileDevice( device.getDeviceView().getFamily(), device.getDeviceView().getModel() ) )
				{
					long timeStamp = System.currentTimeMillis();
					DeviceAlertInput alertInput = new DeviceAlertInput( deviceId, AlertDefinitionEnum.DEVICE_REGISTRATION_ADDRESS_UNREACHABLE, deviceId, timeStamp, timeStamp, timeStamp, "", "", Boolean.TRUE.booleanValue() );

					getHealthService().processHealthAlert( alertInput );
				}
				return;
			}
		}

		RemoteCompositeDeviceOperations adaptor = ( RemoteCompositeDeviceOperations ) getDeviceAdaptorFactory().getDeviceAdaptor( device );
		try
		{
			if ( !deviceCapabilityService.isCapabilityEnabled( Long.parseLong( device.getDeviceId() ), "register.2" ) )
			{
				adaptor.unSubscribeEvents( device.getDeviceView().getEventSubscriptionId() );
			}
		}
		catch ( DeviceException ex )
		{
			LOG.warn( "Could not cancel subscription {} with device {} due to error: {}", new Object[] {device.getDeviceView().getEventSubscriptionId(), device.getDeviceView().getRegistrationAddress(), ex.getMessage()} );
		}

		DeviceSubscriptionType subscriptionType = DeviceSubscriptionType.FULL_EVENTS;
		if ( ( RegistrationStatus.PENDING_REPLACEMENT.equals( device.getDeviceView().getRegistrationStatus() ) ) || ( RegistrationStatus.ERROR_REPLACEMENT.equals( device.getDeviceView().getRegistrationStatus() ) ) )
		{
			subscriptionType = DeviceSubscriptionType.EVENTS_FOR_REPLACEMENT;
		}

		LOG.info( "Re-subscribing for events with device {}.", device.getDeviceId() );
		String newSubsId = null;
		CompositeDevice pd = null;
		try
		{
			newSubsId = adaptor.subscribeToDeviceEvents( subscriptionType );
			String[] subscriptionPrefixes = subscriptionManager.getDeviceEventSubscriptionsByDeviceId( deviceId );
			pd = ( CompositeDevice ) deviceRegistry.getDevice( deviceId );
			pd.setDeviceEventSubscriptionId( newSubsId );
			pd.setEventSubscriptionPrefixes( subscriptionPrefixes );
			pd.setTimeDelta( device.getDeviceView().getTimeDelta() );
			pd.setNotifyInterval( Integer.valueOf( InstrumentationSettings.DEVICE_NOTIFY_INTERVAL ) );
			Long sequence = device.getDeviceView().getDeviceEventSequenceId();
			if ( DeviceManagementConstants.DEVICE_EVENT_START_SEQUENCE_ID.equals( sequence ) )
			{
				pd.initializeDeviceEventSequenceId();
				device.getDeviceView().setDeviceEventSequenceId( pd.getDeviceEventSequenceId() );
			}

			device.getDeviceView().setEventSubscriptionId( newSubsId );
			device.getDeviceView().setDeviceEventSubscriptionPrefixes( subscriptionPrefixes );
			device.getDeviceView().setNotifyInterval( Integer.valueOf( InstrumentationSettings.DEVICE_NOTIFY_INTERVAL ) );

			if ( remoteAddressVerified )
			{
				updateDeviceAddressByDevice( pd, addressFromRequest );
			}

			LOG.debug( "Resubscription successful. Scheduling data synchronize task:" );

			deviceDAO.flush();

			if ( ( RegistrationStatus.PENDING_REPLACEMENT.equals( device.getDeviceView().getRegistrationStatus() ) ) || ( RegistrationStatus.ERROR_REPLACEMENT.equals( device.getDeviceView().getRegistrationStatus() ) ) )
			{
				AbstractDeviceEvent dne = new DeviceSystemChangedEvent( deviceId );
				getEventRegistry().sendEventAfterTransactionCommits( dne );
			}
			else
			{
				subscriptionManager.resynchronizeData( deviceId );
			}
		}
		catch ( Exception ex )
		{
			LOG.warn( "Could not renew subscription with deviceId= {} due to error: {}", deviceId, ex.getMessage() );

			if ( newSubsId != null )
			{
				try
				{
					adaptor.unSubscribeEvents( newSubsId );
					device.getDeviceView().setEventSubscriptionId( null );
					if ( pd != null )
					{
						pd.setDeviceEventSubscriptionId( null );
					}
				}
				catch ( DeviceException e )
				{
					LOG.error( "Error unSubscribing from events: Possible stacked subscription", e );
				}
			}
		}
		LOG.debug( "Resubscribe time taken: " + ( System.currentTimeMillis() - start ) / 1000.0D + " seconds." );
	}

	public void modifyDeviceSubscription( String deviceId, DeviceSubscriptionType subscriptionType )
	{
		LOG.debug( "Modifying subscription with device {} to {}", deviceId, subscriptionType.name() );
		DeviceResource deviceResource = getTopologyService().getDeviceResourceByDeviceId( deviceId );

		try
		{
			String[] eventPreFixes = subscriptionManager.getDeviceEventSubscriptionsByType( subscriptionType );

			RemoteCompositeDeviceOperations adaptor = ( RemoteCompositeDeviceOperations ) getDeviceAdaptorFactory().getDeviceAdaptor( deviceResource );
			adaptor.modifyEventSubscription( deviceResource.getDeviceView().getEventSubscriptionId(), eventPreFixes );

			CompositeDevice compositeDevice = null;
			try
			{
				compositeDevice = ( CompositeDevice ) deviceRegistry.getDevice( deviceId );
			}
			catch ( ClassCastException cce )
			{
				LOG.info( "Parent Device assigned is not a Root Device. Aborting device subscription task." );
				return;
			}

			compositeDevice.setEventSubscriptionPrefixes( eventPreFixes );
			deviceResource.getDeviceView().setDeviceEventSubscriptionPrefixes( eventPreFixes );
			subscriptionManager.setDeviceEventSubscriptionsType( deviceId, subscriptionType );
		}
		catch ( DeviceException e )
		{
			LOG.warn( "Could not modify subscription with device {} due to error: {}", new Object[] {deviceId, e.getMessage()} );
		}
	}

	public void addSubscription( String[] additionalPrefixes )
	{
		subscriptionManager.addSubscription( additionalPrefixes );
	}

	public void removeSubscription( String[] additionalPrefixes )
	{
		subscriptionManager.removeSubscription( additionalPrefixes );
	}

	public List<String> findOfflineDevices()
	{
		List<String> result = new ArrayList();

		Criteria criteria = new Criteria( DeviceResource.class );
		criteria.add( Restrictions.eq( "deviceView.parentDeviceId", null ) );
		criteria.add( Restrictions.in( "deviceView.registrationStatus", new RegistrationStatus[] {RegistrationStatus.REGISTERED, RegistrationStatus.PENDING_REPLACEMENT, RegistrationStatus.ERROR_REPLACEMENT} ) );
		criteria.add( Restrictions.ne( "deviceView.connectState", ConnectState.OFFLINE ) );
		criteria.add( Restrictions.lt( "deviceView.lastCommunicationTime", Long.valueOf( DateUtils.getCurrentUTCTimeInMillis() - InstrumentationSettings.DEVICE_TIME_TO_OFFLINE * 1000 ) ) );

		List<Resource> devices = getTopologyService().getResources( criteria );

		for ( Resource resource : devices )
		{
			DeviceResource device = ( DeviceResource ) resource;

			result.add( device.getDeviceId() );
		}
		return result;
	}

	public void sendDeviceLicense( Long deviceId, String xml ) throws DeviceException
	{
		String sDeviceId = deviceId.toString();
		DeviceResource device = getTopologyService().getDeviceResourceByDeviceId( sDeviceId );
		if ( device == null )
		{
			throw new DeviceException( "Device " + sDeviceId + " could not be found on DB Store. Aborting sending license data." );
		}

		if ( !device.isRootDevice() )
		{
			throw new DeviceException( "Can't send device license to a device that isn't a CompositeDevice" );
		}

		LOG.info( "Sending license data to Device id=" + sDeviceId + "..." );
		sendLicenseXML( sDeviceId, xml );
		LOG.info( " ->> License data sent!" );
	}

	public void sendLicenseXML( String deviceId, String licenseXML ) throws DeviceException
	{
		if ( licenseXML == null )
		{
			licenseXML = "";
		}

		DeviceResource device = getTopologyService().getDeviceResourceByDeviceId( deviceId );
		RemoteCompositeDeviceOperations adaptor = ( RemoteCompositeDeviceOperations ) getDeviceAdaptorFactory().getDeviceAdaptor( device );

		GenericParameter licenseParameter = new GenericParameter();
		licenseParameter.setName( "license.channels.soft" );
		GenericValue gv = new GenericValue();
		gv.setValue( licenseXML );
		licenseParameter.setValue( gv );

		adaptor.setParamValues( new GenericParameter[] {licenseParameter} );
	}

	public void sendServerId( Long deviceId, String serverId, ServerIdHashEvent ev, boolean reset )
	{
		String sDeviceId = deviceId.toString();
		DeviceResource device = getTopologyService().getDeviceResourceByDeviceId( sDeviceId );
		if ( device == null )
		{
			LOG.error( "Could not find device with ID: " + deviceId + " in DB Store" );
		}

		RemoteCompositeDeviceOperations adaptor = ( RemoteCompositeDeviceOperations ) getDeviceAdaptorFactory().getDeviceAdaptor( device );

		GenericParameter parameter = new GenericParameter();
		parameter.setName( "client.server.id" );
		GenericValue value = new GenericValue();
		String parameterValueString = reset ? "" : serverId;
		value.setValue( parameterValueString );
		parameter.setValue( value );
		try
		{
			adaptor.setParamValues( new GenericParameter[] {parameter} );
		}
		catch ( DeviceException e )
		{
			DeferredEvent de = new DeferredEvent( ev, ConnectState.ONLINE.toString(), 172800000L );

			getDeferredEventPool().add( sDeviceId, de );
		}
	}

	public int grabAllocatedLicenses( Long deviceId ) throws DeviceException
	{
		DeviceResource device = getTopologyService().getDeviceResourceByDeviceId( deviceId.toString() );
		if ( device == null )
		{
			return -1;
		}
		if ( !device.isRootDevice() )
		{
			LOG.warn( "Can only updateChannelInUseMax of RootDevices, not deviceId=" + deviceId );
			return -1;
		}

		RemoteCompositeDeviceOperations adaptor = ( RemoteCompositeDeviceOperations ) getDeviceAdaptorFactory().getDeviceAdaptor( device );
		Integer i = adaptor.retrieveIntParam( "license.channels.soft.total" );
		if ( i != null )
		{
			return i.intValue();
		}
		return -1;
	}

	public List<CompositeDeviceMBean> getAllCompositeDevices()
	{
		List<CompositeDevice> devices = deviceDAO.findAllCompositeDevices();
		List<CompositeDeviceMBean> results = new ArrayList( devices );
		return results;
	}

	public List<String> findChannelIdsFromDeviceAndChildren( String deviceId )
	{
		List<String> channelIds = new ArrayList();

		Device dev = ( Device ) deviceRegistry.getDevice( deviceId );
		if ( dev != null )
		{
			for ( Channel channel : dev.getChannels().values() )
			{
				channelIds.add( channel.getChannelId() );
			}
			if ( ( dev instanceof CompositeDevice ) )
			{
				for ( Device childDevice : ( ( CompositeDevice ) dev ).getChildDevices().values() )
				{
					if ( childDevice.getChannels() != null )
					{
						for ( Channel channel : childDevice.getChannels().values() )
						{
							channelIds.add( channel.getChannelId() );
						}
					}
				}
			}
		}
		return channelIds;
	}

	public List<AlarmSource> getAlarmSources( String deviceId ) throws DeviceException
	{
		List<AlarmSource> result = new ArrayList();
		DeviceResource dev = getTopologyService().getDeviceResourceByDeviceId( deviceId );
		if ( dev == null )
		{
			return null;
		}

		if ( !dev.isRootDevice() )
		{
			LOG.warn( "Can only retrieve alarm sources of RootDevices. deviceId={}", deviceId );
			return result;
		}
		RemoteCompositeDeviceOperations adaptor = ( RemoteCompositeDeviceOperations ) getDeviceAdaptorFactory().getDeviceAdaptor( dev );
		return adaptor.getAlarmSources();
	}

	public void closeAlarmEntries( String deviceId, List<AlarmEntryCloseRecord> closeRecords ) throws DeviceException
	{
		DeviceResource dev = getTopologyService().getDeviceResourceByDeviceId( deviceId );
		if ( dev == null )
		{
			return;
		}

		if ( closeRecords.isEmpty() )
		{
			LOG.debug( "No alarm closures to send to device." );
			return;
		}

		if ( !dev.isRootDevice() )
		{
			LOG.warn( "Can only send alarm closures to RootDevices. deviceId={}", deviceId );
			return;
		}

		ConnectState connectState = deviceRegistry.getConnectState( deviceId );
		if ( connectState == ConnectState.OFFLINE )
		{
			DeviceException ex = new DeviceException( "The device is offline", DeviceExceptionTypes.DEVICE_OFFLINE );
			ex.setCommunicationError( true );
			throw ex;
		}

		RemoteCompositeDeviceOperations adaptor = ( RemoteCompositeDeviceOperations ) getDeviceAdaptorFactory().getDeviceAdaptor( dev );
		adaptor.closeAlarmEntries( closeRecords );
	}

	public List<CompositeDeviceMBean> findDevicesByDisconnectionTime( int checkInMinutes )
	{
		List<CompositeDevice> devices = deviceDAO.findDeviceListFromConnectionTime( checkInMinutes );
		List<CompositeDeviceMBean> results = new ArrayList( devices );
		return results;
	}

	public void addChannelToDevice( String rootDeviceId, String channelId )
	{
		DeviceResource deviceResource = getTopologyService().getDeviceResourceByDeviceId( rootDeviceId );
		if ( deviceResource == null )
		{
			LOG.error( "Parent device {} not found in topology when adding channel.", rootDeviceId );
			return;
		}
		try
		{
			RemoteCompositeDeviceOperations adaptor = ( RemoteCompositeDeviceOperations ) getDeviceAdaptorFactory().getDeviceAdaptor( deviceResource );
			ChannelDetails newChannelDetails = adaptor.retrieveChannelDetails( channelId );

			if ( ( newChannelDetails.getIpDevice() != null ) && ( !newChannelDetails.getIpDevice().hasMacAddresses() ) )
			{
				LOG.info( "Channel details for channel:" + channelId + " device:" + rootDeviceId + " did not have MAC populated. Skipping." );
				return;
			}

			CompositeDevice parentDevice = ( CompositeDevice ) deviceDAO.findByIdEager( rootDeviceId );

			if ( newChannelDetails.getIpDevice() != null )
			{
				String[] searchCriteria = DeviceManagementConstants.hasPlaceHolderMAC( newChannelDetails.getIpDevice().getMACAddresses() ) ? newChannelDetails.getIpDevice().getIPAddresses() : newChannelDetails.getIpDevice().getMACAddresses();

				Device childDevice = deviceDAO.findByNetworkAddressAndParent( searchCriteria, parentDevice );

				if ( childDevice == null )
				{
					if ( !ChannelDetails.isChannelEnabled( newChannelDetails ) )
					{
						LOG.warn( "Not adding child device/channel for channelId {}. The channel state reported by the device is {}.", channelId, newChannelDetails.getChannelState() );
						return;
					}
					childDevice = addChildDevice( parentDevice, newChannelDetails.getIpDevice() );

					transitRegistrationStatus( childDevice, RegistrationStatus.REGISTERED );
				}
				else
				{
					childDevice.setParentDevice( parentDevice );
				}
				addChannelToDevice( childDevice, newChannelDetails );
			}
			else
			{
				addChannelToDevice( parentDevice, newChannelDetails );
			}

			LOG.info( "Channel {} from device {} added.", channelId, parentDevice.getDeviceId() );
		}
		catch ( DeviceException ex )
		{
			if ( ex.isCommunicationError() )
			{
				LOG.info( "Could not add child device {} due to communication problem. Will retry.", rootDeviceId );

				registrationScheduler.scheduleChildDeviceRegistration( rootDeviceId, channelId );
			}
			else
			{
				LOG.warn( "Could not add child device {}. Error: {}", rootDeviceId, ex.getMessage() );
			}
			return;
		}
	}

	public void removeChannelFromDevice( String deviceId, String channelId )
	{
		Device deviceChannelOwner = null;
		try
		{
			deviceChannelOwner = deviceDAO.findById( deviceId );
		}
		catch ( ClassCastException cce )
		{
			LOG.info( "Parent Device assigned is not a Root Device. Aborting task." );
			return;
		}

		if ( deviceChannelOwner == null )
		{
			LOG.info( "Device {} not found on DB store. Aborting task.", deviceChannelOwner );
			return;
		}

		String sourceDeviceId = deviceChannelOwner.convertToDeviceIdFromChannelId( channelId );
		if ( sourceDeviceId == null )
		{
			LOG.info( "ChannelId is not associated with the Device anymore. Aborting task. " );
			return;
		}

		try
		{
			ChannelResource topologyChannel = getTopologyService().getChannelResource( deviceId, channelId );
			if ( topologyChannel != null )
			{
				getTopologyService().removeResource( topologyChannel.getId() );
			}
		}
		catch ( TopologyException e )
		{
			LOG.info( "ChannelResource not found in topology when trying to remove channel." );
		}

		Channel channel = deviceChannelOwner.getChannelFromDevice( channelId );
		LOG.info( "Removing device {} / channel {}", channel.getDevice().getDeviceId(), channelId );
		deviceChannelOwner.getChannels().remove( channel.getIdAsString() );

		if ( ( !( deviceChannelOwner instanceof CompositeDevice ) ) && ( deviceChannelOwner.getChannels().isEmpty() ) )
		{
			Long deviceResourceId = getTopologyService().getResourceIdByDeviceId( deviceId );
			if ( deviceResourceId != null )
			{
				try
				{
					getTopologyService().removeResource( deviceResourceId );
				}
				catch ( TopologyException e )
				{
					LOG.info( "Parent device of channel not removed successfully.", e );
				}
			}

			transitRegistrationStatus( deviceChannelOwner, RegistrationStatus.UNREGISTERED );
		}
	}

	public void updateChannelFromDevice( String deviceId, String channelId )
	{
		DeviceResource deviceResource = getTopologyService().getDeviceResourceByDeviceId( deviceId );
		if ( deviceResource == null )
		{
			LOG.info( "Channel changed event points to a device id ({}) that no longer exists. ", deviceId );
			return;
		}
		try
		{
			RemoteDeviceOperations adaptor = null;

			if ( deviceResource.isRootDevice() )
			{
				adaptor = getDeviceAdaptorFactory().getDeviceAdaptor( deviceResource );
			}
			else
			{
				DeviceResource rootDevice = getTopologyService().getDeviceResource( deviceResource.getParentResourceId() );
				adaptor = getDeviceAdaptorFactory().getDeviceAdaptor( rootDevice );
			}

			ChannelDetails channelDetails = adaptor.retrieveChannelDetails( channelId );
			if ( ( channelDetails.getIpDevice() != null ) && ( !channelDetails.getIpDevice().hasMacAddresses() ) )
			{
				LOG.debug( "Channel details for channel:" + channelId + " device:" + deviceId + " did not have MAC populated. Skipping." );
				return;
			}

			boolean channelExists = false;
			DeviceResource deviceChannelOwner = deviceResource;
			Long rootDeviceResourceId = deviceResource.isRootDevice() ? deviceResource.getId() : deviceResource.getParentResourceId();
			ChannelResource channelResource = getTopologyService().getChannelResource( rootDeviceResourceId, channelId );
			if ( channelResource != null )
			{
				channelExists = true;
				ChannelView channelView = channelResource.getChannelView();
				if ( !deviceResource.getDeviceId().equals( channelView.getDeviceId() ) )
				{
					deviceChannelOwner = getTopologyService().getDeviceResourceByDeviceId( channelView.getDeviceId() );
				}
			}

			boolean ipDeviceReplaced = false;
			if ( channelDetails.getIpDevice() != null )
			{
				DeviceDetails deviceDetails = channelDetails.getIpDevice();
				DeviceView deviceView = deviceChannelOwner.getDeviceView();

				boolean isSameFamilyAndModel = ( deviceView.getFamily().equals( String.valueOf( deviceDetails.getFamilyId() ) ) ) && ( deviceView.getModel().equals( String.valueOf( deviceDetails.getModelId() ) ) );
				boolean isSameMACAddress = true;
				if ( ( !DeviceManagementConstants.hasPlaceHolderMAC( deviceDetails.getMACAddresses() ) ) && ( !DeviceManagementConstants.hasPlaceHolderMAC( deviceView.getMacAddresses() ) ) )
				{
					isSameMACAddress = CollectionUtils.sortAndCompareArrays( deviceDetails.getMACAddresses(), deviceView.getMacAddresses() );
				}
				ipDeviceReplaced = ( !isSameFamilyAndModel ) && ( !isSameMACAddress );
			}

			if ( ( channelDetails.getChannelState().equals( com.marchnetworks.server.communications.transport.datamodel.ChannelState.ONLINE ) ) && ( !deviceChannelOwner.isRootDevice() ) && ( ipDeviceReplaced ) )
			{
				LOG.debug( "New Ip device detected. Swapping out channel {} ", channelId );
				AbstractDeviceEvent dne = new DeviceChannelRemovedEvent( deviceChannelOwner.getDeviceId(), channelId );
				getEventRegistry().sendEventAfterTransactionCommits( dne );

				AbstractDeviceEvent dne2 = new DeviceChannelAddedEvent( deviceChannelOwner.getDeviceView().getParentDeviceId(), channelId );
				getEventRegistry().sendEventAfterTransactionCommits( dne2 );
			}
			else if ( ( deviceChannelOwner.isRootDevice() ) && ( channelExists ) && ( ipDeviceReplaced ) )
			{

				CompositeDevice parentDevice = ( CompositeDevice ) deviceDAO.findByIdEager( deviceChannelOwner.getDeviceId() );

				Device childDevice = addChildDevice( parentDevice, channelDetails.getIpDevice() );

				DeviceResource childDeviceResource = new DeviceResource();
				childDeviceResource.setDeviceId( childDevice.getDeviceId() );
				childDeviceResource = ( DeviceResource ) getTopologyService().createResource( childDeviceResource, deviceChannelOwner.getId(), ResourceAssociationType.DEVICE.name() );

				Channel channel = parentDevice.getChannelFromDevice( channelDetails.getId() );
				channel.setDevice( childDevice );

				channelResource = getTopologyService().getChannelResource( deviceChannelOwner.getDeviceId(), channelId );
				channelResource.getChannelView().setDeviceId( childDevice.getDeviceId() );
				getTopologyService().updateResource( channelResource );
				getTopologyService().updateAssociation( new ResourceAssociation( channelResource.getId(), deviceChannelOwner.getId(), ResourceAssociationType.CHANNEL.name() ), new ResourceAssociation( channelResource.getId(), childDeviceResource.getId(), ResourceAssociationType.CHANNEL.name() ) );

				transitRegistrationStatus( childDevice, RegistrationStatus.REGISTERED );
			}
			else
			{
				Device device = deviceDAO.findById( deviceChannelOwner.getDeviceId() );
				Channel aChannel = device.getChannelFromDevice( channelId );
				if ( ( channelDetails != null ) && ( aChannel != null ) )
				{
					device.updateChannelFromTransport( channelDetails );

					if ( ( channelDetails.getChannelState() == com.marchnetworks.server.communications.transport.datamodel.ChannelState.ONLINE ) && ( !deviceChannelOwner.isRootDevice() ) )
					{
						device.setDeviceInfoFromTransport( channelDetails.getIpDevice() );
					}
				}
			}
		}
		catch ( DeviceException e )
		{
			LOG.warn( "There is no channel associated with DeviceMBean id={}", deviceId );
		}
		catch ( TopologyException e )
		{
			LOG.warn( "Exception when updating topology. Cause: {}", e.getMessage() );
		}
	}

	public void updateChannelState( String deviceId, String channelId, String state )
	{
		if ( state.equals( com.marchnetworks.server.communications.transport.datamodel.ChannelState.UNKNOWN.name() ) )
		{
			LOG.info( "Received ChannelConnectionStateEvent with {} state ", com.marchnetworks.server.communications.transport.datamodel.ChannelState.UNKNOWN );
			return;
		}

		DeviceResource deviceResource = getTopologyService().getDeviceResourceByDeviceId( deviceId );
		if ( !deviceResource.isRootDevice() )
		{
			deviceResource = getTopologyService().getDeviceResource( deviceResource.getParentResourceId() );
		}

		ChannelResource channelRes = getTopologyService().getChannelResource( deviceResource.getId(), channelId );
		if ( channelRes == null )
		{
			if ( !state.equals( com.marchnetworks.server.communications.transport.datamodel.ChannelState.DISABLED.name() ) )
			{
				AbstractDeviceEvent channelAddedEvent = new DeviceChannelAddedEvent( deviceResource.getDeviceId(), channelId );
				getEventRegistry().sendEventAfterTransactionCommits( channelAddedEvent );
			}
			return;
		}
		Channel c = ( Channel ) channelDAO.findById( channelRes.getChannelView().getId() );
		c.setChannelState( com.marchnetworks.command.common.device.data.ChannelState.stateFromString( state ) );
		LOG.debug( "Device={} Cam={} --> {}", new Object[] {channelRes.getChannelView().getDeviceId(), c.getName(), state} );
	}

	public void updateDeviceDetails( String deviceId ) throws DeviceException
	{
		RemoteCompositeDeviceOperations adaptor = ( RemoteCompositeDeviceOperations ) getDeviceAdaptorFactory().getDeviceAdaptor( getTopologyService().getDeviceResourceByDeviceId( deviceId ) );
		DeviceDetails deviceDetails = null;
		try
		{
			deviceDetails = adaptor.retrieveInfo();
		}
		catch ( DeviceException e )
		{
			LOG.warn( "Failed to retrieve System details from Device. Cause: {}", e.getMessage() );
			return;
		}

		long deviceTime = adaptor.getDeviceTime();

		CompositeDevice device = ( CompositeDevice ) deviceDAO.findById( deviceId );
		if ( device == null )
		{
			LOG.info( "Device {} not found on DB store. Aborting event processing.", device );
			return;
		}

		device.calculateTimeDelta( deviceTime );

		device.setDeviceInfoFromTransport( deviceDetails );
	}

	private void updateRemoteDeviceAddress( DeviceResource deviceResource, String addressFromRequest )
	{
		if ( ( addressFromRequest != null ) && ( !addressFromRequest.equalsIgnoreCase( deviceResource.getDeviceView().getRegistrationAddress() ) ) )
		{
			updateDeviceAddress( deviceResource.getDeviceId(), addressFromRequest );
		}
	}

	private String getRemoteDeviceAddress( DeviceResource deviceResource, String deviceRequestRemoteAddress )
	{
		String deviceRegistrationAddress = deviceResource.getDeviceView().getRegistrationAddress();

		boolean isRegisteredByIP = HttpUtils.isIPv4Address( deviceRegistrationAddress );
		if ( ( !isRegisteredByIP ) || ( deviceRequestRemoteAddress == null ) )
		{
			return null;
		}

		String deviceRequestRemoteAddressAndPort = deviceRequestRemoteAddress + ":" + deviceRegistrationAddress.split( ":" )[1];

		return deviceRequestRemoteAddressAndPort;
	}

	private boolean testRemoteDeviceAddress( DeviceResource deviceResource, String addressFromRequest, long timestampFromRequest ) throws DeviceException
	{
		if ( addressFromRequest == null )
		{
			return false;
		}
		String deviceRegistrationAddress = deviceResource.getDeviceView().getRegistrationAddress();

		if ( deviceRegistrationAddress.equalsIgnoreCase( addressFromRequest ) )
		{
			return false;
		}

		Map<String, Object> extraConfigMap = deviceResource.getDeviceView().getAdditionalDeviceRegistrationInfo();
		if ( extraConfigMap == null )
		{
			extraConfigMap = new HashMap();
			deviceResource.getDeviceView().setAdditionalDeviceRegistrationInfo( extraConfigMap );
		}
		extraConfigMap.put( "deviceAdress", addressFromRequest );

		try
		{
			boolean legacyTestMode = configuration.getBooleanProperty( ConfigProperty.DEVICE_LEGACY_REMOTE_ADDRESS_TEST, false );

			RemoteCompositeDeviceOperations adaptor = ( RemoteCompositeDeviceOperations ) getDeviceAdaptorFactory().getDeviceAdaptor( deviceResource );
			adaptor.getDeviceInfo( !legacyTestMode, false );
			LOG.info( "RemoteRequestAddress " + addressFromRequest + " read from device " + deviceResource.getDeviceId() + " is different than device registration address " + deviceRegistrationAddress );
			return true;
		}
		catch ( DeviceException e )
		{
			LOG.debug( "CES could not communicate with device " + deviceResource.getDeviceId() + " on remote address " + addressFromRequest + " read from device ping. Cause: " + e.getMessage() );

			deviceResource.getDeviceView().getAdditionalDeviceRegistrationInfo().remove( "deviceAdress" );
			long deviceTimestamp = deviceResource.getDeviceView().getDeviceCreationTime();
			if ( timestampFromRequest != deviceTimestamp )
			{

				LOG.info( "Received ping from device {}, remote address {} but could not establish connection and DVR timestamp doesn't match. Aborting...", deviceResource.getDeviceId(), addressFromRequest );
				throw e;
			}
		}
		return false;
	}

	public void setAlertConfig( String deviceId, AlertConfig alertConfig, boolean forceUpdate ) throws DeviceException
	{
		DeviceResource device = getTopologyService().getDeviceResourceByDeviceId( deviceId );
		if ( device == null )
		{
			throw new DeviceException( String.format( "Unable to set Alert Config on device: %1$s", new Object[] {deviceId} ), DeviceExceptionTypes.DEVICE_NOT_FOUND );
		}

		RemoteCompositeDeviceOperations adaptor = ( RemoteCompositeDeviceOperations ) getDeviceAdaptorFactory().getDeviceAdaptor( device );

		if ( !forceUpdate )
		{
			String currentVersion = alertConfig.getId();
			String deviceVersion = adaptor.getAlertConfigId();

			if ( !deviceVersion.equals( currentVersion ) )
			{
				return;
			}
		}

		adaptor.setAlertConfig( alertConfig );
	}

	public void closeAlerts( String deviceId, List<String> alertIds ) throws DeviceException
	{
		DeviceResource device = getTopologyService().getDeviceResourceByDeviceId( deviceId );
		if ( device == null )
		{
			throw new DeviceException( String.format( "Unable to close alerts on device: %1$s", new Object[] {deviceId} ), DeviceExceptionTypes.DEVICE_NOT_FOUND );
		}

		if ( alertIds.isEmpty() )
		{
			return;
		}

		ConnectState connectState = deviceRegistry.getConnectState( deviceId );
		if ( connectState == ConnectState.OFFLINE )
		{
			DeviceException ex = new DeviceException( "The device is offline", DeviceExceptionTypes.DEVICE_OFFLINE );
			ex.setCommunicationError( true );
			throw ex;
		}

		RemoteCompositeDeviceOperations adaptor = ( RemoteCompositeDeviceOperations ) getDeviceAdaptorFactory().getDeviceAdaptor( device );
		adaptor.closeAlerts( alertIds );
	}

	public List<AlertEntry> getAlerts( String deviceId ) throws DeviceException
	{
		DeviceResource device = getTopologyService().getDeviceResourceByDeviceId( deviceId );
		if ( device == null )
		{
			throw new DeviceException( String.format( "Unable to get alerts on device: %1$s", new Object[] {deviceId} ), DeviceExceptionTypes.DEVICE_NOT_FOUND );
		}

		RemoteCompositeDeviceOperations adaptor = ( RemoteCompositeDeviceOperations ) getDeviceAdaptorFactory().getDeviceAdaptor( device );
		return adaptor.getAlerts();
	}

	private Device addChildDevice( CompositeDevice parentDevice, DeviceDetails deviceDetails )
	{
		Device device = new Device();
		device.setTimeCreated( Calendar.getInstance() );
		device.setParentDevice( parentDevice );
		device.setDeviceInfoFromTransport( deviceDetails );
		deviceDAO.create( device );

		parentDevice.getChildDevices().put( device.getDeviceId(), device );
		return device;
	}

	private void addChannelToDevice( Device channelOwner, ChannelDetails channelDetails )
	{
		if ( !ChannelDetails.isChannelEnabled( channelDetails ) )
		{
			return;
		}
		Channel channel = new Channel();
		channelOwner.decorateNewChannelFromTransport( channel, channelDetails );
		channelDAO.create( channel );
		channelOwner.getChannels().put( channel.getIdAsString(), channel );
	}

	private void removeCachedRegistrationState( String deviceId )
	{
		Set<Long> deviceIdSet = new HashSet();
		deviceIdSet.add( Long.valueOf( Long.parseLong( deviceId ) ) );
		String[] eventPathNames = {EventTypesEnum.DEVICE_REGISTRATION.getFullPathEventName(), EventTypesEnum.DEVICE_UNREGISTRATION.getFullPathEventName(), EventTypesEnum.DEVICE_REPLACEMENT.getFullPathEventName()};

		stateCacheService.removeStateFromCache( deviceIdSet, eventPathNames );
	}

	private void transitRegistrationStatus( Device device, RegistrationStatus registrationStatus )
	{
		transitRegistrationStatus( device, registrationStatus, false );
	}

	private void transitRegistrationStatus( Device device, RegistrationStatus registrationStatus, boolean isMassRegistration )
	{
		device.setRegistrationStatus( registrationStatus );
		device.setTimeRegStatusChanged( DateUtils.getCurrentUTCTime() );
		if ( registrationStatus == RegistrationStatus.REGISTERED )
		{
			device.setLastCommunicationTime( device.getTimeRegStatusChanged() );
		}

		EventRegistry eventRegistry = getEventRegistry();
		if ( device.getParentDevice() != null )
		{
			eventRegistry.sendEventAfterTransactionCommits( new ChildDeviceRegistrationEvent( device.getDeviceId(), registrationStatus ) );
		}
		else
		{
			String deviceResourceId = CommonAppUtils.stringValueOf( getTopologyService().getResourceIdByDeviceId( device.getDeviceId() ) );
			TerritoryAwareDeviceEvent registrationEvent = new DeviceRegistrationEvent( deviceResourceId, device.getDeviceId(), getTerritoryInfoForEvent( device.getDeviceId() ), registrationStatus, isMassRegistration );
			registrationEvent.setDeviceExtraInfo( device.getAdditionalDeviceRegistrationInfo() );

			removeCachedRegistrationState( device.getDeviceId() );

			stateCacheService.putIntoCache( ( DeviceRegistrationEvent ) registrationEvent );
			eventRegistry.sendEventAfterTransactionCommits( registrationEvent );
		}
	}

	private void transitRegistrationError( Device device, RegistrationStatus registrationStatus, DeviceException ex )
	{
		device.setRegistrationStatus( registrationStatus );
		device.setTimeRegStatusChanged( DateUtils.getCurrentUTCTime() );
		device.setRegistrationErrorMessage( device.getTimeRegStatusChangedInString() + " : " + ex.getMessage() );

		EventRegistry bER = getEventRegistry();

		String deviceResourceId = CommonAppUtils.stringValueOf( getTopologyService().getResourceIdByDeviceId( device.getDeviceId() ) );
		AbstractDeviceEvent registrationEvent = new DeviceRegistrationEvent( deviceResourceId, device.getDeviceId(), getTerritoryInfoForEvent( device.getDeviceId() ), registrationStatus, false );
		registrationEvent.setDeviceExtraInfo( device.getAdditionalDeviceRegistrationInfo() );
		registrationEvent.setDeviceExceptionType( ex.getDetailedErrorType() );

		removeCachedRegistrationState( device.getDeviceId() );

		stateCacheService.putIntoCache( ( DeviceRegistrationEvent ) registrationEvent );

		bER.sendEventAfterTransactionCommits( registrationEvent );
	}

	private void updateRegistrationStatus( String deviceId, RegistrationStatus registrationStatus, DeviceException ex )
	{
		String errorMessage = ex.getMessage();
		deviceDAO.updateRegistrationStatus( deviceId, registrationStatus, errorMessage );
		EventRegistry bER = getEventRegistry();

		String deviceResourceId = CommonAppUtils.stringValueOf( getTopologyService().getResourceIdByDeviceId( deviceId ) );
		AbstractDeviceEvent registrationEvent = new DeviceRegistrationEvent( deviceResourceId, deviceId, getTerritoryInfoForEvent( deviceId ), registrationStatus, false );
		registrationEvent.setDeviceExtraInfo( getTopologyService().getDeviceResourceByDeviceId( deviceId ).getDeviceView().getAdditionalDeviceRegistrationInfo() );
		registrationEvent.setDeviceExceptionType( ex.getDetailedErrorType() );

		removeCachedRegistrationState( deviceId );

		stateCacheService.putIntoCache( ( DeviceRegistrationEvent ) registrationEvent );

		bER.sendEventAfterTransactionCommits( registrationEvent );
	}

	private Set<Long> getTerritoryInfoForEvent( String deviceId )
	{
		return Collections.singleton( getTopologyService().getResourceIdByDeviceId( deviceId ) );
	}

	private boolean isRegisteredByThisServer( RegistrationState registrationState )
	{
		if ( !registrationState.isRegistered() )
		{
			return false;
		}

		String serverAddressOfDevice = registrationState.getServerAddress();

		if ( serverAddressOfDevice == null )
		{
			return false;
		}

		int index = serverAddressOfDevice.lastIndexOf( ":" );
		String serverHostOfDevice;

		if ( index > 0 )
		{
			serverHostOfDevice = serverAddressOfDevice.substring( 0, index );
		}
		else
		{
			serverHostOfDevice = serverAddressOfDevice;
		}

		return ServerUtils.isServerAddress( serverHostOfDevice );
	}

	private boolean isRegisteredByAnotherServer( RegistrationState registrationState )
	{
		if ( !registrationState.isRegistered() )
		{
			return false;
		}

		return !isRegisteredByThisServer( registrationState );
	}

	public String retrieveAlertConfigId( String deviceId ) throws DeviceException
	{
		DeviceResource device = getTopologyService().getDeviceResourceByDeviceId( deviceId );
		if ( device == null )
		{
			throw new DeviceException( String.format( "Unable to get Alert Config Id from device: {}", new Object[] {deviceId} ), DeviceExceptionTypes.DEVICE_NOT_FOUND );
		}

		RemoteCompositeDeviceOperations adaptor = ( RemoteCompositeDeviceOperations ) getDeviceAdaptorFactory().getDeviceAdaptor( device );
		return adaptor.getAlertConfigId();
	}

	public void fetchEvents( String deviceId, String deviceSubscriptionId, String deviceRequestRemoteAddress, long deviceTimestamp, boolean hasEvents )
	{
		LOG.debug( "Running Device Event Fetcher Task." );

		long start = System.nanoTime();
		DeviceResource deviceResource = getTopologyService().getDeviceResourceByDeviceId( deviceId );

		if ( deviceResource == null )
		{
			LOG.warn( "Device {} from {} not found in topology. Aborting event fetch task.", deviceId, deviceRequestRemoteAddress );
			return;
		}

		if ( !deviceResource.isRootDevice() )
		{
			LOG.warn( "Incoming event with Device ID:" + deviceId + " does not correspond to a Composite Device" );
			return;
		}

		if ( LOG.isDebugEnabled() )
		{
			LOG.debug( "DeviceFetchEventsTask.run() time taken for getting device resource: " + ( System.nanoTime() - start ) / 1.0E9D + " seconds." );
		}

		RegistrationStatus status = deviceResource.getDeviceView().getRegistrationStatus();
		if ( ( status != RegistrationStatus.REGISTERED ) && ( status != RegistrationStatus.PENDING_REPLACEMENT ) && ( status != RegistrationStatus.ERROR_REPLACEMENT ) )
		{
			LOG.warn( "Device {} not currently registered or pending replacement. Aborting event fetch task.", deviceId );
			return;
		}

		if ( deviceRequestRemoteAddress != null )
		{
			Map<String, Object> extraInfoMap = new HashMap();
			extraInfoMap.put( "deviceRemoteAddress", deviceRequestRemoteAddress );
			deviceResource.getDeviceView().setAdditionalDeviceRegistrationInfo( extraInfoMap );
		}

		String addressFromRequest = getRemoteDeviceAddress( deviceResource, deviceRequestRemoteAddress );
		try
		{
			boolean remoteAddressReachable = testRemoteDeviceAddress( deviceResource, addressFromRequest, deviceTimestamp );
			RemoteCompositeDeviceOperations adaptor = ( RemoteCompositeDeviceOperations ) getDeviceAdaptorFactory().getDeviceAdaptor( deviceResource );

			boolean staleSubscription = ( deviceSubscriptionId != null ) && ( deviceResource.getDeviceView().getEventSubscriptionId() != null ) && ( !deviceResource.getDeviceView().getEventSubscriptionId().equalsIgnoreCase( deviceSubscriptionId ) );
			Integer notifyInterval = deviceResource.getDeviceView().getNotifyInterval();

			if ( ( staleSubscription ) || ( notifyInterval.intValue() != InstrumentationSettings.DEVICE_NOTIFY_INTERVAL ) )
			{
				if ( staleSubscription )
				{
					LOG.warn( "Device {} is currently using subscription {} instead of {}. Ignoring.", new Object[] {deviceResource.getDeviceView().getRegistrationAddress(), deviceResource.getDeviceView().getEventSubscriptionId(), deviceSubscriptionId} );
				}
				else
				{
					LOG.info( "Got event notify for " + deviceId + " but notify interval didn't match, re-subscribing." );
				}
				adaptor.unSubscribeEvents( deviceSubscriptionId );
			}
			else
			{
				if ( hasEvents )
				{
					adaptor.fetchEvents();
				}
				else
				{
					AbstractDeviceEvent event = new DeviceConnectionStateEvent( deviceId, ConnectState.ONLINE, DateUtils.getCurrentUTCTime() );
					deviceEventHandlerScheduler.scheduleDeviceEventHandling( deviceId, Collections.singletonList( event ) );
					getDeviceSessionHolderService().extendSessionForDevice( deviceId );
				}

				if ( remoteAddressReachable )
					updateRemoteDeviceAddress( deviceResource, addressFromRequest );
			}
		}
		catch ( DeviceException ex )
		{
			if ( ex.isCommunicationError() )
			{
				LOG.warn( "Failed to fetch events: Communication error. Error Message:{}", ex.getMessage() );
			}
			else
			{
				LOG.warn( "Failed to fetch events: ", ex );
			}
		}
		catch ( Exception e )
		{
			LOG.warn( "Exception fetching events: ", e );
		}
		finally
		{
			if ( LOG.isDebugEnabled() )
			{
				LOG.debug( ">>tskFetch[" + deviceId + ", Run>Finish:" + ( System.nanoTime() - start ) / 1000000L + "ms" );
			}
		}
	}

	public void massRegister( List<MassRegistrationInfo> devices )
	{
		for ( MassRegistrationInfo device : devices )
		{
			registrationScheduler.scheduleDeviceRegistration( device.getDeviceId(), device.getDeviceRegistrationInfo(), true );
		}
	}

	public void stopMassRegistration()
	{
		registrationScheduler.stopMassRegistration();
	}

	public void massUpdateTimeDelta( long timeOffset )
	{
		List<DeviceResource> rootDevices = getTopologyService().getAllDeviceResources();
		List<Long> exceptionIds = new ArrayList( 1 );
		for ( DeviceResource deviceResource : rootDevices )
		{
			DeviceView deviceView = deviceResource.getDeviceView();

			if ( DeviceManagementConstants.isExtractorDevice( deviceView.getManufacturer(), deviceView.getFamily() ) )
			{
				String address = HttpUtils.getAddressWithoutPort( deviceView.getRegistrationAddress() );
				if ( ServerUtils.isServerAddress( address ) )
				{
					exceptionIds.add( Long.valueOf( deviceView.getDeviceId() ) );
					continue;
				}
			}
			long timeDelta = deviceResource.getDeviceView().getTimeDelta().longValue();
			deviceResource.getDeviceView().setTimeDelta( Long.valueOf( timeDelta + timeOffset ) );
		}

		deviceDAO.updateAllTimeDeltas( timeOffset, exceptionIds );
	}

	public boolean updateDeviceTimeDelta( String deviceId, long timeDelta )
	{
		DeviceView device = getTopologyService().getDeviceResourceByDeviceId( deviceId ).getDeviceView();

		if ( Math.abs( device.getTimeDelta().longValue() - timeDelta ) < 10000L )
		{
			return false;
		}
		device.setTimeDelta( Long.valueOf( timeDelta ) );

		DeviceTimeDeltaUpdater update = new DeviceTimeDeltaUpdater( deviceId, timeDelta );
		getTaskScheduler().executeFixedPoolSerial( update, deviceId );

		return true;
	}

	public void updateDeviceTimeDeltaAsync( String deviceId, long timeDelta )
	{
		deviceDAO.updateTimeDelta( deviceId, timeDelta );
	}

	public GenericValue fetchDeviceParameterValue( String deviceId, String eventName )
	{
		LOG.debug( "Fetching Device state event {} from device {}", eventName, deviceId );

		DeviceResource deviceResource = getTopologyService().getDeviceResourceByDeviceId( deviceId );
		RemoteCompositeDeviceOperations adaptor = getDeviceAdaptorFactory().getDeviceAdaptor( deviceResource );
		GenericParameter[] parameters;

		try
		{
			parameters = adaptor.retrieveParamValues( new String[] {eventName} );
		}
		catch ( DeviceException e )
		{
			LOG.warn( "Failed to retrieve state events from device {} due to error: {} ", deviceId, e.getMessage() );
			return null;
		}

		if ( parameters.length > 0 )
		{
			return parameters[0].getValue();
		}

		return null;
	}

	private void checkDeviceGlobalSettings( String deviceId )
	{
		DeviceResource deviceResource = getTopologyService().getDeviceResourceByDeviceId( deviceId );
		if ( deviceResource == null )
		{
			LOG.info( "Device {} could not be found. Won't try to push global settings.", deviceId );
			return;
		}

		if ( !deviceCapabilityService.isCapabilityEnabled( Long.parseLong( deviceId ), "register.2", false ) )
		{
			LOG.debug( "Device {} doesn't support global settings.", deviceId );
			return;
		}

		Map<String, String> deviceGlobalSettings = deviceResource.getDeviceView().getGlobalSettings();
		boolean newSettingsPushed = false;
		if ( ( ( deviceGlobalSettings == null ) || ( !( ( String ) deviceSettingsMap.get( SERVER_ADDRESSES_LIST ) ).equals( deviceGlobalSettings.get( SERVER_ADDRESSES_LIST ) ) ) ) && ( pushDeviceGlobalSetting( deviceResource, SERVER_ADDRESSES_LIST ) ) )
		{
			if ( deviceGlobalSettings == null )
			{
				deviceGlobalSettings = new HashMap();
				deviceResource.getDeviceView().setGlobalSettings( deviceGlobalSettings );
			}
			deviceGlobalSettings.put( SERVER_ADDRESSES_LIST, deviceSettingsMap.get( SERVER_ADDRESSES_LIST ) );
			newSettingsPushed = true;
		}

		if ( ( deviceSettingsMap.containsKey( AGENT_SETTINGS_LIST ) ) && ( !( ( String ) deviceSettingsMap.get( AGENT_SETTINGS_LIST ) ).equals( deviceGlobalSettings.get( AGENT_SETTINGS_LIST ) ) ) && ( pushDeviceGlobalSetting( deviceResource, AGENT_SETTINGS_LIST ) ) )
		{
			deviceGlobalSettings.put( AGENT_SETTINGS_LIST, deviceSettingsMap.get( AGENT_SETTINGS_LIST ) );
			newSettingsPushed = true;
		}

		if ( newSettingsPushed )
		{
			CompositeDevice device = ( CompositeDevice ) deviceDAO.findById( deviceId );
			device.setGlobalSettings( deviceGlobalSettings );
		}
	}

	private boolean pushDeviceGlobalSetting( DeviceResource deviceResource, String globalSetting )
	{
		RemoteCompositeDeviceOperations adaptor = ( RemoteCompositeDeviceOperations ) getDeviceAdaptorFactory().getDeviceAdaptor( deviceResource );

		if ( globalSetting.equals( SERVER_ADDRESSES_LIST ) )
		{
			List<String> serverAddresses = ServerUtils.getServerAddressList();
			try
			{
				if ( ( serverAddresses == null ) || ( serverAddresses.isEmpty() ) )
				{
					LOG.error( "Not pushing empty server address list" );
					return false;
				}
				adaptor.updateRegistrationDetails( serverAddresses );
			}
			catch ( DeviceException de )
			{
				LOG.info( "Could not update registration details with device: {}, reason: {}", deviceResource.getDeviceId(), de.getMessage() );
				return false;
			}
		}
		else if ( globalSetting.equals( AGENT_SETTINGS_LIST ) )
		{
			Map<String, String> agentSettings = getAgentSettings();
			if ( !agentSettings.isEmpty() )
			{
				List<GenericParameter> parameters = new ArrayList<GenericParameter>( agentSettings.size() );
				for ( Entry<String, String> entry : agentSettings.entrySet() )
				{
					parameters.add( GenericParameter.newGenericParameter( entry.getKey(), Integer.parseInt( entry.getValue() ) ) );
				}
				try
				{
					adaptor.setParamValues( ( GenericParameter[] ) parameters.toArray( new GenericParameter[parameters.size()] ) );
				}
				catch ( DeviceException de )
				{
					LOG.info( "Could not set agent parameters with device: {}, reason: {}", deviceResource.getDeviceId(), de.getMessage() );
					return false;
				}
			}
		}

		return true;
	}

	private Map<String, String> getAgentSettings()
	{
		Map<String, String> agentSettings = new HashMap<String, String>();
		for ( int i = 0; i < 5; i++ )
		{
			switch ( i )
			{
				case 0:
					if ( configuration.getProperty( ConfigProperty.AGENT_NOTIFY_TIMEOUT ) != null )
					{
						agentSettings.put( "agent.svrNotifyTimeoutSec", configuration.getProperty( ConfigProperty.AGENT_NOTIFY_TIMEOUT ) );
					}
					break;
				case 1:
					if ( configuration.getProperty( ConfigProperty.AGENT_NOTIFY_MAX_TIME ) != null )
					{
						agentSettings.put( "agent.svrNotifyMaxSec", configuration.getProperty( ConfigProperty.AGENT_NOTIFY_MAX_TIME ) );
					}
					break;
				case 2:
					if ( configuration.getProperty( ConfigProperty.AGENT_NOTIFY_MIN_WAIT_TIME ) != null )
					{
						agentSettings.put( "agent.svrNotifyMinSec", configuration.getProperty( ConfigProperty.AGENT_NOTIFY_MIN_WAIT_TIME ) );
					}
					break;
				case 3:
					if ( configuration.getProperty( ConfigProperty.AGENT_NOTIFY_TEST_FREQUENCY ) != null )
					{
						agentSettings.put( "agent.svrTestFrequencySec", configuration.getProperty( ConfigProperty.AGENT_NOTIFY_TEST_FREQUENCY ) );
					}
					break;
				case 4:
					if ( configuration.getProperty( ConfigProperty.AGENT_NOTIFY_REACTIVATE ) != null )
					{
						agentSettings.put( "agent.svrNotifyReactivateSec", configuration.getProperty( ConfigProperty.AGENT_NOTIFY_REACTIVATE ) );
					}
					break;
			}
		}
		return agentSettings;
	}

	private void fetchDeviceStateEvents( String deviceId )
	{
		LOG.debug( "Fetching Device state events from device {}", deviceId );

		DeviceResource deviceResource = getTopologyService().getDeviceResourceByDeviceId( deviceId );
		RemoteCompositeDeviceOperations adaptor = ( RemoteCompositeDeviceOperations ) getDeviceAdaptorFactory().getDeviceAdaptor( deviceResource );
		GenericParameter[] parameters;

		try
		{
			parameters = adaptor.retrieveParamValues( new String[] {DeviceEventsEnum.CHANNEL_CONFIGURED.getPath(), DeviceEventsEnum.CHANNEL_STREAMING.getPath(), DeviceEventsEnum.CHANNEL_RECORDING.getPath(), DeviceEventsEnum.SYSTEM_CONFIGURED.getPath(), DeviceEventsEnum.SYSTEM_STREAMING.getPath(), DeviceEventsEnum.SYSTEM_RECORDING.getPath(), DeviceEventsEnum.CLIENT_BUFFER_LIVE.getPath(), DeviceEventsEnum.CLIENT_BUFFER_LIVEPTZ.getPath()} );
		}
		catch ( DeviceException e )
		{
			LOG.warn( "Failed to retrieve state events from device {} due to error: {} ", deviceId, e.getMessage() );
			return;
		}

		List<StateCacheable> events = new ArrayList( parameters.length );
		for ( GenericParameter genericParameter : parameters )
		{
			String paramSource = genericParameter.getSource();
			if ( CommonAppUtils.isNullOrEmptyString( paramSource ) )
			{
				paramSource = deviceId;
			}

			if ( GenericDeviceStateEvent.isDeviceStateEvent( genericParameter.getName() ) )
			{
				events.add( new GenericDeviceStateEvent( deviceId, getTerritoryInfoForEvent( deviceId ), paramSource, genericParameter.getName(), genericParameter.getValue(), genericParameter.getInfo(), genericParameter.getTimestamp().getTicks(), false ) );
			}
			else
			{
				events.add( new DeviceStatisticsStateEvent( deviceId, paramSource, genericParameter.getName(), genericParameter.getValue(), genericParameter.getInfo(), genericParameter.getTimestamp().getTicks(), false ) );
			}
		}
		stateCacheService.putIntoCache( events );
	}

	public void setDeviceDAO( DeviceDAO deviceDAO )
	{
		this.deviceDAO = deviceDAO;
	}

	public void setDeviceRegistry( DeviceRegistry deviceRegistry )
	{
		this.deviceRegistry = deviceRegistry;
	}

	public void setRegistrationScheduler( DeviceRegistrationScheduler registrationScheduler )
	{
		this.registrationScheduler = registrationScheduler;
	}

	public void setDeletedDeviceDAO( DeletedDeviceDAO deletedDeviceDAO )
	{
		this.deletedDeviceDAO = deletedDeviceDAO;
	}

	public void setSubscriptionManager( DeviceSubscriptionManager subscriptionManager )
	{
		this.subscriptionManager = subscriptionManager;
	}

	private ResourceTopologyServiceIF getTopologyService()
	{
		return ( ResourceTopologyServiceIF ) ApplicationContextSupport.getBean( "resourceTopologyService_internal" );
	}

	private DeferredEventPool getDeferredEventPool()
	{
		return ( DeferredEventPool ) ApplicationContextSupport.getBean( "deferredEventPool" );
	}

	private EventRegistry getEventRegistry()
	{
		return ( EventRegistry ) ApplicationContextSupport.getBean( "eventRegistry" );
	}

	private TaskScheduler getTaskScheduler()
	{
		return ( TaskScheduler ) ApplicationContextSupport.getBean( "taskScheduler" );
	}

	private DeviceUpgradeTaskDispatcher getDeviceUpgradeTaskDispatcher()
	{
		return ( DeviceUpgradeTaskDispatcher ) ApplicationContextSupport.getBean( "deviceUpgradeTaskDispatcher" );
	}

	private DeviceAdaptorFactory getDeviceAdaptorFactory()
	{
		return ( DeviceAdaptorFactory ) ApplicationContextSupport.getBean( "deviceAdaptorFactory" );
	}

	private HealthServiceIF getHealthService()
	{
		return ( HealthServiceIF ) ApplicationContextSupport.getBean( "healthServiceProxy_internal" );
	}

	public void setChannelDAO( ChannelDAO channelDAO )
	{
		this.channelDAO = channelDAO;
	}

	public void setStateCacheService( StateCacheService stateCacheService )
	{
		this.stateCacheService = stateCacheService;
	}

	public ConfigService getConfigurationService()
	{
		return ( ConfigService ) ApplicationContextSupport.getBean( "configServiceProxy_internal" );
	}

	public FirmwareService getFirmwareService()
	{
		return ( FirmwareService ) ApplicationContextSupport.getBean( "firmwareServiceProxy_internal" );
	}

	public void setConfiguration( CommonConfiguration configuration )
	{
		this.configuration = configuration;
	}

	public void setDeviceCapabilityService( DeviceCapabilityService deviceCapabilityService )
	{
		this.deviceCapabilityService = deviceCapabilityService;
	}

	private DeviceSessionHolderService getDeviceSessionHolderService()
	{
		if ( deviceSessionHolderService == null )
		{
			deviceSessionHolderService = ( ( DeviceSessionHolderService ) ApplicationContextSupport.getBean( "deviceSessionHolderService" ) );
		}
		return deviceSessionHolderService;
	}

	public void setDeviceEventHandlerScheduler( DeviceEventHandlerScheduler deviceEventHandlerScheduler )
	{
		this.deviceEventHandlerScheduler = deviceEventHandlerScheduler;
	}
}

