package com.marchnetworks.management.topology.model;

import com.marchnetworks.command.common.topology.data.Group;
import com.marchnetworks.command.common.topology.data.Resource;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table( name = "RESOURCE_GROUP" )
public class GroupEntity extends ResourceEntity
{
	public GroupEntity()
	{
	}

	public GroupEntity( Group dataObject )
	{
		super( dataObject );
	}

	protected Resource newDataObject()
	{
		return new Group();
	}

	public Class<Group> getDataObjectClass()
	{
		return Group.class;
	}
}

