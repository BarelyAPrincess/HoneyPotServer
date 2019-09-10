package com.marchnetworks.management.topology.dao;

import com.marchnetworks.management.topology.model.LinkResourceEntity;

import java.util.List;

public abstract interface LinkResourceDAO extends ResourceDAO<LinkResourceEntity>
{
	public abstract List<LinkResourceEntity> findAllByLinkedResourceId( Long paramLong );
}

