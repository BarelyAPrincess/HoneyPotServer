package com.marchnetworks.management.instrumentation.events;

public abstract class AbstractDeviceConfigurationEvent extends AbstractDeviceEvent
{
	private String taskId;

	private String reason;

	private String configHash;

	private DeviceConfigurationEventType deviceEventType;

	private boolean deferred = false;

	public AbstractDeviceConfigurationEvent( String type, String deviceId, DeviceConfigurationEventType deviceEventType, String taskId, String configHash, String reason )
	{
		super( type, deviceId );
		this.taskId = taskId;
		this.reason = reason;
		this.configHash = configHash;
		this.deviceEventType = deviceEventType;
	}

	public AbstractDeviceConfigurationEvent( String type, String deviceId, DeviceConfigurationEventType deviceEventType, String taskId, String reason )
	{
		super( type, deviceId );
		this.taskId = taskId;
		this.reason = reason;
		this.deviceEventType = deviceEventType;
	}

	public AbstractDeviceConfigurationEvent( String type, String deviceId, DeviceConfigurationEventType deviceEventType, String taskId, String reason, boolean deferred )
	{
		super( type, deviceId );
		this.taskId = taskId;
		this.reason = reason;
		this.deviceEventType = deviceEventType;
		this.deferred = deferred;
	}

	public String getTaskId()
	{
		return taskId;
	}

	public String getReason()
	{
		return reason;
	}

	public String getConfigHash()
	{
		return configHash;
	}

	public DeviceConfigurationEventType getDeviceEventType()
	{
		return deviceEventType;
	}

	public void setTaskId( String taskId )
	{
		this.taskId = taskId;
	}

	public boolean getDeferred()
	{
		return deferred;
	}

	public void setDeferred( boolean deferred )
	{
		this.deferred = deferred;
	}
}

