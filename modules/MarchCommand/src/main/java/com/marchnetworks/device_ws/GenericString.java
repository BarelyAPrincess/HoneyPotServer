package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "GenericString", propOrder = {"value"} )
public class GenericString extends GenericValue
{
	@XmlElement( required = true )
	protected String value;

	public String getValue()
	{
		return value;
	}

	public void setValue( String value )
	{
		this.value = value;
	}
}
