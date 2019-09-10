package com.marchnetworks.audit.data;

import com.google.gson.reflect.TypeToken;
import com.marchnetworks.com.util.AuditLogDetailsHelper;
import com.marchnetworks.common.serialization.CoreJsonSerializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;

public class DeviceAuditView
{
	private String eventName;
	private String userRemoteAddress;
	private String username;
	private Long time;
	private String sourceId;
	private List<Long> resourceIds;
	private Map<String, String> eventDetails = new HashMap();

	private Boolean deleted;

	public DeviceAuditView()
	{
	}

	private DeviceAuditView( Builder builder )
	{
		this.eventName = builder.eventName;
		this.userRemoteAddress = builder.remoteIpAddress;
		this.username = builder.username;
		this.time = builder.time;
		this.sourceId = builder.sourceId;
		this.resourceIds = builder.resourceIds;
		this.eventDetails = builder.details;
		this.deleted = false;
	}

	public String getEventName()
	{
		return eventName;
	}

	public void setEventName( String eventName )
	{
		this.eventName = eventName;
	}

	public String getUserRemoteAddress()
	{
		return userRemoteAddress;
	}

	public void setUserRemoteAddress( String userRemoteAddress )
	{
		this.userRemoteAddress = userRemoteAddress;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername( String username )
	{
		this.username = username;
	}

	@XmlElement( required = true )
	public Long getTime()
	{
		return time;
	}

	public void setTime( Long time )
	{
		this.time = time;
	}

	public List<Long> getResourceIds()
	{
		return resourceIds;
	}

	public void setResourceIds( List<Long> resourceIds )
	{
		this.resourceIds = resourceIds;
	}

	public String getSourceId()
	{
		return sourceId;
	}

	public void setSourceId( String sourceId )
	{
		this.sourceId = sourceId;
	}

	public Map<String, String> getEventDetails()
	{
		return eventDetails;
	}

	public void setEventDetails( Map<String, String> eventDetails )
	{
		this.eventDetails = eventDetails;
	}

	public String getEventDetailsAsString()
	{
		return CoreJsonSerializer.toJson( eventDetails );
	}

	public void setEventDetailsFromString( String jsonInput )
	{
		Map<String, String> parsedDetails = ( Map ) CoreJsonSerializer.collectionFromJson( jsonInput, new TypeToken<HashMap<String, String>>()
		{
		} );
		setEventDetails( parsedDetails );
	}

	@XmlElement( required = true )
	public Boolean getDeleted()
	{
		return deleted;
	}

	public void setDeleted( Boolean deleted )
	{
		this.deleted = deleted;
	}

	public static class Builder
	{
		private String eventName;
		private String username;
		private String remoteIpAddress;
		private String sourceId;
		private Long time;
		private List<Long> resourceIds = new ArrayList();
		private Map<String, String> details = new HashMap();

		public Builder( String eventName, String userName, String remoteIpAddress, Long time )
		{
			this.eventName = eventName;
			username = userName;
			this.remoteIpAddress = remoteIpAddress;
			this.time = time;
		}

		public Builder setEventName( String eventName )
		{
			this.eventName = eventName;
			return this;
		}

		public Builder addResourceId( Long id )
		{
			resourceIds.add( id );
			return this;
		}

		public Builder addSourceId( String sourceId )
		{
			this.sourceId = sourceId;
			return this;
		}

		public Builder addDetailsPair( String key, String value )
		{
			details.put( key, value );
			return this;
		}

		public Builder addResourceToAudit( Long resourceId )
		{
			if ( resourceId != null )
			{
				resourceIds.add( resourceId );
				details.put( "resource_path", AuditLogDetailsHelper.findResourcePath( resourceId ) );
			}
			return this;
		}

		public Builder addRootDeviceToAudit( String deviceId, boolean addResourcePath )
		{
			Long deviceResourceId = AuditLogDetailsHelper.findDeviceResourceId( deviceId );
			if ( deviceResourceId != null )
			{
				resourceIds.add( deviceResourceId );
				if ( addResourcePath )
					details.put( "resource_path", AuditLogDetailsHelper.findResourcePath( deviceResourceId ) );
			}
			return this;
		}

		public DeviceAuditView build()
		{
			return new DeviceAuditView( this );
		}
	}
}
