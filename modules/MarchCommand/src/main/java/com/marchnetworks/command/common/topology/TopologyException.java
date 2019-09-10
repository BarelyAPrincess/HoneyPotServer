package com.marchnetworks.command.common.topology;

import javax.xml.ws.WebFault;

@WebFault( name = "TopologyFault" )
public class TopologyException extends Exception
{
	TopologyExceptionTypeEnum error;

	public TopologyException( TopologyExceptionTypeEnum err )
	{
		error = err;
	}

	public TopologyException( TopologyExceptionTypeEnum err, String message )
	{
		super( message );
		error = err;
	}

	public TopologyException( TopologyExceptionTypeEnum err, Throwable cause )
	{
		super( cause );
		error = err;
	}

	public TopologyException( TopologyExceptionTypeEnum err, String message, Throwable cause )
	{
		super( message, cause );
		error = err;
	}

	public TopologyExceptionTypeEnum getError()
	{
		return error;
	}

	public void setError( TopologyExceptionTypeEnum error )
	{
		this.error = error;
	}

	public String toString()
	{
		return "TopologyException [error=" + error + "] Details:" + getMessage();
	}
}
