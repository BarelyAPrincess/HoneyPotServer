package com.marchnetworks.server.communications.transport.datamodel;

public enum ChannelState
{
	DISABLED( "disabled" ),
	UNKNOWN( "unknown" ),
	ONLINE( "online" ),
	OFFLINE( "offline" );

	private final String value;

	ChannelState( String v )
	{
		value = v;
	}

	public String value()
	{
		return value;
	}

	public static ChannelState fromValue( String v )
	{
		for ( ChannelState c : values() )
			if ( c.value.equals( v ) )
				return c;

		throw new IllegalArgumentException( v );
	}
}

