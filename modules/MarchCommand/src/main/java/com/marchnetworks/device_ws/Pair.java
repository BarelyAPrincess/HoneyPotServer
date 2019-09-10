package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "Pair", propOrder = {"name", "value"} )
public class Pair
{
	@XmlElement( required = true )
	protected String name;
	@XmlElement( required = true )
	protected String value;

	public String getName()
	{
		return name;
	}

	public void setName( String value )
	{
		name = value;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue( String value )
	{
		this.value = value;
	}
}
