package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType( name = "ChannelState" )
@XmlEnum
public enum ChannelState
{
	UNKNOWN( "unknown" ),

	DISABLED( "disabled" ),

	ONLINE( "online" ),

	OFFLINE( "offline" );

	private final String value;

	private ChannelState( String v )
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
		{
			if ( c.value.equals( v ) )
			{
				return c;
			}
		}
		throw new IllegalArgumentException( v );
	}
}
