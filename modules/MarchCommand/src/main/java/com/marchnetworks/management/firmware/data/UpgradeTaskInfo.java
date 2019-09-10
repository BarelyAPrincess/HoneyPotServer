package com.marchnetworks.management.firmware.data;

import com.marchnetworks.common.utils.DateUtils;

import java.util.List;

public class UpgradeTaskInfo
{
	private long upgradeStartTime = 0L;
	private String deviceId;
	private String firmwareId;
	private String upgradeTaskId;
	private List<String> channelDeviceIds;

	public UpgradeTaskInfo( String deviceId, String firmwareId )
	{
		this.deviceId = deviceId;
		this.firmwareId = firmwareId;
		upgradeTaskId = "";
		channelDeviceIds = null;
	}

	public UpgradeTaskInfo( String deviceId, String firmwareId, List<String> channelDeviceIds )
	{
		this.deviceId = deviceId;
		this.firmwareId = firmwareId;
		upgradeTaskId = "";
		this.channelDeviceIds = channelDeviceIds;
	}

	public void setUpgradeStartTime( long startTime )
	{
		upgradeStartTime = startTime;
	}

	public void setUpgradeStartTimeWithCurrentTime()
	{
		upgradeStartTime = DateUtils.getCurrentUTCTimeInMillis();
	}

	public long getUpgradeStartTime()
	{
		return upgradeStartTime;
	}

	public long getUpgradeTimeElapsed()
	{
		return upgradeStartTime > 0L ? DateUtils.getCurrentUTCTimeInMillis() - upgradeStartTime : 0L;
	}

	public void setDeviceId( String deviceId )
	{
		this.deviceId = deviceId;
	}

	public String getDeviceId()
	{
		return deviceId;
	}

	public String getFirmwareId()
	{
		return firmwareId;
	}

	public void setUpgradeTaskId( String upgradeTaskId )
	{
		this.upgradeTaskId = upgradeTaskId;
	}

	public String getUpgradeTaskId()
	{
		return upgradeTaskId;
	}

	public void setChannelDeviceIds( List<String> channelDeviceIds )
	{
		this.channelDeviceIds = channelDeviceIds;
	}

	public List<String> getChannelDeviceIds()
	{
		return channelDeviceIds;
	}
}

