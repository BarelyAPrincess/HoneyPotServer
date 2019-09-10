package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "", propOrder = {"getAlertConfigResult"} )
@XmlRootElement( name = "GetAlertConfigResponse" )
public class GetAlertConfigResponse
{
	@XmlElement( name = "GetAlertConfigResult", required = true )
	protected AlertConfig getAlertConfigResult;

	public AlertConfig getGetAlertConfigResult()
	{
		return getAlertConfigResult;
	}

	public void setGetAlertConfigResult( AlertConfig value )
	{
		getAlertConfigResult = value;
	}
}
