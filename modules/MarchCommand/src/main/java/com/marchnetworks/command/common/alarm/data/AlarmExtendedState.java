package com.marchnetworks.command.common.alarm.data;

public enum AlarmExtendedState
{
	BADCONFIG( "badconfig" ),
	CUT( "cut" ),
	DISCONNECTED( "disconnected" ),
	LEARNING( "learning" );

	private String value;

	AlarmExtendedState( String value )
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

	public static AlarmExtendedState fromValue( String value )
	{
		if ( value != null )
		{
			for ( AlarmExtendedState def : values() )
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
