package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType( name = "FrameRequestType" )
@XmlEnum
public enum FrameRequestType
{
	I_FRAME( "i_frame" ),

	I_P_FRAME( "i_p_frame" ),

	ALL( "all" );

	private final String value;

	private FrameRequestType( String v )
	{
		value = v;
	}

	public String value()
	{
		return value;
	}

	public static FrameRequestType fromValue( String v )
	{
		for ( FrameRequestType c : values() )
		{
			if ( c.value.equals( v ) )
			{
				return c;
			}
		}
		throw new IllegalArgumentException( v );
	}
}
