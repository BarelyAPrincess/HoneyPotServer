package com.marchnetworks.esm.common.model;

import com.google.gson.reflect.TypeToken;
import com.marchnetworks.command.common.CollectionUtils;
import com.marchnetworks.command.common.user.data.AppProfileData;
import com.marchnetworks.command.common.user.data.MemberTypeEnum;
import com.marchnetworks.command.common.user.data.MemberView;
import com.marchnetworks.command.common.user.data.RightEnum;
import com.marchnetworks.common.serialization.CoreJsonSerializer;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table( name = "MEMBER" )
public class MemberEntity implements Serializable
{
	private static final long serialVersionUID = -3181002015658176699L;
	@Id
	@GeneratedValue
	private Long id;
	@Column( name = "NAME", nullable = false, unique = true )
	private String name;
	@Enumerated( EnumType.STRING )
	@Column( name = "TYPE", nullable = false )
	private MemberTypeEnum type;
	@Column( name = "FK_PROFILE_ID", nullable = true )
	private Long profileId;
	@OneToOne( cascade = {javax.persistence.CascadeType.ALL} )
	@JoinColumn( name = "FK_USERDETAILS_ID" )
	private UserDetailsEntity userDetails;
	@Column( name = "TERMS_ACCEPTED" )
	private Boolean termsAccepted;
	@Column( name = "GROUPS", length = 1000, nullable = true )
	private String groups;
	@Column( name = "HASH", length = 100, nullable = true )
	private byte[] hash;
	@Column( name = "SALT", length = 50, nullable = true )
	private byte[] salt;
	@Column( name = "LAST_LOGIN", length = 100 )
	private Date lastLogin;
	@Column( name = "SYSTEM_IDS", length = 2000 )
	private String systemRoots;
	@Column( name = "LOGICAL_IDS", length = 2000 )
	private String logicalRoots;
	@Column( name = "PERSONAL_ID" )
	private Long personalId;
	@Column( name = "ASSEMBLED_RIGHTS", length = 4000 )
	private String assembledRights;
	@Column( name = "ASSEMBLED_APP_RIGHTS", length = 4000 )
	private String assembledAppRights;
	@Column( name = "ASSEMBLED_APP_DATA", length = 4000 )
	private String assembledAppData;
	@Column( name = "ASSEMBLED_SYSTEM_IDS", length = 2000 )
	private String assembledSystemRoots;
	@Column( name = "ASSEMBLED_LOGICAL_IDS", length = 2000 )
	private String assembledLogicalRoots;

	public Long getId()
	{
		return id;
	}

	public void setId( Long id )
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	public MemberEntity()
	{
	}

