package com.marchnetworks.alarm.model;

import com.google.gson.reflect.TypeToken;
import com.marchnetworks.alarm.alarmdetails.AlarmDetailEnum;
import com.marchnetworks.alarm.data.AlarmEntryView;
import com.marchnetworks.alarm.data.DeletedSourceAlarmEntry;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.alarm.data.AlarmSourceView;
import com.marchnetworks.common.device.DeletedDevice;
import com.marchnetworks.common.device.DeletedDeviceData;
import com.marchnetworks.common.serialization.CoreJsonSerializer;
import com.marchnetworks.common.utils.CommonUtils;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.persistence.Version;

@Entity
@org.hibernate.annotations.Table( appliesTo = "ALARM_ENTRY", indexes = {@org.hibernate.annotations.Index( name = "LAST_INSTANCE_INDEX", columnNames = {"LAST_INSTANCE_TIME"} )} )
@javax.persistence.Table( name = "ALARM_ENTRY" )
public class AlarmEntryEntity
{
	@Id
	@GeneratedValue
	@Column( name = "ID" )
	private Long id;
	@Version
	@Column( name = "VERSION" )
	private Long version;
	@Column( name = "DEVICE_ALARM_ENTRY_ID" )
	private String deviceAlarmEntryId;
	@Column( name = "FIRST_INSTANCE_TIME" )
	private long firstInstanceTime;
	@Column( name = "LAST_INSTANCE_TIME" )
	private long lastInstanceTime;
	@Column( name = "COUNT" )
	private int count;
	@Column( name = "CLOSED_TIME" )
	private long closedTime;
	@Column( name = "CLOSED_BY_USER" )
	private String closedByUser;
	@Column( name = "CLOSED_TEXT", length = 4000 )
	private String closedText;
	@Column( name = "DEVICE_RECONCILIATION_STATE" )
	private boolean reconciledWithDevice = false;

	@Column( name = "HANDLING_USERS", length = 4000 )
	private String handlingUsersString;

	@Transient
	private Set<String> handlingUsers;

	@Lob
	@Column( name = "ASSOCIATED_CHANNELS" )
	private byte[] associatedChannelsString;

	@Transient
	private Set<String> associatedChannels;

	@ManyToOne
	@JoinColumn( name = "ALARM_SOURCE", nullable = false )
	private AlarmSourceEntity alarmSource;

	@Column( name = "ALARM_DETAILS", length = 100 )
	String alarmDetailsString;

	public AlarmEntryEntity()
	{
	}

	public AlarmEntryEntity( String deviceAlarmEntryID, AlarmSourceEntity alarmSource )
	{
		deviceAlarmEntryId = deviceAlarmEntryID;
		this.alarmSource = alarmSource;
	}

	public AlarmEntryView toDataObject()
	{
		if ( alarmSource.isDeleted() )
		{
			AlarmSourceView deletedAlarmSource = alarmSource.toDataObject();
			DeletedDevice deletedDevice = alarmSource.getDeletedDevice();

			DeletedDeviceData deletedDeviceData = null;
			if ( deletedDevice != null )
			{
				deletedDeviceData = deletedDevice.toDataObject();
			}

			DeletedSourceAlarmEntry alarmData = new DeletedSourceAlarmEntry( id.toString(), deviceAlarmEntryId, firstInstanceTime, lastInstanceTime, count, closedTime, closedByUser, closedText, getHandlingUsers(), getAssociatedChannels(), deletedAlarmSource, deletedDeviceData, getAlarmDetails() );

			return alarmData;
		}

		AlarmEntryView alarmData = new AlarmEntryView( id.toString(), deviceAlarmEntryId, alarmSource.getIdAsString(), firstInstanceTime, lastInstanceTime, count, closedTime, closedByUser, closedText, getHandlingUsers(), getAssociatedChannels(), getAlarmDetails() );

		return alarmData;
	}

	public Set<String> getHandlingUsers()
	{
		if ( handlingUsers == null )
		{
			handlingUsers = CommonUtils.jsonToStringSet( handlingUsersString );
		}
		return handlingUsers;
	}

	public Set<String> getAssociatedChannels()
	{
		if ( associatedChannels == null )
		{
			associatedChannels = CommonUtils.jsonToStringSet( getAssociatedChannelsString() );
		}
		return associatedChannels;
	}

	public void setAssociatedChannels( Set<String> associatedChannelsSet )
	{
		if ( associatedChannels == null )
		{
			associatedChannels = new LinkedHashSet();
		}
		setAssociatedChannelsString( CommonUtils.setToJson( associatedChannels, associatedChannelsSet ) );
	}

	public void clearAssociatedChannels()
	{
		if ( associatedChannels != null )
		{
			associatedChannels.clear();
		}
		associatedChannelsString = null;
	}

	public boolean addToHandlingUsers( String handlingUser )
	{
		Set<String> handlingUsersSet = getHandlingUsers();
		boolean added = handlingUsersSet.add( handlingUser );
		if ( added )
		{
			handlingUsersString = CoreJsonSerializer.toJson( handlingUsersSet );
		}
		return added;
	}

	public boolean removeFromHandlingUsers( String handlingUser )
	{
		Set<String> handlingUsersSet = getHandlingUsers();
		boolean removed = handlingUsersSet.remove( handlingUser );
		if ( removed )
		{
			handlingUsersString = CoreJsonSerializer.toJson( handlingUsersSet );
		}
		return removed;
	}

