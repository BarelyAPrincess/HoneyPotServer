package com.marchnetworks.audit.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class AuditEntity
{
	@Id
	@Column( name = "ID" )
	@GeneratedValue
	private Long id;
	@Column( name = "REMOTE_ADDRESS" )
	private Integer remoteAddressId;
	@Column( name = "USER_NAME" )
	private Integer usernameId;
	@Column( name = "EVENT_NAME" )
	private Integer eventNameId;
	@Column( name = "DETAILS" )
	private Integer detailsId;
	@Column( name = "RESOURCES" )
	private String resourceIds;
	@Column( name = "START_TIMESTAMP" )
	private Long startTime;

	public Set<Integer> getKeys()
	{
		Set<Integer> keys = new HashSet( 4 );
		keys.add( usernameId );
		keys.add( eventNameId );
		if ( remoteAddressId != null )
		{
			keys.add( remoteAddressId );
		}
		if ( detailsId != null )
		{
			keys.add( detailsId );
		}
		return keys;
	}

	public Long getId()
	{
		return id;
	}

	public void setId( Long id )
	{
		this.id = id;
	}

	public Integer getRemoteAddressId()
	{
		return remoteAddressId;
	}

	public void setRemoteAddressId( Integer remoteAddressId )
	{
		this.remoteAddressId = remoteAddressId;
	}

	public Integer getUsernameId()
	{
		return usernameId;
	}

	public void setUsernameId( Integer usernameId )
	{
		this.usernameId = usernameId;
	}

	public Integer getEventNameId()
	{
		return eventNameId;
	}

	public void setEventNameId( Integer eventNameId )
	{
		this.eventNameId = eventNameId;
	}

	public Integer getDetailsId()
	{
		return detailsId;
	}

	public void setDetailsId( Integer detailsId )
	{
		this.detailsId = detailsId;
	}

	public String getResourceIds()
	{
		return resourceIds;
	}

	public void setResourceIds( String resourceIds )
	{
		this.resourceIds = resourceIds;
	}

	public Long getStartTime()
	{
		return startTime;
	}

	public void setStartTime( Long startTime )
	{
		this.startTime = startTime;
	}
}
