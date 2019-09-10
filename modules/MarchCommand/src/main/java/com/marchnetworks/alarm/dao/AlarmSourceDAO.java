package com.marchnetworks.alarm.dao;

import com.marchnetworks.alarm.model.AlarmSourceEntity;
import com.marchnetworks.command.common.dao.GenericDAO;

import java.util.List;

public abstract interface AlarmSourceDAO extends GenericDAO<AlarmSourceEntity, Long>
{
	public abstract AlarmSourceEntity findByDeviceIdAndDeviceAlarmSourceId( Long paramLong, String paramString );

	public abstract List<AlarmSourceEntity> findAll();

	public abstract List<AlarmSourceEntity> findAllByDeviceId( Long paramLong );

	public abstract List<AlarmSourceEntity> findAllWithDeletedByDeviceId( Long paramLong );

	public abstract List<Long> findAllDeletedAlarmSourceIds( List<Long> paramList );

	public abstract List<Long> findAllDeletedDeviceAlarmSourceIds( List<String> paramList );

	public abstract List<AlarmSourceEntity> findAllUnreferencedDeleted( List<Long> paramList );

	public abstract List<Long> findAllDeletedDeviceIds();

	public abstract List<AlarmSourceEntity> findAllIncludeDeleted();
}
