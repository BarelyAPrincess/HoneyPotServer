package com.marchnetworks.casemanagement.model;

import com.google.gson.reflect.TypeToken;
import com.marchnetworks.casemanagementservice.data.Case;
import com.marchnetworks.casemanagementservice.data.CaseNode;
import com.marchnetworks.common.serialization.CoreJsonSerializer;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table( name = "CASE_EVIDENCE" )
public class CaseEntity
{
	@Id
	@GeneratedValue
	@Column( name = "ID" )
	private Long id;
	@Column( name = "NAME" )
	private String name;
	@OneToMany( cascade = {javax.persistence.CascadeType.ALL}, orphanRemoval = true, fetch = FetchType.LAZY )
	@JoinColumn( name = "CASE_ID" )
	private List<CaseNodeEntity> childNodes = new ArrayList( 1 );

	@Column( name = "TIME_CREATED" )
	private Long timeCreated;

	@Column( name = "LAST_UPDATE" )
	private Long lastUpdate;

	@Column( name = "MEMBER" )
	private String member;

	@Column( name = "LOCAL_GROUPS" )
	private String groupsString;

	public CaseEntity()
	{
		timeCreated = Long.valueOf( System.currentTimeMillis() );
		lastUpdate = timeCreated;
	}

	public Case toDataObject( boolean loadChildren )
	{
		Case dataObject = new Case();
		dataObject.setId( id );
		dataObject.setName( name );
		dataObject.setMember( member );
		dataObject.setTimeCreated( timeCreated );
		dataObject.setTimeLastUpdate( lastUpdate );
		dataObject.setGroupIds( getGroups() );

		if ( loadChildren )
		{
			List<CaseNode> childNodeList = new ArrayList();

			for ( CaseNodeEntity entity : getChildNodes() )
			{
				childNodeList.add( entity.toDataObject() );
			}

			dataObject.setCaseNodes( childNodeList );
		}

		return dataObject;
	}

	public CaseEntity readFromDataObject( Case dataObject )
	{
		setMember( dataObject.getMember() );
		setName( dataObject.getName() );
		setGroups( dataObject.getGroupIds() );
		setTimeCreated( dataObject.getTimeCreated() );
		setLastUpdate( dataObject.getTimeLastUpdate() );

		return this;
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

	public List<CaseNodeEntity> getChildNodes()
	{
		return childNodes;
	}

	public void setChildNodes( List<CaseNodeEntity> childNodes )
	{
		this.childNodes = childNodes;
	}

	public void addChildNode( CaseNodeEntity child )
	{
		childNodes.add( child );
	}

	public Long getTimeCreated()
	{
		return timeCreated;
	}

	public void setTimeCreated( Long timeCreated )
	{
		this.timeCreated = timeCreated;
	}

	public String getMember()
	{
		return member;
	}

	public void setMember( String member )
	{
		this.member = member;
	}

	public List<Long> getGroups()
	{
		if ( groupsString == null )
		{
			return null;
		}

		List<String> groupIdsAsString = ( List ) CoreJsonSerializer.collectionFromJson( groupsString, new TypeToken()
		{
		} );
		List<Long> result = new ArrayList( groupIdsAsString.size() );
		for ( String groupId : groupIdsAsString )
		{
			result.add( Long.valueOf( groupId ) );
		}
		return result;
	}

	public void setGroups( List<Long> groupIds )
	{
		if ( ( groupIds == null ) || ( groupIds.isEmpty() ) )
		{
			groupsString = null;
		}
		else
		{
			List<String> groupIdsAsString = new ArrayList( groupIds.size() );
			for ( Long groupId : groupIds )
			{
				groupIdsAsString.add( groupId.toString() );
			}
			groupsString = CoreJsonSerializer.toJson( groupIdsAsString );
		}
	}

	public Long getLastUpdate()
	{
		return lastUpdate;
	}

	public void setLastUpdate( Long lastUpdate )
	{
		this.lastUpdate = lastUpdate;
	}

	public boolean isOrphan()
	{
		return member == null;
	}
}
