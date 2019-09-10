package com.marchnetworks.health.event;

import com.marchnetworks.command.common.device.data.RegistrationStatus;
import com.marchnetworks.health.input.DeviceAlertInput;
import com.marchnetworks.health.service.HealthServiceIF;
import com.marchnetworks.management.instrumentation.events.AbstractDeviceEvent;
import com.marchnetworks.management.instrumentation.events.ChildDeviceRegistrationEvent;
import com.marchnetworks.management.instrumentation.events.DeviceAlertClosureDispatchEvent;
import com.marchnetworks.management.instrumentation.events.DeviceAlertEvent;
import com.marchnetworks.management.instrumentation.events.DeviceAlertEventType;
import com.marchnetworks.management.instrumentation.events.DeviceChannelRemovedEvent;
import com.marchnetworks.management.instrumentation.events.DeviceHealthReconciliationEvent;
import com.marchnetworks.management.instrumentation.events.DeviceRegistrationEvent;
import com.marchnetworks.management.instrumentation.events.DeviceSetAlertConfigEvent;
import com.marchnetworks.server.event.EventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HealthEventHandler implements EventListener
{
	private static Logger LOG = LoggerFactory.getLogger( HealthEventHandler.class );

	private HealthServiceIF healthService;

	public String getListenerName()
	{
		return HealthEventHandler.class.getSimpleName();
	}

	public void process( com.marchnetworks.common.event.Event event )
	{
		LOG.debug( "process: event={}", event );

		AbstractDeviceEvent deviceEvent = ( AbstractDeviceEvent ) event;

		if ( ( deviceEvent instanceof DeviceRegistrationEvent ) )
		{
			DeviceRegistrationEvent registrationEvent = ( DeviceRegistrationEvent ) deviceEvent;
			RegistrationStatus status = registrationEvent.getRegistrationStatus();

			String deviceId = deviceEvent.getDeviceId();
			if ( status == RegistrationStatus.REGISTERED )
			{
				healthService.processDeviceRegistered( deviceId, registrationEvent.isMassRegistration() );
			}
			else if ( status == RegistrationStatus.UNREGISTERED )
			{
				healthService.processDeviceUnregistered( deviceId );
			}
		}
		else if ( ( deviceEvent instanceof ChildDeviceRegistrationEvent ) )
		{
			RegistrationStatus status = ( ( ChildDeviceRegistrationEvent ) deviceEvent ).getRegistrationStatus();
			if ( status == RegistrationStatus.UNREGISTERED )
			{
				healthService.processDeviceUnregistered( deviceEvent.getDeviceId() );
			}
		}
		else if ( ( deviceEvent instanceof DeviceChannelRemovedEvent ) )
		{
			DeviceChannelRemovedEvent channelEvent = ( DeviceChannelRemovedEvent ) deviceEvent;
			healthService.processDeviceChannelRemoved( channelEvent.getDeviceId(), channelEvent.getChannelId() );
		}
		else if ( ( deviceEvent instanceof DeviceHealthReconciliationEvent ) )
		{
			DeviceHealthReconciliationEvent reconcilliationEvent = ( DeviceHealthReconciliationEvent ) deviceEvent;
			healthService.processDeviceReconciliation( reconcilliationEvent.getDeviceId() );
		}
		else if ( ( deviceEvent instanceof DeviceAlertClosureDispatchEvent ) )
		{
			DeviceAlertClosureDispatchEvent closuresDispatchEvent = ( DeviceAlertClosureDispatchEvent ) deviceEvent;
			healthService.processAlertClosureDispatch( closuresDispatchEvent.getDeviceId() );
		}
		else if ( ( deviceEvent instanceof DeviceSetAlertConfigEvent ) )
		{
			DeviceSetAlertConfigEvent setAlertConfigEvent = ( DeviceSetAlertConfigEvent ) deviceEvent;
			healthService.processSetAlertConfig( setAlertConfigEvent.getDeviceId() );
		}
		else if ( ( deviceEvent instanceof DeviceAlertEvent ) )
		{
			DeviceAlertEvent alertEvent = ( DeviceAlertEvent ) deviceEvent;

			if ( alertEvent.getDeviceAlertEventType().equals( DeviceAlertEventType.ALERT_CLOSED ) )
			{
				String deviceId = alertEvent.getDeviceId();
				String deviceAlertId = alertEvent.getAlertEvent().getSource();
				healthService.processDeviceAlertClosure( deviceId, deviceAlertId );
			}
			else
			{
				DeviceAlertInput deviceAlertInput = healthService.createDeviceHealthAlert( alertEvent.getDeviceId(), alertEvent.getAlertEvent(), alertEvent.getDeviceAlertEventType() != DeviceAlertEventType.LEGACY );

				if ( deviceAlertInput == null )
				{
					LOG.warn( "Unable to process device alert." );
					return;
				}

				healthService.processHealthAlert( deviceAlertInput );
			}
		}
	}

	public void setHealthService( HealthServiceIF healthService )
	{
		this.healthService = healthService;
	}
}
