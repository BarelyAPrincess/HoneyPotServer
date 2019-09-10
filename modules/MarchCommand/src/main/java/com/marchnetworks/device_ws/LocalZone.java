package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "LocalZone", propOrder = {"address", "httpPort", "httpsPort"} )
public class LocalZone
{
	protected ArrayOfString address;
	protected int httpPort;
	protected int httpsPort;

	public ArrayOfString getAddress()
	{
		return address;
	}

	public void setAddress( ArrayOfString value )
	{
		address = value;
	}

	public int getHttpPort()
	{
		return httpPort;
	}

	public void setHttpPort( int value )
	{
		httpPort = value;
	}

	public int getHttpsPort()
	{
		return httpsPort;
	}

	public void setHttpsPort( int value )
	{
		httpsPort = value;
	}
}
