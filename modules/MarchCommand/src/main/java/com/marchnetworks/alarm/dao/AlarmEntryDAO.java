package com.marchnetworks.alarm.dao;

import com.marchnetworks.alarm.model.AlarmEntryEntity;
import com.marchnetworks.alarm.model.AlarmSourceEntity;
import com.marchnetworks.command.common.dao.GenericDAO;

import java.util.List;

public abstract interface AlarmEntryDAO extends GenericDAO<AlarmEntryEntity, Long>
{
	public abstract AlarmEntryEntity findByDeviceEntryIdAndSource( String paramString, AlarmSourceEntity paramAlarmSourceEntity );

	public abstract AlarmEntryEntity findOpenEntryByDeviceEntryIdAndSource( String paramString, AlarmSourceEntity paramAlarmSourceEntity );

	public abstract List<AlarmEntryEntity> findByAlarmSource( AlarmSourceEntity paramAlarmSourceEntity );

	public abstract List<AlarmEntryEntity> findAllByQuery( List<Long> paramList, boolean paramBoolean1, boolean paramBoolean2, long paramLong1, long paramLong2, int paramInt );

	public abstract List<Long> findReferencedAlarmSources( boolean paramBoolean1, boolean paramBoolean2, long paramLong1, long paramLong2 );

	public abstract List<AlarmEntryEntity> findClosedNotReconciledByDevice( Long paramLong );

	public abstract List<Long> findReferencedDeletedAlarmSourceIds();

	public abstract int deleteClosedAlarmsByLastInstanceTime( long paramLong );

	public abstract AlarmEntryEntity findUnclosedByAlarmSource( AlarmSourceEntity paramAlarmSourceEntity );

	public abstract List<AlarmEntryEntity> findAllOpen();
}