	public boolean addToAssociatedChannels( String channel )
	{
		Set<String> associatedChannelsSet = getAssociatedChannels();
		boolean added = associatedChannelsSet.add( channel );
		if ( added )
		{
			setAssociatedChannelsString( CoreJsonSerializer.toJson( associatedChannelsSet ) );
		}
		return added;
	}

	public boolean isOpen()
	{
		return closedTime == 0L;
	}

	public Long getAlarmSourceID()
	{
		return alarmSource.getId();
	}

	public String getAlarmSourceIDAsString()
	{
		return alarmSource.getIdAsString();
	}

	public Long getId()
	{
		return id;
	}

	public String getDeviceAlarmEntryID()
	{
		return deviceAlarmEntryId;
	}

	public void setDeviceAlarmEntryID( String deviceAlarmEntryID )
	{
		deviceAlarmEntryId = deviceAlarmEntryID;
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

	protected String getHandlingUsersString()
	{
		return handlingUsersString;
	}

	protected void setHandlingUsersString( String handlingUsers )
	{
		handlingUsersString = handlingUsers;
	}

	public AlarmSourceEntity getAlarmSource()
	{
		return alarmSource;
	}

	public void setAlarmSource( AlarmSourceEntity alarmSource )
	{
		this.alarmSource = alarmSource;
	}

	public Long getVersion()
	{
		return version;
	}

	public void setVersion( Long version )
	{
		this.version = version;
	}

	protected String getAssociatedChannelsString()
	{
		return CommonAppUtils.encodeToUTF8String( associatedChannelsString );
	}

	protected void setAssociatedChannelsString( String associatedChannels )
	{
		associatedChannelsString = CommonAppUtils.encodeStringToBytes( associatedChannels );
	}

	public boolean isReconciledWithDevice()
	{
		return reconciledWithDevice;
	}

	public void setReconciledWithDevice( boolean reconciledWithDevice )
	{
		this.reconciledWithDevice = reconciledWithDevice;
	}

	public Set<AlarmDetailEnum> getAlarmDetails()
	{
		if ( alarmDetailsString != null )
		{

			Set<AlarmDetailEnum> details = new HashSet();

			Set<String> jsonStringSet = CoreJsonSerializer.collectionFromJson( alarmDetailsString, new TypeToken<Set<String>>()
			{
			} );

			for ( String json : jsonStringSet )
			{
				details.add( getenumFromValue( json ) );
			}
			return details;
		}

		return new HashSet( 0 );
	}

	public void setAlarmDetails( Set<AlarmDetailEnum> alarmDetails )
	{
		if ( ( alarmDetails == null ) || ( alarmDetails.isEmpty() ) )
		{
			alarmDetailsString = null;
		}
		else
		{
			Set<String> alarmDetailCharacters = new HashSet();

			for ( AlarmDetailEnum detail : alarmDetails )
			{
				alarmDetailCharacters.add( detail.getValue() );
			}
			alarmDetailsString = CoreJsonSerializer.toJson( alarmDetailCharacters );
		}
	}

	public String getAlarmDetailsString()
	{
		return alarmDetailsString;
	}

	public void setAlarmDetailsString( String alarmDetailsString )
	{
		this.alarmDetailsString = alarmDetailsString;
	}

	private AlarmDetailEnum getenumFromValue( String value )
	{
		if ( value.equals( "a" ) )
			return AlarmDetailEnum.PARTY_INVOLVED_POLICE;
		if ( value.equals( "b" ) )
			return AlarmDetailEnum.PARTY_INVOLVED_FIRE;
		if ( value.equals( "c" ) )
			return AlarmDetailEnum.PARTY_INVOLVED_AMBULANCE;
		if ( value.equals( "d" ) )
			return AlarmDetailEnum.PARTY_INVOLVED_OTHER;
		if ( value.equals( "e" ) )
			return AlarmDetailEnum.INCIDENT_SHOPLIFTING;
		if ( value.equals( "f" ) )
			return AlarmDetailEnum.INCIDENT_LOITERING;
		if ( value.equals( "g" ) )
			return AlarmDetailEnum.INCIDENT_PERMANENCY;
		if ( value.equals( "h" ) )
			return AlarmDetailEnum.INCIDENT_PANIC;
		if ( value.equals( "i" ) )
			return AlarmDetailEnum.INCIDENT_FALL;
		if ( value.equals( "j" ) )
			return AlarmDetailEnum.INCIDENT_SUSPICIOUS;
		if ( value.equals( "k" ) )
			return AlarmDetailEnum.INCIDENT_VANDALISM;
		if ( value.equals( "l" ) )
			return AlarmDetailEnum.SEVERITY_UNSPECIFIED;
		if ( value.equals( "m" ) )
			return AlarmDetailEnum.SEVERITY_CRITICAL;
		if ( value.equals( "n" ) )
			return AlarmDetailEnum.SEVERITY_SEVERE;
		if ( value.equals( "o" ) )
			return AlarmDetailEnum.SEVERITY_MINOR;
		if ( value.equals( "p" ) )
			return AlarmDetailEnum.SEVERITY_FALSE;
		if ( value.equals( "q" ) )
			return AlarmDetailEnum.VICTIM_UNSPECIFIED;
		if ( value.equals( "r" ) )
			return AlarmDetailEnum.VICTIM_EMPLOYEE;
		if ( value.equals( "s" ) )
			return AlarmDetailEnum.VICTIM_CUSTOMER;
		if ( value.equals( "t" ) )
			return AlarmDetailEnum.VICTIM_OTHER;
		if ( value.equals( "u" ) )
		{
			return AlarmDetailEnum.VICTIM_NONE;
		}
		return null;
	}
}
