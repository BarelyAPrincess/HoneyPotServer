package com.marchnetworks.command.common.extractor.data;

public enum CompletionState
{
	SUCCESS( "Success" ),
	SUCCESS_MISSING_DATA( "SuccessMissingData" ),
	NO_DATA( "NoData" ),
	FAIL_TIMEOUT( "FailTimeout" ),
	FAIL_LOCAL_STORAGE( "FailLocalStorage" ),
	FAIL_OTHER( "FailOther" );

	private String value;

	private CompletionState( String value )
	{
		this.value = value;
	}

	public String getValue()
	{
		return value;
	}

	public static CompletionState fromValue( String value )
	{
		if ( value != null )
		{
			for ( CompletionState def : values() )
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
