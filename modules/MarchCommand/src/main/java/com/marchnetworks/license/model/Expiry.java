package com.marchnetworks.license.model;

public enum Expiry
{
	TRIAL,
	PERMANENT;

	private Expiry()
	{
	}

	public String toString()
	{
		if ( equals( TRIAL ) )
			return "trial";
		if ( equals( PERMANENT ) )
		{
			return "perm";
		}
		return "";
	}

	public static Expiry getByValue( String value )
	{
		if ( value == null )
			return null;
		if ( value.equals( "trial" ) )
			return TRIAL;
		if ( value.equals( "perm" ) )
		{
			return PERMANENT;
		}
		return null;
	}
}
