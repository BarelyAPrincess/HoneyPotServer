package com.marchnetworks.management.instrumentation.dao;

import com.marchnetworks.command.common.dao.GenericDAO;
import com.marchnetworks.management.instrumentation.model.DeviceOutputEntity;

import java.util.List;
import java.util.Set;

public abstract interface DeviceOutputDAO<T extends DeviceOutputEntity> extends GenericDAO<T, Long>
{
	public abstract List<T> findAllByDeviceId( Long paramLong );

	public abstract Set<String> findAllOutputIdsByDeviceId( Long paramLong );

	public abstract T findByDeviceAndOutputId( Long paramLong, String paramString );

	public abstract void deleteByDeviceId( Long paramLong );
}

