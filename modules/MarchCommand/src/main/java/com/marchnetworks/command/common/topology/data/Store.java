package com.marchnetworks.command.common.topology.data;

public enum Store
{
	GLOBAL,
	USER;

	private Store()
	{
	}

	public static Store fromString( String value )
	{
		if ( value.equals( "global" ) )
			return GLOBAL;
		if ( value.equals( "user" ) )
		{
			return USER;
		}
		return null;
	}

	public String toString()
	{
		return super.toString().toLowerCase();
	}
}
