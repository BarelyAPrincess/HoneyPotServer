package com.marchnetworks.license.model;

public enum AppType
{
	COMMAND_API,
	APP,
	BUILT_IN_APP,
	LICENSE_EXEMPT_APP;

	private AppType()
	{
	}

	public static AppType getByValue( String value )
	{
		if ( value.equals( "command_api" ) )
			return COMMAND_API;
		if ( value.equals( "app" ) )
			return APP;
		if ( value.equals( "builtin_app" ) )
			return BUILT_IN_APP;
		if ( value.equals( "license_exempt_app" ) )
		{
			return LICENSE_EXEMPT_APP;
		}
		return null;
	}

	public String toString()
	{
		if ( this == COMMAND_API )
			return "command_api";
		if ( this == APP )
			return "app";
		if ( this == BUILT_IN_APP )
			return "builtin_app";
		if ( this == LICENSE_EXEMPT_APP )
			return "license_exempt_app";
		return "";
	}
}
