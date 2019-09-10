package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "Capabilities", propOrder = {"protocolVersion", "strCapabilities"} )
public class Capabilities
{
	protected int protocolVersion;
	@XmlElement( required = true )
	protected ArrayOfString strCapabilities;

	public int getProtocolVersion()
	{
		return protocolVersion;
	}

	public void setProtocolVersion( int value )
	{
		protocolVersion = value;
	}

	public ArrayOfString getStrCapabilities()
	{
		return strCapabilities;
	}

	public void setStrCapabilities( ArrayOfString value )
	{
		strCapabilities = value;
	}
}
