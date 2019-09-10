package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "", propOrder = {"getServiceCapabilitiesResult"} )
@XmlRootElement( name = "GetServiceCapabilitiesResponse" )
public class GetServiceCapabilitiesResponse
{
	@XmlElement( name = "GetServiceCapabilitiesResult", required = true )
	protected Capabilities getServiceCapabilitiesResult;

	public Capabilities getGetServiceCapabilitiesResult()
	{
		return getServiceCapabilitiesResult;
	}

	public void setGetServiceCapabilitiesResult( Capabilities value )
	{
		getServiceCapabilitiesResult = value;
	}
}
