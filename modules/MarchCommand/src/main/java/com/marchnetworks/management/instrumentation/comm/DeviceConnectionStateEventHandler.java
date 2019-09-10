package com.marchnetworks.management.instrumentation.comm;

import com.marchnetworks.command.api.metrics.ApiMetricsTypes;
import com.marchnetworks.command.api.metrics.MetricsCoreService;
import com.marchnetworks.command.common.device.data.ConnectState;
import com.marchnetworks.common.diagnostics.metrics.MetricsHelper;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.management.instrumentation.DeviceRegistry;
import com.marchnetworks.management.instrumentation.events.DeviceConnectionStateEvent;
import com.marchnetworks.server.event.EventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceConnectionStateEventHandler implements EventListener
{
	private static final Logger LOG = LoggerFactory.getLogger( DeviceConnectionStateEventHandler.class );

	public void process( Event aEvent )
	{
		if ( ( aEvent instanceof DeviceConnectionStateEvent ) )
		{
			DeviceConnectionStateEvent connectionEvent = ( DeviceConnectionStateEvent ) aEvent;

			long start = System.currentTimeMillis();
			LOG.debug( "Processing connectionState event for device:{}, state: {}, connectionTime: {}", new Object[] {connectionEvent.getDeviceId(), connectionEvent.getConnectState(), connectionEvent.getLastConnectionTime()} );
			if ( connectionEvent.getConnectState() == ConnectState.ONLINE )
			{
				DeviceRegistry deviceRegistry = ( DeviceRegistry ) ApplicationContextSupport.getBean( "deviceRegistryProxy" );
				deviceRegistry.updateLastConnectionTime( connectionEvent.getDeviceId(), connectionEvent.getLastConnectionTime() );
				MetricsHelper.metrics.addBucketMinMaxAvg( ApiMetricsTypes.DEVICE_TASKS.getName(), "UpdateConnectionTime", System.currentTimeMillis() - start );
			}

			start = System.currentTimeMillis();

			DeviceRegistry deviceRegistry = ( DeviceRegistry ) ApplicationContextSupport.getBean( "deviceRegistry" );
			deviceRegistry.updateConnectState( connectionEvent.getDeviceId(), connectionEvent.getConnectState() );

			MetricsHelper.metrics.addBucketMinMaxAvg( ApiMetricsTypes.DEVICE_TASKS.getName(), "UpdateConnectionState", System.currentTimeMillis() - start );
		}
	}

	public String getListenerName()
	{
		return DeviceConnectionStateEventHandler.class.getSimpleName();
	}
}

