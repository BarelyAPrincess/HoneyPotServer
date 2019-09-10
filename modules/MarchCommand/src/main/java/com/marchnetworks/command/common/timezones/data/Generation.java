package com.marchnetworks.command.common.timezones.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType( XmlAccessType.FIELD )
public class Generation
{
	@XmlAttribute
	private String date;

	public Generation()
	{
	}

	public Generation( String date )
	{
		this.date = date;
	}

	public String getDate()
	{
		return date;
	}

	public void setDate( String date )
	{
		this.date = date;
	}

	public String toString()
	{
		return String.format( "Generation [date=%s]", new Object[] {date} );
	}
}
