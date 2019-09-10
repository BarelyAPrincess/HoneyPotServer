package com.marchnetworks.esm.common.model;

import com.google.gson.reflect.TypeToken;
import com.marchnetworks.command.common.user.data.AppProfileData;
import com.marchnetworks.command.common.user.data.ProfileView;
import com.marchnetworks.command.common.user.data.RightEnum;
import com.marchnetworks.common.serialization.CoreJsonSerializer;
import com.marchnetworks.common.utils.CommonUtils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table( name = "PROFILE" )
public class ProfileEntity
{
	@Id
	@GeneratedValue
	private Long id = null;

	@Column( name = "NAME", unique = true )
	private String name;

	@Column( name = "DESCRIPTION", length = 4000, nullable = true, unique = false )
	private String description;

	@Column( name = "RIGHTS", length = 4000 )
	private String rights;

	@Column( name = "APP_RIGHTS", length = 4000 )
	private String appRights;

	@Column( name = "ADMIN_ROLE" )
	private boolean superAdmin;

	@Column( name = "SIMPLIFIED_UI" )
	private Boolean simplifiedUI = Boolean.valueOf( false );

	@Column( name = "APP_PROFILE_DATA", length = 4000 )
	private String appProfileData;

	public ProfileEntity()
	{
	}

	public ProfileEntity( String name )
	{
		this.name = name;
	}

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

	public String getDescription()
	{
		return description;
	}

	public void setDescription( String description )
	{
		this.description = description;
	}

	public Set<RightEnum> getRights()
	{
		if ( this.rights != null )
		{
			Set<RightEnum> rights = ( Set ) CoreJsonSerializer.collectionFromJson( this.rights, new TypeToken()
			{
			} );
			return rights;
		}
		return new HashSet( 0 );
	}

	public void setRights( Set<RightEnum> rights )
	{
		this.rights = null;
		if ( ( rights != null ) && ( !rights.isEmpty() ) )
		{
			this.rights = CoreJsonSerializer.toJson( rights );
		}
	}

	public void addRight( RightEnum right )
	{
		Set<RightEnum> rights = getRights();
		if ( rights.add( right ) )
		{
			setRights( rights );
		}
	}

	public void removeRight( RightEnum right )
	{
		Set<RightEnum> rights = getRights();
		rights.remove( rights );
		setRights( rights );
	}

	public void removeAllRights()
	{
		rights = null;
	}

	public boolean isSuperAdmin()
	{
		return superAdmin;
	}

	public void setSuperAdmin( boolean superAdmin )
	{
		this.superAdmin = superAdmin;
	}

	public void readFromDataObject( ProfileView profileView )
	{
		if ( profileView != null )
		{
			if ( ( profileView.getProfileId() != null ) && ( !profileView.getProfileId().equals( "" ) ) )
			{
				setId( Long.valueOf( profileView.getProfileId() ) );
			}
			setName( profileView.getName() );
			setSuperAdmin( profileView.isSuperAdmin() );
			setDescription( profileView.getDescription() );

			Set<RightEnum> rights = profileView.getProfileRights();
			for ( RightEnum aRightEnum : rights )
			{
				addRight( aRightEnum );
			}
			Set<String> applicationRights = profileView.getProfileAppRights();
			for ( String appRight : applicationRights )
			{
				addAppRight( appRight );
			}

			setAppProfileData( profileView.getAppProfileData() );
			setSimplifiedUI( Boolean.valueOf( profileView.isSimplifiedUI() ) );
		}
	}

	public ProfileView toDataObject()
	{
		ProfileView profileView = new ProfileView();
		if ( getId() != null )
		{
			profileView.setProfileId( getId().toString() );
		}

		profileView.setName( getName() );
		profileView.setSuperAdmin( isSuperAdmin() );
		profileView.setDescription( getDescription() );

		profileView.setProfileAppRights( getAppRights() );
		profileView.setProfileRights( getRights() );
		profileView.setAppProfileData( getAppProfileData() );
		profileView.setSimplifiedUI( isSimplifiedUI().booleanValue() );
		return profileView;
	}

	public Set<String> getAppRights()
	{
		if ( appRights != null )
			CoreJsonSerializer.collectionFromJson( appRights, new TypeToken<HashSet<String>>()
			{
			} );

		return new LinkedHashSet( 0 );
	}

	public void setAppRights( Set<String> set )
	{
		if ( ( set == null ) || ( set.isEmpty() ) )
		{
			appRights = null;
		}
		else
		{
			appRights = CommonUtils.setToJson( set );
		}
	}

	public void addAppRight( String appRight )
	{
		Set<String> appRights = getAppRights();
		appRights.add( appRight );
		this.appRights = CommonUtils.setToJson( appRights );
	}

	public void removeAllAppRights()
	{
		appRights = null;
	}

	public void removeByGuid( String guid )
	{
		Set<String> appRights = getAppRights();
		if ( appRights.remove( guid ) )
		{
			this.appRights = CommonUtils.setToJson( appRights );
		}

		Set<AppProfileData> appProfileData = getAppProfileData();
		for ( Iterator<AppProfileData> iterator = appProfileData.iterator(); iterator.hasNext(); )
		{
			AppProfileData appData = ( AppProfileData ) iterator.next();
			if ( appData.getAppId().equals( guid ) )
			{
				iterator.remove();
			}
		}
		setAppProfileData( appProfileData );
	}

	public Set<AppProfileData> getAppProfileData()
	{
		if ( appProfileData != null )
			CoreJsonSerializer.collectionFromJson( appProfileData, new TypeToken<HashSet<AppProfileData>>()
			{
			} );

		return new HashSet( 0 );
	}

	public void setAppProfileData( Set<AppProfileData> data )
	{
		if ( ( data == null ) || ( data.isEmpty() ) )
		{
			appProfileData = null;
		}
		else
		{
			appProfileData = CoreJsonSerializer.toJson( data );
		}
	}

	public Boolean isSimplifiedUI()
	{
		return Boolean.valueOf( simplifiedUI == null ? false : simplifiedUI.booleanValue() );
	}

	public void setSimplifiedUI( Boolean simplifiedUI )
	{
		this.simplifiedUI = simplifiedUI;
	}
}
