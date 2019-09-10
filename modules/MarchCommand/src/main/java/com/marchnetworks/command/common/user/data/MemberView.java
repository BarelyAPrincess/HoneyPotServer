package com.marchnetworks.command.common.user.data;

import com.marchnetworks.command.common.CollectionUtils;
import com.marchnetworks.command.common.topology.TopologyConstants;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class MemberView
{
	private String name;
	private Long profileId;
	private UserDetailsView detailsView;
	private Set<Long> systemRoots = new HashSet();
	private Set<Long> logicalRoots = new HashSet();
	private Long personalRoot;
	private Boolean termsAccepted;
	private MemberTypeEnum type;
	private Date lastLoginTime;
	private Set<Long> groups;
	private byte[] hash;
	private byte[] salt;
	private Set<RightEnum> assembledRights = new HashSet();
	private Set<String> assembledAppRights = new HashSet();
	private Set<AppProfileData> assembledAppData = new HashSet();
	private Set<Long> assembledSystemRoots = new HashSet();
	private Set<Long> assembledLogicalRoots = new HashSet();

	public boolean hasResource( Long resourceId )
	{
		return ( systemRoots.contains( resourceId ) ) || ( logicalRoots.contains( resourceId ) ) || ( personalRoot == resourceId );
	}

	public boolean belongsToGroup( Long groupId )
	{
		if ( ( groups == null ) || ( groups.isEmpty() ) )
		{
			return false;
		}

		return groups.contains( groupId );
	}

	public String toString()
	{
		return "MemberView " + name + ", Profile: " + profileId + ", System Roots: " + CollectionUtils.arrayToString( systemRoots.toArray(), ",", true ) + ", Logical Roots: " + CollectionUtils.arrayToString( logicalRoots.toArray(), ",", true ) + ", Personal Root: " + personalRoot.toString() + ", Type: " + type;
	}

	public Set<Long> getAllRoots( boolean includePersonal )
	{
		Set<Long> allResources = new HashSet();
		allResources.addAll( assembledSystemRoots );
		allResources.addAll( assembledLogicalRoots );
		if ( ( includePersonal ) && ( personalRoot != null ) )
		{
			allResources.add( personalRoot );
		}

		return allResources;
	}

	public Set<Long> getAllAssignedRoots()
	{
		Set<Long> allResources = new HashSet();
		allResources.addAll( systemRoots );
		allResources.addAll( logicalRoots );
		return allResources;
	}

	public boolean hasResources()
	{
		return ( !getAssembledSystemRoots().isEmpty() ) || ( !getAssembledLogicalRoots().isEmpty() );
	}

	public void clearAssembledData()
	{
		assembledRights.clear();
		assembledAppRights.clear();
		assembledAppData.clear();
		assembledSystemRoots.clear();
		assembledLogicalRoots.clear();
	}

	public boolean addSystemRoot( Long systemId )
	{
		return systemRoots.add( systemId );
	}

	public boolean addLogicalRoot( Long logicalId )
	{
		return logicalRoots.add( logicalId );
	}

	public void addAppRights( Set<String> appRights )
	{
		assembledAppRights.addAll( appRights );
	}

	public void addAppProfileData( Set<AppProfileData> appProfileDataSet )
	{
		boolean added = false;
		for ( AppProfileData appProfileData : appProfileDataSet )
		{
			for ( AppProfileData profileAppProfileData : getAssembledAppData() )
			{
				if ( appProfileData.getAppId().equals( profileAppProfileData.getAppId() ) )
				{
					profileAppProfileData.getAppRights().addAll( appProfileData.getAppRights() );
					added = true;
				}
			}
			if ( !added )
			{
				assembledAppData.add( appProfileData );
			}
		}
	}

	public boolean hasRight( RightEnum right )
	{
		return assembledRights.contains( right );
	}

	public boolean isTopLevelUser()
	{
		return ( assembledSystemRoots.contains( TopologyConstants.SYSTEM_ROOT_ID ) ) && ( assembledLogicalRoots.contains( TopologyConstants.LOGICAL_ROOT_ID ) ) && ( assembledRights.contains( RightEnum.MANAGE_USERS ) );
	}

	public String getName()
	{
		return name;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	public UserDetailsView getDetailsView()
	{
		return detailsView;
	}

	public void setDetailsView( UserDetailsView detailsView )
	{
		this.detailsView = detailsView;
	}

	public Set<Long> getSystemRoots()
	{
		return systemRoots;
	}

	public void setSystemRoots( Set<Long> systemRoots )
	{
		this.systemRoots = systemRoots;
	}

	public Set<Long> getLogicalRoots()
	{
		return logicalRoots;
	}

	public void setLogicalRoots( Set<Long> logicalRoots )
	{
		this.logicalRoots = logicalRoots;
	}

	public Long getPersonalRoot()
	{
		return personalRoot;
	}

	public void setPersonalRoot( Long personalRoot )
	{
		this.personalRoot = personalRoot;
	}

	public Boolean getTermsAccepted()
	{
		return termsAccepted;
	}

	public void setTermsAccepted( Boolean termsAccepted )
	{
		this.termsAccepted = termsAccepted;
	}

	public MemberTypeEnum getType()
	{
		return type;
	}

	public void setType( MemberTypeEnum type )
	{
		this.type = type;
	}

	public Date getLastLoginTime()
	{
		return lastLoginTime;
	}

	public void setLastLoginTime( Date lastLoginTime )
	{
		this.lastLoginTime = lastLoginTime;
	}

	public Set<Long> getGroups()
	{
		return groups;
	}

	public void setGroups( Set<Long> groups )
	{
		this.groups = groups;
	}

	public byte[] getHash()
	{
		return hash;
	}

	public void setHash( byte[] hash )
	{
		this.hash = hash;
	}

	public byte[] getSalt()
	{
		return salt;
	}

	public void setSalt( byte[] salt )
	{
		this.salt = salt;
	}

	public Set<RightEnum> getAssembledRights()
	{
		return assembledRights;
	}

	public void setAssembledRights( Set<RightEnum> assembledRights )
	{
		this.assembledRights = assembledRights;
	}

	public void addAssembledRights( Set<RightEnum> rights )
	{
		assembledRights.addAll( rights );
	}

	public Set<String> getAssembledAppRights()
	{
		return assembledAppRights;
	}

	public void setAssembledAppRights( Set<String> assembledAppRights )
	{
		this.assembledAppRights = assembledAppRights;
	}

	public void addAssembledAppRights( Set<String> appRights )
	{
		assembledAppRights.addAll( appRights );
	}

	public Set<AppProfileData> getAssembledAppData()
	{
		return assembledAppData;
	}

	public void setAssembledAppData( Set<AppProfileData> assembledAppData )
	{
		this.assembledAppData = assembledAppData;
	}

	public void addAssembledAppData( Set<AppProfileData> appProfileData )
	{
		assembledAppData.addAll( appProfileData );
	}

	public Set<Long> getAssembledSystemRoots()
	{
		return assembledSystemRoots;
	}

	public void setAssembledSystemRoots( Set<Long> assembledSystemRoots )
	{
		this.assembledSystemRoots = assembledSystemRoots;
	}

	public boolean addAssembledSystemRoots( Set<Long> systemRoots )
	{
		return assembledSystemRoots.addAll( systemRoots );
	}

	public boolean addAssembledSystemRoot( Long id )
	{
		return assembledSystemRoots.add( id );
	}

	public Set<Long> getAssembledLogicalRoots()
	{
		return assembledLogicalRoots;
	}

	public void setAssembledLogicalRoots( Set<Long> assembledLogicalRoots )
	{
		this.assembledLogicalRoots = assembledLogicalRoots;
	}

	public boolean addAssembledLogicalRoots( Set<Long> logicalRoots )
	{
		return assembledLogicalRoots.addAll( logicalRoots );
	}

	public boolean addAssembledLogicalRoot( Long id )
	{
		return assembledLogicalRoots.add( id );
	}

	public Long getProfileId()
	{
		return profileId;
	}

	public void setProfileId( Long profileId )
	{
		this.profileId = profileId;
	}
}
