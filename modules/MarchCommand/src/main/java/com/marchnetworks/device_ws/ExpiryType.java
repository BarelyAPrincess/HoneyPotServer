package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType( name = "ExpiryType" )
@XmlEnum
public enum ExpiryType
{
	TRIAL( "trial" ),

	PERM( "perm" );

	private final String value;

	private ExpiryType( String v )
	{
		value = v;
	}

	public String value()
	{
		return value;
	}

	public static ExpiryType fromValue( String v )
	{
		for ( ExpiryType c : values() )
		{
			if ( c.value.equals( v ) )
			{
				return c;
			}
		}
		throw new IllegalArgumentException( v );
	}
}
