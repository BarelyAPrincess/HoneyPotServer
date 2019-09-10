package com.marchnetworks.command.api.topology;

import java.util.List;
import java.util.Set;

public interface ArchiverAssociationCoreService
{
	Long getPrimaryArchiverByDeviceresourceId( Long paramLong );

	Set<Long> getAssociatedDeviceResourceIdsByArchiverId( Long paramLong );

	List<Long> getAllArchiverResourceIds();
}
