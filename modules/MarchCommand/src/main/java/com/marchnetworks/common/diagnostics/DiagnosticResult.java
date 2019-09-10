package com.marchnetworks.common.diagnostics;

public class DiagnosticResult
{
	private DiagnosticResultType type;
	private DiagnosticError error;
	private String message;
	public static final DiagnosticResult RESULT_OK = new DiagnosticResult( DiagnosticResultType.OK, null, null );

	public DiagnosticResult( DiagnosticResultType type, DiagnosticError error, String message )
	{
		setType( type );
		setError( error );
		setMessage( message );
	}

	public void setType( DiagnosticResultType type )
	{
		this.type = type;
	}

	public DiagnosticResultType getType()
	{
		return type;
	}

	public void setError( DiagnosticError error )
	{
		this.error = error;
	}

	public DiagnosticError getError()
	{
		return error;
	}

	public void setMessage( String message )
	{
		this.message = message;
	}

	public String getMessage()
	{
		return message;
	}
}
