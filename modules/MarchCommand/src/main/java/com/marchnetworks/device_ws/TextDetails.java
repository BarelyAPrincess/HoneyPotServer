package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "TextDetails", propOrder = {"encoderId", "protocolName"} )
public class TextDetails
{
	@XmlElement( required = true )
	protected String encoderId;
	@XmlElement( required = true )
	protected String protocolName;

	public String getEncoderId()
	{
		return encoderId;
	}

	public void setEncoderId( String value )
	{
		encoderId = value;
	}

	public String getProtocolName()
	{
		return protocolName;
	}

	public void setProtocolName( String value )
	{
		protocolName = value;
	}
}
