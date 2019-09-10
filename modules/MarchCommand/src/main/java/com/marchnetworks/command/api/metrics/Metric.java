package com.marchnetworks.command.api.metrics;

import java.util.List;

public abstract class Metric
{
	protected String name;

	public abstract String getValueString();

	public List<String> getAdditionalInfo()
	{
		return null;
	}

	public List<List<String>> getAdditionalTable()
	{
		return null;
	}

	public void onSerialization()
	{
	}

	public Metric()
	{
	}

	public Metric( String name )
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	public String toString()
	{
		return name + ", " + getValueString();
	}
}
