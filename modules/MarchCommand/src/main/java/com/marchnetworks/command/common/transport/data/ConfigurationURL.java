package com.marchnetworks.command.common.transport.data;

public class ConfigurationURL
{
	protected String scheme;

	protected String relativeUrl;

	protected int port;

	public String getScheme()
	{
		return scheme;
	}

	public void setScheme( String value )
	{
		scheme = value;
	}

	public String getRelativeUrl()
	{
		return relativeUrl;
	}

	public void setRelativeUrl( String value )
	{
		relativeUrl = value;
	}

	public int getPort()
	{
		return port;
	}

	public void setPort( int value )
	{
		port = value;
	}
}
