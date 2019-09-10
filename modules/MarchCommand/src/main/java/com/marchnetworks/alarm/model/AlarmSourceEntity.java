package com.marchnetworks.alarm.model;

import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.alarm.data.AlarmExtendedState;
import com.marchnetworks.command.common.alarm.data.AlarmSourceView;
import com.marchnetworks.command.common.alarm.data.AlarmState;
import com.marchnetworks.common.device.DeletedDevice;
import com.marchnetworks.common.serialization.CoreJsonSerializer;
import com.marchnetworks.common.utils.CommonUtils;
import com.marchnetworks.server.communications.transport.datamodel.AlarmSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

@Entity
@Table( name = "ALARM_SOURCE" )
public class AlarmSourceEntity implements AlarmSourceMBean
{
	@Id
	@GeneratedValue
	@Column( name = "ID" )
	private Long id;
	@Version
	@Column( name = "VERSION" )
	private Long version;
	@Column( name = "DEVICE_ALARM_SOURCE_ID" )
	private String deviceAlarmSourceId;
	@Column( name = "DEVICEID" )
	private Long deviceId;
	@Column( name = "ALARM_TYPE" )
	private String alarmType;
	@Column( name = "NAME" )
	private String name;
	@Lob
	@Column( name = "ASSOCIATED_CHANNELS" )
	private byte[] associatedChannelsString;
	@Transient
	private Set<String> associatedChannels;
	@Column( name = "STATE" )
	@Enumerated( EnumType.STRING )
	private AlarmState state;
	@Column( name = "EXTENDED_STATE" )
	@Enumerated( EnumType.STRING )
	private AlarmExtendedState extendedState;
	@Column( name = "IS_DELETED" )
	private boolean isDeleted = false;

	@ManyToOne
	@JoinColumn( name = "DELETED_DEVICE" )
	private DeletedDevice deletedDevice;

	@Column( name = "LAST_STATE_CHANGE_TIME" )
	private Long lastStateChangeTime;

	public AlarmSourceView toDataObject()
	{
		AlarmSourceView alertData = new AlarmSourceView( id.toString(), deviceAlarmSourceId, getDeviceIdAsString(), alarmType, name, getAssociatedChannels(), state, extendedState );
		return alertData;
	}

	public void readFromDataObject( AlarmSourceView alarmSource )
	{
		if ( alarmSource != null )
		{
			id = ( alarmSource.getId() != null ? Long.valueOf( Long.parseLong( alarmSource.getId() ) ) : null );
			deviceAlarmSourceId = alarmSource.getDeviceAlarmSourceId();
			deviceId = ( alarmSource.getDeviceId() != null ? Long.valueOf( Long.parseLong( alarmSource.getDeviceId() ) ) : null );
			alarmType = alarmSource.getAlarmType();
			name = alarmSource.getName();
			state = alarmSource.getState();
			extendedState = alarmSource.getExtendedState();
			setAssociatedChannels( alarmSource.getAssociatedChannels() );
		}
	}

	public boolean readFromTransportObject( AlarmSource alarmSource )
	{
		boolean updated = false;
		if ( alarmSource != null )
		{
			String type = alarmSource.getType();
			String name = alarmSource.getName();
			AlarmState state = AlarmState.fromValue( alarmSource.getState() );
			AlarmExtendedState extendedState = AlarmExtendedState.fromValue( alarmSource.getExtState() );
			String[] assocIds = alarmSource.getAssocIds();
			List<String> associatedChannelsSet = Arrays.asList( assocIds );
			List<String> existingAssociations = new ArrayList( getAssociatedChannels() );

			if ( ( !type.equals( alarmType ) ) || ( !name.equals( this.name ) ) || ( state != this.state ) || ( extendedState != this.extendedState ) || ( !associatedChannelsSet.equals( existingAssociations ) ) )
			{
				alarmType = type;
				this.name = name;
				this.state = state;
				this.extendedState = extendedState;
				setAssociatedChannels( assocIds );
				updated = true;
			}
		}
		return updated;
	}

	public String getDeviceIdAsString()
	{
		if ( deviceId != null )
		{
			return String.valueOf( deviceId );
		}
		return null;
	}

	public String getIdAsString()
	{
		return String.valueOf( id );
	}

	public Set<String> getAssociatedChannels()
	{
		if ( associatedChannels == null )
		{
			associatedChannels = CommonUtils.jsonToStringSet( getAssociatedChannelsString() );
		}
		return associatedChannels;
	}

	public void setAssociatedChannels( String[] associatedChannelsArray )
	{
		if ( associatedChannels == null )
		{
			associatedChannels = new LinkedHashSet();
		}
		setAssociatedChannelsString( CommonUtils.setToJson( associatedChannels, associatedChannelsArray ) );
	}

	public void clearAssociatedChannels()
	{
		if ( associatedChannels != null )
		{
			associatedChannels.clear();
		}
		associatedChannelsString = null;
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

	public boolean removeFromAssociatedChannels( String channel )
	{
		Set<String> associatedChannelsSet = getAssociatedChannels();
		boolean removed = associatedChannelsSet.remove( channel );
		if ( removed )
		{
			setAssociatedChannelsString( CoreJsonSerializer.toJson( associatedChannelsSet ) );
		}
		return removed;
	}

	public Long getId()
	{
		return id;
	}

	public String getDeviceAlarmSourceID()
	{
		return deviceAlarmSourceId;
	}

	public void setDeviceAlarmSourceID( String deviceAlarmSourceID )
	{
		deviceAlarmSourceId = deviceAlarmSourceID;
	}

	public Long getDeviceId()
	{
		return deviceId;
	}

	public void setDeviceId( Long deviceId )
	{
		this.deviceId = deviceId;
	}

	public String getAlarmType()
	{
		return alarmType;
	}

	public void setAlarmType( String alarmType )
	{
		this.alarmType = alarmType;
	}

	public String getName()
	{
		return name;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	public AlarmState getState()
	{
		return state;
	}

	public void setState( AlarmState state )
	{
		this.state = state;
	}

	public AlarmExtendedState getExtendedState()
	{
		return extendedState;
	}

	public void setExtendedState( AlarmExtendedState extendedState )
	{
		this.extendedState = extendedState;
	}

	protected String getAssociatedChannelsString()
	{
		return CommonAppUtils.encodeToUTF8String( associatedChannelsString );
	}

	protected void setAssociatedChannelsString( String associatedChannels )
	{
		associatedChannelsString = CommonAppUtils.encodeStringToBytes( associatedChannels );
	}

	public boolean isDeleted()
	{
		return isDeleted;
	}

	public void setDeleted( boolean isDeleted )
	{
		this.isDeleted = isDeleted;
	}

	public DeletedDevice getDeletedDevice()
	{
		return deletedDevice;
	}

	public void setDeletedDevice( DeletedDevice deletedDevice )
	{
		this.deletedDevice = deletedDevice;
	}

	public Long getLastStateChangeTime()
	{
		return lastStateChangeTime;
	}

	public void setLastStateChangeTime( Long lastStateChangeTime )
	{
		this.lastStateChangeTime = lastStateChangeTime;
	}

	public Long getVersion()
	{
		return version;
	}

	public void setVersion( Long version )
	{
		this.version = version;
	}
}
