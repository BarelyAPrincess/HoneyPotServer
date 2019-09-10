package com.marchnetworks.map.dao;

import com.marchnetworks.command.common.dao.GenericDAO;
import com.marchnetworks.map.model.MapEntity;

public abstract interface MapDAO extends GenericDAO<MapEntity, Long>
{
	public abstract MapEntity findByHash( byte[] paramArrayOfByte );

	public abstract boolean checkExists( Long paramLong );
}

