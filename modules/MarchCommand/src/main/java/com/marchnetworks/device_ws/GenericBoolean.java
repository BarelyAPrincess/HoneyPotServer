package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "GenericBoolean", propOrder = {"value"} )
public class GenericBoolean extends GenericValue
{
	protected boolean value;

	public boolean isValue()
	{
		return value;
	}

	public void setValue( boolean value )
	{
		this.value = value;
	}
}
