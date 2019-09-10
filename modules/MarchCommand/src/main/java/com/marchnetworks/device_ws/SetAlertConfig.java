package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "", propOrder = {"alertConfig"} )
@XmlRootElement( name = "SetAlertConfig" )
public class SetAlertConfig
{
	@XmlElement( required = true )
	protected AlertConfig alertConfig;

	public AlertConfig getAlertConfig()
	{
		return alertConfig;
	}

	public void setAlertConfig( AlertConfig value )
	{
		alertConfig = value;
	}
}
