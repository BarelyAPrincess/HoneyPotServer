package com.marchnetworks.command.common.extractor.data;

public enum ResultState
{
	SUCCESS( "Success" ),
	FAIL( "Fail" ),
	FAIL_NOT_FOUND( "FailNotFound" );

	private String value;

	private ResultState( String value )
	{
		this.value = value;
	}

	public String getValue()
	{
		return value;
	}

	public static ResultState fromValue( String value )
	{
		if ( value != null )
		{
			for ( ResultState def : values() )
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
