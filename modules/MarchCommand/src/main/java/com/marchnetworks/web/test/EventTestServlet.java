package com.marchnetworks.web.test;

import com.marchnetworks.command.api.query.Criteria;
import com.marchnetworks.command.api.query.Restrictions;
import com.marchnetworks.command.common.device.DeviceEventsEnum;
import com.marchnetworks.command.common.device.data.ChannelState;
import com.marchnetworks.command.common.device.data.RegistrationStatus;
import com.marchnetworks.command.common.scheduling.TaskScheduler;
import com.marchnetworks.command.common.topology.data.ChannelResource;
import com.marchnetworks.command.common.topology.data.DeviceResource;
import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.command.common.transport.data.Event;
import com.marchnetworks.command.common.transport.data.EventType;
import com.marchnetworks.command.common.transport.data.GenericValue;
import com.marchnetworks.command.common.transport.data.Pair;
import com.marchnetworks.command.common.transport.data.Timestamp;
import com.marchnetworks.common.diagnostics.DiagnosticSettings;
import com.marchnetworks.common.event.StateCacheable;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.management.instrumentation.DeviceRegistry;
import com.marchnetworks.management.instrumentation.DeviceTestService;
import com.marchnetworks.management.instrumentation.adaptation.DeviceEventHandlerScheduler;
import com.marchnetworks.management.instrumentation.events.AbstractDeviceEvent;
import com.marchnetworks.management.instrumentation.events.ChannelConnectionStateEvent;
import com.marchnetworks.management.instrumentation.events.DeviceAlertEvent;
import com.marchnetworks.management.instrumentation.events.DeviceAlertEventType;
import com.marchnetworks.management.instrumentation.events.DeviceConfigRetrieveEvent;
import com.marchnetworks.management.instrumentation.events.DeviceRestartEvent;
import com.marchnetworks.management.instrumentation.events.DeviceStatisticsListEvent;
import com.marchnetworks.management.instrumentation.events.DeviceStatisticsStateEvent;
import com.marchnetworks.management.instrumentation.events.DeviceSystemChangedEvent;
import com.marchnetworks.management.instrumentation.model.ChannelMBean;
import com.marchnetworks.management.instrumentation.model.CompositeDeviceMBean;
import com.marchnetworks.management.instrumentation.model.DeviceMBean;
import com.marchnetworks.management.instrumentation.task.DeviceFetchEventsTask;
import com.marchnetworks.management.topology.ResourceTopologyServiceIF;
import com.marchnetworks.security.authentication.SessionService;
import com.marchnetworks.security.device.DeviceSessionHolderService;
import com.marchnetworks.server.event.EventPusher;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet( name = "EventTest", urlPatterns = {"/EventTest"} )
public class EventTestServlet extends HttpServlet
{
	private static final long serialVersionUID = -1438547422699140179L;
	private EventPusher eventPusher = ( EventPusher ) ApplicationContextSupport.getBean( "eventPusher" );
	private SessionService sessionCoreService = ( SessionService ) ApplicationContextSupport.getBean( "sessionCoreService" );
	private DeviceRegistry deviceRegistry = ( DeviceRegistry ) ApplicationContextSupport.getBean( "deviceRegistryProxy" );
	private DeviceEventHandlerScheduler deviceEventHandlerScheduler = ( DeviceEventHandlerScheduler ) ApplicationContextSupport.getBean( "deviceEventHandlerScheduler" );
	private ResourceTopologyServiceIF topologyService = ( ResourceTopologyServiceIF ) ApplicationContextSupport.getBean( "resourceTopologyServiceProxy_internal" );
	private TaskScheduler taskScheduler = ( TaskScheduler ) ApplicationContextSupport.getBean( "taskScheduler" );
	private DeviceSessionHolderService deviceSessionHolder = ( DeviceSessionHolderService ) ApplicationContextSupport.getBean( "deviceSessionHolderService" );
	private DeviceTestService deviceTestService = ( DeviceTestService ) ApplicationContextSupport.getBean( "deviceTestServiceProxy" );

