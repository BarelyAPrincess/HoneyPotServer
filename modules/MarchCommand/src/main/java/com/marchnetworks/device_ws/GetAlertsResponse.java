package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "", propOrder = {"getAlertsResult"} )
@XmlRootElement( name = "GetAlertsResponse" )
public class GetAlertsResponse
{
	@XmlElement( name = "GetAlertsResult", required = true )
	protected ArrayOfAlertEntry getAlertsResult;

	public ArrayOfAlertEntry getGetAlertsResult()
	{
		return getAlertsResult;
	}

	public void setGetAlertsResult( ArrayOfAlertEntry value )
	{
		getAlertsResult = value;
	}
}
