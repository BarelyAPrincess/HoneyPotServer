package com.marchnetworks.management.topology;

import com.marchnetworks.command.common.topology.data.ArchiverAssociation;

import java.util.Set;

public abstract interface ArchiverAssociationService
{
	public abstract void updateArchiverAssociation( ArchiverAssociation paramArchiverAssociation );

	public abstract ArchiverAssociation[] getArchiverAssociations();

	public abstract void removeDevice( Long paramLong );

	public abstract Long getPrimaryArchiverByDeviceresourceId( Long paramLong );

	public abstract Set<Long> getAssociatedDeviceResourceIdsByArchiverId( Long paramLong );
}

