package com.marchnetworks.command.common.device.data;

public enum AudioOutputState
{
	DISABLED( "disabled" ),
	FREE( "free" ),
	INUSE( "inuse" ),
	OFFLINE( "offline" );

	private String value;

	private AudioOutputState( String value )
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

	public static AudioOutputState fromValue( String value )
	{
		if ( value != null )
		{
			for ( AudioOutputState def : values() )
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
