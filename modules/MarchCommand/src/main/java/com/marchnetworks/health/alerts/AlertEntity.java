package com.marchnetworks.health.alerts;

import com.marchnetworks.command.api.alert.AlertCategoryEnum;
import com.marchnetworks.common.types.AlertSeverityEnum;
import com.marchnetworks.common.types.AlertUserStateEnum;
import com.marchnetworks.health.data.AlertData;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

@Entity
@Table( name = "ALERT" )
@Inheritance( strategy = InheritanceType.JOINED )
public class AlertEntity implements Serializable
{
	private static final long serialVersionUID = 3101432734700118795L;
	@Id
	@GeneratedValue
	@Column( name = "ID" )
	private Long id;
	@Column( name = "ALERTCODE" )
	private String alertCode;
	@Column( name = "SOURCEID" )
	private String sourceId;
	@Column( name = "ALERT_TIME" )
	private long alertTime;
	@Column( name = "LAST_INSTANCE_TIME" )
	private Long lastInstanceTime;
	@Column( name = "COUNT" )
	private Long count;
	@Enumerated( EnumType.STRING )
	@Column( name = "CATEGORY" )
	private AlertCategoryEnum category;
	@Column( name = "SOURCE_DESC" )
	private String sourceDesc;
	@Column( name = "ALERT_RESOLVED_TIME" )
	private long alertResolvedTime;
	@Column( name = "DEVICE_STATE" )
	private boolean deviceState = true;

	@Column( name = "EXTRA_INFO", length = 4000 )
	private String info;

	@Enumerated( EnumType.STRING )
	@Column( name = "USER_STATE" )
	private AlertUserStateEnum userState = AlertUserStateEnum.OPEN;

	@Column( name = "LAST_USER_STATE_CHANGED_TIME" )
	private long lastUserStateChangedTime;

	@Enumerated( EnumType.STRING )
	@Column( name = "SEVERITY" )
	private AlertSeverityEnum severity = AlertSeverityEnum.IGNORE;

	public AlertEntity()
	{
	}

	public AlertEntity( String a_AlertCode, AlertCategoryEnum a_Category, String a_SourceId, String a_SourceDesc, long alertTime, long lastTime, boolean deviceState, String a_Info )
	{
		alertCode = a_AlertCode;
		category = a_Category;
		sourceId = a_SourceId;
		sourceDesc = a_SourceDesc;
		this.alertTime = ( alertTime < 0L ? lastTime : alertTime );
		lastInstanceTime = Long.valueOf( lastTime );
		count = Long.valueOf( 1L );
		info = a_Info;
		this.deviceState = deviceState;
	}

	public String getAlertCode()
	{
		return alertCode;
	}

	public boolean getDeviceState()
	{
		return deviceState;
	}

	public AlertUserStateEnum getUserState()
	{
		return userState;
	}

	public void setUserState( AlertUserStateEnum a_UserState )
	{
		userState = a_UserState;
	}

	public long getLastUserStateChangedTime()
	{
		return lastUserStateChangedTime;
	}

	public void setLastUserStateChangedTime( long time )
	{
		lastUserStateChangedTime = time;
	}

	public void setDeviceState( boolean a_DeviceState )
	{
		deviceState = a_DeviceState;
	}

	public void setAlertClear( long a_DeviceAdjustedTime )
	{
		alertResolvedTime = a_DeviceAdjustedTime;
	}

	public AlertSeverityEnum getSeverity()
	{
		return severity;
	}

	public void setSeverity( AlertSeverityEnum a_Severity )
	{
		if ( ( a_Severity != null ) && ( severity != a_Severity ) )
		{
			severity = a_Severity;
		}
	}

	public AlertCategoryEnum getCategory()
	{
		return category;
	}

	public String getSourceDesc()
	{
		return sourceDesc;
	}

	public void setSourceDesc( String sourceDesc )
	{
		this.sourceDesc = sourceDesc;
	}

	public String getSourceId()
	{
		return sourceId;
	}

	public Long getAlertTime()
	{
		return Long.valueOf( alertTime );
	}

	public long getAlertResolvedTime()
	{
		return alertResolvedTime;
	}

	public void setAlertResolvedTime( long alertResolvedTime )
	{
		this.alertResolvedTime = alertResolvedTime;
	}

	public void setAlertTime( long time )
	{
		alertTime = time;
	}

	public Long getLastInstanceTime()
	{
		if ( lastInstanceTime == null )
		{
			return Long.valueOf( alertTime );
		}
		return lastInstanceTime;
	}

	public void setLastInstanceTime( Long lastInstanceTime )
	{
		this.lastInstanceTime = lastInstanceTime;
	}

	public Long getCount()
	{
		if ( count == null )
		{
			return Long.valueOf( 1L );
		}
		return count;
	}

	public void setCount( Long count )
	{
		this.count = count;
	}

	public long getId()
	{
		return id.longValue();
	}

	public String getInfo()
	{
		return info;
	}

	public void setInfo( String info )
	{
		this.info = info;
	}

	public AlertData toDataObject()
	{
		AlertData alertData = new AlertData( getAlertCode(), getAlertTime().longValue(), getLastInstanceTime().longValue(), getCount().longValue(), getAlertResolvedTime(), getDeviceState(), getSourceId(), getSourceDesc(), getSeverity(), getCategory(), getInfo(), getId(), getUserState(), getLastUserStateChangedTime() );

		return alertData;
	}
}
