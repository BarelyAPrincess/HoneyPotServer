package com.marchnetworks.alarm.service;

import com.marchnetworks.alarm.alarmdetails.AlarmDetailEnum;
import com.marchnetworks.alarm.data.AlarmEntryView;
import com.marchnetworks.alarm.model.AlarmSourceMBean;
import com.marchnetworks.command.common.alarm.data.AlarmSourceView;
import com.marchnetworks.management.instrumentation.events.DeviceAlarmEvent;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public abstract interface AlarmService
{
	public abstract AlarmEntryView[] queryAlarmEntries( String paramString, String[] paramArrayOfString, boolean paramBoolean1, boolean paramBoolean2, long paramLong1, long paramLong2, int paramInt );

	public abstract void closeAlarmEntries( String paramString, AlarmEntryCloseRecord[] paramArrayOfAlarmEntryCloseRecord ) throws AlarmException;

	public abstract void setAlarmHandling( String paramString, String[] paramArrayOfString, boolean paramBoolean ) throws AlarmException;

	public abstract boolean getAlarmsEnabled();

	public abstract void processAlarmEvent( DeviceAlarmEvent paramDeviceAlarmEvent );

	public abstract AlarmSourceMBean getAlarmSource( String paramString );

	public abstract boolean deleteAlarmSource( String paramString );

	public abstract void processDeviceUnregistered( String paramString );

	public abstract void processDeviceRegistered( String paramString );

	public abstract void processAlarmReconciliationWithDevice( String paramString );

	public abstract void processAlarmClosureDispatch( String paramString );

	public abstract void updateAlarmEntryDetails( String paramString1, String paramString2, Set<AlarmDetailEnum> paramSet, String paramString3 ) throws AlarmException;

	public abstract void markAlarmEntriesReconciled( Collection<AlarmEntryView> paramCollection );

	public abstract void purgeOldAlarms( long paramLong );

	public abstract List<Long> getReferencedDeletedDevices();

	public abstract void createAlarmSource( AlarmSourceView paramAlarmSourceView, boolean paramBoolean );
}
