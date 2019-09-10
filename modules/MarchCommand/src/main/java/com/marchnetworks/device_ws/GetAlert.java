package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "", propOrder = {"alertId"} )
@XmlRootElement( name = "GetAlert" )
public class GetAlert
{
	@XmlElement( required = true )
	protected String alertId;

	public String getAlertId()
	{
		return alertId;
	}

	public void setAlertId( String value )
	{
		alertId = value;
	}
}
