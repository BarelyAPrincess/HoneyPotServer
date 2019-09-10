package com.marchnetworks.common.event.util;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "Timestamp", propOrder = {"tm", "ticks"} )
public class Timestamp
{
	@XmlElement( required = true )
	@XmlSchemaType( name = "dateTime" )
	private XMLGregorianCalendar tm;
	private long ticks;

	public XMLGregorianCalendar getTm()
	{
		return tm;
	}

	public void setTm( XMLGregorianCalendar value )
	{
		tm = value;
	}

	public long getTicks()
	{
		return ticks;
	}

	public void setTicks( long value )
	{
		ticks = value;
	}
}
