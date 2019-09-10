package com.marchnetworks.alarm.events;

import com.marchnetworks.alarm.service.AlarmService;
import com.marchnetworks.command.common.device.data.RegistrationStatus;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.management.instrumentation.events.DeviceAlarmClosuresDispatchEvent;
import com.marchnetworks.management.instrumentation.events.DeviceAlarmEvent;
import com.marchnetworks.management.instrumentation.events.DeviceAlarmReconciliationEvent;
import com.marchnetworks.management.instrumentation.events.DeviceRegistrationEvent;
import com.marchnetworks.server.event.EventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlarmEventHandler implements EventListener
{
	private static final Logger LOG = LoggerFactory.getLogger( AlarmEventHandler.class );
	private AlarmService alarmService;

	public String getListenerName()
	{
		return AlarmEventHandler.class.getSimpleName();
	}

	public void process( Event aEvent )
	{
		if ( ( aEvent instanceof DeviceAlarmEvent ) )
		{

			if ( !alarmService.getAlarmsEnabled() )
			{
				LOG.info( "Alarms features are disabled. Ignoring alarm event sent from device {}", aEvent.getEventType() );
				return;
			}
			DeviceAlarmEvent alarmEvent = ( DeviceAlarmEvent ) aEvent;
			alarmService.processAlarmEvent( alarmEvent );
		}
		else if ( ( aEvent instanceof DeviceRegistrationEvent ) )
		{
			DeviceRegistrationEvent deviceRegistrationEvent = ( DeviceRegistrationEvent ) aEvent;
			RegistrationStatus status = deviceRegistrationEvent.getRegistrationStatus();

			if ( status == RegistrationStatus.UNREGISTERED )
			{
				alarmService.processDeviceUnregistered( deviceRegistrationEvent.getDeviceId() );
			}
			else if ( status == RegistrationStatus.REGISTERED )
			{
				long start = System.currentTimeMillis();
				alarmService.processDeviceRegistered( deviceRegistrationEvent.getDeviceId() );
				long end = System.currentTimeMillis();
				LOG.debug( "Post-Registration execution time for AlarmEventHandler: " + ( end - start ) + " ms." );
			}
		}
		else if ( ( aEvent instanceof DeviceAlarmReconciliationEvent ) )
		{
			DeviceAlarmReconciliationEvent alarmReconcileEvent = ( DeviceAlarmReconciliationEvent ) aEvent;
			alarmService.processAlarmReconciliationWithDevice( alarmReconcileEvent.getDeviceId() );
		}
		else if ( ( aEvent instanceof DeviceAlarmClosuresDispatchEvent ) )
		{
			DeviceAlarmClosuresDispatchEvent alarmClosureDispatchEvent = ( DeviceAlarmClosuresDispatchEvent ) aEvent;
			alarmService.processAlarmClosureDispatch( alarmClosureDispatchEvent.getDeviceId() );
		}
	}

	public void setAlarmService( AlarmService alarmService )
	{
		this.alarmService = alarmService;
	}
}
