package com.marchnetworks.command.common.license.data;

public class AppLicenseInfo
{
	private String appId;

	private String name;

	private boolean valid;

	private int count;

	private Long[] resources;

	private String[] resourceTypes;

	public AppLicenseInfo( String appId, String name, boolean valid, int count, Long[] resources, String[] resourceTypes )
	{
		this.appId = appId;
		this.name = name;
		this.valid = valid;
		this.count = count;
		this.resources = resources;
		this.resourceTypes = resourceTypes;
	}

	public String getAppId()
	{
		return appId;
	}

	public void setAppId( String appId )
	{
		this.appId = appId;
	}

	public String getName()
	{
		return name;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	public boolean isValid()
	{
		return valid;
	}

	public void setValid( boolean valid )
	{
		this.valid = valid;
	}

	public int getCount()
	{
		return count;
	}

	public void setCount( int count )
	{
		this.count = count;
	}

	public Long[] getResources()
	{
		return resources;
	}

	public void setResources( Long[] resources )
	{
		this.resources = resources;
	}

	public String[] getResourceTypes()
	{
		return resourceTypes;
	}

	public void setResourceTypes( String[] resourceTypes )
	{
		this.resourceTypes = resourceTypes;
	}
}
