package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "DataDetails", propOrder = {"encoderId", "codec"} )
public class DataDetails
{
	@XmlElement( required = true )
	protected String encoderId;
	@XmlElement( required = true )
	protected String codec;

	public String getEncoderId()
	{
		return encoderId;
	}

	public void setEncoderId( String value )
	{
		encoderId = value;
	}

	public String getCodec()
	{
		return codec;
	}

	public void setCodec( String value )
	{
		codec = value;
	}
}
