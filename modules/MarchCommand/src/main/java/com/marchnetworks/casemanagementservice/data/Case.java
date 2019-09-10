package com.marchnetworks.casemanagementservice.data;

import com.marchnetworks.casemanagementservice.common.CaseNodeType;
import com.marchnetworks.command.common.CollectionUtils;
import com.marchnetworks.common.serialization.CoreJsonSerializer;

import java.util.ArrayList;
import java.util.List;

public class Case
{
	private Long id;
	private String name;
	private List<CaseNode> caseNodes;
	private Long timeCreated;
	private Long timeLastUpdate;
	private String member;
	private List<Long> groupIds;

	public Case()
	{
	}

	public Case( Long id, String name, List<CaseNode> caseNodes, Long timeCreated, String member )
	{
		this.id = id;
		this.name = name;
		this.caseNodes = caseNodes;
		this.timeCreated = timeCreated;
		this.member = member;
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

	public List<CaseNode> getCaseNodes()
	{
		return caseNodes;
	}

	public void setCaseNodes( List<CaseNode> caseNodes )
	{
		this.caseNodes = caseNodes;
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

	public String toJson()
	{
		return CoreJsonSerializer.toJson( this );
	}

	public List<Long> getGroupIds()
	{
		return groupIds;
	}

	public void setGroupIds( List<Long> groupIds )
	{
		this.groupIds = groupIds;
	}

	public Long getTimeLastUpdate()
	{
		return timeLastUpdate;
	}

	public void setTimeLastUpdate( Long timeLastUpdate )
	{
		this.timeLastUpdate = timeLastUpdate;
	}

	public List<CaseNode> getNodesOfType( CaseNodeType... nodeType )
	{
		List<CaseNode> result = new ArrayList<>();
		for ( CaseNode caseNode : caseNodes )
		{
			List<CaseNode> allChildNodes = caseNode.getAllCaseNodes();
			for ( CaseNode childNode : allChildNodes )
			{
				if ( CollectionUtils.contains( nodeType, childNode.getType() ) )
				{
					result.add( childNode );
				}
			}
		}
		return result;
	}
}
