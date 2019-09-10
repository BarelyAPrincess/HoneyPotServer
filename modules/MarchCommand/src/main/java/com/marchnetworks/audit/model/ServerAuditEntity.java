package com.marchnetworks.audit.model;

import com.google.gson.reflect.TypeToken;
import com.marchnetworks.audit.data.AuditView;
import com.marchnetworks.audit.data.AuditView.Builder;
import com.marchnetworks.common.serialization.CoreJsonSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
@org.hibernate.annotations.Table( appliesTo = "AUDIT_LOGS", indexes = {@org.hibernate.annotations.Index( name = "EVENT_TAG_INDEX", columnNames = {"EVENT_TAG"} )} )
@javax.persistence.Table( name = "AUDIT_LOGS" )
public class ServerAuditEntity extends AuditEntity
{
	@Column( name = "APP_ID" )
	private Integer appId;
	@Column( name = "EVENT_TAG" )
	private String eventTag;
	@Column( name = "END_TIMESTAMP" )
	private Long endTime;

	public String getEventTag()
	{
		return eventTag;
	}

	public void setEventTag( String eventTag )
	{
		this.eventTag = eventTag;
	}

	public Long getEndTime()
	{
		return endTime;
	}

	public void setEndTime( Long endTime )
	{
		this.endTime = endTime;
	}

	public Integer getAppId()
	{
		return appId;
	}

	public void setAppId( Integer appId )
	{
		this.appId = appId;
	}

	public Set<Integer> getKeys()
	{
		Set<Integer> keys = super.getKeys();
		if ( appId != null )
		{
			keys.add( appId );
		}
		return keys;
	}

	public AuditView toDataObject( Map<Integer, AuditDictionaryEntity> dictionaryEntries )
	{
		String eventName = dictionaryEntries.containsKey( getEventNameId() ) ? ( ( AuditDictionaryEntity ) dictionaryEntries.get( getEventNameId() ) ).readValue() : "N/A";
		String username = dictionaryEntries.containsKey( getUsernameId() ) ? ( ( AuditDictionaryEntity ) dictionaryEntries.get( getUsernameId() ) ).readValue() : "N/A";
		String remoteAddress = null;
		if ( getRemoteAddressId() != null )
		{
			remoteAddress = dictionaryEntries.containsKey( getRemoteAddressId() ) ? ( ( AuditDictionaryEntity ) dictionaryEntries.get( getRemoteAddressId() ) ).readValue() : "N/A";
		}

		AuditView av = new Builder( eventName, username, remoteAddress, getStartTime() ).build();
		if ( getResourceIds() != null )
		{
			List<Long> resourceIdList = new ArrayList();
			List<String> idsAsString = ( List ) CoreJsonSerializer.collectionFromJson( getResourceIds(), new TypeToken()
			{
			} );
			for ( String idAsString : idsAsString )
			{
				resourceIdList.add( Long.valueOf( idAsString ) );
			}
			av.setResourceIds( resourceIdList );
		}
		if ( getAppId() != null )
		{
			String appId = dictionaryEntries.containsKey( getAppId() ) ? ( ( AuditDictionaryEntity ) dictionaryEntries.get( getAppId() ) ).readValue() : "N/A";
			av.setAppId( appId );
		}

		av.setEndTime( endTime );

		if ( ( getDetailsId() != null ) && ( dictionaryEntries.containsKey( getDetailsId() ) ) )
		{
			av.setEventDetailsFromString( ( ( AuditDictionaryEntity ) dictionaryEntries.get( getDetailsId() ) ).readValue() );
		}

		av.setEndTime( endTime );

		if ( eventTag != null )
		{
			av.setEventTag( eventTag );
		}

		return av;
	}
}
