package com.marchnetworks.command.api.id;

public interface IdCoreService
{
	Long getNextId( Long paramLong, String paramString );

	Long getLastId( String paramString );

	void deleteRow( String paramString );
}
