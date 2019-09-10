package com.marchnetworks.management.topology.dao;

import com.marchnetworks.management.topology.model.GroupEntity;

import java.util.List;

public abstract interface GroupResourceDAO extends ResourceDAO<GroupEntity>
{
	public abstract List<GroupEntity> findAllEmptyResourceNodes();
}

