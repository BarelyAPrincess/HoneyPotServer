package com.marchnetworks.device_ws;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "ArrayOfAlarmEntryCloseRecord", propOrder = {"alarmEntryCloseRecord"} )
public class ArrayOfAlarmEntryCloseRecord
{
	@XmlElement( name = "AlarmEntryCloseRecord" )
	protected List<AlarmEntryCloseRecord> alarmEntryCloseRecord;

	public List<AlarmEntryCloseRecord> getAlarmEntryCloseRecord()
	{
		if ( alarmEntryCloseRecord == null )
		{
			alarmEntryCloseRecord = new ArrayList();
		}
		return alarmEntryCloseRecord;
	}
}
