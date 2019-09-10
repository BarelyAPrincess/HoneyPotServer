package com.marchnetworks.management.instrumentation.adaptation;

import com.marchnetworks.command.api.metrics.MetricsCoreService;
import com.marchnetworks.command.common.scheduling.TaskScheduler;
import com.marchnetworks.common.diagnostics.metrics.MetricsHelper;
import com.marchnetworks.common.diagnostics.metrics.MetricsTypes;
import com.marchnetworks.management.instrumentation.events.AbstractDeviceEvent;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceEventHandlerScheduler
{
	private static final Logger LOG = LoggerFactory.getLogger( DeviceEventHandlerScheduler.class );

	private TaskScheduler taskScheduler;

	private DeviceEventHandler deviceEventHandler;

	public void scheduleDeviceEventHandling( String deviceId, List<AbstractDeviceEvent> notifications )
	{
		for ( AbstractDeviceEvent deviceNotification : notifications )
		{
			DeviceEventHandlerTask task = new DeviceEventHandlerTask( deviceId, deviceEventHandler, deviceNotification );
			taskScheduler.executeFixedPoolSerial( task, deviceId );
		}
	}

	private class DeviceEventHandlerTask implements Runnable
	{
		private AbstractDeviceEvent event;
		private DeviceEventHandler handler;
		private String deviceId;

		public DeviceEventHandlerTask( String deviceId, DeviceEventHandler handler, AbstractDeviceEvent event )
		{
			this.event = event;
			this.handler = handler;
			this.deviceId = deviceId;
		}

		public void run()
		{
			try
			{
				long start = System.currentTimeMillis();
				handler.handleEvent( deviceId, event );
				String name = event.getEventType();
				name = name.substring( name.lastIndexOf( "." ) + 1, name.length() );
				MetricsHelper.metrics.addBucketMinMaxAvg( MetricsTypes.DEVICE_EVENT_HANDLER.getName(), name, System.currentTimeMillis() - start );
				DeviceEventHandlerScheduler.LOG.debug( "Processed event {}", event );
			}
			catch ( Exception ex )
			{
				DeviceEventHandlerScheduler.LOG.debug( "Error processing notification " + event, ex );
			}
		}
	}

	public TaskScheduler getTaskScheduler()
	{
		return taskScheduler;
	}

	public void setTaskScheduler( TaskScheduler taskScheduler )
	{
		this.taskScheduler = taskScheduler;
	}

	public DeviceEventHandler getDeviceEventHandler()
	{
		return deviceEventHandler;
	}

	public void setDeviceEventHandler( DeviceEventHandler deviceEventHandler )
	{
		this.deviceEventHandler = deviceEventHandler;
	}
}

