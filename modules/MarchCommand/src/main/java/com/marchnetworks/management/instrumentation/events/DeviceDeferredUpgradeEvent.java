package com.marchnetworks.management.instrumentation.events;

public class DeviceDeferredUpgradeEvent extends AbstractDeviceEvent
{
	private String firmwareFileObjectId;

	private String key;

	private Long firstCreateTime;

	public DeviceDeferredUpgradeEvent( String deviceId, String firmwareFileObjectId, String key, Long firstCreateTime )
	{
		super( DeviceDeferredUpgradeEvent.class.getName(), deviceId );
		this.firmwareFileObjectId = firmwareFileObjectId;
		this.key = key;
		this.firstCreateTime = firstCreateTime;
	}

	public DeviceDeferredUpgradeEvent( String deviceId )
	{
		super( DeviceDeferredUpgradeEvent.class.getName(), deviceId );
		firmwareFileObjectId = "";
		key = "";
		firstCreateTime = Long.valueOf( 0L );
	}

	public String getKey()
	{
		return key;
	}

	public String getFirmwareFileObjectId()
	{
		return firmwareFileObjectId;
	}

	public Long getFirstCreateTime()
	{
		return firstCreateTime;
	}
}

