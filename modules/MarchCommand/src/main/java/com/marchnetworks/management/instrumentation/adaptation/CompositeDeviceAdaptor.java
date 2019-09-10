package com.marchnetworks.management.instrumentation.adaptation;

import com.marchnetworks.command.api.metrics.MetricsCoreService;
import com.marchnetworks.command.api.rest.DeviceManagementConstants;
import com.marchnetworks.command.api.security.DeviceSessionException;
import com.marchnetworks.command.common.CollectionUtils;
import com.marchnetworks.command.common.HttpUtils;
import com.marchnetworks.command.common.device.DeviceEventsEnum;
import com.marchnetworks.command.common.device.data.ChannelState;
import com.marchnetworks.command.common.device.data.ChannelView;
import com.marchnetworks.command.common.device.data.ConnectState;
import com.marchnetworks.command.common.device.data.DeviceView;
import com.marchnetworks.command.common.topology.data.ChannelResource;
import com.marchnetworks.command.common.topology.data.DeviceResource;
import com.marchnetworks.command.common.transport.data.Event;
import com.marchnetworks.command.common.transport.data.EventType;
import com.marchnetworks.command.common.transport.data.GenericValue;
import com.marchnetworks.command.common.transport.data.Pair;
import com.marchnetworks.command.common.transport.data.Timestamp;
import com.marchnetworks.common.config.ConfigProperty;
import com.marchnetworks.common.diagnostics.DiagnosticSettings;
import com.marchnetworks.common.diagnostics.metrics.MetricsHelper;
import com.marchnetworks.common.diagnostics.metrics.MetricsTypes;
import com.marchnetworks.common.event.StateCacheable;
import com.marchnetworks.common.service.CertificationService;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.transport.DeviceServiceTransportSettings;
import com.marchnetworks.common.transport.datamodel.DeviceException;
import com.marchnetworks.common.types.DeviceExceptionTypes;
import com.marchnetworks.common.utils.CommonUtils;
import com.marchnetworks.common.utils.DateUtils;
import com.marchnetworks.common.utils.ServerUtils;
import com.marchnetworks.health.service.HealthServiceIF;
import com.marchnetworks.license.LicenseService;
import com.marchnetworks.management.instrumentation.DeviceAdaptor;
import com.marchnetworks.management.instrumentation.DeviceCapabilityService;
import com.marchnetworks.management.instrumentation.InstrumentationSettings;
import com.marchnetworks.management.instrumentation.RemoteCompositeDeviceOperations;
import com.marchnetworks.management.instrumentation.data.DeviceSubscriptionType;
import com.marchnetworks.management.instrumentation.data.RegistrationState;
import com.marchnetworks.management.instrumentation.events.AbstractDeviceEvent;
import com.marchnetworks.management.instrumentation.events.ChannelConnectionStateEvent;
import com.marchnetworks.management.instrumentation.events.CompositeDeviceUpgradeEvent;
import com.marchnetworks.management.instrumentation.events.DeviceAlarmConfigEvent;
import com.marchnetworks.management.instrumentation.events.DeviceAlarmEvent;
import com.marchnetworks.management.instrumentation.events.DeviceAlarmEventType;
import com.marchnetworks.management.instrumentation.events.DeviceAlertEvent;
import com.marchnetworks.management.instrumentation.events.DeviceAlertEventType;
import com.marchnetworks.management.instrumentation.events.DeviceAppEvent;
import com.marchnetworks.management.instrumentation.events.DeviceAudioOutputConfigEvent;
import com.marchnetworks.management.instrumentation.events.DeviceAudioOutputEvent;
import com.marchnetworks.management.instrumentation.events.DeviceChannelChangedEvent;
import com.marchnetworks.management.instrumentation.events.DeviceChannelRemovedEvent;
import com.marchnetworks.management.instrumentation.events.DeviceChannelsInUseEvent;
import com.marchnetworks.management.instrumentation.events.DeviceChannelsMaxEvent;
import com.marchnetworks.management.instrumentation.events.DeviceConfigurationEventType;
import com.marchnetworks.management.instrumentation.events.DeviceConnectionStateEvent;
import com.marchnetworks.management.instrumentation.events.DeviceOutputEventType;
import com.marchnetworks.management.instrumentation.events.DeviceSequenceIdUpdateEvent;
import com.marchnetworks.management.instrumentation.events.DeviceStatisticsListEvent;
import com.marchnetworks.management.instrumentation.events.DeviceStatisticsStateEvent;
import com.marchnetworks.management.instrumentation.events.DeviceSwitchConfigEvent;
import com.marchnetworks.management.instrumentation.events.DeviceSwitchEvent;
import com.marchnetworks.management.instrumentation.events.DeviceSystemChangedEvent;
import com.marchnetworks.management.instrumentation.events.ExtractorJobEvent;
import com.marchnetworks.management.instrumentation.events.GenericDeviceAuditEvent;
import com.marchnetworks.management.instrumentation.events.GenericDeviceConfigurationEvent;
import com.marchnetworks.management.instrumentation.events.GenericDeviceStateEvent;
import com.marchnetworks.management.instrumentation.events.GenericDeviceUpgradeEvent;
import com.marchnetworks.management.instrumentation.subscription.DeviceSubscriptionManager;
import com.marchnetworks.management.topology.ResourceTopologyServiceIF;
import com.marchnetworks.security.device.DeviceSessionHolderService;
import com.marchnetworks.server.communications.http.CommandRestClient;
import com.marchnetworks.server.communications.soap.SoapProxyInvocationHandler;
import com.marchnetworks.server.communications.transport.DeviceServiceTransport;
import com.marchnetworks.server.communications.transport.datamodel.AlarmEntryCloseRecord;
import com.marchnetworks.server.communications.transport.datamodel.AlarmSource;
import com.marchnetworks.server.communications.transport.datamodel.AlertConfig;
import com.marchnetworks.server.communications.transport.datamodel.AlertEntry;
import com.marchnetworks.server.communications.transport.datamodel.AudioOutput;
import com.marchnetworks.server.communications.transport.datamodel.Capabilities;
import com.marchnetworks.server.communications.transport.datamodel.ChannelDetails;
import com.marchnetworks.server.communications.transport.datamodel.ConfigurationEnvelope;
import com.marchnetworks.server.communications.transport.datamodel.DeviceDetails;
import com.marchnetworks.server.communications.transport.datamodel.DeviceInfo;
import com.marchnetworks.server.communications.transport.datamodel.GenericParameter;
import com.marchnetworks.server.communications.transport.datamodel.GetParametersResult;
import com.marchnetworks.server.communications.transport.datamodel.RegistrationDetails;
import com.marchnetworks.server.communications.transport.datamodel.Switch;
import com.marchnetworks.server.event.AppEventService;
import com.marchnetworks.shared.config.CommonConfiguration;

