package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "AlertConfig", propOrder = {"id", "thresholds"} )
public class AlertConfig
{
	@XmlElement( required = true )
	protected String id;
	@XmlElement( required = true )
	protected ArrayOfAlertThreshold thresholds;

	public String getId()
	{
		return id;
	}

	public void setId( String value )
	{
		id = value;
	}

	public ArrayOfAlertThreshold getThresholds()
	{
		return thresholds;
	}

	public void setThresholds( ArrayOfAlertThreshold value )
	{
		thresholds = value;
	}
}
