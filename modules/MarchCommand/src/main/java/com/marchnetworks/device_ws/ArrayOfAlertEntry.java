package com.marchnetworks.device_ws;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "ArrayOfAlertEntry", propOrder = {"alertEntry"} )
public class ArrayOfAlertEntry
{
	@XmlElement( name = "AlertEntry" )
	protected List<AlertEntry> alertEntry;

	public List<AlertEntry> getAlertEntry()
	{
		if ( alertEntry == null )
		{
			alertEntry = new ArrayList();
		}
		return alertEntry;
	}
}
