package com.marchnetworks.map.service;

import javax.xml.ws.WebFault;

@WebFault( name = "MapFault" )
public class MapException extends Exception
{
	MapExceptionTypeEnum error;

	public MapException( MapExceptionTypeEnum err )
	{
		error = err;
	}

	public MapException( MapExceptionTypeEnum err, String message )
	{
		super( message );
		error = err;
	}

	public MapException( MapExceptionTypeEnum err, Throwable cause )
	{
		super( cause );
		error = err;
	}

	public MapException( MapExceptionTypeEnum err, String message, Throwable cause )
	{
		super( message, cause );
		error = err;
	}

	public MapExceptionTypeEnum getError()
	{
		return error;
	}

	public void setError( MapExceptionTypeEnum error )
	{
		this.error = error;
	}

	public String toString()
	{
		return "MapException [error=" + error + "]";
	}
}

