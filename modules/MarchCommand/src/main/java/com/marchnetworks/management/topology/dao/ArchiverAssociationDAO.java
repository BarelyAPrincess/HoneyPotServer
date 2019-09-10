package com.marchnetworks.management.topology.dao;

import com.marchnetworks.command.common.dao.GenericDAO;
import com.marchnetworks.management.topology.model.ArchiverAssociationEntity;

import java.util.List;

public abstract interface ArchiverAssociationDAO extends GenericDAO<ArchiverAssociationEntity, Long>
{
	public abstract ArchiverAssociationEntity findByArchiverId( Long paramLong );

	public abstract List<Long> findAllArchiverIds();
}

