package com.marchnetworks.alarm.data;

import com.marchnetworks.alarm.alarmdetails.AlarmDetailEnum;

import java.util.Set;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlType( name = "alarmEntry" )
public class AlarmEntryView
{
	private String id;
	private String deviceAlarmEntryId;
	private String alarmSourceId;
	private long firstInstanceTime;
	private long lastInstanceTime;
	private int count;
	private long closedTime;
	private String closedByUser;
	private String closedText;
	private String[] handlingUsers;
	private boolean isOpen = true;

	private String[] associatedChannels;

	private Set<AlarmDetailEnum> alarmDetails;

	public AlarmEntryView()
	{
	}

	public AlarmEntryView( String id, String deviceAlarmEntryId, String alarmSourceId, long firstInstanceTime, long lastInstanceTime, int count, long closedTime, String closedByUser, String closedText, Set<String> handlingUsers, Set<String> associatedChannels, Set<AlarmDetailEnum> alarmDetails )
	{
		this.id = id;
		this.deviceAlarmEntryId = deviceAlarmEntryId;
		this.alarmSourceId = alarmSourceId;
		this.firstInstanceTime = firstInstanceTime;
		this.lastInstanceTime = lastInstanceTime;
		this.count = count;
		this.closedTime = closedTime;
		this.closedByUser = closedByUser;
		this.closedText = closedText;
		this.handlingUsers = ( ( String[] ) handlingUsers.toArray( new String[handlingUsers.size()] ) );
		isOpen = ( closedTime == 0L );
		this.associatedChannels = ( ( String[] ) associatedChannels.toArray( new String[associatedChannels.size()] ) );
		this.alarmDetails = alarmDetails;
	}

	public String getId()
	{
		return id;
	}

	public void setId( String id )
	{
		this.id = id;
	}

	public String getDeviceAlarmEntryId()
	{
		return deviceAlarmEntryId;
	}

	public String getAlarmSourceId()
	{
		return alarmSourceId;
	}

	public void setAlarmSourceId( String alarmSourceId )
	{
		this.alarmSourceId = alarmSourceId;
	}

	public long getFirstInstanceTime()
	{
		return firstInstanceTime;
	}

	public void setFirstInstanceTime( long firstInstanceTime )
	{
		this.firstInstanceTime = firstInstanceTime;
	}

	public long getLastInstanceTime()
	{
		return lastInstanceTime;
	}

	public void setLastInstanceTime( long lastInstanceTime )
	{
		this.lastInstanceTime = lastInstanceTime;
	}

	public int getCount()
	{
		return count;
	}

	public void setCount( int count )
	{
		this.count = count;
	}

	public long getClosedTime()
	{
		return closedTime;
	}

	public void setClosedTime( long closedTime )
	{
		this.closedTime = closedTime;
		isOpen = false;
	}

	public String getClosedByUser()
	{
		return closedByUser;
	}

	public void setClosedByUser( String closedByUser )
	{
		this.closedByUser = closedByUser;
	}

	public String getClosedText()
	{
		return closedText;
	}

	public void setClosedText( String closedText )
	{
		this.closedText = closedText;
	}

	public String[] getHandlingUsers()
	{
		return handlingUsers;
	}

	public void setHandlingUsers( String[] handlingUsers )
	{
		this.handlingUsers = handlingUsers;
	}

	public boolean getIsOpen()
	{
		return isOpen;
	}

	public void setIsOpen( boolean isOpen )
	{
		this.isOpen = isOpen;
	}

	public String[] getAssociatedChannels()
	{
		return associatedChannels;
	}

	public void setAssociatedChannels( String[] associatedChannels )
	{
		this.associatedChannels = associatedChannels;
	}

	@XmlTransient
	public long getClosedTimeInMillis()
	{
		return closedTime / 1000L;
	}

	public Set<AlarmDetailEnum> getAlarmDetails()
	{
		return alarmDetails;
	}

	public void setAlarmDetails( Set<AlarmDetailEnum> alarmDetails )
	{
		this.alarmDetails = alarmDetails;
	}
}
