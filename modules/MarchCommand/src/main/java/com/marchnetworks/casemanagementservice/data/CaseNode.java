package com.marchnetworks.casemanagementservice.data;

import com.marchnetworks.casemanagementservice.common.CaseNodeType;
import com.marchnetworks.command.common.extractor.data.CompletionState;
import com.marchnetworks.command.common.extractor.data.State;
import com.marchnetworks.common.serialization.CoreJsonSerializer;

import java.util.ArrayList;
import java.util.List;

public class CaseNode
{
	private Long id;
	private String name;
	private Long caseId;
	private List<CaseNode> childNodes;
	private Long timeCreated;
	private Long evidenceStartTime;
	private Long evidenceEndTime;
	private Long associatedResource;
	private String attachment;
	private CaseNodeType type;
	private String sectorId;
	private String tag;
	private Long extractorId;
	private State state;
	private CompletionState completionState;

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

	public Long getCaseId()
	{
		return caseId;
	}

	public void setCaseId( Long caseId )
	{
		this.caseId = caseId;
	}

	public Long getTimeCreated()
	{
		return timeCreated;
	}

	public void setTimeCreated( Long timeCreated )
	{
		this.timeCreated = timeCreated;
	}

	public Long getEvidenceStartTime()
	{
		return evidenceStartTime;
	}

	public void setEvidenceStartTime( Long evidenceStartTime )
	{
		this.evidenceStartTime = evidenceStartTime;
	}

	public Long getEvidenceEndTime()
	{
		return evidenceEndTime;
	}

	public void setEvidenceEndTime( Long evidenceEndTime )
	{
		this.evidenceEndTime = evidenceEndTime;
	}

	public Long getAssociatedResource()
	{
		return associatedResource;
	}

	public void setAssociatedResource( Long associatedResource )
	{
		this.associatedResource = associatedResource;
	}

	public String getAttachment()
	{
		return attachment;
	}

	public void setAttachment( String attachment )
	{
		this.attachment = attachment;
	}

	public CaseNodeType getType()
	{
		return type;
	}

	public void setType( CaseNodeType type )
	{
		this.type = type;
	}

	public String getSectorId()
	{
		return sectorId;
	}

	public void setSectorId( String sectorId )
	{
		this.sectorId = sectorId;
	}

	public List<CaseNode> getChildNodes()
	{
		return childNodes;
	}

	public void setChildNodes( List<CaseNode> childNodes )
	{
		this.childNodes = childNodes;
	}

	public String toJsonString()
	{
		return CoreJsonSerializer.toJson( this );
	}

	public String getTag()
	{
		return tag;
	}

	public void setTag( String tag )
	{
		this.tag = tag;
	}

	public Long getExtractorId()
	{
		return extractorId;
	}

	public void setExtractorId( Long extractorId )
	{
		this.extractorId = extractorId;
	}

	public State getState()
	{
		return state;
	}

	public void setState( State state )
	{
		this.state = state;
	}

	public CompletionState getCompletionState()
	{
		return completionState;
	}

	public void setCompletionState( CompletionState completionState )
	{
		this.completionState = completionState;
	}

	public List<CaseNode> getAllCaseNodes()
	{
		List<CaseNode> nodes = new ArrayList();
		getAllChildrenNodes( nodes, this );
		return nodes;
	}

	public CaseNode getFirstChildNodeByType( CaseNodeType nodeType )
	{
		for ( CaseNode childNode : childNodes )
		{
			if ( nodeType == childNode.getType() )
			{
				return childNode;
			}
		}
		return null;
	}

	private void getAllChildrenNodes( List<CaseNode> nodesList, CaseNode node )
	{
		nodesList.add( node );
		for ( CaseNode caseNode : node.getChildNodes() )
		{
			getAllChildrenNodes( nodesList, caseNode );
		}
	}
}
