package com.marchnetworks.health.task;

import com.marchnetworks.command.common.device.data.ConnectState;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.transport.datamodel.DeviceException;
import com.marchnetworks.common.types.DeviceExceptionTypes;
import com.marchnetworks.health.input.AlertInput;
import com.marchnetworks.health.service.HealthServiceIF;
import com.marchnetworks.management.instrumentation.DeviceService;
import com.marchnetworks.management.instrumentation.events.DeviceSetAlertConfigEvent;
import com.marchnetworks.management.instrumentation.pooling.DeferredEvent;
import com.marchnetworks.management.instrumentation.pooling.DeferredEventPool;
import com.marchnetworks.server.communications.transport.datamodel.AlertEntry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class AlertQueryTask implements Runnable
{
	private static final Logger LOG = LoggerFactory.getLogger( AlertClosureTask.class );

	private String deviceId;

	public AlertQueryTask( String deviceId )
	{
		this.deviceId = deviceId;
	}

	public void run()
	{
		try
		{
			List<AlertEntry> alerts = getDeviceService().getAlerts( deviceId );

			HealthServiceIF healthService = getHealthService();

			List<AlertInput> alertInputs = new ArrayList();
			for ( AlertEntry alertEntry : alerts )
			{
				alertInputs.add( healthService.createDeviceHealthAlert( deviceId, alertEntry ) );
			}

			healthService.processHealthAlerts( alertInputs );
		}
		catch ( DeviceException e )
		{
			LOG.warn( "Error when trying to query alerts from device {} . Cause : {}", new Object[] {deviceId, e.getMessage()} );
			if ( e.getDetailedErrorType() != DeviceExceptionTypes.FEATURE_NOT_SUPPORTED )
			{
				DeferredEvent de = new DeferredEvent( new DeviceSetAlertConfigEvent( deviceId.toString() ), ConnectState.ONLINE.toString() );
				getDeferredEventPool().add( deviceId.toString(), de );
			}
		}
	}

	private DeviceService getDeviceService()
	{
		return ( DeviceService ) ApplicationContextSupport.getBean( "deviceServiceProxy" );
	}

	private DeferredEventPool getDeferredEventPool()
	{
		return ( DeferredEventPool ) ApplicationContextSupport.getBean( "deferredEventPool" );
	}

	private HealthServiceIF getHealthService()
	{
		return ( HealthServiceIF ) ApplicationContextSupport.getBean( "healthServiceProxy_internal" );
	}
}
