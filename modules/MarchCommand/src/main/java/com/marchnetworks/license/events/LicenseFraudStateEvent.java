package com.marchnetworks.license.events;

import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.command.api.event.EventNotification.Builder;
import com.marchnetworks.common.event.EventTypesEnum;
import com.marchnetworks.common.event.StateCacheable;

public class LicenseFraudStateEvent extends LicenseExternalEvent implements StateCacheable
{
	protected int m_iCode;
	protected int m_iRemaining;

	public LicenseFraudStateEvent( int fraudState )
	{
		super( LicenseExternalEvent.class.getName() );

		if ( fraudState < 0 )
		{
			m_iCode = 0;
		}
		else
		{
			m_iCode = 1;
		}
		m_iRemaining = fraudState;
	}

	public String getEventNotificationType()
	{
		return EventTypesEnum.LICENSE_NAG_SCREEN.getFullPathEventName();
	}

	public EventNotification getNotificationInfo()
	{
		if ( m_iCode > 0 )
		{
			return new Builder( getEventNotificationType() ).source( "" ).value( m_iCode ).info( "remaining", Integer.toString( m_iRemaining ) ).build();
		}

		return new Builder( getEventNotificationType() ).source( "" ).value( m_iCode ).build();
	}

	public Long getDeviceIdLong()
	{
		return StateCacheable.CES_EVENT;
	}

	public long getTimestamp()
	{
		return 0L;
	}

	public boolean isDeleteEvent()
	{
		return false;
	}
}
