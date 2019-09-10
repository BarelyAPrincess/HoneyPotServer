package com.marchnetworks.management.file.service;

import javax.xml.ws.WebFault;

@WebFault
public class FileStorageException extends Exception
{
	private static final long serialVersionUID = -852132075742175006L;
	private FileStorageExceptionType error = FileStorageExceptionType.NO_ERROR;

	public FileStorageExceptionType getErrorType()
	{
		return error;
	}

	public void setErrorType( FileStorageExceptionType error )
	{
		this.error = error;
	}

	public FileStorageException()
	{
	}

	public FileStorageException( FileStorageExceptionType error, String msg )
	{
		super( msg );
		this.error = error;
	}

	public FileStorageException( String msg )
	{
		super( msg );
	}

	public FileStorageException( Throwable throwable )
	{
		super( throwable );
	}

	public FileStorageException( String msg, Throwable cause )
	{
		super( msg, cause );
	}
}

