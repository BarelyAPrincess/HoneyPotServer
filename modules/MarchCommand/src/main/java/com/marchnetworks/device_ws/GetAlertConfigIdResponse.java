package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "", propOrder = {"getAlertConfigIdResult"} )
@XmlRootElement( name = "GetAlertConfigIdResponse" )
public class GetAlertConfigIdResponse
{
	@XmlElement( name = "GetAlertConfigIdResult", required = true )
	protected String getAlertConfigIdResult;

	public String getGetAlertConfigIdResult()
	{
		return getAlertConfigIdResult;
	}

	public void setGetAlertConfigIdResult( String value )
	{
		getAlertConfigIdResult = value;
	}
}

