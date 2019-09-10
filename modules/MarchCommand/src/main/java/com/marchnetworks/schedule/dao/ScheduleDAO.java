package com.marchnetworks.schedule.dao;

import com.marchnetworks.command.common.dao.GenericDAO;
import com.marchnetworks.schedule.model.ScheduleEntity;

import java.util.List;

public abstract interface ScheduleDAO extends GenericDAO<ScheduleEntity, Long>
{
	public abstract List<ScheduleEntity> findAllByGroupAndAppId( String paramString1, String paramString2 );

	public abstract boolean existsByGroupAppIdAndName( String paramString1, String paramString2, String paramString3 );

	public abstract void deleteByAppId( String paramString );

	public abstract List<ScheduleEntity> findAllByUsername( String paramString );
}

