package com.marchnetworks.command.api.alarm;

import com.marchnetworks.command.common.alarm.data.AlarmSourceView;

public abstract interface AlarmCoreService
{
	public abstract AlarmSourceView getAlarmSourceData( Long paramLong, String paramString );
}
