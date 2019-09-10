package com.marchnetworks.common.diagnostics;

public enum DiagnosticResultType
{
	OK( "OK" ),
	WARNING( "Warning" ),
	FAILURE( "Failure" );

	private String code;

	private DiagnosticResultType( String code )
	{
		this.code = code;
	}

	public String getCode()
	{
		return code;
	}
}
