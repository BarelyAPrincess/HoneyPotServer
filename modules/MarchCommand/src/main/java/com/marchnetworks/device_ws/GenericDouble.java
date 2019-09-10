package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "GenericDouble", propOrder = {"value"} )
public class GenericDouble extends GenericValue
{
	protected double value;

	public double getValue()
	{
		return value;
	}

	public void setValue( double value )
	{
		this.value = value;
	}
}
