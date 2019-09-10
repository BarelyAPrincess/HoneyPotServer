package com.marchnetworks.device_ws;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "ArrayOfAlertThreshold", propOrder = {"alertThreshold"} )
public class ArrayOfAlertThreshold
{
	@XmlElement( name = "AlertThreshold" )
	protected List<AlertThreshold> alertThreshold;

	public List<AlertThreshold> getAlertThreshold()
	{
		if ( alertThreshold == null )
		{
			alertThreshold = new ArrayList();
		}
		return alertThreshold;
	}
}
