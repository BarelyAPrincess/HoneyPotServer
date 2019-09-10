package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType( name = "SectorType" )
@XmlEnum
public enum SectorType
{
	NORMAL( "normal" ),

	SHADOW( "shadow" );

	private final String value;

	private SectorType( String v )
	{
		value = v;
	}

	public String value()
	{
		return value;
	}

	public static SectorType fromValue( String v )
	{
		for ( SectorType c : values() )
		{
			if ( c.value.equals( v ) )
			{
				return c;
			}
		}
		throw new IllegalArgumentException( v );
	}
}
