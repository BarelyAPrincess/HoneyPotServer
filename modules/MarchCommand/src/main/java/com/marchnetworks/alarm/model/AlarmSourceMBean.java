package com.marchnetworks.alarm.model;

import com.marchnetworks.command.common.alarm.data.AlarmExtendedState;
import com.marchnetworks.command.common.alarm.data.AlarmSourceView;
import com.marchnetworks.command.common.alarm.data.AlarmState;

import java.util.Set;

public abstract interface AlarmSourceMBean
{
	public abstract AlarmSourceView toDataObject();

	public abstract Long getId();

	public abstract String getDeviceAlarmSourceID();

	public abstract Long getDeviceId();

	public abstract String getAlarmType();

	public abstract String getName();

	public abstract AlarmState getState();

	public abstract AlarmExtendedState getExtendedState();

	public abstract Set<String> getAssociatedChannels();
}
