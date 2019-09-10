package com.marchnetworks.management.data;

public class ModelVersionsForUPG
{
	private String family;
	private String model;
	private String minVersion;
	private String maxVersion;

	public void setFamily( String family )
	{
		this.family = family;
	}

	public String getFamily()
	{
		return family;
	}

	public void setModel( String model )
	{
		this.model = model;
	}

	public String getModel()
	{
		return model;
	}

	public void setMinversion( String minVersion )
	{
		this.minVersion = minVersion;
	}

	public String getMinversion()
	{
		return minVersion;
	}

	public void setMaxversion( String maxVersion )
	{
		this.maxVersion = maxVersion;
	}

	public String getMaxversion()
	{
		return maxVersion;
	}
}

