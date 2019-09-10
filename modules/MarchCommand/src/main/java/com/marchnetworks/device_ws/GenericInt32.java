package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "GenericInt32", propOrder = {"value"} )
public class GenericInt32 extends GenericValue
{
	protected int value;

	public int getValue()
	{
		return value;
	}

	public void setValue( int value )
	{
		this.value = value;
	}
}
