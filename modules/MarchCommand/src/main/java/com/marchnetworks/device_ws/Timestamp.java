package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "Timestamp", propOrder = {"ticks"} )
public class Timestamp
{
	protected long ticks;

	public long getTicks()
	{
		return ticks;
	}

	public void setTicks( long value )
	{
		ticks = value;
	}
}
