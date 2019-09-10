package com.marchnetworks.management.firmware.service;

public class FirmwareException extends Exception
{
	private static final long serialVersionUID = 7566593825316674769L;
	private FirmwareExceptionTypeEnum error;

	public FirmwareExceptionTypeEnum getErrorType()
	{
		return error;
	}

	public void setErrorType( FirmwareExceptionTypeEnum error )
	{
		this.error = error;
	}

	public FirmwareException()
	{
	}

	public FirmwareException( FirmwareExceptionTypeEnum error, String msg )
	{
		super( msg );
		this.error = error;
	}

	public FirmwareException( String msg )
	{
		super( msg );
	}

	public FirmwareException( Throwable throwable )
	{
		super( throwable );
	}

	public FirmwareException( String msg, Throwable cause )
	{
		super( msg, cause );
	}
}

