package com.marchnetworks.monitoring.diagnostics;

import java.util.ArrayList;
import java.util.List;

public enum CommandIndexTableNames
{
	ALARM_ENTRY( "ALARM_ENTRY" ),
	AUDIT_LOGS( "AUDIT_LOGS" );

	private String name;

	private CommandIndexTableNames( String tableName )
	{
		name = tableName;
	}

	public static List<String> getAllTableNames()
	{
		List<String> result = new ArrayList( values().length );
		for ( CommandIndexTableNames table : values() )
		{
			result.add( table.getTableName() );
		}
		return result;
	}

	String getTableName()
	{
		return name;
	}
}

