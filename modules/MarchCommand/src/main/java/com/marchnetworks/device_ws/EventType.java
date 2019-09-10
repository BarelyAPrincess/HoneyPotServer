package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType( name = "EventType" )
@XmlEnum
public enum EventType
{
	DELETE( "delete" ),

	NOTIFY( "notify" ),

	UPDATE( "update" );

	private final String value;

	private EventType( String v )
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
