package com.marchnetworks.license.model;

public enum AppLicenseType
{
	APP_FULL_ACCESS,
	APP_RECORDER_ACCESS,
	APP_CHANNEL_ACCESS,
	APP_GENERIC_ACCESS,
	APP_GENERIC_CHANNEL_ACCESS;

	private AppLicenseType()
	{
	}

	public static AppLicenseType getByValue( String value )
	{
		if ( value.equals( "app.full_access" ) )
			return APP_FULL_ACCESS;
		if ( value.equals( "app.recorder_access" ) )
			return APP_RECORDER_ACCESS;
		if ( value.equals( "app.channel_access" ) )
			return APP_CHANNEL_ACCESS;
		if ( value.equals( "app.generic_access" ) )
			return APP_GENERIC_ACCESS;
		if ( value.equals( "app.generic_channel_access" ) )
		{
			return APP_GENERIC_CHANNEL_ACCESS;
		}
		return null;
	}

	public boolean isGeneric()
	{
		return ( this == APP_GENERIC_ACCESS ) || ( this == APP_GENERIC_CHANNEL_ACCESS );
	}

	public String toString()
	{
		if ( this == APP_FULL_ACCESS )
			return "app.full_access";
		if ( this == APP_RECORDER_ACCESS )
			return "app.recorder_access";
		if ( this == APP_CHANNEL_ACCESS )
			return "app.channel_access";
		if ( this == APP_GENERIC_ACCESS )
			return "app.generic_access";
		if ( this == APP_GENERIC_CHANNEL_ACCESS )
			return "app.generic_channel_access";
		return "";
	}
}
