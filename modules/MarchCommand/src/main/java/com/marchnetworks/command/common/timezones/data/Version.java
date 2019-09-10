package com.marchnetworks.command.common.timezones.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType( XmlAccessType.FIELD )
public class Version
{
	@XmlAttribute
	private String number;

	public Version()
	{
	}

	public Version( String number )
	{
		this.number = number;
	}

	public String getNumber()
	{
		return number;
	}

	public void setNumber( String number )
	{
		this.number = number;
	}

	public String toString()
	{
		return String.format( "Version [number=%s]", new Object[] {number} );
	}
}
