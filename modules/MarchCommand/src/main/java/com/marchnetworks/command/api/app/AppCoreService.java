package com.marchnetworks.command.api.app;

import com.marchnetworks.command.common.app.AppException;

public interface AppCoreService
{
	void appInitializationComplete( String paramString, boolean paramBoolean );

	String getAppDirectory( String paramString ) throws AppException;

	Integer getCurrentVersion( String paramString );

	void setDatabaseVersion( String paramString, Integer paramInteger );

	boolean upgraded( String paramString );
}
