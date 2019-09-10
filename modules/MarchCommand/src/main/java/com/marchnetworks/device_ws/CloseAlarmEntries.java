package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "", propOrder = {"closeRecords"} )
@XmlRootElement( name = "CloseAlarmEntries" )
public class CloseAlarmEntries
{
	@XmlElement( required = true )
	protected ArrayOfAlarmEntryCloseRecord closeRecords;

	public ArrayOfAlarmEntryCloseRecord getCloseRecords()
	{
		return closeRecords;
	}

	public void setCloseRecords( ArrayOfAlarmEntryCloseRecord value )
	{
		closeRecords = value;
	}
}
