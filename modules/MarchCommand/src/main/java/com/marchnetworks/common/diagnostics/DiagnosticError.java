package com.marchnetworks.common.diagnostics;

public enum DiagnosticError
{
	DATABASE( "DatabaseFailure", "Database failure" ),
	APPLICATION_DEADLOCK( "Deadlocked", "Application deadlocked" ),
	LOW_MEMORY( "LowMemory", "Low memory" ),
	TIME_JUMP( "TimeJump", "Time jump" );

	private String code;
	private String name;

	private DiagnosticError( String code, String name )
	{
		this.code = code;
		this.name = name;
	}

	public String getCode()
	{
		return code;
	}

	public String getName()
	{
		return name;
	}

	public static DiagnosticError fromCode( String code )
	{
		if ( code != null )
		{
			for ( DiagnosticError def : values() )
			{
				if ( code.equalsIgnoreCase( code ) )
				{
					return def;
				}
			}
		}
		return null;
	}
}
