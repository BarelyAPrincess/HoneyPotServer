package com.marchnetworks.management.topology.dao;

import com.marchnetworks.command.common.dao.GenericDAO;
import com.marchnetworks.management.topology.model.ResourceAssociationEntity;
import com.marchnetworks.management.topology.model.ResourceEntity;

import java.util.List;
import java.util.Map;

public abstract interface ResourceAssociationDAO extends GenericDAO<ResourceAssociationEntity, Long>
{
	public abstract Map<String, ResourceAssociationEntity> findAssociations( ResourceEntity paramResourceEntity, String paramString );

	public abstract List<ResourceAssociationEntity> findParentAssociations( List<ResourceEntity> paramList );

	public abstract Map<String, ResourceEntity> findAssociatedResources( ResourceEntity paramResourceEntity, String paramString );

	public abstract ResourceAssociationEntity findAssociation( Long paramLong1, Long paramLong2, String paramString );
}

