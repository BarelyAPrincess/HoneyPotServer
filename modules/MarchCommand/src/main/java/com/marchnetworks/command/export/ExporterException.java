package com.marchnetworks.command.export;

public class ExporterException extends Exception
{
	private static final long serialVersionUID = -9137800548645940102L;

	ExporterExceptionTypeEnum error;

	public ExporterException( ExporterExceptionTypeEnum err )
	{
		error = err;
	}

	public ExporterException( ExporterExceptionTypeEnum err, String message )
	{
		super( message );
		error = err;
	}

	public ExporterException( ExporterExceptionTypeEnum err, Throwable cause )
	{
		super( cause );
		error = err;
	}

	public ExporterException( ExporterExceptionTypeEnum err, String message, Throwable cause )
	{
		super( message, cause );
		error = err;
	}

	public ExporterExceptionTypeEnum getError()
	{
		return error;
	}

	public void setError( ExporterExceptionTypeEnum error )
	{
		this.error = error;
	}

	public String toString()
	{
		return "Exporter Exception [error=" + error + "] Details:" + getMessage();
	}
}
