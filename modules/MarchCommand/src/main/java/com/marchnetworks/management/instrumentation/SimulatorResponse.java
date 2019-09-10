package com.marchnetworks.management.instrumentation;

import java.io.UnsupportedEncodingException;

public class SimulatorResponse
{
	private boolean success = false;
	private byte[] response;

	public boolean isSuccess()
	{
		return success;
	}

	public void setSuccess( boolean success )
	{
		this.success = success;
	}

	public byte[] getResponse()
	{
		return response;
	}

	public void setResponse( byte[] response )
	{
		this.response = response;
	}

	public String getResponseAsString()
	{
		try
		{
			return new String( response, "UTF-8" );
		}
		catch ( UnsupportedEncodingException e )
		{
			throw new RuntimeException( e );
		}
	}
}

