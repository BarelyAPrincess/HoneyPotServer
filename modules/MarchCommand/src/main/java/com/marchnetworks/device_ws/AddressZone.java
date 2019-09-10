package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "AddressZone", propOrder = {"name", "address", "httpPort", "httpsPort"} )
public class AddressZone
{
	@XmlElement( required = true )
	protected String name;
	@XmlElement( required = true )
	protected String address;
	protected Integer httpPort;
	protected Integer httpsPort;

	public String getName()
	{
		return name;
	}

	public void setName( String value )
	{
		name = value;
	}

	public String getAddress()
	{
		return address;
	}

	public void setAddress( String value )
	{
		address = value;
	}

	public Integer getHttpPort()
	{
		return httpPort;
	}

	public void setHttpPort( Integer value )
	{
		httpPort = value;
	}

	public Integer getHttpsPort()
	{
		return httpsPort;
	}

	public void setHttpsPort( Integer value )
	{
		httpsPort = value;
	}
}
