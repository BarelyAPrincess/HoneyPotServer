package com.marchnetworks.command.common.transport.data;

public enum EventType
{
	DELETE( "delete" ),
	NOTIFY( "notify" ),
	UPDATE( "update" );

	private final String value;

	EventType( String v )
	{
		value = v;
	}

	public String value()
	{
		return value;
	}

	public static EventType fromValue( String v )
	{
		for ( EventType c : values() )
		{
			if ( c.value.equals( v ) )
			{
				return c;
			}
		}
		throw new IllegalArgumentException( v );
	}
}
