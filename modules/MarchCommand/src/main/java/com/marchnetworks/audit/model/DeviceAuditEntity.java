package com.marchnetworks.audit.model;

import com.google.gson.reflect.TypeToken;
import com.marchnetworks.audit.data.DeviceAuditView;
import com.marchnetworks.audit.data.DeviceAuditView.Builder;
import com.marchnetworks.common.serialization.CoreJsonSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table( name = "DEVICE_AUDIT_LOGS" )
public class DeviceAuditEntity extends AuditEntity
{
	@Column( name = "SOURCEID" )
	Integer sourceId;
	@Column( name = "DELETED" )
	Boolean deleted;

	public DeviceAuditView toDataObject( Map<Integer, AuditDictionaryEntity> dictionaryEntries )
	{
		String eventName = ( ( AuditDictionaryEntity ) dictionaryEntries.get( getEventNameId() ) ).readValue();
		String username = ( ( AuditDictionaryEntity ) dictionaryEntries.get( getUsernameId() ) ).readValue();
		String remoteAddress = null;
		if ( getRemoteAddressId() != null )
		{
			remoteAddress = ( ( AuditDictionaryEntity ) dictionaryEntries.get( getRemoteAddressId() ) ).readValue();
		}

		DeviceAuditView av = new Builder( eventName, username, remoteAddress, getStartTime() ).build();
		if ( sourceId != null )
		{
			av.setSourceId( ( ( AuditDictionaryEntity ) dictionaryEntries.get( sourceId ) ).readValue() );
		}

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

		if ( deleted != null )
		{
			av.setDeleted( deleted );
		}

		if ( getDetailsId() != null )
		{
			av.setEventDetailsFromString( ( ( AuditDictionaryEntity ) dictionaryEntries.get( getDetailsId() ) ).readValue() );
		}
		return av;
	}

	public Set<Integer> getKeys()
	{
		Set<Integer> keys = super.getKeys();
		if ( sourceId != null )
		{
			keys.add( sourceId );
		}
		return keys;
	}

	public Integer getSourceId()
	{
		return sourceId;
	}

	public void setSourceId( Integer sourceId )
	{
		this.sourceId = sourceId;
	}

	public Boolean getDeleted()
	{
		return deleted;
	}

	public void setDeleted( boolean deleted )
	{
		this.deleted = Boolean.valueOf( deleted );
	}
}
