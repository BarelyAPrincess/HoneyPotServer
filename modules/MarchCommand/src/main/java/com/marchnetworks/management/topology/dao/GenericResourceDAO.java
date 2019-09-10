package com.marchnetworks.management.topology.dao;

import com.marchnetworks.management.topology.model.GenericResourceEntity;

import java.util.List;

public abstract interface GenericResourceDAO extends ResourceDAO<GenericResourceEntity>
{
	public abstract List<Long> findIdsByOwner( String paramString );
}

