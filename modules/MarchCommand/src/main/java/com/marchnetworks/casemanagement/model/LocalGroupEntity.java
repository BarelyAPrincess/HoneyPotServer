package com.marchnetworks.casemanagement.model;

import com.google.gson.reflect.TypeToken;
import com.marchnetworks.casemanagementservice.data.LocalGroup;
import com.marchnetworks.common.serialization.CoreJsonSerializer;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table( name = "LOCAL_GROUP" )
public class LocalGroupEntity
{
	@Id
	@GeneratedValue
	@Column( name = "ID" )
	private Long id;
	@Column( name = "NAME" )
	private String name;
	@Column( name = "DESCRIPTION", length = 4000 )
	private String description;
	@Column( name = "MEMBERS", length = 4000 )
	private String membersString;

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

	public List<String> getMembers()
	{
		if ( membersString == null )
		{
			return null;
		}

		return CoreJsonSerializer.collectionFromJson( membersString, new TypeToken<ArrayList<String>>()
		{
		} );
	}

	public void setMembers( List<String> members )
	{
		if ( ( members == null ) || ( members.isEmpty() ) )
		{
			membersString = null;
		}
		else
		{
			membersString = CoreJsonSerializer.toJson( members );
		}
	}

	public LocalGroup toDataObject()
	{
		LocalGroup dataObject = new LocalGroup();

		dataObject.setId( id );
		dataObject.setDescription( description );
		dataObject.setName( name );
		dataObject.setUsernames( getMembers() );

		return dataObject;
	}

	public void readFromDataObject( LocalGroup dataObject )
	{
		setDescription( dataObject.getDescription() );
		setName( dataObject.getName() );
		setMembers( dataObject.getUsersnames() );
	}
}
