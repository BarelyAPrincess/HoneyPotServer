package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "", propOrder = {"getSystemDetailsResult"} )
@XmlRootElement( name = "GetSystemDetailsResponse" )
public class GetSystemDetailsResponse
{
	@XmlElement( name = "GetSystemDetailsResult", required = true )
	protected DeviceDetails getSystemDetailsResult;

	public DeviceDetails getGetSystemDetailsResult()
	{
		return getSystemDetailsResult;
	}

	public void setGetSystemDetailsResult( DeviceDetails value )
	{
		getSystemDetailsResult = value;
	}
}
