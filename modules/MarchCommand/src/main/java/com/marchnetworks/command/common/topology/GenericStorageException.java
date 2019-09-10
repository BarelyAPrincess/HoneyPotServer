package com.marchnetworks.command.common.topology;

import javax.xml.ws.WebFault;

@WebFault( name = "GenericStorageFault" )
public class GenericStorageException extends Exception implements ExpectedException
{
	private GenericStorageExceptionType faultCode;

	public GenericStorageException( GenericStorageExceptionType faultCode, String message )
	{
		super( message );
		this.faultCode = faultCode;
	}

	public GenericStorageException( GenericStorageExceptionType faultCode, String msg, Exception inner )
	{
		super( msg, inner );
		this.faultCode = faultCode;
	}

	public GenericStorageExceptionType getFaultCode()
	{
		return faultCode;
	}

	public void setFaultCode( GenericStorageExceptionType faultCode )
	{
		this.faultCode = faultCode;
	}
}
