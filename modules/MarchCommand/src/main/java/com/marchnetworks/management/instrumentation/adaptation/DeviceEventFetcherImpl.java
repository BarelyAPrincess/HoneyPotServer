package com.marchnetworks.management.instrumentation.adaptation;

import com.marchnetworks.command.api.metrics.MetricsCoreService;
import com.marchnetworks.command.common.scheduling.TaskScheduler;
import com.marchnetworks.common.diagnostics.metrics.MetricsHelper;
import com.marchnetworks.common.diagnostics.metrics.MetricsTypes;
import com.marchnetworks.management.instrumentation.data.RegistrationState;
import com.marchnetworks.management.instrumentation.events.AbstractDeviceEvent;
import com.marchnetworks.management.instrumentation.events.DeviceRestartEvent;
import com.marchnetworks.management.instrumentation.task.DeviceFetchEventsTask;
import com.marchnetworks.management.instrumentation.task.DeviceSubscribeEventsTask;
import com.marchnetworks.security.device.DeviceSessionHolderService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceEventFetcherImpl implements DeviceEventFetcher
{
	private static final Logger LOG = LoggerFactory.getLogger( DeviceEventFetcherImpl.class );
	private DeviceSessionHolderService deviceSessionHolder;
	private TaskScheduler taskScheduler;
	private DeviceEventHandlerScheduler deviceEventHandlerScheduler;

	public boolean processFetchNotification( Map<String, String> notificationParams )
	{
		String type = ( String ) notificationParams.get( "type" );
		String assignedDeviceId = ( String ) notificationParams.get( "deviceId" );
		String subscriptionId = ( String ) notificationParams.get( "id" );
		String requestRemoteAddress = ( String ) notificationParams.get( "deviceRemoteAddress" );
		boolean hasEvents = notificationParams.get( "noCB" ) == null;

		RegistrationState registrationState = new RegistrationState( "", "", assignedDeviceId );
		String deviceId = registrationState.getDeviceId();
		long deviceTimestamp = registrationState.getDeviceCreateTime();

		LOG.debug( "type={}", type );
		LOG.debug( "deviceId={}", deviceId );
		LOG.debug( "subscriptionId={}", subscriptionId );

		Map<String, Object> extraInfoMap = new HashMap( 1 );
		extraInfoMap.put( "deviceRemoteAddress", requestRemoteAddress );
		extraInfoMap.put( "deviceTimestamp", Long.valueOf( deviceTimestamp ) );

		if ( "restart".equals( type ) )
		{
			LOG.info( "DeviceId={} restart notification received", deviceId );

			deviceSessionHolder.invalidateAllDeviceSessions( deviceId );

			AbstractDeviceEvent restartEvent = new DeviceRestartEvent( deviceId );
			restartEvent.setDeviceExtraInfo( extraInfoMap );
			deviceEventHandlerScheduler.scheduleDeviceEventHandling( deviceId, Collections.singletonList( restartEvent ) );

			MetricsHelper.metrics.addCounter( MetricsTypes.DEVICE_RESTARTS.getName() );
		}
		else if ( "subscribe".equals( type ) )
		{
			LOG.info( "Received Agent Re-Subscribe notification for deviceId=" + deviceId );

			DeviceSubscribeEventsTask deviceSubscribeTask = new DeviceSubscribeEventsTask( deviceId, extraInfoMap );
			taskScheduler.executeFixedPoolSerial( deviceSubscribeTask, deviceId );

			MetricsHelper.metrics.addCounter( MetricsTypes.DEVICE_RESUBSCRIBE.getName() );
		}
		else if ( "test".equals( type ) )
		{
			LOG.debug( "Received ping from device {}", deviceId );
		}
		else
		{
			DeviceFetchEventsTask fetchEventsTask = new DeviceFetchEventsTask( deviceId, subscriptionId, requestRemoteAddress, deviceTimestamp, hasEvents );
			taskScheduler.executeFixedPoolSerial( fetchEventsTask, deviceId );

			MetricsHelper.metrics.addCounter( MetricsTypes.DEVICE_FETCH_EVENTS.getName() );
		}
		return true;
	}

	public void setDeviceSessionHolder( DeviceSessionHolderService deviceSessionHolder )
	{
		this.deviceSessionHolder = deviceSessionHolder;
	}

	public void setTaskScheduler( TaskScheduler taskScheduler )
	{
		this.taskScheduler = taskScheduler;
	}

	public void setDeviceEventHandlerScheduler( DeviceEventHandlerScheduler deviceEventHandlerScheduler )
	{
		this.deviceEventHandlerScheduler = deviceEventHandlerScheduler;
	}
}

