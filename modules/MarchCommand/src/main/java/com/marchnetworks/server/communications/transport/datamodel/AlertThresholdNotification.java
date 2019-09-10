package com.marchnetworks.server.communications.transport.datamodel;

public enum AlertThresholdNotification
{
	ALWAYS( "always" ),
	NEVER( "never" ),
	FREQUENCY( "frequency" ),
	DURATION( "duration" ),
	FREQUENCY_AND_DURATION( "duration_and_frequency" );

	private final String value;

	private AlertThresholdNotification( String v )
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

