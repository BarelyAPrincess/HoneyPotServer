package com.marchnetworks.command.common.device.data;

public enum AudioOutputType
{
	BUILT_IN( "built_in" ),
	LOCAL( "local" ),
	IP( "ip" ),
	IP_CAMERA( "ip_camera" );

	private String value;

	private AudioOutputType( String value )
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

	public static AudioOutputType fromValue( String value )
	{
		if ( value != null )
		{
			for ( AudioOutputType def : values() )
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
