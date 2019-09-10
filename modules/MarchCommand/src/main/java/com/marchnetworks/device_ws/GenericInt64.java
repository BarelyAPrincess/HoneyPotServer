package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "GenericInt64", propOrder = {"value"} )
public class GenericInt64 extends GenericValue
{
	protected long value;

	public long getValue()
	{
		return value;
	}

	public void setValue( long value )
	{
		this.value = value;
	}
}