import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompositeDeviceAdaptor implements RemoteCompositeDeviceOperations, DeviceAdaptor
{
	private static final Logger LOG = LoggerFactory.getLogger( CompositeDeviceAdaptor.class );
	private String serviceName;
	private String serverContextPath;
	private CommonConfiguration commonConfig;
	private DeviceResource deviceResource;
	private DeviceEventHandlerScheduler deviceEventHandlerScheduler;
	private DeviceSubscriptionManager deviceSubscriptionManager;
	private HealthServiceIF healthService;
	private DeviceServiceTransport serviceProxyInstance;
	private DeviceSessionHolderService deviceSessionHolderService;
	private ResourceTopologyServiceIF topologyService;
	private CertificationService certificationService;
	private AppEventService appEventService;
	private LicenseService licenseService;

	private URL getServiceURL()
	{
		try
		{
			String address = getDeviceAddress();
			return new URL( DeviceServiceTransportSettings.getServiceURL( address ) );
		}
		catch ( MalformedURLException e )
		{
			throw new RuntimeException( e );
		}
	}

	private String getDeviceId()
	{
		return deviceResource.getDeviceId();
	}

	private String getAssignedDeviceId()
	{
		return deviceResource.getDeviceView().getAssignedDeviceId();
	}

	private String convertToDeviceIdFromChannelId( String eventSource )
	{
		ChannelResource channelResource = getTopologyService().getChannelResource( deviceResource.getDeviceId(), eventSource );
		if ( channelResource != null )
		{
			return channelResource.getChannelView().getDeviceId();
		}
		return null;
	}

	private Map<String, Object> getAdditionalRegistrationInfo()
	{
		return deviceResource.getDeviceView().getAdditionalDeviceRegistrationInfo();
	}

	private void setAdditionalRegistrationInfo( Map<String, Object> registrationInfo )
	{
		deviceResource.getDeviceView().setAdditionalDeviceRegistrationInfo( registrationInfo );
	}

	private String getDeviceEventSubscriptionId()
	{
		return deviceResource.getDeviceView().getEventSubscriptionId();
	}

	private String getDeviceAddress()
	{
		String deviceAddress = null;

		deviceAddress = deviceResource.getDeviceView().getRegistrationAddress();
		Map<String, Object> additionalRegistrationInfo = null;

		additionalRegistrationInfo = deviceResource.getDeviceView().getAdditionalDeviceRegistrationInfo();

		if ( ( additionalRegistrationInfo != null ) && ( additionalRegistrationInfo.get( "deviceAdress" ) != null ) )
		{
			String tempAddress = ( String ) additionalRegistrationInfo.get( "deviceAdress" );
			if ( tempAddress.split( ":" ).length == 1 )
			{
				String[] addressParts = deviceAddress.split( ":" );
				if ( addressParts.length > 1 )
				{
					StringBuilder sb = new StringBuilder( tempAddress );
					sb.append( ":" );
					sb.append( addressParts[1] );
					return sb.toString();
				}
			}
			deviceAddress = tempAddress;
		}

		return deviceAddress;
	}

	private String[] getServerAddresses( boolean priorityList )
	{
		List<String> serverAddressList = null;
		if ( priorityList )
		{
			serverAddressList = ServerUtils.getServerAddressList();
		}
		else
		{
			serverAddressList = commonConfig.getPropertyList( ConfigProperty.CERT_ALL_IPS );
		}
		String[] results = ( String[] ) serverAddressList.toArray( new String[serverAddressList.size()] );

		return HttpUtils.setPortOnAddresses( results, commonConfig.getServerPort() );
	}

	private String[] getServerHostnames()
	{
		List<String> allHostnames = commonConfig.getPropertyList( ConfigProperty.CERT_ALL_HOSTNAMES );

		String[] results = ( String[] ) allHostnames.toArray( new String[allHostnames.size()] );
		return HttpUtils.setPortOnAddresses( results, commonConfig.getServerPort() );
	}

	private DeviceServiceTransport getProxy( Map<String, Object> additionalConfiguration )
	{
		if ( additionalConfiguration == null )
		{
			additionalConfiguration = new HashMap();
		}

		int httpSocketConnectionTimeout = commonConfig.getIntProperty( ConfigProperty.HTTP_CLIENT_SOCKET_CONNECTION_TIMEOUT, 5000 );
		int httpSocketDataTimeout = commonConfig.getIntProperty( ConfigProperty.HTTP_CLIENT_SOCKET_DATA_TIMEOUT, 15000 );

		additionalConfiguration.put( ConfigProperty.HTTP_CLIENT_SOCKET_CONNECTION_TIMEOUT.getXmlName(), Integer.valueOf( httpSocketConnectionTimeout ) );
		additionalConfiguration.put( ConfigProperty.HTTP_CLIENT_SOCKET_DATA_TIMEOUT.getXmlName(), Integer.valueOf( httpSocketDataTimeout ) );

		DeviceServiceTransport currentProxy = getServiceProxyInstance();
		String deviceId = getDeviceId();
		if ( currentProxy == null )
		{
			currentProxy = ( DeviceServiceTransport ) Proxy.newProxyInstance( SoapProxyInvocationHandler.class.getClassLoader(), new Class[] {DeviceServiceTransport.class}, new SoapProxyInvocationHandler( getServiceURL(), deviceId, getMaxRetry(), additionalConfiguration ) );

			setServiceProxyInstance( currentProxy );
		}
		else
		{
			currentProxy.configureTransport( getServiceURL(), deviceId, getMaxRetry(), additionalConfiguration );
		}
		return currentProxy;
	}

	private String getSessionIdFromDevice( boolean forceSession ) throws DeviceException
	{
		try
		{
			String deviceId = getDeviceId();

			String deviceSession = null;
			if ( forceSession )
			{
				deviceSession = getDeviceSessionHolderService().getNewSessionFromDevice( getDeviceAddress(), deviceId );
			}
			return getDeviceSessionHolderService().getSessionFromDevice( getDeviceAddress(), deviceId );

		}
		catch ( DeviceSessionException e )
		{
			DeviceException dex = new DeviceException();
			if ( ( e.getCause() instanceof DeviceException ) )
			{
				dex = ( DeviceException ) e.getCause();
			}

			if ( dex.getDetailedErrorType().equals( DeviceExceptionTypes.UNKNOWN ) )
				dex.setDetailedErrorType( DeviceExceptionTypes.DEVICE_SESSION_REQUEST_ERROR );
			throw dex;
		}
	}

	private String configureSessionSettings() throws DeviceException
	{
		String sessionId = getSessionIdFromDevice( false );
		putIntoDeviceConfigurationMap( "securityToken", sessionId );

		return sessionId;
	}

	private String configureSessionSettings( boolean forceSession ) throws DeviceException
	{
		String sessionId = getSessionIdFromDevice( forceSession );
		putIntoDeviceConfigurationMap( "securityToken", sessionId );
		return sessionId;
	}

	public String register( DeviceSubscriptionType subscriptionType ) throws DeviceException
	{
		DeviceInfo deviceInfo = getDeviceInfo( true, true );
		if ( !DeviceManagementConstants.isRegistrationAllowed( deviceInfo.manufacturer, deviceInfo.family ) )
		{
			throw new DeviceException( "Registration of family id " + deviceInfo.family + " is not allowed." );
		}

		String serverInterfaceVersion = ServerUtils.getInterfaceVersion();

		if ( ( deviceInfo.interfaceVersion != null ) && ( CommonUtils.compareVersions( CommonUtils.getVersionPart( deviceInfo.interfaceVersion, 2 ), serverInterfaceVersion ) == 1 ) )
		{
			String error = String.format( "Device version %s is not supported on CES version %s when registering", deviceInfo.interfaceVersion, serverInterfaceVersion );
			LOG.error( error );
			throw new DeviceException( error, DeviceExceptionTypes.DEVICE_VERSION_NOT_SUPPORTED );
		}

		long deviceTime = getDeviceTime();
		deviceResource.getDeviceView().calculateTimeDelta( deviceTime );

		Map<String, Object> additionalRegistrationInfo = getAdditionalRegistrationInfo();

		DeviceServiceTransport proxy = getProxy( additionalRegistrationInfo );

		Capabilities devCapabilities = proxy.getServiceCapabilities();
		if ( devCapabilities != null )
		{
			getDeviceCapabilityService().updateCapabilities( Long.parseLong( getDeviceId() ), Arrays.asList( devCapabilities.getStrCapabilities() ) );
		}
		String deviceCertSignUp = null;
		if ( CollectionUtils.contains( devCapabilities.getStrCapabilities(), "register.2" ) )
		{
			deviceCertSignUp = proxy.register( new String[0], serverContextPath + "?deviceId=" + getAssignedDeviceId(), getAssignedDeviceId(), new String[0], getServerAddresses( true ) );
		}
		else
		{
			String[] serverAddresses = getServerAddresses( false );
			String[] serverHostnames = getServerHostnames();
			deviceCertSignUp = proxy.register( serverAddresses, serverContextPath + "?deviceId=" + getAssignedDeviceId(), getAssignedDeviceId(), serverHostnames, new String[0] );
		}

		String[] serverCertificatesForDevice = signDeviceCertificate( deviceCertSignUp );
		proxy.setCertificateChain( serverCertificatesForDevice );

		closeCurrentSession();

		additionalRegistrationInfo.clear();

		String subscriptionId = getDeviceEventSubscriptionId();
		if ( subscriptionId != null )
		{
			unSubscribeEvents( subscriptionId );
		}

		return subscribeToDeviceEvents( subscriptionType );
	}

	public DeviceDetails getDeviceDetailsInfo() throws DeviceException
	{
		configureSessionSettings();

		DeviceDetails deviceDetails = retrieveInfo();

		DeviceDetails channelsAndChildrenInfo = retrieveAllChannelDetails();
		deviceDetails.setDeviceChannels( channelsAndChildrenInfo.getDeviceChannels() );
		deviceDetails.setChildDevices( channelsAndChildrenInfo.getChildDevices() );

		deviceDetails.setChannelsInUse( retrieveIntParam( DeviceEventsEnum.LICENSE_CHANNELS_INUSE.getPath() ) );
		deviceDetails.setMaxChannels( retrieveIntParam( "system.maxChannelsSupported" ) );

		return deviceDetails;
	}

	public String subscribeToDeviceEvents( DeviceSubscriptionType subscriptionType ) throws DeviceException
	{
		String deviceSubscriptionId = null;
		try
		{
			if ( ( getAdditionalRegistrationInfo() == null ) || ( getAdditionalRegistrationInfo().get( "useTrusted" ) == null ) )
			{
				configureSessionSettings( false );
			}

			deviceSubscriptionManager.setDeviceEventSubscriptionsType( getDeviceId(), subscriptionType );

			Long sequenceId = deviceResource.getDeviceView().getDeviceEventSequenceId();
			DeviceServiceTransport proxy = getProxy( getAdditionalRegistrationInfo() );

			DiagnosticSettings.onDeviceSubscribe( Long.valueOf( Long.parseLong( getDeviceId() ) ) );

			deviceSubscriptionId = proxy.subscribeEventsNotify( deviceSubscriptionManager.getDeviceEventSubscriptionsByDeviceId( getDeviceId() ), 172800.0D, serverContextPath + "?deviceId=" + getAssignedDeviceId(), InstrumentationSettings.DEVICE_NOTIFY_INTERVAL, ( sequenceId == null ? DeviceManagementConstants.DEVICE_EVENT_START_SEQUENCE_ID : sequenceId ).longValue() );

			LOG.debug( "New event subscription {} created.", deviceSubscriptionId );
		}
		catch ( DeviceException de )
		{
			handleDeviceException( de );
			throw de;
		}
		return deviceSubscriptionId;
	}

	public void modifyEventSubscription( String subscriptionId, String[] eventPrefixes ) throws DeviceException
	{
		try
		{
			configureSessionSettings();
			DeviceServiceTransport proxy = getProxy( getAdditionalRegistrationInfo() );

			proxy.modifySubscription( getDeviceEventSubscriptionId(), eventPrefixes );
			LOG.debug( "Modified event subscription {} with device {}.", new Object[] {getDeviceEventSubscriptionId(), deviceResource.getDeviceView().getRegistrationAddress()} );
		}
		catch ( DeviceException de )
		{
			handleDeviceException( de );
			throw de;
		}
	}

	public void unSubscribeEvents( String subscriptionId ) throws DeviceException
	{
		configureSessionSettings();
		DeviceServiceTransport proxy = getProxy( getAdditionalRegistrationInfo() );

		if ( subscriptionId != null )
		{
			proxy.unsubscribe( subscriptionId );
			LOG.debug( "Unsubscribed subscription {} for device {}.", subscriptionId, getDeviceId() );
		}

		deviceSubscriptionManager.removeDeviceEventSubscriptionsType( getDeviceId() );
	}

	private String[] signDeviceCertificate( String certificateSignUp )
	{
		String[] Result = new String[0];

		if ( certificateSignUp == null )
		{
			LOG.warn( "No Certificate Sign Request from Device received." );
			return Result;
		}
		try
		{
			Result = certificationService.signAgentCSRArray( certificateSignUp );
		}
		catch ( Exception ex )
		{
			LOG.warn( "Error on Certificate Signing Request: ", ex );
		}
		return Result;
	}

	private void putIntoDeviceConfigurationMap( String key, Object value )
	{
		Map<String, Object> additionalConfMap = getAdditionalRegistrationInfo();
		if ( additionalConfMap == null )
		{
			additionalConfMap = new HashMap();
			setAdditionalRegistrationInfo( additionalConfMap );
		}
		additionalConfMap.put( key, value );
	}

	private List<ChannelDetails> convertDeviceChannels( ChannelDetails[] deviceChannelsInfo )
	{
		List<ChannelDetails> rootDeviceChannels = new ArrayList();
		for ( ChannelDetails channelDetails : deviceChannelsInfo )
		{
			if ( ( channelDetails != null ) && ( channelDetails.getIpDevice() == null ) )
			{
				rootDeviceChannels.add( channelDetails );
			}
		}
		return rootDeviceChannels;
	}

	private List<DeviceDetails> convertDeviceChildrenInfo( ChannelDetails[] childrenInfo ) throws DeviceException
	{
		List<DeviceDetails> childrenDeviceInfo = new ArrayList();
		for ( ChannelDetails channelDetails : childrenInfo )
		{
			DeviceDetails deviceDetail = channelDetails.getIpDevice();
			if ( deviceDetail != null )
			{
				deviceDetail.addChannelDetails( channelDetails );
				childrenDeviceInfo.add( deviceDetail );
			}
		}
		return childrenDeviceInfo;
	}

	public RegistrationState retrieveRegistrationState() throws DeviceException
	{
		if ( ( getAdditionalRegistrationInfo() == null ) || ( getAdditionalRegistrationInfo().get( "useTrusted" ) == null ) )
		{
			configureSessionSettings();
		}
		DeviceServiceTransport proxy = getProxy( getAdditionalRegistrationInfo() );

		RegistrationDetails result = proxy.getRegistrationDetails();

		return new RegistrationState( result.getRegisteredServer(), result.getRegisteredServerPath(), result.getRegisteredDeviceId() );
	}

	public DeviceDetails retrieveChildDeviceInfo( String childDeviceId ) throws DeviceException
	{
		configureSessionSettings();
		DeviceServiceTransport proxy = getProxy( getAdditionalRegistrationInfo() );

		ChannelDetails childrenInfo = proxy.getChannelDetails( childDeviceId );
		List<DeviceDetails> childDeviceDetails = convertDeviceChildrenInfo( new ChannelDetails[] {childrenInfo} );
		if ( !childDeviceDetails.isEmpty() )
			return ( DeviceDetails ) childDeviceDetails.get( 0 );
		throw new DeviceException( "Child device " + childDeviceId + " not found.", DeviceExceptionTypes.DEVICE_NOT_FOUND );
	}

	public DeviceDetails retrieveAllChannelDetails() throws DeviceException
	{
		LOG.debug( "retrieve all Channel Details for device {} ", getDeviceId() );
		configureSessionSettings();
		DeviceServiceTransport proxy = getProxy( getAdditionalRegistrationInfo() );
		ChannelDetails[] channelsAndChildrenInfo = proxy.getAllChannelDetails();

		Set<ChannelDetails> purifiedSet = new HashSet();
		for ( ChannelDetails channelDetails : channelsAndChildrenInfo )
		{
			purifiedSet.add( channelDetails );
		}

		if ( ( LOG.isDebugEnabled() ) && ( purifiedSet.size() != channelsAndChildrenInfo.length ) )
		{
			LOG.debug( "Device {} sent duplicate channel information.", getDeviceId() );
		}

		channelsAndChildrenInfo = ( ChannelDetails[] ) purifiedSet.toArray( new ChannelDetails[purifiedSet.size()] );

		DeviceDetails deviceDetails = new DeviceDetails();
		deviceDetails.setDeviceChannels( convertDeviceChannels( channelsAndChildrenInfo ) );
		deviceDetails.setChildDevices( convertDeviceChildrenInfo( channelsAndChildrenInfo ) );
		return deviceDetails;
	}

	public ChannelDetails retrieveChannelDetails( String channelId ) throws DeviceException
	{
		LOG.debug( "retrieve Channel {} Details for device {} ", channelId, getDeviceId() );
		configureSessionSettings();
		DeviceServiceTransport proxy = getProxy( getAdditionalRegistrationInfo() );
		return proxy.getChannelDetails( channelId );
	}

	public DeviceDetails retrieveInfo() throws DeviceException
	{
		LOG.debug( "retrieve Info(get system details) for device {} ", getDeviceId() );
		if ( ( getAdditionalRegistrationInfo() == null ) || ( getAdditionalRegistrationInfo().get( "useTrusted" ) == null ) )
		{
			configureSessionSettings();
		}
		DeviceServiceTransport proxy = getProxy( getAdditionalRegistrationInfo() );

		return proxy.getSystemDetails();
	}

	public void unregister() throws DeviceException
	{
		boolean hasSession = getDeviceSessionHolderService().hasValidSession( getDeviceId() );

		if ( hasSession )
		{
			configureSessionSettings();
			DeviceServiceTransport proxy = getProxy( getAdditionalRegistrationInfo() );

			proxy.unregister();
			LOG.debug( "Unregistered device {}.", getDeviceId() );
		}

		deviceSubscriptionManager.removeDeviceEventSubscriptionsType( getDeviceId() );

		getDeviceSessionHolderService().processDeviceUnregistered( getDeviceId() );
	}

	public ConfigurationEnvelope retrieveConfiguration() throws DeviceException
	{
		String sessionId = configureSessionSettings();

		CommandRestClient restClient = new CommandRestClient( getDeviceId(), getDeviceAddress(), sessionId );
		return restClient.getDeviceConfiguration();
	}

	public ConfigurationEnvelope retrieveChildDeviceConfiguration( String childDeviceId ) throws DeviceException
	{
		String sessionId = configureSessionSettings();

		CommandRestClient restClient = new CommandRestClient( getDeviceId(), getDeviceAddress(), sessionId );
		return restClient.getDeviceConfiguration( childDeviceId );
	}

	public String retrieveConfigurationHash() throws DeviceException
	{
		LOG.debug( "retrieve Configuration HASH from  device {}.", getDeviceId() );
		configureSessionSettings();
		DeviceServiceTransport proxy = getProxy( getAdditionalRegistrationInfo() );

		String result = proxy.getConfigHash();
		return result;
	}

	public String configure( String childDeviceId, byte[] configuration ) throws DeviceException
	{
		String sessionId = configureSessionSettings();

		CommandRestClient restClient = new CommandRestClient( getDeviceId(), getDeviceAddress(), sessionId );
		return restClient.setDeviceConfiguration( childDeviceId, configuration );
	}

	public String configure( byte[] configuration ) throws DeviceException
	{
		LOG.debug( "Set Configuration to  device {} length :{} ", getDeviceId(), Integer.valueOf( configuration.length ) );

		String sessionId = configureSessionSettings();

		CommandRestClient restClient = new CommandRestClient( getDeviceId(), getDeviceAddress(), sessionId );
		return restClient.setDeviceConfiguration( configuration );
	}

	public String upgrade( String childDeviceId, String fileName, InputStream fileContent, String key ) throws DeviceException
	{
		String sessionId = configureSessionSettings();

		CommandRestClient restClient = new CommandRestClient( getDeviceId(), getDeviceAddress(), sessionId );
		return restClient.sendDeviceUpgrade( childDeviceId, fileName, fileContent, key );
	}

	public String upgrade( List<String> childDeviceIds, String fileName, InputStream fileContent, String key ) throws DeviceException
	{
		String sessionId = configureSessionSettings();

		CommandRestClient restClient = new CommandRestClient( getDeviceId(), getDeviceAddress(), sessionId );
		return restClient.sendDeviceUpgrade( childDeviceIds, fileName, fileContent, key );
	}

	public String upgrade( String fileName, InputStream fileContent, String key ) throws DeviceException
	{
		String sessionId = configureSessionSettings();

		CommandRestClient restClient = new CommandRestClient( getDeviceId(), getDeviceAddress(), sessionId );
		return restClient.sendDeviceUpgrade( fileName, fileContent, key );
	}

	public void closeCurrentSession() throws DeviceException
	{
		String sessionId = ( String ) getAdditionalRegistrationInfo().get( "securityToken" );
		if ( sessionId != null )
		{
			CommandRestClient restClient = new CommandRestClient( getDeviceId(), getDeviceAddress(), sessionId );
			restClient.closeDeviceSession();
		}
	}

	public Integer retrieveIntParam( String paramName ) throws DeviceException
	{
		Integer Result = null;

		if ( ( getAdditionalRegistrationInfo() == null ) || ( getAdditionalRegistrationInfo().get( "admin" ) == null ) )
		{
			configureSessionSettings();
		}
		DeviceServiceTransport proxy = getProxy( getAdditionalRegistrationInfo() );
		GetParametersResult result = proxy.getParameters( new String[] {paramName} );
		GenericParameter[] p = result.getParameters();

		if ( ( p != null ) && ( p.length > 0 ) )
		{
			GenericValue gv = p[0].getValue();
			if ( gv.getType() == 0 )
				Result = Integer.valueOf( gv.getIntValue() );
		}
		return Result;
	}

	public GenericParameter[] retrieveParamValues( String... paramNames ) throws DeviceException
	{
		configureSessionSettings();

		DeviceServiceTransport proxy = getProxy( getAdditionalRegistrationInfo() );
		GetParametersResult result = proxy.getParameters( paramNames );
		return result.getParameters();
	}

	public void setParamValues( GenericParameter[] parameters ) throws DeviceException
	{
		configureSessionSettings();

		DeviceServiceTransport proxy = getProxy( getAdditionalRegistrationInfo() );
		proxy.setParameters( parameters );
	}

	public void fetchEvents() throws DeviceException
	{
		List<AbstractDeviceEvent> parsedDeviceEvents = new ArrayList();

		Event[] events = null;
		try
		{
			configureSessionSettings();
			DeviceServiceTransport proxy = getProxy( getAdditionalRegistrationInfo() );
			String subscriptionId = getDeviceEventSubscriptionId();

			if ( subscriptionId == null )
			{
				deviceSubscriptionManager.renewSubscription( getDeviceId() );
				return;
			}

			events = proxy.getWaitingEvents( subscriptionId, 2.0D );
		}
		catch ( DeviceException de )
		{
			handleDeviceException( de );
			throw de;
		}

		MetricsHelper.metrics.addCounter( MetricsTypes.DEVICE_EVENTS.getName(), events.length );

		parsedDeviceEvents.add( new DeviceConnectionStateEvent( getDeviceId(), ConnectState.ONLINE, DateUtils.getCurrentUTCTime() ) );

		Long eventSequenceId = deviceResource.getDeviceView().getDeviceEventSequenceId();
		for ( Event event : events )
		{
			List<AbstractDeviceEvent> eventsFetched = parseDeviceEvent( event );

			if ( eventsFetched != null )
			{
				if ( ( event.getId() != DeviceManagementConstants.DEVICE_EVENT_MIN_SEQUENCE_ID.longValue() ) && ( eventSequenceId.longValue() < event.getId() ) )
				{
					eventSequenceId = Long.valueOf( event.getId() );
				}
				parsedDeviceEvents.addAll( eventsFetched );
			}
		}
		if ( !eventSequenceId.equals( deviceResource.getDeviceView().getDeviceEventSequenceId() ) )
		{
			parsedDeviceEvents.add( new DeviceSequenceIdUpdateEvent( getDeviceId(), eventSequenceId ) );
		}

		List<StateCacheable> statisticsEvents = new ArrayList();
		for ( Iterator<AbstractDeviceEvent> iterator = parsedDeviceEvents.iterator(); iterator.hasNext(); )
		{
			AbstractDeviceEvent event = ( AbstractDeviceEvent ) iterator.next();
			if ( ( event instanceof DeviceStatisticsStateEvent ) )
			{
				statisticsEvents.add( ( DeviceStatisticsStateEvent ) event );
				iterator.remove();
			}
		}
		if ( !statisticsEvents.isEmpty() )
		{
			parsedDeviceEvents.add( new DeviceStatisticsListEvent( getDeviceId(), statisticsEvents ) );
		}
		deviceEventHandlerScheduler.scheduleDeviceEventHandling( getDeviceId(), parsedDeviceEvents );
	}

	private void handleDeviceException( DeviceException de )
	{
		if ( de.getDetailedErrorType().equals( DeviceExceptionTypes.INVALID_DEVICE_SUBSCRIPTION ) )
		{
			deviceSubscriptionManager.renewSubscription( getDeviceId() );
		}
	}

	private List<AbstractDeviceEvent> parseDeviceEvent( Event event )
	{
		List<AbstractDeviceEvent> deviceEventsList = new ArrayList();
		AbstractDeviceEvent deviceEvent = null;

		String eventName = null;
		String eventSource = null;
		String eventValue = null;
		boolean ignoreEvent = false;
		try
		{
			eventName = event.getName();
			eventSource = event.getSource();
			eventValue = event.getValue().convertToString();
		}
		catch ( ClassCastException cce )
		{
			LOG.warn( "Invalid event Name/Source/Value parameters. Ignoring event {}", event );
			return deviceEventsList;
		}

		boolean isDeviceStateEvent = GenericDeviceStateEvent.isDeviceStateEvent( eventName );
		boolean isDeviceStatisticsEvent = DeviceStatisticsStateEvent.isDeviceStatisticsEvent( eventName );

		if ( ( event.getType() == EventType.DELETE ) && ( !isDeviceStateEvent ) && ( !isDeviceStatisticsEvent ) )
		{
			LOG.debug( "Ignoring delete type state Event:{} Source:{}", new Object[] {eventName, eventSource} );
			return deviceEventsList;
		}

		boolean appSubscriptionExists = appEventService.subscriptionExists( eventName );

		if ( ( eventSource == null ) || ( eventSource.isEmpty() ) || ( eventSource.equals( getAssignedDeviceId() ) ) )
		{
			eventSource = getDeviceId();
		}

		LOG.debug( "Event:{} Source:{} Value:{}", new Object[] {eventName, eventSource, eventValue} );

		String sourceDeviceId = convertToDeviceIdFromChannelId( eventSource );
		if ( sourceDeviceId == null )
		{
			sourceDeviceId = getDeviceId();
		}

		long timestamp = event.getTimestamp().getTicks() / 1000L;

		Pair[] details = event.getInfo();

		String hash = null;
		String taskId = null;
		String reason = null;

		if ( details != null )
		{
			for ( Pair pair : details )
			{
				String value = pair.getValue();
				String name = pair.getName();

				if ( name.equals( "hash" ) )
				{
					hash = value;
				}
				else if ( name.equals( "taskId" ) )
				{
					taskId = value;
				}
				else if ( name.equals( "reason" ) )
				{
					reason = value;
				}
			}
		}
		if ( ( DeviceEventsEnum.CHANNEL_STATE.getPath().equals( eventName ) ) || ( DeviceEventsEnum.CHANNEL_CAPTURE.getPath().equals( eventName ) ) )
		{
			ChannelState channelState = ChannelState.stateFromString( eventValue );
			if ( channelState == null )
			{
				ignoreEvent = true;
				LOG.warn( "Unknown Value type for Event {}, Value {}, Source {}", new Object[] {eventName, eventValue, eventSource} );
			}
			deviceEvent = new ChannelConnectionStateEvent( sourceDeviceId, eventSource, channelState, deviceResource.getId() );
			MetricsHelper.metrics.addBucketCounter( MetricsTypes.CHANNEL_STATES.getName(), channelState.toString() );
		}
		else if ( DeviceEventsEnum.CHANNEL_CONFIG.getPath().equals( eventName ) )
		{
			if ( "ok".equals( eventValue ) )
			{
				deviceEvent = new GenericDeviceConfigurationEvent( sourceDeviceId, DeviceConfigurationEventType.CONFIG_APPLIED, taskId, reason );
			}
			else if ( "failed".equals( eventValue ) )
			{
				deviceEvent = new GenericDeviceConfigurationEvent( sourceDeviceId, DeviceConfigurationEventType.CONFIG_FAILED_FROM_DEVICE, taskId, reason );
			}
		}
		else if ( DeviceEventsEnum.SYSTEM_CONFIG.getPath().equals( eventName ) )
		{
			LOG.debug( "Device system_config  Event from device {}  with taskID {} ", sourceDeviceId, taskId );
			if ( "ok".equals( eventValue ) )
			{
				LOG.debug( "Device config applied Event from device {} ", sourceDeviceId );
				deviceEvent = new GenericDeviceConfigurationEvent( sourceDeviceId, DeviceConfigurationEventType.CONFIG_APPLIED, taskId, reason );
			}
			else if ( "failed".equals( eventValue ) )
			{
				LOG.debug( "Device config fialed Event from device {} ", sourceDeviceId );
				deviceEvent = new GenericDeviceConfigurationEvent( sourceDeviceId, DeviceConfigurationEventType.CONFIG_FAILED_FROM_DEVICE, taskId, reason );
			}
		}
		else if ( DeviceEventsEnum.SYSTEM_LASTCONFIG.getPath().equals( eventName ) )
		{
			LOG.info( "Device lastconfig applied Event from device {} with taskID {} ", sourceDeviceId, taskId );
			if ( "ok".equals( eventValue ) )
			{
				LOG.debug( "Device config applied Event from device {} ", sourceDeviceId );
				deviceEvent = new GenericDeviceConfigurationEvent( sourceDeviceId, DeviceConfigurationEventType.CONFIG_APPLIED_LASTCONFIG, taskId, reason );
			}
			else if ( "failed".equals( eventValue ) )
			{
				LOG.debug( "Device config fialed Event from device {} ", sourceDeviceId );
				deviceEvent = new GenericDeviceConfigurationEvent( sourceDeviceId, DeviceConfigurationEventType.CONFIG_FAILED_FROM_DEVICE, taskId, reason );
			}
		}
		else if ( DeviceEventsEnum.CHANNEL_UPDATE.getPath().equals( eventName ) )
		{
			LOG.info( "event name: channel.update, source: {}. value: {}, task ID: " + taskId, eventSource, eventValue );
			if ( "ok".equals( eventValue ) )
			{
				LOG.debug( "Update Value type for Event {} Source {} Value {}", new Object[] {eventName, sourceDeviceId, eventValue} );
				deviceEvent = new GenericDeviceUpgradeEvent( sourceDeviceId, DeviceConfigurationEventType.UPGRADE_ACCEPTED, taskId, reason );
			}
			else if ( "failed".equals( eventValue ) )
			{
				deviceEvent = new GenericDeviceUpgradeEvent( sourceDeviceId, DeviceConfigurationEventType.UPGRADE_FAILED, taskId, reason );
			}
		}
		else if ( DeviceEventsEnum.SYSTEM_CONFIGHASH.getPath().equals( eventName ) )
		{
			LOG.info( "Device system confighash Changed Event from device {} ", getDeviceId() );
			hash = eventValue;
			deviceEvent = new GenericDeviceConfigurationEvent( sourceDeviceId, DeviceConfigurationEventType.CONFIG_CHANGED, taskId, hash, reason );
			MetricsHelper.metrics.addCounter( MetricsTypes.SYSTEM_CONFIGHASH_EVENT.getName() );
		}
		else if ( DeviceEventsEnum.SYSTEM_CHANGED.getPath().equals( eventName ) )
		{
			LOG.info( "Device System Changed Event from device {} ", getDeviceId() );
			deviceEvent = new DeviceSystemChangedEvent( getDeviceId() );
			MetricsHelper.metrics.addCounter( MetricsTypes.SYSTEM_CHANGED_EVENT.getName() );
		}
		else if ( DeviceEventsEnum.CHANNEL_CONFIGHASH.getPath().equals( eventName ) )
		{
			if ( sourceDeviceId.equals( getDeviceId() ) )
			{
				LOG.warn( "Ignoring channel.configHash event for unknown source {} ", eventSource );
				ignoreEvent = true;
			}
			else
			{
				hash = eventValue;
				deviceEvent = new GenericDeviceConfigurationEvent( sourceDeviceId, DeviceConfigurationEventType.CONFIG_CHANGED, taskId, hash, reason );
			}
		}
		else if ( DeviceEventsEnum.CHANNEL_CHANGED.getPath().equals( eventName ) )
		{
			reason = eventSource;
			deviceEvent = new DeviceChannelChangedEvent( sourceDeviceId, eventSource );
		}
		else if ( DeviceEventsEnum.CHANNEL_CREATED.getPath().equals( eventName ) )
		{
			LOG.debug( "Channel {} from device {} created event ignored...", eventSource, sourceDeviceId );

		}
		else if ( DeviceEventsEnum.CHANNEL_REMOVED.getPath().equals( eventName ) )
		{
			deviceEvent = new DeviceChannelRemovedEvent( sourceDeviceId, eventSource );
		}
		else if ( DeviceEventsEnum.LICENSE_CHANNELS_INUSE.getPath().equals( eventName ) )
		{
			if ( event.getValue().getType() == 0 )
			{
				deviceEvent = new DeviceChannelsInUseEvent( sourceDeviceId, event.getValue().getIntValue() );
			}
			else
				LOG.warn( "Ignoring errant " + DeviceEventsEnum.LICENSE_CHANNELS_INUSE.getPath() + " event from deviceId=" + getDeviceId() );
		}
		else if ( DeviceEventsEnum.SYSTEM_LASTUPDATE.getPath().equals( eventName ) )
		{
			if ( eventValue.equals( "ok" ) )
			{
				deviceEvent = new CompositeDeviceUpgradeEvent( sourceDeviceId, DeviceConfigurationEventType.UPGRADE_ACCEPTED, taskId, reason );
			}
			else if ( eventValue.equals( "failed" ) )
			{
				deviceEvent = new CompositeDeviceUpgradeEvent( sourceDeviceId, DeviceConfigurationEventType.UPGRADE_FAILED, taskId, reason );
			}
		}
		else if ( "system.maxChannelsSupported".equals( eventName ) )
		{
			if ( event.getValue().getType() == 0 )
			{
				deviceEvent = new DeviceChannelsMaxEvent( sourceDeviceId, event.getValue().getIntValue() );
			}
			else
			{
				LOG.warn( "Ignoring errant system.maxChannelsSupported event from deviceId=" + getDeviceId() );
			}
		}
		else if ( DeviceEventsEnum.ALARM_CONFIG.getPath().equals( eventName ) )
		{
			deviceEvent = new DeviceAlarmConfigEvent( getDeviceId(), event.getTimestamp().getTicks() );
		}
		else if ( DeviceEventsEnum.ALARM_ENTRY.getPath().equals( eventName ) )
		{
			deviceEvent = new DeviceAlarmEvent( DeviceAlarmEventType.ALARM_ENTRY, getDeviceId(), event.getTimestamp().getTicks(), eventSource, eventValue, details );
			MetricsHelper.metrics.addCounter( MetricsTypes.ALARM_ENTRIES.getName() );
		}
		else if ( DeviceEventsEnum.ALARM_ENTRY_CLOSED.getPath().equals( eventName ) )
		{
			deviceEvent = new DeviceAlarmEvent( DeviceAlarmEventType.ALARM_CLOSED, getDeviceId(), event.getTimestamp().getTicks(), eventSource, eventValue, details );
		}
		else if ( DeviceEventsEnum.ALARM_STATE.getPath().equals( eventName ) )
		{
			deviceEvent = new DeviceAlarmEvent( DeviceAlarmEventType.ALARM_STATE, getDeviceId(), event.getTimestamp().getTicks(), eventSource, eventValue, details );
			MetricsHelper.metrics.addBucketCounter( MetricsTypes.ALARM_STATES.getName(), eventValue );
		}
		else if ( DeviceEventsEnum.SWITCH_STATE.getPath().equals( eventName ) )
		{
			deviceEvent = new DeviceSwitchEvent( getDeviceId(), DeviceOutputEventType.OUTPUT_STATE, eventSource, eventValue, details );
			MetricsHelper.metrics.addBucketCounter( MetricsTypes.SWITCH_STATES.getName(), eventValue );
		}
		else if ( DeviceEventsEnum.SWITCH_CONFIG.getPath().equals( eventName ) )
		{
			deviceEvent = new DeviceSwitchConfigEvent( getDeviceId() );
		}
		else if ( DeviceEventsEnum.AUDIO_OUT_STATE.getPath().equals( eventName ) )
		{
			deviceEvent = new DeviceAudioOutputEvent( getDeviceId(), DeviceOutputEventType.OUTPUT_STATE, eventSource, eventValue, details );
			MetricsHelper.metrics.addBucketCounter( MetricsTypes.AUDIO_OUT_STATES.getName(), eventValue );
		}
		else if ( DeviceEventsEnum.ALERT_UPDATED.getPath().equals( eventName ) )
		{
			deviceEvent = new DeviceAlertEvent( DeviceAlertEventType.ALERT_UPDATED, getDeviceId(), event );
		}
		else if ( DeviceEventsEnum.ALERT_CLOSED.getPath().equals( eventName ) )
		{
			deviceEvent = new DeviceAlertEvent( DeviceAlertEventType.ALERT_CLOSED, getDeviceId(), event );
		}
		else if ( DeviceEventsEnum.AUDIO_OUT_CONFIG.getPath().equals( eventName ) )
		{
			deviceEvent = new DeviceAudioOutputConfigEvent( getDeviceId() );
		}
		else if ( isDeviceStateEvent )
		{
			Set<Long> territoryInfo = Collections.singleton( deviceResource.getId() );

			boolean deleteEvent = event.getType().equals( EventType.DELETE );
			GenericValue genericValue = event.getValue();
			String eventSourceInfo = eventSource;
			if ( DeviceEventsEnum.EXTRACTOR_STORAGE_FREE.getPath().equals( eventName ) )
			{
				eventSourceInfo = getDeviceId();
				genericValue = new GenericValue();
				String valueString = event.getValue().convertToString();
				genericValue.setValue( valueString );
			}

			deviceEvent = new GenericDeviceStateEvent( getDeviceId(), territoryInfo, eventSourceInfo, eventName, genericValue, details, event.getTimestamp().getTicks(), deleteEvent );
		}
		else if ( isDeviceStatisticsEvent )
		{
			boolean deleteEvent = event.getType().equals( EventType.DELETE );
			deviceEvent = new DeviceStatisticsStateEvent( getDeviceId(), eventSource, eventName, event.getValue(), details, event.getTimestamp().getTicks(), deleteEvent );
		}
		else if ( eventName.startsWith( DeviceEventsEnum.EXTRACTOR.getPath() ) )
		{
			deviceEvent = new ExtractorJobEvent( getDeviceId(), deviceResource.getIdAsString(), event );
		}
		else if ( appSubscriptionExists )
		{

			boolean eventLicensed = true;
			if ( eventName.startsWith( DeviceEventsEnum.ANALYTICS_STATISTICS.getPath() ) )
			{
				Long channelResourceId = getTopologyService().getChannelResourceId( getDeviceId(), eventSource );
				if ( ( channelResourceId != null ) && ( !licenseService.checkAnalyticsLicense( channelResourceId, sourceDeviceId ) ) )
				{
					eventLicensed = false;
				}
			}

			if ( eventLicensed )
			{
				String deviceResourceId = deviceResource.getIdAsString();
				deviceEvent = new DeviceAppEvent( getDeviceId(), deviceResourceId, event );
			}
		}

		if ( deviceEvent != null )
		{
			deviceEventsList.add( deviceEvent );
		}
		if ( !ignoreEvent )
		{

			if ( ( healthService.isHealthAlert( event ) ) && ( !getDeviceCapabilityService().isCapabilityEnabled( Long.parseLong( getDeviceId() ), "alert" ) ) )
			{
				AbstractDeviceEvent deviceAlert = new DeviceAlertEvent( DeviceAlertEventType.LEGACY, getDeviceId(), event );
				deviceEventsList.add( deviceAlert );
			}

			if ( GenericDeviceAuditEvent.isDeviceAuditEvent( eventName ) )
			{
				AbstractDeviceEvent deviceAuditEvent = new GenericDeviceAuditEvent( getDeviceId(), eventName, eventSource, eventValue, details, timestamp );
				deviceEventsList.add( deviceAuditEvent );
			}
		}

		if ( ( !appSubscriptionExists ) && ( DeviceEventsEnum.getByPath( eventName ) == null ) )
		{
			LOG.warn( "Agent sent an event CES nor any App is subscribed for: {}", eventName );
		}
		if ( deviceEventsList.isEmpty() )
		{
			LOG.debug( "Event type {} has irrelevant event value. Ignoring event.", eventName );
		}

		return deviceEventsList;
	}

	public List<String> getDeviceCapabilities() throws DeviceException
	{
		List<String> capabilities = new ArrayList();

		configureSessionSettings();
		DeviceServiceTransport proxy = getProxy( getAdditionalRegistrationInfo() );

		Capabilities result = proxy.getServiceCapabilities();
		if ( ( result != null ) && ( result.getStrCapabilities() != null ) )
		{
			capabilities = Arrays.asList( result.getStrCapabilities() );
		}
		return capabilities;
	}

	public List<AlarmSource> getAlarmSources() throws DeviceException
	{
		List<AlarmSource> alarmSources = new ArrayList();

		configureSessionSettings();
		DeviceServiceTransport proxy = getProxy( getAdditionalRegistrationInfo() );
		AlarmSource[] result = proxy.getAlarmSources();
		if ( result != null )
		{
			alarmSources = Arrays.asList( result );
		}

		return alarmSources;
	}

	public void closeAlarmEntries( List<AlarmEntryCloseRecord> closeRecords ) throws DeviceException
	{
		if ( closeRecords == null )
		{
			return;
		}
		configureSessionSettings();
		DeviceServiceTransport proxy = getProxy( getAdditionalRegistrationInfo() );

		proxy.closeAlarmEntries( ( AlarmEntryCloseRecord[] ) closeRecords.toArray( new AlarmEntryCloseRecord[closeRecords.size()] ) );
	}

	public long getDeviceTime() throws DeviceException
	{
		String sessionId = null;
		boolean useTrustedCommunication = false;
		Map<String, Object> deviceInfoMap = getAdditionalRegistrationInfo();
		if ( deviceInfoMap != null )
		{
			sessionId = ( String ) deviceInfoMap.get( "securityToken" );
			useTrustedCommunication = deviceInfoMap.containsKey( "useTrusted" );
		}
		CommandRestClient deviceRestClient = new CommandRestClient( getDeviceId(), getDeviceAddress(), sessionId );
		deviceRestClient.setDoPeerCertValidation( !useTrustedCommunication );
		return deviceRestClient.getDeviceTime();
	}

	public DeviceInfo getDeviceInfo( boolean https, boolean retry ) throws DeviceException
	{
		String sessionId = null;
		boolean useTrustedCommunication = false;
		Map<String, Object> deviceInfoMap = getAdditionalRegistrationInfo();
		if ( deviceInfoMap != null )
		{
			sessionId = ( String ) deviceInfoMap.get( "securityToken" );
			useTrustedCommunication = deviceInfoMap.containsKey( "useTrusted" );
		}

		CommandRestClient deviceRestClient = new CommandRestClient( getDeviceId(), getDeviceAddress(), sessionId );
		deviceRestClient.setDoPeerCertValidation( !useTrustedCommunication );
		if ( !retry )
		{
			deviceRestClient.setMaxRetry( 1 );
		}

		DeviceInfo response = deviceRestClient.getDeviceInfo( https );
		if ( response == null )
		{
			throw new DeviceException( "Could not read device info" );
		}
		return response;
	}

	public List<Switch> getSwitches() throws DeviceException
	{
		List<Switch> switches = new ArrayList();

		configureSessionSettings();
		DeviceServiceTransport proxy = getProxy( getAdditionalRegistrationInfo() );
		Switch[] result = proxy.getSwitches();
		if ( result != null )
		{
			Collections.addAll( switches, result );
		}
		return switches;
	}

	public List<AudioOutput> getAudioOutputs() throws DeviceException
	{
		List<AudioOutput> audioOutputs = new ArrayList();

		configureSessionSettings();
		DeviceServiceTransport proxy = getProxy( getAdditionalRegistrationInfo() );
		AudioOutput[] result = proxy.getAudioOutputs();
		if ( result != null )
		{
			audioOutputs = Arrays.asList( result );
		}
		return audioOutputs;
	}

	public void setAlertConfig( AlertConfig alertConfig ) throws DeviceException
	{
		configureSessionSettings();
		DeviceServiceTransport proxy = getProxy( getAdditionalRegistrationInfo() );
		proxy.setAlertConfig( alertConfig );
	}

	public String getAlertConfigId() throws DeviceException
	{
		configureSessionSettings();
		DeviceServiceTransport proxy = getProxy( getAdditionalRegistrationInfo() );
		return proxy.getAlertConfigId();
	}

	public void closeAlerts( List<String> alertIds ) throws DeviceException
	{
		configureSessionSettings();
		DeviceServiceTransport proxy = getProxy( getAdditionalRegistrationInfo() );
		proxy.closeAlerts( ( String[] ) alertIds.toArray( new String[alertIds.size()] ) );
	}

	public List<AlertEntry> getAlerts() throws DeviceException
	{
		configureSessionSettings();
		DeviceServiceTransport proxy = getProxy( getAdditionalRegistrationInfo() );
		List<AlertEntry> alertEntries = new ArrayList();
		AlertEntry[] result = proxy.getAlerts();

		if ( result != null )
		{
			alertEntries = Arrays.asList( result );
		}

		return alertEntries;
	}

	public String getSessionIdWithESMToken( String esmSecurityToken )
	{
		String address = deviceResource.getDeviceView().getRegistrationAddress();
		CommandRestClient restClient = new CommandRestClient( address );
		try
		{
			return restClient.getDeviceSessionWithESMToken( esmSecurityToken );
		}
		catch ( DeviceException e )
		{
			if ( e.isCommunicationError() )
			{
				try
				{
					DeviceInfo info = getDeviceInfo( false, true );
					int https = info.httpsPort;
					address = address.split( ":" )[0];
					address = address.concat( ":" ).concat( String.valueOf( https ) );
					restClient.setAddress( address );
					return restClient.getDeviceSessionWithESMToken( esmSecurityToken );
				}
				catch ( DeviceException ex )
				{
					LOG.warn( "Could not obtain session for device {}. Details {}", address, ex );
				}
			}
		}

		return null;
	}

	public void updateRegistrationDetails( List<String> serverAddresses ) throws DeviceException
	{
		configureSessionSettings();
		DeviceServiceTransport proxy = getProxy( getAdditionalRegistrationInfo() );
		proxy.updateRegistrationDetails( ( String[] ) serverAddresses.toArray( new String[serverAddresses.size()] ) );
	}

	private int getMaxRetry()
	{
		return commonConfig.getIntProperty( ConfigProperty.HTTP_CLIENT_MAX_RETRIES, 2 );
	}

	public void setCommonConfig( CommonConfiguration commonConfig )
	{
		this.commonConfig = commonConfig;
	}

	public void setDeviceEventHandlerScheduler( DeviceEventHandlerScheduler deviceEventHandlerScheduler )
	{
		this.deviceEventHandlerScheduler = deviceEventHandlerScheduler;
	}

	private DeviceServiceTransport getServiceProxyInstance()
	{
		return serviceProxyInstance;
	}

	private void setServiceProxyInstance( DeviceServiceTransport serviceProxy )
	{
		serviceProxyInstance = serviceProxy;
	}

	public String getServiceName()
	{
		return serviceName;
	}

	public void setServiceName( String serviceName )
	{
		this.serviceName = serviceName;
	}

	public String getServerContextPath()
	{
		return serverContextPath;
	}

	public void setServerContextPath( String serverContextPath )
	{
		this.serverContextPath = serverContextPath;
	}

	public void setDeviceSubscriptionScheduler( DeviceSubscriptionManager deviceSubscriptionScheduler )
	{
		deviceSubscriptionManager = deviceSubscriptionScheduler;
	}

	public void setCertificationService( CertificationService cs )
	{
		certificationService = cs;
	}

	public void setHealthService( HealthServiceIF healthService )
	{
		this.healthService = healthService;
	}

	private DeviceSessionHolderService getDeviceSessionHolderService()
	{
		if ( deviceSessionHolderService == null )
		{
			deviceSessionHolderService = ( ( DeviceSessionHolderService ) ApplicationContextSupport.getBean( "deviceSessionHolderService" ) );
		}
		return deviceSessionHolderService;
	}

	private ResourceTopologyServiceIF getTopologyService()
	{
		if ( topologyService == null )
		{
			topologyService = ( ( ResourceTopologyServiceIF ) ApplicationContextSupport.getBean( "resourceTopologyServiceProxy_internal" ) );
		}
		return topologyService;
	}

	private DeviceCapabilityService getDeviceCapabilityService()
	{
		return ( DeviceCapabilityService ) ApplicationContextSupport.getBean( "deviceCapabilityService" );
	}

	public void setAppEventService( AppEventService appEventService )
	{
		this.appEventService = appEventService;
	}

	public void setLicenseService( LicenseService licenseService )
	{
		this.licenseService = licenseService;
	}

	public DeviceResource getDeviceResource()
	{
		return deviceResource;
	}

	public void setDeviceResource( DeviceResource deviceResource )
	{
		this.deviceResource = deviceResource;
	}
}

