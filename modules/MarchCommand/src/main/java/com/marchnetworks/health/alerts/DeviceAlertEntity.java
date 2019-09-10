package com.marchnetworks.health.alerts;

import com.marchnetworks.command.api.alert.AlertCategoryEnum;
import com.marchnetworks.common.device.DeletedDevice;
import com.marchnetworks.health.data.AlertData;
import com.marchnetworks.health.data.DeletedDeviceAlertData;
import com.marchnetworks.health.data.DeviceAlertData;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table( name = "DEVICE_ALERT" )
public class DeviceAlertEntity extends AlertEntity
{
	private static final long serialVersionUID = -1228124315227692872L;
	@Column( name = "DEVICEID" )
	private String deviceId;
	@ManyToOne
	@JoinColumn( name = "FK_DELETED_DEVICE_ID" )
	private DeletedDevice deletedDevice;
	@Column( name = "CHANNEL_NAME" )
	private String channelName;
	@Column( name = "DEVICE_RECONCILIATION_STATE" )
	private Boolean reconciledWithDevice;
	@Column( name = "DEVICE_ALERT_ID" )
	private String deviceAlertId;
	@Column( name = "THRESHOLD_DURATION" )
	private Integer thresholdDuration;
	@Column( name = "THRESHOLD_FREQUENCY" )
	private Integer thresholdFrequency;

	public DeviceAlertEntity()
	{
	}

	public DeviceAlertEntity( String deviceId, String alertId, String alertCode, AlertCategoryEnum category, String sourceId, String sourceDesc, long alertTime, long lastTime, boolean deviceState, String info, int thresholdDuration, int thresholdFrequency )
	{
		super( alertCode, category, sourceId, sourceDesc, alertTime, lastTime, deviceState, info );
		deviceAlertId = alertId;
		this.deviceId = deviceId;
		this.thresholdDuration = Integer.valueOf( thresholdDuration );
		this.thresholdFrequency = Integer.valueOf( thresholdFrequency );
		reconciledWithDevice = Boolean.valueOf( false );
	}

	public String getDeviceId()
	{
		return deviceId;
	}

	public String getDeviceAlertId()
	{
		return deviceAlertId;
	}

	public void setDeviceAlertId( String alertId )
	{
		deviceAlertId = alertId;
	}

	public void setDeviceId( String deviceId )
	{
		this.deviceId = deviceId;
	}

	public String getChannelName()
	{
		return channelName;
	}

	public Integer getThresholdDuration()
	{
		if ( thresholdDuration == null )
			return Integer.valueOf( 0 );
		return thresholdDuration;
	}

	public void setThresholdDuration( Integer thresholdDuration )
	{
		this.thresholdDuration = thresholdDuration;
	}

	public Integer getThresholdFrequency()
	{
		if ( thresholdFrequency == null )
			return Integer.valueOf( 0 );
		return thresholdFrequency;
	}

	public void setThresholdFrequency( Integer thresholdFrequency )
	{
		this.thresholdFrequency = thresholdFrequency;
	}

	public void setChannelName( String name )
	{
		channelName = name;
	}

	public DeletedDevice getDeletedDevice()
	{
		return deletedDevice;
	}

	public void setDeletedDevice( DeletedDevice dev )
	{
		deletedDevice = dev;
	}

	public Boolean getReconciledWithDevice()
	{
		return reconciledWithDevice;
	}

	public void setReconciledWithDevice( Boolean reconciledWithDevice )
	{
		this.reconciledWithDevice = reconciledWithDevice;
	}

	public AlertData toDataObject()
	{
		AlertData alertData = null;
		if ( deletedDevice != null )
		{
			alertData = new DeletedDeviceAlertData( getAlertCode(), getAlertTime().longValue(), getLastInstanceTime().longValue(), getCount().longValue(), getAlertResolvedTime(), getDeviceState(), getSourceId(), getSourceDesc(), getSeverity(), getCategory(), getInfo(), getId(), getUserState(), getLastUserStateChangedTime(), getChannelName(), deletedDevice, getThresholdDuration().intValue(), getThresholdFrequency().intValue() );
		}
		else
		{
			alertData = new DeviceAlertData( getAlertCode(), getAlertTime().longValue(), getLastInstanceTime().longValue(), getCount().longValue(), getAlertResolvedTime(), getDeviceState(), getSourceId(), getSourceDesc(), getSeverity(), getCategory(), getInfo(), getId(), getUserState(), getLastUserStateChangedTime(), deviceId, channelName, deviceAlertId, getThresholdDuration().intValue(), getThresholdFrequency().intValue() );
		}

		return alertData;
	}
}
