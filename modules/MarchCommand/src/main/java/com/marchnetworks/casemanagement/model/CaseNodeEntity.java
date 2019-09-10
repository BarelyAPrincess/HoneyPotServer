package com.marchnetworks.casemanagement.model;

import com.marchnetworks.casemanagementservice.common.CaseNodeType;
import com.marchnetworks.casemanagementservice.data.CaseNode;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.extractor.data.CompletionState;
import com.marchnetworks.command.common.extractor.data.State;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table( name = "CASE_NODE" )
public class CaseNodeEntity
{
	@Id
	@GeneratedValue
	@Column( name = "ID" )
	private Long id;
	@Column( name = "NAME" )
	private String name;
	@ManyToOne
	@JoinColumn( name = "CASE_ID" )
	private CaseEntity caseEntity;
	@ManyToOne
	@JoinColumn( name = "PARENT_ID" )
	private CaseNodeEntity parentNode;
	@OneToMany( mappedBy = "parentNode", orphanRemoval = true, fetch = FetchType.LAZY )
	private List<CaseNodeEntity> childNodes = new ArrayList();

	@Column( name = "TIME_CREATED" )
	private Long timeAdded;

	@Column( name = "EVIDENCE_START_TIME" )
	private Long evidenceStartTime;

	@Column( name = "EVIDENCE_END_TIME" )
	private Long evidenceEndTime;

	@Basic( fetch = FetchType.LAZY )
	@Lob
	@Column( name = "NODE_DATA" )
	private byte[] attachment;

	@Column( name = "RESOURCE_ID" )
	private Long associatedResourceId;

	@Enumerated( EnumType.STRING )
	@Column( name = "TYPE" )
	private CaseNodeType type;

	@Column( name = "SECTOR_ID" )
	private String sectorId;

	@Column( name = "ORDER_TAG" )
	private String tag;

	@Column( name = "GUID" )
	private String guid;

	@Column( name = "EXTRACTOR_ID" )
	private Long extractorId;

	@Column( name = "EXTRACTOR_SERIAL" )
	private String extractorSerial;

	@Column( name = "STATE" )
	private State state;

	@Column( name = "COMPLETION_STATE" )
	private CompletionState completionState;

	public CaseNode toDataObject()
	{
		CaseNode dataObject = new CaseNode();

		dataObject.setCaseId( getCase().getId() );
		dataObject.setAssociatedResource( associatedResourceId );
		dataObject.setEvidenceEndTime( evidenceEndTime );
		dataObject.setEvidenceStartTime( evidenceStartTime );
		dataObject.setId( id );
		dataObject.setName( name );
		dataObject.setSectorId( sectorId );
		dataObject.setTimeCreated( timeAdded );
		dataObject.setType( type );
		dataObject.setTag( tag );
		dataObject.setExtractorId( extractorId );
		dataObject.setState( state );
		dataObject.setCompletionState( completionState );

		List<CaseNode> nodeDataObjects = new ArrayList();

		if ( getChildNodes() != null )
		{
			for ( CaseNodeEntity entity : getChildNodes() )
			{
				CaseNode node = entity.toDataObject();
				nodeDataObjects.add( node );
			}
		}

		dataObject.setChildNodes( nodeDataObjects );
		return dataObject;
	}

	public CaseNodeEntity readMetaDataFromDataObject( CaseNode node )
	{
		setAssociatedResourceId( node.getAssociatedResource() );
		setEvidenceEndTime( node.getEvidenceEndTime() );
		setEvidenceStartTime( node.getEvidenceStartTime() );
		setName( node.getName() );
		setSectorId( node.getSectorId() );
		setTimeAdded( node.getTimeCreated() );
		setType( node.getType() );
		setTag( node.getTag() );

		if ( node.getAttachment() != null )
		{
			byte[] array = CommonAppUtils.stringBase64ToByte( node.getAttachment() );
			setAttachment( array );
		}
		else
		{
			attachment = null;
		}

		return this;
	}

	public CaseEntity getCase()
	{
		if ( ( getCaseEntity() == null ) && ( parentNode != null ) )
		{
			return parentNode.getCase();
		}
		return caseEntity;
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

	public Long getTimeAdded()
	{
		return timeAdded;
	}

	public void setTimeAdded( Long timeAdded )
	{
		this.timeAdded = timeAdded;
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

	public byte[] getAttachment()
	{
		return attachment;
	}

	public void setAttachment( byte[] attachment )
	{
		this.attachment = attachment;
	}

	public Long getAssociatedResourceId()
	{
		return associatedResourceId;
	}

	public void setAssociatedResourceId( Long associatedResourceId )
	{
		this.associatedResourceId = associatedResourceId;
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

	public CaseEntity getCaseEntity()
	{
		return caseEntity;
	}

	public void setCaseEntity( CaseEntity caseEntity )
	{
		this.caseEntity = caseEntity;
	}

	public CaseNodeEntity getParentNode()
	{
		return parentNode;
	}

	public void setParentNode( CaseNodeEntity parentNode )
	{
		this.parentNode = parentNode;
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

	public boolean removeChildNode( CaseNodeEntity child )
	{
		return childNodes.remove( child );
	}

	public String getAttachmentAsBase64String()
	{
		return attachment != null ? CommonAppUtils.byteToBase64( attachment ) : null;
	}

	public String getTag()
	{
		return tag;
	}

	public void setTag( String tag )
	{
		this.tag = tag;
	}

	public String getGuid()
	{
		return guid;
	}

	public void setGuid( String guid )
	{
		this.guid = guid;
	}

	public Long getExtractorId()
	{
		return extractorId;
	}

	public void setExtractorId( Long extractorId )
	{
		this.extractorId = extractorId;
	}

	public String getExtractorSerial()
	{
		return extractorSerial;
	}

	public void setExtractorSerial( String extractorSerial )
	{
		this.extractorSerial = extractorSerial;
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

	public List<CaseNodeEntity> getAllCaseNodes()
	{
		List<CaseNodeEntity> nodes = new ArrayList();
		getAllChildrenNodes( nodes, this );
		return nodes;
	}

	private void getAllChildrenNodes( List<CaseNodeEntity> nodesList, CaseNodeEntity node )
	{
		nodesList.add( node );
		for ( CaseNodeEntity caseNode : node.getChildNodes() )
		{
			getAllChildrenNodes( nodesList, caseNode );
		}
	}
}
