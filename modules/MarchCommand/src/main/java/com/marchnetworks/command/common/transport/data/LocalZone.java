package com.marchnetworks.command.common.transport.data;

import java.util.Arrays;

public class LocalZone
{
	private String[] address;
	private int httpPort;
	private int httpsPort;

	public String[] getAddress()
	{
		return address;
	}

	public void setAddress( String[] address )
	{
		this.address = address;
	}

	public int getHttpPort()
	{
		return httpPort;
	}

	public void setHttpPort( int httpPort )
	{
		this.httpPort = httpPort;
	}

	public int getHttpsPort()
	{
		return httpsPort;
	}

	public void setHttpsPort( int httpsPort )
	{
		this.httpsPort = httpsPort;
	}

	public int hashCode()
	{
		int prime = 31;
		int result = 1;
		result = 31 * result + Arrays.hashCode( address );
		result = 31 * result + httpPort;
		result = 31 * result + httpsPort;
		return result;
	}

	public boolean equals( Object obj )
	{
		if ( this == obj )
			return true;
		if ( obj == null )
			return false;
		if ( getClass() != obj.getClass() )
			return false;
		LocalZone other = ( LocalZone ) obj;
		if ( !Arrays.equals( address, address ) )
			return false;
		if ( httpPort != httpPort )
			return false;
		if ( httpsPort != httpsPort )
			return false;
		return true;
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer( "" );
		sb.append( "addresses:" );
		sb.append( Arrays.toString( address ) );
		sb.append( "; httpPort:" );
		sb.append( httpPort );
		sb.append( "; httpsPort:" );
		sb.append( httpsPort );
		return sb.toString();
	}
}
