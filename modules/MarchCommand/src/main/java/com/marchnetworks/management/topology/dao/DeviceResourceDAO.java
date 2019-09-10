package com.marchnetworks.management.topology.dao;

import com.marchnetworks.management.topology.model.DeviceResourceEntity;

public abstract interface DeviceResourceDAO extends ResourceDAO<DeviceResourceEntity>
{
	public abstract DeviceResourceEntity findByDeviceId( String paramString );

	public abstract Long findResourceIdByDeviceId( String paramString );
}

