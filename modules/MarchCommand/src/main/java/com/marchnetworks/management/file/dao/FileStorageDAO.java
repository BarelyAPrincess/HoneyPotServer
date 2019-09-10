package com.marchnetworks.management.file.dao;

import com.marchnetworks.command.common.dao.GenericDAO;
import com.marchnetworks.management.file.model.FileStorageEntity;
import com.marchnetworks.management.file.model.FileStorageType;

import java.util.List;

public abstract interface FileStorageDAO extends GenericDAO<FileStorageEntity, Long>
{
	public abstract FileStorageEntity findById( Long paramLong );

	public abstract List<FileStorageEntity> findByCategory( FileStorageType paramFileStorageType );

	public abstract List<FileStorageEntity> findByName( String paramString );

	public abstract FileStorageEntity findByNameCategory( String paramString, FileStorageType paramFileStorageType );

	public abstract void deleteByPath( String paramString );

	public abstract void deleteByCategory( FileStorageType paramFileStorageType );

	public abstract boolean usersHaveFile( String paramString );
}

