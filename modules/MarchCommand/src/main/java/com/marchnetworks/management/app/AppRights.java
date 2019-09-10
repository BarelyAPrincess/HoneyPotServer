package com.marchnetworks.management.app;

import java.util.Set;

public class AppRights
{
	private String appId;
	private Set<String> rights;

	public String getAppId()
	{
		return appId;
	}

	public void setAppId( String appId )
	{
		this.appId = appId;
	}

	public Set<String> getRights()
	{
		return rights;
	}

	public void setRights( Set<String> rights )
	{
		this.rights = rights;
	}

	public AppRights( String appId, Set<String> rights )
	{
		this.appId = appId;
		this.rights = rights;
	}

	public AppRights()
	{
	}
}
