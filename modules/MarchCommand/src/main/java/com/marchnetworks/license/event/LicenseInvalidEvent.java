package com.marchnetworks.license.event;

import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.command.api.event.EventNotification.Builder;
import com.marchnetworks.common.event.StateCacheable;

public class LicenseInvalidEvent extends LicenseEvent implements StateCacheable
{
	private static final String LICENSE_NAME = "licenseName";
	protected int remainingDays;
	protected boolean isDeleteEvent;
	private String licenseName;

	public LicenseInvalidEvent()
	{
		super( LicenseInvalidEvent.class.getName(), LicenseEventType.INVALID );
	}

	public LicenseInvalidEvent( int remainingDays, String licenseName )
	{
		super( LicenseInvalidEvent.class.getName(), LicenseEventType.INVALID );
		this.remainingDays = remainingDays;
		this.licenseName = licenseName;
	}

	public LicenseInvalidEvent( int remainingDays, boolean isDeleteEvent )
	{
		super( LicenseInvalidEvent.class.getName(), LicenseEventType.INVALID );
		this.remainingDays = remainingDays;
		this.isDeleteEvent = isDeleteEvent;
	}

	public EventNotification getNotificationInfo()
	{
		Builder builder = new Builder( getEventNotificationType() ).source( "" ).value( Integer.valueOf( remainingDays ) ).info( "licenseName", licenseName );

		EventNotification en = builder.build();
		return en;
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
		return isDeleteEvent;
	}

	public void setRemainingDays( int remainingDays )
	{
		this.remainingDays = remainingDays;
	}

	public String getLicenseName()
	{
		return licenseName;
	}

	public void setLicenseName( String licenseName )
	{
		this.licenseName = licenseName;
	}
}