	public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		response.setContentType( "text/html" );
		PrintWriter out = response.getWriter();
		createPageContent( request.getContextPath(), out, "Refresh complete" );
	}

	public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		response.setContentType( "text/html" );

		String status = "";
		if ( request.getParameter( "deleteSubscriptions" ) != null )
		{
			eventPusher.cancelAllSubscriptions();
			status = "All subscriptions deleted";
		}
		else if ( request.getParameter( "deleteSessions" ) != null )
		{
			sessionCoreService.deleteAllSessions( false );
			status = "All sessions deleted";
		}
		else if ( request.getParameter( "expireSessions" ) != null )
		{
			sessionCoreService.deleteAllSessions( true );
			status = "All sessions deleted";
		}
		else if ( request.getParameter( "sendCameraConnectState" ) != null )
		{
			int total = Integer.parseInt( request.getParameter( "total" ) );
			int interval = Integer.parseInt( request.getParameter( "interval" ) );
			String deviceId = request.getParameter( "deviceId" );
			boolean isLegacyAlert = request.getParameter( "isLegacy" ) != null;
			sendCameraConnectState( deviceId, total, interval, isLegacyAlert );
			status = "Camera Offline/Online Sent for device " + deviceId;
		}
		else if ( request.getParameter( "sendStats" ) != null )
		{
			int total = Integer.parseInt( request.getParameter( "totalStat" ) );
			int interval = Integer.parseInt( request.getParameter( "intervalStat" ) );
			String deviceId = request.getParameter( "deviceId" );
			sendDeviceStatistics( deviceId, total, interval );
			status = "Device Statistics Sent for device " + deviceId;
		}
		else if ( request.getParameter( "sendSystemChanged" ) != null )
		{
			sendSystemChangedEvent( request.getParameter( "deviceId" ) );
		}
		else if ( request.getParameter( "sendGetConfig" ) != null )
		{
			sendGetConfigEvent( request.getParameter( "deviceId" ) );
		}
		else if ( request.getParameter( "sendRestart" ) != null )
		{
			int total = Integer.parseInt( request.getParameter( "totalRestart" ) );
			int interval = Integer.parseInt( request.getParameter( "intervalRestart" ) );
			String deviceId = request.getParameter( "deviceId" );
			sendDeviceRestart( deviceId, total, interval );
			status = "Device Restarts Sent for device " + deviceId;
		}
		else if ( request.getParameter( "sendFetch" ) != null )
		{
			int total = Integer.parseInt( request.getParameter( "totalFetch" ) );
			int interval = Integer.parseInt( request.getParameter( "intervalFetch" ) );
			String deviceId = request.getParameter( "deviceId" );
			sendDeviceFetch( deviceId, total, interval );
			status = "Device Fetch Events for device " + deviceId;
		}
		else if ( request.getParameter( "sendAllRestart" ) != null )
		{
			int total = Integer.parseInt( request.getParameter( "totalDevicesRestart" ) );
			sendAllDeviceRestart( total );
			status = "Device Restarts Sent for multiple devices";
		}
		else if ( request.getParameter( "sendAllFetch" ) != null )
		{
			int total = Integer.parseInt( request.getParameter( "totalDevicesFetch" ) );
			sendAllDeviceFetch( total );
			status = "Device Fetch Events for multiple devices";
		}
		else if ( request.getParameter( "blockDevice" ) != null )
		{
			String deviceId = request.getParameter( "deviceId" );
			String blockSetting = request.getParameter( "blockSetting" );
			setBlockDevice( deviceId, blockSetting );
			status = "Device subscribe block " + deviceId + " is " + blockSetting;
		}
		else if ( request.getParameter( "invalidateSession" ) != null )
		{
			String deviceId = request.getParameter( "deviceId" );
			deviceSessionHolder.invalidateAllDeviceSessions( deviceId );
			status = "Device session invalidated for device " + deviceId;
		}
		else if ( request.getParameter( "checkOffline" ) != null )
		{
			CheckOfflineThread checkOfflineThread = new CheckOfflineThread();
			taskScheduler.executeNow( checkOfflineThread );

			status = "CheckOffline started";
		}
		else if ( request.getParameter( "sendCustomEvent" ) != null )
		{
			String path = request.getParameter( "path" );
			String source = request.getParameter( "source" );
			String value = request.getParameter( "value" );
			String info = request.getParameter( "info" );

			String deviceId = request.getParameter( "customDeviceId" );
			boolean isDeleteEvent = request.getParameter( "isDeleteEvent" ) != null;
			sendCustomEvent( path, source, value, info, deviceId, isDeleteEvent );
		}
		else if ( request.getParameter( "updateTimeDelta" ) != null )
		{
			String deviceId = request.getParameter( "deviceId" );
			int timeDelta = Integer.parseInt( request.getParameter( "timeDelta" ) ) * 60000;

			deviceTestService.updateTimeDelta( deviceId, timeDelta );
		}
		else
		{
			status = "Refresh complete";
		}

		PrintWriter out = response.getWriter();
		createPageContent( request.getContextPath(), out, status );
	}

	public void sendCameraConnectState( String deviceId, int repetitions, int interval, boolean isLegacyAlert )
	{
		CompositeDeviceMBean rootDevice = ( CompositeDeviceMBean ) deviceRegistry.getDeviceEagerDetached( deviceId );
		Map<String, DeviceMBean> children = rootDevice.getChildDeviceMBeans();
		DeviceMBean firstChild = ( DeviceMBean ) children.values().iterator().next();
		ChannelMBean channel = ( ChannelMBean ) firstChild.getChannelMBeans().values().iterator().next();

		String childDeviceId = firstChild.getDeviceId();
		String channelId = channel.getChannelId();
		String channelName = channel.getName();

		for ( int count = 0; count < repetitions; count++ )
		{
			boolean offline = count % 2 == 0;

			ChannelState state = ChannelState.ONLINE;
			String stateString = "online";
			if ( offline )
			{
				state = ChannelState.OFFLINE;
				stateString = "offline";
			}

			List<AbstractDeviceEvent> eventList = new ArrayList();

			DeviceResource deviceResource = topologyService.getDeviceResourceByDeviceId( rootDevice.getDeviceId() );
			AbstractDeviceEvent deviceEvent = new ChannelConnectionStateEvent( childDeviceId, channelId, state, deviceResource.getId() );
			eventList.add( deviceEvent );

			Event event = new Event();
			DeviceAlertEventType alertEventType = DeviceAlertEventType.LEGACY;
			Timestamp ts = new Timestamp();
			ts.setTicks( System.currentTimeMillis() * 1000L );
			event.setTimestamp( ts );
			GenericValue value = new GenericValue();
			Pair[] details = null;

			if ( isLegacyAlert )
			{
				event.setName( DeviceEventsEnum.CHANNEL_STATE.getPath() );
				event.setSource( channelId );
				event.setType( EventType.UPDATE );
				value.setValue( stateString );
				details = new Pair[1];
				Pair pair = new Pair();
				pair.setName( "name" );
				pair.setValue( channelName );
				details[0] = pair;
			}
			else
			{
				alertEventType = DeviceAlertEventType.ALERT_UPDATED;
				event.setName( DeviceEventsEnum.ALERT_UPDATED.getPath() );
				event.setSource( "1" );
				event.setType( EventType.NOTIFY );
				value.setValue( offline ? "unresolved" : "resolved" );
				String timeStamp = Long.toString( System.currentTimeMillis() * 1000L );

				details = new Pair[9];
				details[0] = new Pair( "name", channelName );
				details[1] = new Pair( "path", DeviceEventsEnum.CHANNEL_STATE.getPath() );
				details[2] = new Pair( "source", channelId );
				details[3] = new Pair( "count", "1" );
				details[4] = new Pair( "first", timeStamp );
				details[5] = new Pair( "last", timeStamp );
				details[6] = new Pair( "lastResolved", timeStamp );
				details[7] = new Pair( "value", stateString );
				details[8] = new Pair( "valuetype", "string" );
			}
			event.setValue( value );
			event.setInfo( details );

			AbstractDeviceEvent deviceAlert = new DeviceAlertEvent( alertEventType, deviceId, event );
			eventList.add( deviceAlert );
			deviceEventHandlerScheduler.scheduleDeviceEventHandling( deviceId, eventList );
			try
			{
				Thread.sleep( interval );
			}
			catch ( InterruptedException e )
			{
				e.printStackTrace();
			}
		}
	}

	public void sendDeviceStatistics( String deviceId, int repetitions, int interval )
	{
		DeviceResource device = topologyService.getDeviceResourceByDeviceId( deviceId );
		List<Resource> channels = device.createFilteredResourceList( new Class[] {ChannelResource.class} );
		ChannelResource channel = ( ChannelResource ) channels.get( 0 );
		String channelId = channel.getChannelId();

		GenericValue value = new GenericValue();
		value.setValue( 296506 );
		Pair[] details = new Pair[2];
		Pair pair = new Pair();
		pair.setName( "min" );
		pair.setValue( "3372528" );
		details[0] = pair;
		pair = new Pair();
		pair.setName( "max" );
		pair.setValue( "4168856" );
		details[1] = pair;

		GenericValue valueLong = new GenericValue();
		valueLong.setValue( 1041230L );

		long timestamp = System.currentTimeMillis() * 1000L;

		for ( int i = 0; i < repetitions; i++ )
		{
			List<StateCacheable> eventList = new ArrayList();

			DeviceStatisticsStateEvent deviceEvent = new DeviceStatisticsStateEvent( deviceId, deviceId, DeviceEventsEnum.SYSTEM_CPULOAD.getPath(), value, details, timestamp, false );
			eventList.add( deviceEvent );

			deviceEvent = new DeviceStatisticsStateEvent( deviceId, deviceId, DeviceEventsEnum.SYSTEM_MEMORYUSED.getPath(), value, details, timestamp, false );
			eventList.add( deviceEvent );

			deviceEvent = new DeviceStatisticsStateEvent( deviceId, deviceId, DeviceEventsEnum.SYSTEM_MEMORYUSED_TOTAL.getPath(), value, details, timestamp, false );
			eventList.add( deviceEvent );

			deviceEvent = new DeviceStatisticsStateEvent( deviceId, deviceId, DeviceEventsEnum.SYSTEM_CPULOAD_TOTAL.getPath(), valueLong, details, timestamp, false );
			eventList.add( deviceEvent );

			deviceEvent = new DeviceStatisticsStateEvent( deviceId, channelId, DeviceEventsEnum.CHANNEL_BANDWIDH_INCOMING.getPath(), valueLong, details, timestamp, false );
			eventList.add( deviceEvent );

			deviceEvent = new DeviceStatisticsStateEvent( deviceId, channelId, DeviceEventsEnum.CHANNEL_BANDWIDH_RECORDING.getPath(), value, details, timestamp, false );
			eventList.add( deviceEvent );

			deviceEvent = new DeviceStatisticsStateEvent( deviceId, deviceId, DeviceEventsEnum.SYSTEM_BANDWIDTH_INCOMING_IP.getPath(), value, details, timestamp, false );
			eventList.add( deviceEvent );

			deviceEvent = new DeviceStatisticsStateEvent( deviceId, deviceId, DeviceEventsEnum.SYSTEM_BANDWIDTH_OUTGOING.getPath(), value, details, timestamp, false );
			eventList.add( deviceEvent );

			AbstractDeviceEvent statisticsList = new DeviceStatisticsListEvent( deviceId, eventList );
			deviceEventHandlerScheduler.scheduleDeviceEventHandling( deviceId, Collections.singletonList( statisticsList ) );
			try
			{
				Thread.sleep( interval );
			}
			catch ( InterruptedException e )
			{
				e.printStackTrace();
			}
		}
	}

	private void sendCustomEvent( String path, String source, String value, String info, String deviceId, boolean isDeleteEvent )
	{
		long timestamp = System.currentTimeMillis() * 1000L;
		GenericValue genericValue = new GenericValue();
		genericValue.setValue( value );

		Pair[] details = null;

		if ( ( info != null ) && ( !info.isEmpty() ) )
		{
			String[] detail = info.split( "," );
			String key = detail[0];
			String detailValue = detail[1];

			if ( ( key != null ) && ( detailValue != null ) )
			{
				details = new Pair[1];
				details[0] = new Pair( key, detailValue );
			}
		}

		StateCacheable event = new DeviceStatisticsStateEvent( deviceId, source, path, genericValue, details, timestamp, isDeleteEvent );
		AbstractDeviceEvent statisticsList = new DeviceStatisticsListEvent( deviceId, Collections.singletonList( event ) );
		deviceEventHandlerScheduler.scheduleDeviceEventHandling( deviceId, Collections.singletonList( statisticsList ) );
	}

	private void sendSystemChangedEvent( String deviceId )
	{
		AbstractDeviceEvent event = new DeviceSystemChangedEvent( deviceId );
		deviceEventHandlerScheduler.scheduleDeviceEventHandling( deviceId, Collections.singletonList( event ) );
	}

	private void sendGetConfigEvent( String deviceId )
	{
		AbstractDeviceEvent event = new DeviceConfigRetrieveEvent( deviceId, RegistrationStatus.REGISTERED );
		deviceEventHandlerScheduler.scheduleDeviceEventHandling( deviceId, Collections.singletonList( event ) );
	}

	public void sendAllDeviceRestart( int total )
	{
		Criteria criteria = new Criteria( DeviceResource.class );
		criteria.add( Restrictions.eq( "deviceView.parentDeviceId", null ) );
		List<Resource> devices = topologyService.getResources( criteria );

		if ( ( total == 0 ) || ( total > devices.size() ) )
		{
			total = devices.size();
		}
		for ( int i = 0; i < total; i++ )
		{
			DeviceResource device = ( DeviceResource ) devices.get( i );
			deviceSessionHolder.invalidateAllDeviceSessions( device.getDeviceId() );

			Map<String, Object> extraInfoMap = new HashMap( 1 );
			extraInfoMap.put( "deviceRemoteAddress", getDeviceAddress( device ) );
			AbstractDeviceEvent restartEvent = new DeviceRestartEvent( device.getDeviceId() );
			restartEvent.setDeviceExtraInfo( extraInfoMap );
			deviceEventHandlerScheduler.scheduleDeviceEventHandling( device.getDeviceId(), Collections.singletonList( restartEvent ) );
		}
	}

	public void sendAllDeviceFetch( int total )
	{
		Criteria criteria = new Criteria( DeviceResource.class );
		criteria.add( Restrictions.eq( "deviceView.parentDeviceId", null ) );
		List<Resource> devices = topologyService.getResources( criteria );

		if ( ( total == 0 ) || ( total > devices.size() ) )
		{
			total = devices.size();
		}

		for ( int i = 0; i < total; i++ )
		{
			DeviceResource device = ( DeviceResource ) devices.get( i );

			DeviceFetchEventsTask fetchEventsTask = new DeviceFetchEventsTask( device.getDeviceId(), device.getDeviceView().getEventSubscriptionId(), getDeviceAddress( device ), device.getDeviceView().getDeviceCreationTime(), true );
			taskScheduler.executeFixedPoolSerial( fetchEventsTask, device.getDeviceId() );
		}
	}

	public void sendDeviceRestart( String deviceId, int repetitions, int interval )
	{
		DeviceResource device = topologyService.getDeviceResourceByDeviceId( deviceId );

		deviceSessionHolder.invalidateAllDeviceSessions( deviceId );

		for ( int i = 0; i < repetitions; i++ )
		{
			Map<String, Object> extraInfoMap = new HashMap( 1 );
			extraInfoMap.put( "deviceRemoteAddress", getDeviceAddress( device ) );
			AbstractDeviceEvent restartEvent = new DeviceRestartEvent( deviceId );
			restartEvent.setDeviceExtraInfo( extraInfoMap );
			deviceEventHandlerScheduler.scheduleDeviceEventHandling( deviceId, Collections.singletonList( restartEvent ) );
			try
			{
				Thread.sleep( interval );
			}
			catch ( InterruptedException e )
			{
				e.printStackTrace();
			}
		}
	}

	public void sendDeviceFetch( String deviceId, int repetitions, int interval )
	{
		DeviceResource device = topologyService.getDeviceResourceByDeviceId( deviceId );

		for ( int i = 0; i < repetitions; i++ )
		{
			DeviceFetchEventsTask fetchEventsTask = new DeviceFetchEventsTask( deviceId, device.getDeviceView().getEventSubscriptionId(), getDeviceAddress( device ), device.getDeviceView().getDeviceCreationTime(), true );
			taskScheduler.executeFixedPoolSerial( fetchEventsTask, deviceId );
			try
			{
				Thread.sleep( interval );
			}
			catch ( InterruptedException e )
			{
				e.printStackTrace();
			}
		}
	}

	public void setBlockDevice( String deviceId, String blockSetting )
	{
		if ( blockSetting.equals( "ON" ) )
		{
			DiagnosticSettings.blockDeviceSubscribe( Long.valueOf( Long.parseLong( deviceId ) ) );
		}
		else
		{
			DiagnosticSettings.unblockDeviceSubscribe();
		}
	}

	private String getDeviceAddress( DeviceResource device )
	{
		String fullAddress = device.getDeviceView().getRegistrationAddress();
		String address = fullAddress.split( ":" )[0];
		return address;
	}

	public void createPageContent( String path, PrintWriter out, String status )
	{
		Criteria criteria = new Criteria( DeviceResource.class );
		criteria.add( Restrictions.eq( "deviceView.parentDeviceId", null ) );
		List<Resource> devices = topologyService.getResources( criteria );

		out.println( "<html><head>" );

		out.println( "<title>Event Test</title></head><body>" );
		out.println( "<h2>Event Test</h2>" );

		out.println( "<form method='post' action ='" + path + "/EventTest' >" );

		out.println( "<table border='0'>" );
		out.println( "<tr>" );
		out.println( "<td> Subscriptions: " + eventPusher.getTotalSubscriptions() + "</td> " );

		out.println( "<td> <input type='submit' name='deleteSubscriptions' value='Delete All'> </td>" );
		out.println( "</tr>" );
		out.println( "</table>" );
		out.println( "<br>" );

		out.println( "<table border='0'>" );
		out.println( "<tr>" );
		out.println( "<td> Sessions: " + sessionCoreService.getTotalSessions() + "</td> " );
		out.println( "<td> <input type='submit' name='deleteSessions' value='Delete All'> </td>" );
		out.println( "<td> <input type='submit' name='expireSessions' value='Expire All'> </td>" );
		out.println( "</tr>" );
		out.println( "</table>" );
		out.println( "<br>" );

		out.println( "<table border='0'>" );
		out.println( "<tr>" );
		out.println( "<td> Device: </td> " );
		out.println( "<td>" );
		out.println( "<select name='deviceId'>" );
		for ( Resource device : devices )
		{
			DeviceResource deviceResource = ( DeviceResource ) device;
			out.println( "<option value='" + deviceResource.getDeviceId() + "'>" + deviceResource.getName() + "</option>" );
		}
		out.println( "</select>" );
		out.println( "</td>" );
		out.println( "</tr>" );
		out.println( "</table>" );
		out.println( "<br>" );

		out.println( "<table border='0'>" );
		out.println( "<tr>" );
		out.println( "<td> Total: </td> " );
		out.println( "<td> <input type='text' name='total' value='1' size='10'> </td>" );
		out.println( "<td> Interval (ms): </td> " );
		out.println( "<td> <input type='text' name='interval' value='100' size='10'> </td>" );
		out.println( "<td> <input type='submit' name='sendCameraConnectState' value='Send Camera On/Offline'> </td>" );
		out.println( "<td> <input type='checkbox' name='isLegacy' value='isLegacy'> Legacy Alerts. </td>" );
		out.println( "</tr>" );
		out.println( "</table>" );
		out.println( "<br>" );

		out.println( "<table border='0'>" );
		out.println( "<tr>" );
		out.println( "<td> Total: </td> " );
		out.println( "<td> <input type='text' name='totalStat' value='10' size='10'> </td>" );
		out.println( "<td> Interval (ms): </td> " );
		out.println( "<td> <input type='text' name='intervalStat' value='100' size='10'> </td>" );
		out.println( "<td> <input type='submit' name='sendStats' value='Send Device Statistics'> </td>" );
		out.println( "</tr>" );
		out.println( "</table>" );
		out.println( "<br>" );

		out.println( "<table border='0'>" );

		out.println( "<tr>" );
		out.println( "<td> <input type='submit' name='sendSystemChanged' value='Send System Changed'> </td>" );
		out.println( "</tr>" );

		out.println( "<tr>" );
		out.println( "<td> <input type='submit' name='sendGetConfig' value='Send Get Configuration'> </td>" );
		out.println( "</tr>" );
		out.println( "</table>" );
		out.println( "<br>" );

		out.println( "<table border='0'>" );
		out.println( "<tr>" );
		out.println( "<td> Total: </td> " );
		out.println( "<td> <input type='text' name='totalRestart' value='1' size='10'> </td>" );
		out.println( "<td> Interval (ms): </td> " );
		out.println( "<td> <input type='text' name='intervalRestart' value='100' size='10'> </td>" );
		out.println( "<td> <input type='submit' name='sendRestart' value='Send Restart'> </td>" );
		out.println( "</tr>" );
		out.println( "</table>" );
		out.println( "<br>" );

		out.println( "<table border='0'>" );
		out.println( "<tr>" );
		out.println( "<td> Total: </td> " );
		out.println( "<td> <input type='text' name='totalFetch' value='1' size='10'> </td>" );
		out.println( "<td> Interval (ms): </td> " );
		out.println( "<td> <input type='text' name='intervalFetch' value='100' size='10'> </td>" );
		out.println( "<td> <input type='submit' name='sendFetch' value='Send Fetch Notification'> </td>" );
		out.println( "</tr>" );
		out.println( "</table>" );
		out.println( "<br>" );

		out.println( "<table border='0'>" );
		out.println( "<tr>" );
		out.println( "<td> Total (0=All): </td> " );
		out.println( "<td> <input type='text' name='totalDevicesRestart' value='0' size='10'> </td>" );
		out.println( "<td> <input type='submit' name='sendAllRestart' value='Send All Restart'> </td>" );
		out.println( "</tr>" );
		out.println( "</table>" );
		out.println( "<br>" );

		out.println( "<table border='0'>" );
		out.println( "<tr>" );
		out.println( "<td> Total (0=All): </td> " );
		out.println( "<td> <input type='text' name='totalDevicesFetch' value='0' size='10'> </td>" );
		out.println( "<td> <input type='submit' name='sendAllFetch' value='Send All Fetch'> </td>" );
		out.println( "</tr>" );
		out.println( "</table>" );
		out.println( "<br>" );

		out.println( "<table border='0'>" );
		out.println( "<tr>" );
		out.println( "<td>" );
		out.println( "<select name='blockSetting'>" );
		out.println( "<option value='ON'>ON</option>" );
		out.println( "<option value='OFF'>OFF</option>" );
		out.println( "</select>" );
		out.println( "</td>" );
		out.println( "<td> <input type='submit' name='blockDevice' value='Block Device'> </td>" );
		out.println( "</tr>" );
		out.println( "</table>" );
		out.println( "<br>" );

		out.println( "<table border='0'>" );
		out.println( "<tr>" );
		out.println( "<td> <input type='submit' name='invalidateSession' value='Invalidate Device Session'> </td>" );
		out.println( "</tr>" );
		out.println( "</table>" );
		out.println( "<br>" );

		out.println( "<table border='0'>" );
		out.println( "<tr>" );
		out.println( "<td> <input type='submit' name='checkOffline' value='Check Offline'> </td>" );
		out.println( "</tr>" );
		out.println( "</table>" );
		out.println( "<br>" );

		out.println( "<table border='0'>" );
		out.println( "<tr>" );
		out.println( "<td> Path: </td> " );
		out.println( "<td> <input type='text' name='path' value='' size='10'> </td>" );
		out.println( "<td> Source: </td> " );
		out.println( "<td> <input type='text' name='source' value='' size='10'> </td>" );
		out.println( "<td> Value: </td> " );
		out.println( "<td> <input type='text' name='value' value='' size='10'> </td>" );

		out.println( "<td> Info: </td> " );
		out.println( "<td> <input type='text' name='info' value='' size='25'> </td>" );
		out.println( "<td> <input type='checkbox' name='isDeleteEvent' value='isDeleteEvent'> Delete Event Type </td>" );
		out.println( "<td> <input type='submit' name='sendCustomEvent' value='Send Custom Event'> </td>" );
		out.println( "</tr>" );
		out.println( "</table>" );
		out.println( "<br>" );

		out.println( "<table border='0'>" );
		out.println( "<tr>" );
		out.println( "<td> Time Delta (min): </td> " );
		out.println( "<td> <input type='text' name='timeDelta' value='120' size='10'> </td>" );
		out.println( "<td> <input type='submit' name='updateTimeDelta' value='Update Time Delta'> </td>" );
		out.println( "</tr>" );
		out.println( "</table>" );
		out.println( "<br>" );

		out.println( "<input type='submit' name='refresh' value=Refresh>" );
		out.println( "</form>" );

		out.println( "<h4>Status: " + status + "</h4>" );

		out.println( "</body></html>" );
	}
}
