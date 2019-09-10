package com.marchnetworks.management.topology.dao;

import com.marchnetworks.command.common.dao.GenericDAO;
import com.marchnetworks.management.topology.model.ResourceEntity;

public abstract interface ResourceDAO<T extends ResourceEntity> extends GenericDAO<T, Long>
{
}

