package com.marchnetworks.health.task;

import com.marchnetworks.command.common.device.data.ConnectState;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.transport.datamodel.DeviceException;
import com.marchnetworks.common.types.DeviceExceptionTypes;
import com.marchnetworks.health.data.AlertConfigData;
import com.marchnetworks.health.data.AlertThresholdData;
import com.marchnetworks.health.data.ThresholdNotificationData;
import com.marchnetworks.management.instrumentation.DeviceService;
import com.marchnetworks.management.instrumentation.events.DeviceSetAlertConfigEvent;
import com.marchnetworks.management.instrumentation.pooling.DeferredEvent;
import com.marchnetworks.management.instrumentation.pooling.DeferredEventPool;
import com.marchnetworks.server.communications.transport.datamodel.AlertConfig;
import com.marchnetworks.server.communications.transport.datamodel.AlertThreshold;
import com.marchnetworks.server.communications.transport.datamodel.AlertThresholdNotification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AlertThresholdSetTask implements Runnable
{
	private static final Logger LOG = LoggerFactory.getLogger( AlertThresholdSetTask.class );
	private Long deviceId;
	private AlertConfigData alertConfigData;
	private boolean forceUpdate = false;

	public AlertThresholdSetTask( Long deviceId, AlertConfigData alertConfigData, boolean forceUpdate )
	{
		this.deviceId = deviceId;
		this.alertConfigData = alertConfigData;
		this.forceUpdate = forceUpdate;
	}

	public void run()
	{
		if ( alertConfigData == null )
		{
			LOG.debug( "No alert config provided to send to device. Aborting Task" );
			return;
		}

		AlertConfig transportAlertConfig = new AlertConfig();
		Collection<AlertThresholdData> alertThresholdDataResult = alertConfigData.getThresholds();

		List<AlertThreshold> transportThresholds = new ArrayList();
		AlertThreshold transportThreshold = null;

		for ( AlertThresholdData alertThresoldData : alertThresholdDataResult )
		{
			transportThreshold = new AlertThreshold();
			transportThreshold.setAlertCode( alertThresoldData.getAlertCode() );
			transportThreshold.setDurationSec( alertThresoldData.getDuration() );
			transportThreshold.setFrequencyCount( alertThresoldData.getFrequencyCount() );
			transportThreshold.setFrequencySec( alertThresoldData.getFrequencyDuration() );
			transportThreshold.setNotificationType( Convert( alertThresoldData.getNotificationType() ) );

			transportThresholds.add( transportThreshold );
		}
		transportAlertConfig.setThresholds( ( AlertThreshold[] ) transportThresholds.toArray( new AlertThreshold[transportThresholds.size()] ) );
		transportAlertConfig.setId( String.valueOf( alertConfigData.getVersion() ) );
		try
		{
			getDeviceService().setAlertConfig( deviceId.toString(), transportAlertConfig, forceUpdate );
		}
		catch ( DeviceException e )
		{
			LOG.warn( "Error when trying to push the Alert Thresholds to device {} . Cause : {}", new Object[] {deviceId, e.getMessage()} );
			if ( e.getDetailedErrorType() != DeviceExceptionTypes.FEATURE_NOT_SUPPORTED )
			{
				DeferredEvent de = new DeferredEvent( new DeviceSetAlertConfigEvent( deviceId.toString() ), ConnectState.ONLINE.toString(), 172800000L );
				getDeferredEventPool().add( deviceId.toString(), de );
			}
		}
	}

	private AlertThresholdNotification Convert( ThresholdNotificationData data )
	{
		if ( data == ThresholdNotificationData.ALWAYS )
			return AlertThresholdNotification.ALWAYS;
		if ( data == ThresholdNotificationData.DURATION )
			return AlertThresholdNotification.DURATION;
		if ( data == ThresholdNotificationData.FREQUENCY )
			return AlertThresholdNotification.FREQUENCY;
		if ( data == ThresholdNotificationData.FREQUENCYORDURATION )
			return AlertThresholdNotification.FREQUENCY_AND_DURATION;
		return AlertThresholdNotification.NEVER;
	}

	private DeviceService getDeviceService()
	{
		return ( DeviceService ) ApplicationContextSupport.getBean( "deviceServiceProxy" );
	}

	private DeferredEventPool getDeferredEventPool()
	{
		return ( DeferredEventPool ) ApplicationContextSupport.getBean( "deferredEventPool" );
	}
}
