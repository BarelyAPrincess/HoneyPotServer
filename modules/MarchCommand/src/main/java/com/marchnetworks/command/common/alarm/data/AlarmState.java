package com.marchnetworks.command.common.alarm.data;

public enum AlarmState
{
	ON( "on" ),
	OFF( "off" ),
	NOTREADY( "notready" ),
	DISABLED( "disabled" );

	private String value;

	private AlarmState( String value )
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

	public static AlarmState fromValue( String value )
	{
		if ( value != null )
		{
			for ( AlarmState def : values() )
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
