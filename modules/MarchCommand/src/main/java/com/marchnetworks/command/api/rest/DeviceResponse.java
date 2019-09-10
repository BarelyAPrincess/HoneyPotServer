package com.marchnetworks.command.api.rest;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

public class DeviceResponse
{
	private byte[] response;
	private Map<String, List<String>> responseHeaders;

	public String getFirstHeader( String headerName )
	{
		if ( responseHeaders != null )
		{
			List<String> headers = ( List ) responseHeaders.get( headerName );
			if ( ( headers != null ) && ( !headers.isEmpty() ) )
			{
				return ( String ) headers.get( 0 );
			}
		}
		return null;
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

	public Map<String, List<String>> getResponseHeaders()
	{
		return responseHeaders;
	}

	public void setResponseHeaders( Map<String, List<String>> responseHeaders )
	{
		this.responseHeaders = responseHeaders;
	}

	public byte[] getResponse()
	{
		return response;
	}

	public void setResponse( byte[] response )
	{
		this.response = response;
	}
}
