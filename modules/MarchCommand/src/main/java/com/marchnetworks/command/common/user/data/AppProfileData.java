package com.marchnetworks.command.common.user.data;

import java.util.HashSet;
import java.util.Set;

public class AppProfileData
{
	private String appId;
	private Set<String> appRights;

	public AppProfileData()
	{
	}

	public AppProfileData( String appId, Set<String> data )
	{
		this.appId = appId;
		appRights = data;
	}

	public String getAppId()
	{
		return appId;
	}

	public void setAppId( String appId )
	{
		this.appId = appId;
	}

	public Set<String> getAppRights()
	{
		if ( appRights == null )
		{
			return new HashSet( 1 );
		}
		return appRights;
	}

	public void setAppRights( Set<String> appRights )
	{
		this.appRights = appRights;
	}

	public int hashCode()
	{
		int prime = 31;
		int result = 1;
		result = 31 * result + ( appId == null ? 0 : appId.hashCode() );
		result = 31 * result + ( appRights == null ? 0 : appRights.hashCode() );
		return result;
	}

	public boolean equals( Object obj )
	{
		if ( this == obj )
			return true;
		if ( obj == null )
			return false;
		if ( getClass() != obj.getClass() )
			return false;
		AppProfileData other = ( AppProfileData ) obj;
		if ( appId == null )
		{
			if ( appId != null )
				return false;
		}
		else if ( !appId.equals( appId ) )
			return false;
		if ( appRights == null )
		{
			if ( appRights != null )
				return false;
		}
		else if ( !appRights.equals( appRights ) )
			return false;
		return true;
	}
}
