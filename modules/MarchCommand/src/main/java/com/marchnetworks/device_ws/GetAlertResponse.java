package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "", propOrder = {"getAlertResult"} )
@XmlRootElement( name = "GetAlertResponse" )
public class GetAlertResponse
{
	@XmlElement( name = "GetAlertResult", required = true )
	protected AlertEntry getAlertResult;

	public AlertEntry getGetAlertResult()
	{
		return getAlertResult;
	}

	public void setGetAlertResult( AlertEntry value )
	{
		getAlertResult = value;
	}
}
