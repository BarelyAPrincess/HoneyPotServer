package com.marchnetworks.license.model;

public enum Type
{
	RECORDER,
	CHANNEL;

	private Type()
	{
	}

	public String toString()
	{
		if ( equals( RECORDER ) )
		{
			return "nvr_connection";
		}
		if ( equals( CHANNEL ) )
			return "channel";
		return "";
	}

	public boolean isExternal()
	{
		return this == CHANNEL;
	}

	public static Type getByValue( String value )
	{
		if ( value.equals( "recorder" ) )
		{
			return RECORDER;
		}

		if ( value.equals( "nvr_connection" ) )
			return RECORDER;
		if ( value.equals( "channel" ) )
		{
			return CHANNEL;
		}
		return null;
	}

	public static boolean isEqual( Type t, String type )
	{
		if ( t == null )
			return false;
		return t == getByValue( type );
	}
}
