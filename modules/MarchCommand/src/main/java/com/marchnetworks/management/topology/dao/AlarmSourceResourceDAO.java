package com.marchnetworks.management.topology.dao;

import com.marchnetworks.management.topology.model.AlarmSourceResourceEntity;

public abstract interface AlarmSourceResourceDAO extends ResourceDAO<AlarmSourceResourceEntity>
{
	public abstract AlarmSourceResourceEntity findByAlarmSourceId( Long paramLong );
}