	public MemberEntity( String username )
	{
		name = username;
	}

	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append( "Username=" ).append( name );
		return buf.toString();
	}

	public void readFromDataObject( MemberView memberView )
	{
		setName( memberView.getName() );
		setMemberType( memberView.getType() );
		setGroups( memberView.getGroups() );
		setLastLogin( memberView.getLastLoginTime() );
		setHash( memberView.getHash() );
		setSalt( memberView.getSalt() );

		if ( ( !getMemberType().equals( MemberTypeEnum.GROUP ) ) && ( memberView.getDetailsView() != null ) && ( userDetails != null ) )
		{
			userDetails.readFromDataObject( memberView.getDetailsView() );
		}

		setSystemRoots( memberView.getSystemRoots() );
		setLogicalRoots( memberView.getLogicalRoots() );
		setPersonalId( memberView.getPersonalRoot() );

		if ( ( type == MemberTypeEnum.LOCAL_USER ) || ( type == MemberTypeEnum.GROUP ) )
		{
			setAssembledSystemRoots( getSystemRoots() );
			setAssembledLogicalRoots( getLogicalRoots() );
		}
		else
		{
			setAssembledSystemRoots( memberView.getAssembledSystemRoots() );
			setAssembledLogicalRoots( memberView.getAssembledLogicalRoots() );
		}

		setAssembledRights( memberView.getAssembledRights() );
		setAssembledAppRights( memberView.getAssembledAppRights() );
		setAssembledAppData( memberView.getAssembledAppData() );
	}

	public MemberView toDataObject()
	{
		MemberView memberView = new MemberView();

		if ( getName() != null )
		{
			memberView.setName( getName() );
		}

		if ( getMemberType() != null )
		{
			memberView.setType( getMemberType() );
		}

		if ( getProfileId() != null )
		{
			memberView.setProfileId( profileId );
		}

		memberView.setSystemRoots( getSystemRoots() );
		memberView.setLogicalRoots( getLogicalRoots() );
		memberView.setPersonalRoot( personalId );

		if ( userDetails != null )
		{
			memberView.setDetailsView( userDetails.toDataObject() );
		}

		memberView.setLastLoginTime( lastLogin );
		memberView.setHash( hash );
		memberView.setSalt( salt );

		memberView.setTermsAccepted( termsAccepted );
		memberView.setGroups( getGroups() );

		memberView.setAssembledRights( getAssembledRights() );
		memberView.setAssembledAppRights( getAssembledAppRights() );
		memberView.setAssembledAppData( getAssembledAppData() );
		memberView.setAssembledSystemRoots( getAssembledSystemRoots() );
		memberView.setAssembledLogicalRoots( getAssembledLogicalRoots() );

		return memberView;
	}

	public boolean hasResources()
	{
		return ( !getSystemRoots().isEmpty() ) || ( !getLogicalRoots().isEmpty() ) || ( personalId != null );
	}

	public boolean isLocalUser()
	{
		return type.equals( MemberTypeEnum.LOCAL_USER );
	}

	public MemberTypeEnum getMemberType()
	{
		return type;
	}

	public void setMemberType( MemberTypeEnum type )
	{
		this.type = type;
	}

	public UserDetailsEntity getUserDetails()
	{
		return userDetails;
	}

	public void setUserDetails( UserDetailsEntity userDetails )
	{
		this.userDetails = userDetails;
	}

	public Boolean getTermsAccepted()
	{
		return termsAccepted;
	}

	public void setTermsAccepted( Boolean termsAccepted )
	{
		this.termsAccepted = termsAccepted;
	}

	public Long getPersonalResource()
	{
		return personalId;
	}

	public void updateLdapInfo( MemberView memberView )
	{
		if ( ( !getMemberType().equals( MemberTypeEnum.GROUP ) ) && ( memberView.getDetailsView() != null ) && ( userDetails != null ) )
		{
			userDetails.updateLdapInfo( memberView.getDetailsView() );
		}
	}

	public boolean removeResource( Long resourceId )
	{
		Set<Long> systemRoots = getSystemRoots();
		Set<Long> logicalRoots = getLogicalRoots();
		Set<Long> assembledSystemRoots = getAssembledSystemRoots();
		Set<Long> assembledLogicalRoots = getAssembledLogicalRoots();

		if ( assembledSystemRoots.remove( resourceId ) )
		{
			setAssembledSystemRoots( assembledSystemRoots );
		}
		if ( assembledLogicalRoots.remove( resourceId ) )
		{
			setAssembledLogicalRoots( assembledLogicalRoots );
		}

		if ( systemRoots.remove( resourceId ) )
		{
			setSystemRoots( systemRoots );
			return true;
		}
		if ( logicalRoots.remove( resourceId ) )
		{
			setLogicalRoots( logicalRoots );
			return true;
		}
		if ( personalId.equals( resourceId ) )
		{
			setPersonalId( null );
			return true;
		}
		return false;
	}

	public Set<Long> getGroups()
	{
		if ( groups != null )
		{
			Set<String> stringSet = ( Set ) CoreJsonSerializer.collectionFromJson( groups, new TypeToken()
			{
			} );
			return CollectionUtils.convertStringToLongSet( stringSet );
		}
		return new HashSet( 0 );
	}

	public void setGroups( Set<Long> groups )
	{
		this.groups = null;
		if ( ( groups != null ) && ( !groups.isEmpty() ) )
		{
			Set<String> stringSet = CollectionUtils.convertToStringSet( groups );
			this.groups = CoreJsonSerializer.toJson( stringSet );
		}
	}

	public void addGroup( Long groupId )
	{
		Set<Long> groups = getGroups();
		if ( groups.add( groupId ) )
		{
			setGroups( groups );
		}
	}

	public boolean removeGroup( Long groupId )
	{
		Set<Long> groups = getGroups();
		if ( groups.remove( groupId ) )
		{
			setGroups( groups );
			return true;
		}
		return false;
	}

	public boolean belongsToGroup( Long groupId )
	{
		Set<Long> groups = getGroups();
		if ( ( groups == null ) || ( groups.isEmpty() ) )
		{
			return false;
		}
		return groups.contains( groupId );
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

	public Date getLastLogin()
	{
		return lastLogin;
	}

	public void setLastLogin( Date lastLogin )
	{
		this.lastLogin = lastLogin;
	}

	public MemberTypeEnum getType()
	{
		return type;
	}

	public void setType( MemberTypeEnum type )
	{
		this.type = type;
	}

	public Long getProfileId()
	{
		return profileId;
	}

	public void setProfileId( Long profileId )
	{
		this.profileId = profileId;
	}

	public Set<Long> getSystemRoots()
	{
		if ( systemRoots != null )
		{
			Set<String> stringSet = ( Set ) CoreJsonSerializer.collectionFromJson( systemRoots, new TypeToken()
			{
			} );
			return CollectionUtils.convertStringToLongSet( stringSet );
		}
		return new HashSet( 0 );
	}

	public void setSystemRoots( Set<Long> systemIdSet )
	{
		systemRoots = null;
		if ( ( systemIdSet != null ) && ( !systemIdSet.isEmpty() ) )
		{
			Set<String> stringSet = CollectionUtils.convertToStringSet( systemIdSet );
			systemRoots = CoreJsonSerializer.toJson( stringSet );
		}
	}

	public Set<Long> getLogicalRoots()
	{
		if ( logicalRoots != null )
		{
			Set<String> stringSet = ( Set ) CoreJsonSerializer.collectionFromJson( logicalRoots, new TypeToken()
			{
			} );
			return CollectionUtils.convertStringToLongSet( stringSet );
		}
		return new HashSet( 0 );
	}

	public void setLogicalRoots( Set<Long> logicalIdSet )
	{
		logicalRoots = null;
		if ( ( logicalIdSet != null ) && ( !logicalIdSet.isEmpty() ) )
		{
			Set<String> stringSet = CollectionUtils.convertToStringSet( logicalIdSet );
			logicalRoots = CoreJsonSerializer.toJson( stringSet );
		}
	}

	public Set<Long> getAllRoots( boolean includePersonal )
	{
		Set<Long> allResources = new HashSet();
		allResources.addAll( getSystemRoots() );
		allResources.addAll( getLogicalRoots() );
		if ( ( includePersonal ) && ( personalId != null ) )
		{
			allResources.add( personalId );
		}
		return allResources;
	}

	public Long getPersonalRootId()
	{
		return personalId;
	}

	public void setPersonalId( Long personalId )
	{
		this.personalId = personalId;
	}

	public Set<RightEnum> getAssembledRights()
	{
		if ( assembledRights != null )
		{
			Set<RightEnum> rights = ( Set ) CoreJsonSerializer.collectionFromJson( assembledRights, new TypeToken()
			{
			} );
			return rights;
		}
		return new HashSet( 0 );
	}

	public void setAssembledRights( Set<RightEnum> rights )
	{
		assembledRights = null;
		if ( ( rights != null ) && ( !rights.isEmpty() ) )
		{
			assembledRights = CoreJsonSerializer.toJson( rights );
		}
	}

	public Set<String> getAssembledAppRights()
	{
		if ( assembledAppRights != null )
			CoreJsonSerializer.collectionFromJson( assembledAppRights, new TypeToken<HashSet<String>>()
			{
			} );

		return new HashSet( 0 );
	}

	public void setAssembledAppRights( Set<String> appRights )
	{
		if ( ( appRights == null ) || ( appRights.isEmpty() ) )
		{
			assembledAppRights = null;
		}
		else
		{
			assembledAppRights = CoreJsonSerializer.toJson( appRights );
		}
	}

	public Set<AppProfileData> getAssembledAppData()
	{
		if ( assembledAppData != null )
			CoreJsonSerializer.collectionFromJson( assembledAppData, new TypeToken<HashSet<AppProfileData>>()
			{
			} );

		return new HashSet( 0 );
	}

	public void setAssembledAppData( Set<AppProfileData> data )
	{
		if ( ( data == null ) || ( data.isEmpty() ) )
		{
			assembledAppData = null;
		}
		else
		{
			assembledAppData = CoreJsonSerializer.toJson( data );
		}
	}

	public Set<Long> getAssembledSystemRoots()
	{
		if ( assembledSystemRoots != null )
		{
			Set<String> roots = ( Set ) CoreJsonSerializer.collectionFromJson( assembledSystemRoots, new TypeToken()
			{
			} );
			return CollectionUtils.convertStringToLongSet( roots );
		}
		return new HashSet( 0 );
	}

	public void setAssembledSystemRoots( Set<Long> assembledSystemRoots )
	{
		if ( ( assembledSystemRoots == null ) || ( assembledSystemRoots.isEmpty() ) )
		{
			this.assembledSystemRoots = null;
		}
		else
		{
			Set<String> roots = CollectionUtils.convertToStringSet( assembledSystemRoots );
			this.assembledSystemRoots = CoreJsonSerializer.toJson( roots );
		}
	}

	public Set<Long> getAssembledLogicalRoots()
	{
		if ( assembledLogicalRoots != null )
		{
			Set<String> roots = ( Set ) CoreJsonSerializer.collectionFromJson( assembledLogicalRoots, new TypeToken()
			{
			} );
			return CollectionUtils.convertStringToLongSet( roots );
		}
		return new HashSet( 0 );
	}

	public void setAssembledLogicalRoots( Set<Long> assembledLogicalRoots )
	{
		if ( ( assembledLogicalRoots == null ) || ( assembledLogicalRoots.isEmpty() ) )
		{
			this.assembledLogicalRoots = null;
		}
		else
		{
			Set<String> roots = CollectionUtils.convertToStringSet( assembledLogicalRoots );
			this.assembledLogicalRoots = CoreJsonSerializer.toJson( roots );
		}
	}
}
