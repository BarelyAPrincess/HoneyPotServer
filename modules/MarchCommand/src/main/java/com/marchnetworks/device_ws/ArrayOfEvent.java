package com.marchnetworks.device_ws;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "ArrayOfEvent", propOrder = {"event"} )
public class ArrayOfEvent
{
	@XmlElement( name = "Event" )
	protected List<Event> event;

	public List<Event> getEvent()
	{
		if ( event == null )
		{
			event = new ArrayList();
		}
		return event;
	}
}
