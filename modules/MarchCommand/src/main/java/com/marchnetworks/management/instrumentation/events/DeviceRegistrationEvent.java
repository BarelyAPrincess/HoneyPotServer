package com.marchnetworks.management.instrumentation.events;

import com.marchnetworks.command.api.event.AppNotifiable;
import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.command.api.event.EventNotification.Builder;
import com.marchnetworks.command.common.device.data.RegistrationStatus;
import com.marchnetworks.common.event.EventTypesEnum;
import com.marchnetworks.common.event.StateCacheable;
import com.marchnetworks.common.types.DeviceExceptionTypes;

import java.util.Set;

public class DeviceRegistrationEvent extends TerritoryAwareDeviceEvent implements StateCacheable, AppNotifiable
{
	private RegistrationStatus registrationStatus;
	private String deviceResourceId;
	private boolean isMassRegistration;

	public DeviceRegistrationEvent( String deviceResourceId, String deviceId, Set<Long> territoryInfo, RegistrationStatus registrationStatus, boolean isMassRegistration )
	{
		super( DeviceRegistrationEvent.class.getName(), deviceId, territoryInfo );
		this.registrationStatus = registrationStatus;
		this.deviceResourceId = deviceResourceId;
		this.isMassRegistration = isMassRegistration;
	}

	public EventNotification getNotificationInfo()
	{
		return new Builder( getEventNotificationType() ).source( getDeviceId() ).value( registrationStatus.name() ).info( "ErrorType", m_DeviceExceptionType != null ? m_DeviceExceptionType.toString() : null ).info( "CES_DEVICE_RESOURCE_ID", deviceResourceId ).build();
	}

	public String getEventNotificationType()
	{
		String eventNotificationType = "";

		if ( ( registrationStatus == RegistrationStatus.REGISTERED ) || ( registrationStatus == RegistrationStatus.ERROR_REGISTRATION ) || ( registrationStatus == RegistrationStatus.PENDING_REGISTRATION ) )
		{

			eventNotificationType = EventTypesEnum.DEVICE_REGISTRATION.getFullPathEventName();
		}
		else if ( registrationStatus == RegistrationStatus.UNREGISTERED )
		{
			eventNotificationType = EventTypesEnum.DEVICE_UNREGISTRATION.getFullPathEventName();
		}
		else if ( ( registrationStatus == RegistrationStatus.MARKED_FOR_REPLACEMENT ) || ( registrationStatus == RegistrationStatus.PENDING_REPLACEMENT ) || ( registrationStatus == RegistrationStatus.ERROR_REPLACEMENT ) )
		{

			eventNotificationType = EventTypesEnum.DEVICE_REPLACEMENT.getFullPathEventName();
		}

		return eventNotificationType;
	}

	public RegistrationStatus getRegistrationStatus()
	{
		return registrationStatus;
	}

	public Long getDeviceIdLong()
	{
		return Long.valueOf( getDeviceId() );
	}

	public Long getResourceId()
	{
		if ( deviceResourceId != null )
		{
			return Long.valueOf( deviceResourceId );
		}
		return null;
	}

	public boolean isDeleteEvent()
	{
		return false;
	}

	public boolean isMassRegistration()
	{
		return isMassRegistration;
	}
}

