package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType( name = "MediaType" )
@XmlEnum
public enum MediaType
{
	VIDEO( "video" ),

	AUDIO( "audio" ),

	TEXT( "text" );

	private final String value;

	private MediaType( String v )
	{
		value = v;
	}

	public String value()
	{
		return value;
	}

	public static MediaType fromValue( String v )
	{
		for ( MediaType c : values() )
		{
			if ( c.value.equals( v ) )
			{
				return c;
			}
		}
		throw new IllegalArgumentException( v );
	}
}
