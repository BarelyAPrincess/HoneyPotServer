package com.marchnetworks.alarm.service;

import com.marchnetworks.alarm.data.AlarmEntryView;
import com.marchnetworks.alarm.model.AlarmSourceEntity;
import com.marchnetworks.alarm.model.AlarmSourceMBean;
import com.marchnetworks.command.common.alarm.data.AlarmSourceView;
import com.marchnetworks.management.instrumentation.model.CompositeDevice;

import java.util.List;
import java.util.Map;

public abstract interface AlarmTestService
{
	public abstract List<AlarmSourceView> getAlarmSources();

	public abstract List<AlarmEntryView> getAlarmEntries();

	public abstract List<AlarmEntryView> getOpenAlarmEntries();

	public abstract List<AlarmSourceView> getAlarmSourcesIncludeDeleted();

	public abstract void createAlarmSource( String paramString1, String paramString2, String paramString3, String paramString4, boolean paramBoolean );

	public abstract void createManyAlarmSources( int paramInt, boolean paramBoolean );

	public abstract void deleteTestAlarmSourcesAndEntries();

	public abstract Map<Long, List<AlarmSourceEntity>> createSimulatedAlarmSources( List<CompositeDevice> paramList, int paramInt );

	public abstract AlarmEntryView getAlarmEntry( String paramString );

	public abstract AlarmEntryView findUnclosedAlarmEntry( String paramString );

	public abstract AlarmSourceView getAlarmSourceData( String paramString );

	public abstract void deleteAlarmEntries();

	public abstract void generateAlarmEntries( int paramInt );

	public abstract Map<String, Long> runBenchmark();

	public abstract int getAlarmEntriesCount();

	public abstract Long getLastAlarmSourceId();

	public abstract AlarmSourceMBean getAlarmSourceByDeviceAndDeviceAlarmSourceId( Long paramLong, String paramString );
}
