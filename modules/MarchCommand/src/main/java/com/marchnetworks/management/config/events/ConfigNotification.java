package com.marchnetworks.management.config.events;

import com.marchnetworks.management.config.DeviceImageState;
import com.marchnetworks.management.config.DeviceSnapshotState;

import javax.management.Notification;

public abstract class ConfigNotification extends Notification
{
	private static final long serialVersionUID = -1276850032865136898L;
	private static long sequenceNumber;
	private DeviceImageState imageState;
	private DeviceSnapshotState snapshotState;
	private Long devConfigId = null;
	private ConfigNotificationReasonCode reasonCode;
	private String firmwareVersionInfo;
	private ConfigNotificationType notifyType;

	protected static synchronized long newSequenceNumber()
	{
		return sequenceNumber++;
	}

	public ConfigNotification( String deviceId, ConfigNotificationType type )
	{
		super( type.toString(), deviceId, newSequenceNumber(), System.currentTimeMillis() );
		notifyType = type;
	}

	public ConfigNotification( String deviceId, ConfigNotificationType type, String message )
	{
		super( type.toString(), deviceId, newSequenceNumber(), System.currentTimeMillis(), message );
		notifyType = type;
	}

	public String getDeviceId()
	{
		return ( String ) getSource();
	}

	public ConfigNotificationType getNotificationType()
	{
		return notifyType;
	}

	public DeviceImageState getImageState()
	{
		return imageState;
	}

	public void setImageState( DeviceImageState imageState )
	{
		this.imageState = imageState;
	}

	public DeviceSnapshotState getSnapshotState()
	{
		return snapshotState;
	}

	public void setSnapshotState( DeviceSnapshotState snapshotState )
	{
		this.snapshotState = snapshotState;
	}

	public ConfigNotificationType getNotifyType()
	{
		return notifyType;
	}

	public void setNotifyType( ConfigNotificationType notifyType )
	{
		this.notifyType = notifyType;
	}

	public Long getDevConfigId()
	{
		return devConfigId;
	}

	public void setDevConfigId( Long devConfigId )
	{
		this.devConfigId = devConfigId;
	}

	public ConfigNotificationReasonCode getReasonCode()
	{
		return reasonCode;
	}

	public void setReasonCode( ConfigNotificationReasonCode reasonCode )
	{
		this.reasonCode = reasonCode;
	}

	public String getFirmwareVersionInfo()
	{
		return firmwareVersionInfo;
	}

	public void setFirmwareVersionInfo( String firmwareVersionInfo )
	{
		this.firmwareVersionInfo = firmwareVersionInfo;
	}
}
