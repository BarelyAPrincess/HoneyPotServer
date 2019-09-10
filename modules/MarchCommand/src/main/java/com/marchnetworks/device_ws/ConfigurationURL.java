package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "ConfigurationURL", propOrder = {"scheme", "relativeUrl", "port"} )
public class ConfigurationURL
{
	@XmlElement( required = true )
	protected String scheme;
	@XmlElement( required = true )
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
