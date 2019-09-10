package com.marchnetworks.command.common.device.data;

public enum SwitchState
{
	DISABLED( "disabled" ),
	UNKNOWN( "unknown" ),
	ON( "on" ),
	OFF( "off" ),
	OFFLINE( "offline" );

	private String value;

	SwitchState( String value )
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

	public static SwitchState fromValue( String value )
	{
		if ( value != null )
		{
			for ( SwitchState def : values() )
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
