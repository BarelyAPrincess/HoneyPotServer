package com.marchnetworks.command.common.extractor.data;

public enum State
{
	CREATED( "Created" ),
	WAITING( "Waiting" ),
	PAUSED( "Paused" ),
	ACTIVE( "Active" ),
	COMPLETE( "Complete" );

	private String value;

	private State( String value )
	{
		this.value = value;
	}

	public String getValue()
	{
		return value;
	}

	public static State fromValue( String value )
	{
		if ( value != null )
		{
			for ( State def : values() )
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
