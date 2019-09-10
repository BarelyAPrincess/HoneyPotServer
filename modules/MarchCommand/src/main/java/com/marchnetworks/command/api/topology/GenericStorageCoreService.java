package com.marchnetworks.command.api.topology;

import com.marchnetworks.command.common.topology.GenericStorageException;
import com.marchnetworks.command.common.topology.data.GenericObjectInfo;
import com.marchnetworks.command.common.topology.data.Store;

public interface GenericStorageCoreService
{
	void setObject( Store paramStore, String paramString1, byte[] paramArrayOfByte, String paramString2 ) throws GenericStorageException;

	byte[] getObject( Store paramStore, String paramString1, String paramString2, String paramString3, boolean paramBoolean ) throws GenericStorageException;

	void deleteObject( Store paramStore, String paramString1, String paramString2, String paramString3, boolean paramBoolean ) throws GenericStorageException;

	GenericObjectInfo[] listObjects( Store paramStore, String paramString1, String paramString2, boolean paramBoolean ) throws GenericStorageException;

	String getObjectTag( Store paramStore, String paramString1, String paramString2, String paramString3, boolean paramBoolean ) throws GenericStorageException;

	void deleteUserStore( String paramString );
}
