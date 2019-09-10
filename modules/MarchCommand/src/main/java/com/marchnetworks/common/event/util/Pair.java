package com.marchnetworks.common.event.util;

import javax.xml.bind.annotation.XmlType;

@XmlType( name = "Pair" )
public class Pair
{
	private String name;
	private String value;

	public Pair()
	{
	}

	public Pair( String name, String value )
	{
		this.name = name;
		this.value = value;
	}

	public String getName()
	{
		return name;
	}

	public String getValue()
	{
		return value;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	public void setValue( String value )
	{
		this.value = value;
	}
}
