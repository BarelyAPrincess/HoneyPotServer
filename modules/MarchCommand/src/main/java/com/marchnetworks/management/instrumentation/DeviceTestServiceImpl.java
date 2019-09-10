package com.marchnetworks.management.instrumentation;

import com.marchnetworks.alarm.model.AlarmSourceEntity;
import com.marchnetworks.alarm.service.AlarmTestService;
import com.marchnetworks.command.api.rest.DeviceManagementConstants;
import com.marchnetworks.command.api.simulator.DeviceTestCoreService;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.device.data.ChannelState;
import com.marchnetworks.command.common.device.data.DeviceView;
import com.marchnetworks.command.common.device.data.EncoderView;
import com.marchnetworks.command.common.device.data.RegistrationStatus;
import com.marchnetworks.command.common.device.data.VideoEncoderView;
import com.marchnetworks.command.common.simulator.DeviceInfo;
import com.marchnetworks.command.common.simulator.DeviceSpecification;
import com.marchnetworks.command.common.topology.TopologyException;
import com.marchnetworks.command.common.topology.data.DeviceResource;
import com.marchnetworks.command.common.transport.data.ConfigurationURL;
import com.marchnetworks.command.common.transport.data.Event;
import com.marchnetworks.common.diagnostics.DiagnosticSettings;
import com.marchnetworks.common.serialization.CoreJsonSerializer;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.utils.ServerUtils;
import com.marchnetworks.license.LicenseService;
import com.marchnetworks.license.exception.LicenseException;
import com.marchnetworks.management.instrumentation.adaptation.DeviceEventHandlerScheduler;
import com.marchnetworks.management.instrumentation.dao.ChannelDAO;
import com.marchnetworks.management.instrumentation.dao.DeviceDAO;
import com.marchnetworks.management.instrumentation.data.DeviceNetworkInfoType;
import com.marchnetworks.management.instrumentation.data.DeviceSubscriptionType;
import com.marchnetworks.management.instrumentation.events.AbstractDeviceEvent;
import com.marchnetworks.management.instrumentation.events.DeviceAppEvent;
import com.marchnetworks.management.instrumentation.model.Channel;
import com.marchnetworks.management.instrumentation.model.CompositeDevice;
import com.marchnetworks.management.instrumentation.model.Device;
import com.marchnetworks.management.instrumentation.model.DeviceNetworkInfo;
import com.marchnetworks.management.instrumentation.subscription.DeviceSubscriptionManager;
import com.marchnetworks.management.topology.ResourceTopologyServiceIF;
import com.marchnetworks.management.topology.ResourceTopologyTestService;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceTestServiceImpl implements DeviceTestService, DeviceTestCoreService
{
	private static final Logger LOG = LoggerFactory.getLogger( DeviceServiceImpl.class );
	public static Integer SIM_REST_PORT = Integer.valueOf( 8080 );
	public static Integer SIM_SOAP_PORT = Integer.valueOf( 8081 );

	public static String SIMULATOR_ADDRESS;

	private DeviceDAO deviceDAO;
	private ChannelDAO channelDAO;
	private ResourceTopologyTestService topologyTestService;
	private AlarmTestService alarmTestService;

	public List<DeviceInfo> createSimulatedDevices( Long parentId, DeviceSpecification specification, String address )
	{
		int numDevices = specification.getNumDevices();
		int numCameras = specification.getNumCameras();
		Set<String> serials = specification.getSerials();

		Calendar now = Calendar.getInstance();

		ConfigurationURL rootDeviceConfigUrl = new ConfigurationURL();
		rootDeviceConfigUrl.setScheme( "http" );
		rootDeviceConfigUrl.setRelativeUrl( "setup" );
		rootDeviceConfigUrl.setPort( 0 );

		ConfigurationURL childConfigUrl = new ConfigurationURL();
		childConfigUrl.setScheme( "http" );
		childConfigUrl.setRelativeUrl( "setup/" );
		childConfigUrl.setPort( 0 );

		DeviceSubscriptionManager subscriptionManager = ( DeviceSubscriptionManager ) ApplicationContextSupport.getBean( "deviceSubscriptionManager" );
		String[] eventPrefixes = subscriptionManager.getDeviceEventSubscriptionsByType( DeviceSubscriptionType.FULL_EVENTS );
		LicenseService licenseService = ( LicenseService ) ApplicationContextSupport.getBean( "licenseService_internal" );

		CompositeDevice lastDevice = deviceDAO.findLastTestDevice();
		int startId = 1;
		int startPort = 1024;

		boolean serialsProvided = serials != null;
		long deviceTotal = 0L;
		long topologyTotal = 0L;
		long licenseTotal = 0L;
		long childTotal = 0L;
		long alarmTotal = 0L;

		if ( ( lastDevice != null ) && ( lastDevice.getModelName().equals( "Simulated" ) ) )
		{
			String lastAddress = lastDevice.getAddress();
			startId = Integer.parseInt( lastAddress.split( ":" )[1] ) + 1 - startPort;
		}

		if ( !serialsProvided )
		{
			serials = new LinkedHashSet( numDevices );
			for ( int i = startId; i < startId + numDevices; i++ )
			{
				serials.add( String.valueOf( i ) );
			}
		}

		if ( address == null )
			address = SIMULATOR_ADDRESS != null ? SIMULATOR_ADDRESS : ServerUtils.HOSTNAME_CACHED;

		String hostname;
		String ip;

		try
		{
			InetAddress inetAddress = InetAddress.getByName( address );
			ip = inetAddress.getHostAddress();
			hostname = inetAddress.getHostName();
		}
		catch ( UnknownHostException e )
		{
			throw new IllegalStateException( "Specified address does not exist" );
		}

		List<CompositeDevice> rootDevices = new ArrayList( numDevices );

		int i = startId;
		for ( String serial : serials )
		{
			long start = System.currentTimeMillis();
			String port = String.valueOf( 1024 + i );

			CompositeDevice rootDevice = new CompositeDevice();

			rootDevice.setAddress( address + ":" + port );
			rootDevice.setFamily( specification.getFamily() );
			rootDevice.setFamilyName( specification.getFamilyName() );
			rootDevice.setHardwareVersion( specification.getHardwareVersion() );
			rootDevice.setLastCommunicationTime( now );
			rootDevice.setChannelsInUse( Integer.valueOf( numCameras ) );
			rootDevice.setChannelsMax( Integer.valueOf( 128 ) );
			rootDevice.setConfigurationUrl( rootDeviceConfigUrl );
			rootDevice.setManufacturer( specification.getManufacturer() );
			rootDevice.setManufacturerName( specification.getManufacturerName() );
			rootDevice.setModel( specification.getModel() );
			rootDevice.setModelName( "Simulated" );
			rootDevice.setRegistrationStatus( RegistrationStatus.REGISTERED );
			rootDevice.setSerial( serial );
			rootDevice.setSoftwareVersion( specification.getSoftwareVersion() );
			rootDevice.setTimeCreated( now );
			rootDevice.setTimeRegStatusChanged( now );
			rootDevice.setDeviceEventSequenceId( DeviceManagementConstants.DEVICE_EVENT_START_SEQUENCE_ID );
			rootDevice.setDeviceEventSubscriptionId( UUID.randomUUID().toString() );
			rootDevice.setEventSubscriptionPrefixes( eventPrefixes );
			rootDevice.setTimeDelta( Long.valueOf( 0L ) );
			rootDevice.setTimeZoneInfo( "{\"autoAdjust\":true,\"zoneBias\":300,\"standardName\":\"Eastern Standard Time\",\"standardDate\":\"0 01/11/0000 02:00:00\",\"standardBias\":0,\"daylightName\":\"Eastern Daylight Time\",\"daylightDate\":\"0 02/03/0000 02:00:00\",\"daylightBias\":-60}" );

			Set<DeviceNetworkInfo> networkInfoSet = rootDevice.getDeviceNetworkInfos();

			DeviceNetworkInfo deviceNetInfo = new DeviceNetworkInfo();
			deviceNetInfo.setNetworkInfoType( DeviceNetworkInfoType.NETWORK_NAME );
			deviceNetInfo.setValue( "[\"" + ( !serialsProvided ? hostname + "-" : "" ) + serial + "\"]" );
			deviceNetInfo.setDevice( rootDevice );
			networkInfoSet.add( deviceNetInfo );

			deviceNetInfo = new DeviceNetworkInfo();
			deviceNetInfo.setNetworkInfoType( DeviceNetworkInfoType.NETWORK_ADDRESS );
			deviceNetInfo.setValue( "[\"" + ip + "\"]" );
			deviceNetInfo.setDevice( rootDevice );
			networkInfoSet.add( deviceNetInfo );

			deviceNetInfo = new DeviceNetworkInfo();
			deviceNetInfo.setNetworkInfoType( DeviceNetworkInfoType.LOCAL_ADDRESS_ZONE );
			deviceNetInfo.setValue( "{\"address\":[\"" + ip + "\",\"" + hostname + "\"],\"httpPort\":80,\"httpsPort\":443}" );
			deviceNetInfo.setDevice( rootDevice );
			networkInfoSet.add( deviceNetInfo );

			deviceNetInfo = new DeviceNetworkInfo();
			deviceNetInfo.setNetworkInfoType( DeviceNetworkInfoType.MAC_ADDRESS );
			deviceNetInfo.setValue( "[\"00:50:56:3C:63:E9\"]" );
			deviceNetInfo.setDevice( rootDevice );
			networkInfoSet.add( deviceNetInfo );

			deviceDAO.create( rootDevice );

			rootDevices.add( rootDevice );

			deviceTotal += System.currentTimeMillis() - start;

			start = System.currentTimeMillis();
			try
			{
				licenseService.allocateForTestDevice( rootDevice );
			}
			catch ( LicenseException e )
			{
				throw new IllegalStateException( "Throwing runtime exception because license allocation operation failed and transaction should not be comitted." );
			}
			licenseTotal += System.currentTimeMillis() - start;

			for ( int j = 1; j <= numCameras; j++ )
			{
				start = System.currentTimeMillis();

				Device parent = rootDevice;

				if ( specification.hasChildDevices() )
				{
					parent = new Device();
					parent.setParentDevice( rootDevice );
					parent.setFamily( "0" );
					parent.setFamilyName( "Edge Device" );
					parent.setHardwareVersion( "N/A" );
					parent.setLastCommunicationTime( now );
					parent.setConfigurationUrl( childConfigUrl );
					parent.setManufacturer( "1" );
					parent.setManufacturerName( "March Networks" );
					parent.setModel( "16" );
					parent.setModelName( "MegaPX 720p" );
					parent.setRegistrationStatus( RegistrationStatus.REGISTERED );
					parent.setSerial( "testChildSerial" + i + "-" + j );
					parent.setSoftwareVersion( "1.8.5.20100802152712" );
					parent.setTimeCreated( now );
					parent.setTimeRegStatusChanged( now );

					networkInfoSet = parent.getDeviceNetworkInfos();

					deviceNetInfo = new DeviceNetworkInfo();
					deviceNetInfo.setNetworkInfoType( DeviceNetworkInfoType.MAC_ADDRESS );
					deviceNetInfo.setValue( "[\"00:12:81:61:10:EE\"]" );
					deviceNetInfo.setDevice( parent );
					networkInfoSet.add( deviceNetInfo );

					deviceNetInfo = new DeviceNetworkInfo();
					deviceNetInfo.setNetworkInfoType( DeviceNetworkInfoType.NETWORK_ADDRESS );
					deviceNetInfo.setValue( "[\"10.51.71.14\"]" );
					deviceNetInfo.setDevice( rootDevice );
					networkInfoSet.add( deviceNetInfo );

					deviceNetInfo = new DeviceNetworkInfo();
					deviceNetInfo.setNetworkInfoType( DeviceNetworkInfoType.NETWORK_NAME );
					deviceNetInfo.setValue( "[\"VS720P6110EE\"]" );
					deviceNetInfo.setDevice( rootDevice );
					networkInfoSet.add( deviceNetInfo );
				}

				Channel channel = new Channel();
				channel.setChannelId( "channel-" + j );
				channel.setName( "Channel-" + j );
				channel.setChannelState( ChannelState.ONLINE );
				channel.setPtzDomeIdentifier( "" );
				channel.setDevice( parent );

				VideoEncoderView videoEncoder = new VideoEncoderView();
				videoEncoder.setEncoderId( "testEncoder" + j );
				videoEncoder.setCodec( "testCodec" );
				EncoderView[] encoders = new EncoderView[1];
				encoders[0] = videoEncoder;
				channel.setEncoders( encoders );
				channelDAO.create( channel );

				parent.getChannels().put( channel.getIdAsString(), channel );

				if ( specification.hasChildDevices() )
				{
					deviceDAO.create( parent );
					rootDevice.getChildDevices().put( parent.getDeviceId(), parent );
				}

				childTotal += System.currentTimeMillis() - start;
			}

			if ( i % 200 == 0 )
			{
				deviceDAO.flushAndClear();
			}
			i++;
		}

		long start = System.currentTimeMillis();
		Map<Long, List<AlarmSourceEntity>> alarms = alarmTestService.createSimulatedAlarmSources( rootDevices, specification.getNumAlarms() );
		alarmTotal = System.currentTimeMillis() - start;

		start = System.currentTimeMillis();
		List<Long> ids = topologyTestService.createSimulatedDevices( parentId, rootDevices, alarms );
		topologyTotal = System.currentTimeMillis() - start;

		LOG.info( "Device: " + deviceTotal + ", Topology: " + topologyTotal + ", License: " + licenseTotal + ", Child:" + childTotal + ", Alarm: " + alarmTotal );

		List<DeviceInfo> deviceInfo = new ArrayList( rootDevices.size() );
		for ( i = 0; i < rootDevices.size(); i++ )
		{
			CompositeDevice device = ( CompositeDevice ) rootDevices.get( i );
			Long resourceId = ( Long ) ids.get( i );
			DeviceInfo info = new DeviceInfo( resourceId, Long.valueOf( Long.parseLong( device.getDeviceId() ) ), device.getDeviceEventSubscriptionId() );
			deviceInfo.add( info );
		}
		return deviceInfo;
	}

	public void removeSimulatedDevices()
	{
		try
		{
			topologyTestService.removeSimulatedDevices();
		}
		catch ( TopologyException e )
		{
			throw new IllegalStateException( "Throwing runtime exception because topology removal operation failed and transaction should not be comitted." );
		}
	}

	public void testBlockDatabaseConnection( int waitTime )
	{
		long start = System.currentTimeMillis();
		boolean done = false;
		while ( !done )
		{
			try
			{
				deviceDAO.findById( "1" );
				done = true;
			}
			catch ( Exception e )
			{
				LOG.info( "Unable to get test database connection after {} ms. Exception: {}", Long.valueOf( System.currentTimeMillis() - start ), e.getMessage() );
			}
		}
		try
		{
			Thread.sleep( waitTime * 1000 );
		}
		catch ( InterruptedException e )
		{
			LOG.error( "Interrupted while blocking connection ", e );
		}
	}

	public void updateTimeDelta( String deviceId, long timeDelta )
	{
		ResourceTopologyServiceIF topologyService = ( ResourceTopologyServiceIF ) ApplicationContextSupport.getBean( "resourceTopologyService_internal" );
		DeviceResource device = topologyService.getDeviceResourceByDeviceId( deviceId );
		DeviceView deviceView = device.getDeviceView();
		deviceView.setTimeDelta( Long.valueOf( timeDelta ) );

		CompositeDevice d = ( CompositeDevice ) deviceDAO.findById( deviceId );
		d.setTimeDelta( Long.valueOf( timeDelta ) );
	}

	public void injectDeviceEvent( Long deviceResourceId, Event event )
	{
		injectDeviceEvents( deviceResourceId, Collections.singletonList( event ) );
	}

	public void injectDeviceEvents( Long deviceResourceId, List<Event> events )
	{
		DeviceEventHandlerScheduler deviceEventHandlerScheduler = ( DeviceEventHandlerScheduler ) ApplicationContextSupport.getBean( "deviceEventHandlerScheduler" );
		ResourceTopologyServiceIF topologyService = ( ResourceTopologyServiceIF ) ApplicationContextSupport.getBean( "resourceTopologyService_internal" );

		String deviceId = null;
		try
		{
			DeviceResource deviceResource = ( DeviceResource ) topologyService.getResource( deviceResourceId );
			deviceId = deviceResource.getDeviceId();
		}
		catch ( TopologyException localTopologyException )
		{
		}

		List<AbstractDeviceEvent> eventList = new ArrayList( events.size() );
		for ( Event event : events )
		{
			DeviceAppEvent deviceEvent = new DeviceAppEvent( deviceId, String.valueOf( deviceResourceId ), event );
			eventList.add( deviceEvent );
		}

		deviceEventHandlerScheduler.scheduleDeviceEventHandling( deviceId, eventList );
	}

	public boolean registerSimulator( String address )
	{
		SimulatorResponse response = sendCommandToSimulator( address, "setCesHost?cesHost=" + ServerUtils.HOSTNAME_CACHED, null );
		boolean result = response.isSuccess();
		if ( result )
		{
			SIMULATOR_ADDRESS = address;
			enableSimulation();
		}
		return result;
	}

	public boolean unregisterSimulator()
	{
		SimulatorResponse response = sendCommandToSimulator( SIMULATOR_ADDRESS, "setCesHost", null );
		SIMULATOR_ADDRESS = null;
		disableSimulation();
		return response.isSuccess();
	}

	public SimulatorInfo getSimulatorInfo()
	{
		if ( SIMULATOR_ADDRESS != null )
		{
			SimulatorResponse response = sendCommandToSimulator( SIMULATOR_ADDRESS, "getDevices", null );
			int devices = 0;
			if ( response.isSuccess() )
			{
				devices = Integer.parseInt( response.getResponseAsString() );
			}
			return new SimulatorInfo( SIMULATOR_ADDRESS, devices );
		}
		return null;
	}

	public void injectSimulatedAlarms( int numAlarms, int timePeriod, int percentDevices )
	{
		String path = "injectAlarms?numAlarms=" + numAlarms + "&" + "timePeriod" + "=" + timePeriod + "&" + "percentDevices" + "=" + percentDevices;
		sendCommandToSimulator( SIMULATOR_ADDRESS, path, null );
	}

	public void injectSimulatedAlerts( int numAlerts, int timePeriod, int percentDevices )
	{
		String path = "injectAlerts?numAlerts=" + numAlerts + "&" + "timePeriod" + "=" + timePeriod + "&" + "percentDevices" + "=" + percentDevices;
		sendCommandToSimulator( SIMULATOR_ADDRESS, path, null );
	}

	public void sendDevicesToSimulator( List<DeviceInfo> devices, int numCameras, int numAlarms )
	{
		if ( SIMULATOR_ADDRESS != null )
		{
			String json = CoreJsonSerializer.toJson( devices );

			String path = "addDevices?numCameras=" + numCameras + "&" + "numAlarms" + "=" + numAlarms;
			sendCommandToSimulator( SIMULATOR_ADDRESS, path, json );
		}
	}

	private void enableSimulation()
	{
		DiagnosticSettings.setRestPort( SIM_REST_PORT );
		DiagnosticSettings.setSoapPort( SIM_SOAP_PORT );
		DiagnosticSettings.setTransport( "http" );
		DiagnosticSettings.setMaxRestConnectionsPerHost( Integer.valueOf( 20 ) );
	}

	private void disableSimulation()
	{
		DiagnosticSettings.setRestPort( null );
		DiagnosticSettings.setSoapPort( null );
		DiagnosticSettings.setTransport( null );
		DiagnosticSettings.setMaxRestConnectionsPerHost( null );
	}

	private SimulatorResponse sendCommandToSimulator( String address, String path, String data )
	{
		SimulatorResponse response = new SimulatorResponse();
		String requestURL = "http://" + address + ":" + SIM_REST_PORT + "/" + path;
		try
		{
			URL url = new URL( requestURL );
			HttpURLConnection httpConn = ( HttpURLConnection ) url.openConnection();
			httpConn.setRequestMethod( "POST" );
			httpConn.setConnectTimeout( 20000 );
			httpConn.setReadTimeout( 20000 );

			if ( data != null )
			{
				httpConn.setRequestProperty( "Content-Type", "application/json; charset=utf8" );
				httpConn.setDoOutput( true );
				PrintWriter out = new PrintWriter( httpConn.getOutputStream() );
				out.print( data );
				out.close();
			}

			InputStream is = null;
			try
			{
				is = httpConn.getInputStream();

				byte[] byteArray = CommonAppUtils.readInputStream( is );
				response.setResponse( byteArray );
			}
			finally
			{
				if ( is != null )
				{
					is.close();
				}
			}

			int code = httpConn.getResponseCode();
			if ( code != 200 )
			{
				LOG.error( "Error sending command " + path + " to simulator, code " + code );
			}
			else
			{
				response.setSuccess( true );
			}
		}
		catch ( IOException e )
		{
			LOG.error( "Error sending command " + path + " to simulator, " + e.getMessage() );
		}
		return response;
	}

	public void setTopologyTestService( ResourceTopologyTestService topologyTestService )
	{
		this.topologyTestService = topologyTestService;
	}

	public void setAlarmTestService( AlarmTestService alarmTestService )
	{
		this.alarmTestService = alarmTestService;
	}

	public void setDeviceDAO( DeviceDAO deviceDAO )
	{
		this.deviceDAO = deviceDAO;
	}

	public void setChannelDAO( ChannelDAO channelDAO )
	{
		this.channelDAO = channelDAO;
	}
}

