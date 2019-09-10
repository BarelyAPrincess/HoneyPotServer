package com.marchnetworks.device_ws;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "ArrayOfAlarmSource", propOrder = {"alarmSource"} )
public class ArrayOfAlarmSource
{
	@XmlElement( name = "AlarmSource" )
	protected List<AlarmSource> alarmSource;

	public List<AlarmSource> getAlarmSource()
	{
		if ( alarmSource == null )
		{
			alarmSource = new ArrayList();
		}
		return alarmSource;
	}
}
