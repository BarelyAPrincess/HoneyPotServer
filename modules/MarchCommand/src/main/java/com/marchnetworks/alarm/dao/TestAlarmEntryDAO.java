package com.marchnetworks.alarm.dao;

import com.marchnetworks.alarm.model.AlarmEntryEntity;
import com.marchnetworks.command.common.dao.GenericDAO;

import java.util.List;

public abstract interface TestAlarmEntryDAO extends GenericDAO<AlarmEntryEntity, Long>
{
	public abstract void batchInsert( List<AlarmEntryEntity> paramList, int paramInt );
}
