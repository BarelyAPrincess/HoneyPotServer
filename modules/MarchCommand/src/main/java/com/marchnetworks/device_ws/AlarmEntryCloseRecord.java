package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "AlarmEntryCloseRecord", propOrder = {"entryId", "closingText", "closedUser", "closedTime"} )
public class AlarmEntryCloseRecord
{
	@XmlElement( required = true )
	protected String entryId;
	@XmlElement( required = true )
	protected String closingText;
	protected String closedUser;
	@XmlSchemaType( name = "dateTime" )
	protected XMLGregorianCalendar closedTime;

	public String getEntryId()
	{
		return entryId;
	}

	public void setEntryId( String value )
	{
		entryId = value;
	}

	public String getClosingText()
	{
		return closingText;
	}

	public void setClosingText( String value )
	{
		closingText = value;
	}

	public String getClosedUser()
	{
		return closedUser;
	}

	public void setClosedUser( String value )
	{
		closedUser = value;
	}

	public XMLGregorianCalendar getClosedTime()
	{
		return closedTime;
	}

	public void setClosedTime( XMLGregorianCalendar value )
	{
		closedTime = value;
	}
}

