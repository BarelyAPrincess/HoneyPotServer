package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType( name = "AlertThresholdNotification" )
@XmlEnum
public enum AlertThresholdNotification
{
	ALWAYS( "always" ),

	NEVER( "never" ),

	FREQUENCY( "frequency" ),

	DURATION( "duration" ),

	DURATION_AND_FREQUENCY( "duration_and_frequency" );

	private final String value;

	AlertThresholdNotification( String v )
	{
		value = v;
	}

	public String value()
	{
		return value;
	}

	public static AlertThresholdNotification fromValue( String v )
	{
		for ( AlertThresholdNotification c : values() )
		{
			if ( c.value.equals( v ) )
			{
				return c;
			}
		}
		throw new IllegalArgumentException( v );
	}
}
