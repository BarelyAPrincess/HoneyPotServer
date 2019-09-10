package com.marchnetworks.command.common.diagnostics;

public enum DatabaseNameEnum
{
	COMMAND_DB( "command" ),
	APPS_DB( "apps" );

	private String databaseName;

	private DatabaseNameEnum( String name )
	{
		databaseName = name;
	}

	public String getName()
	{
		return databaseName;
	}
}
