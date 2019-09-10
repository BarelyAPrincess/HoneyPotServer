package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "", propOrder = {"alertIds"} )
@XmlRootElement( name = "CloseAlerts" )
public class CloseAlerts
{
	@XmlElement( required = true )
	protected ArrayOfString alertIds;

	public ArrayOfString getAlertIds()
	{
		return alertIds;
	}

	public void setAlertIds( ArrayOfString value )
	{
		alertIds = value;
	}
}
