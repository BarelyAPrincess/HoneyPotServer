package com.marchnetworks.command.common.transport.data;

public class Pair
{
	protected String name;

	protected String value;

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

	public String toString()
	{
		return name + " : " + value;
	}
}
