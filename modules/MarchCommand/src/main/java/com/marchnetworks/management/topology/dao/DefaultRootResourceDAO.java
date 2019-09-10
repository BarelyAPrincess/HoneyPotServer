package com.marchnetworks.management.topology.dao;

import com.marchnetworks.command.common.dao.GenericDAO;
import com.marchnetworks.management.topology.model.DefaultRootResourceEntity;

public abstract interface DefaultRootResourceDAO extends GenericDAO<DefaultRootResourceEntity, String>
{
	public abstract DefaultRootResourceEntity getSystemRootResource();

	public abstract DefaultRootResourceEntity getLogicalRootResource();
}

