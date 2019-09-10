package com.marchnetworks.management.topology.dao;

import com.marchnetworks.command.common.dao.GenericDAO;
import com.marchnetworks.command.common.topology.data.Store;
import com.marchnetworks.management.topology.model.GenericStorageEntity;

import java.util.List;

public abstract interface GenericStorageDAO extends GenericDAO<GenericStorageEntity, Long>
{
	public abstract GenericStorageEntity findByIdentifiers( Store paramStore, String paramString1, String paramString2, String paramString3 );

	public abstract Long findIdByIdentifiers( Store paramStore, String paramString1, String paramString2, String paramString3 );

	public abstract boolean checkExists( Store paramStore, String paramString1, String paramString2, String paramString3 );

	public abstract List<GenericStorageEntity> findAllByIdentifiers( Store paramStore, String paramString1, String paramString2 );

	public abstract List<GenericStorageEntity> findByUserId( String paramString );

	public abstract long getSize( Store paramStore, String paramString );

	public abstract long getTotalSize();

	public abstract long getTotalUserSize();

	public abstract int deleteByUserId( String paramString );

	public abstract int deleteByAppId( String paramString );
}

