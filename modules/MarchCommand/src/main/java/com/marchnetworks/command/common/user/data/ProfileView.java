package com.marchnetworks.command.common.user.data;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class ProfileView
{
	private String profileId;
	private String name;
	private String description;
	private boolean simplifiedUI;
	private boolean superAdmin;
	private Set<RightEnum> profileRights = new HashSet();

	private Set<String> profileAppRights = new LinkedHashSet();

	private Set<AppProfileData> appProfileData = new HashSet();

	public String getProfileId()
	{
		return profileId;
	}

	public void setProfileId( String profileId )
	{
		this.profileId = profileId;
	}

	public String getName()
	{
		return name;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	public Set<RightEnum> getProfileRights()
	{
		return profileRights;
	}

	public void setProfileRights( Set<RightEnum> profileRights )
	{
		this.profileRights = profileRights;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription( String description )
	{
		this.description = description;
	}

	public Set<String> getProfileAppRights()
	{
		return profileAppRights;
	}

	public void setProfileAppRights( Set<String> profileAppRights )
	{
		this.profileAppRights = profileAppRights;
	}

	public Set<AppProfileData> getAppProfileData()
	{
		return appProfileData;
	}

	public void setAppProfileData( Set<AppProfileData> appProfileData )
	{
		this.appProfileData = appProfileData;
	}

	public boolean isSuperAdmin()
	{
		return superAdmin;
	}

	public void setSuperAdmin( boolean superAdmin )
	{
		this.superAdmin = superAdmin;
	}

	public Set<String> getAppRights( String appId )
	{
		for ( AppProfileData appData : appProfileData )
		{
			if ( appData.getAppId().equals( appId ) )
			{
				return appData.getAppRights();
			}
		}
		return null;
	}

	public boolean hasRight( String right )
	{
		for ( RightEnum coreRight : profileRights )
		{
			if ( coreRight.name().equals( right ) )
			{
				return true;
			}
		}

		for ( AppProfileData appRight : appProfileData )
		{
			if ( appRight.getAppRights().contains( right ) )
			{
				return true;
			}
		}
		return false;
	}

	public boolean isSimplifiedUI()
	{
		return simplifiedUI;
	}

	public void setSimplifiedUI( boolean simplifiedUI )
	{
		this.simplifiedUI = simplifiedUI;
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append( "ProfileView [profileId=" );
		builder.append( profileId );
		builder.append( ", name=" );
		builder.append( name );
		builder.append( ", description=" );
		builder.append( description );
		builder.append( ", simplifiedUI=" );
		builder.append( simplifiedUI );
		builder.append( ", superAdmin=" );
		builder.append( superAdmin );
		builder.append( ", profileRights=" );
		builder.append( profileRights );
		builder.append( ", profileAppRights=" );
		builder.append( profileAppRights );
		builder.append( ", appProfileData=" );
		builder.append( appProfileData );
		builder.append( "]" );
		return builder.toString();
	}
}
