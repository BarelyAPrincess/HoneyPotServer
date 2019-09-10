package com.marchnetworks.command.common.device.data;

public enum SwitchType
{
	BUILT_IN( "built_in" ),
	LOCAL( "local" ),
	IP( "ip" ),
	IP_CAMERA( "ip_camera" );

	private String value;

	SwitchType( String value )
	{
		this.value = value;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue( String value )
	{
		this.value = value;
	}

	public static SwitchType fromValue( String value )
	{
		if ( value != null )
		{
			for ( SwitchType def : values() )
			{
				if ( value.equalsIgnoreCase( value ) )
				{
					return def;
				}
			}
		}
		return null;
	}
}
