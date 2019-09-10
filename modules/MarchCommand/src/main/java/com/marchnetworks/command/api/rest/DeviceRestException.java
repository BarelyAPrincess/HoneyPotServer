package com.marchnetworks.command.api.rest;

import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLHandshakeException;

public class DeviceRestException extends Exception
{
	private int code = 0;
	private Map<String, List<String>> responseHeaders;

	public DeviceRestException( String message )
	{
		super( message );
	}

	public DeviceRestException( Throwable cause )
	{
		super( cause );
	}

	public DeviceRestException( String message, Throwable cause )
	{
		super( message, cause );
	}

	public DeviceRestException( String message, int statusCode, Map<String, List<String>> responseHeaders, Throwable cause )
	{
		super( message );
		if ( cause != null )
		{
			initCause( cause );
		}
		code = statusCode;
		this.responseHeaders = responseHeaders;
	}

	public DeviceRestException( Throwable cause, int statusCode )
	{
		super( cause );
		code = statusCode;
	}

	public DeviceRestException( String message, Throwable cause, int statusCode )
	{
		super( message, cause );
		code = statusCode;
	}

	public int getErrorCode()
	{
		return code;
	}

	public Map<String, List<String>> getResponseHeaders()
	{
		return responseHeaders;
	}

	public void setResponseHeaders( Map<String, List<String>> responseHeaders )
	{
		this.responseHeaders = responseHeaders;
	}

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

	public DeviceRestErrorEnum getError()
	{
		if ( code == 401 )
			return DeviceRestErrorEnum.ERROR_UNAUTHORIZED;
		if ( ( getCause() != null ) && ( ( getCause() instanceof SocketTimeoutException ) ) )
			return DeviceRestErrorEnum.ERROR_SOCKET_TIMEOUT;
		if ( ( getCause() != null ) && ( ( getCause() instanceof SSLHandshakeException ) ) )
		{
			return DeviceRestErrorEnum.ERROR_SSL_HANDSHAKE;
		}
		return DeviceRestErrorEnum.ERROR_SERVER_ERROR;
	}
}
