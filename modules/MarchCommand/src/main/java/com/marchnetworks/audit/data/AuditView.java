package com.marchnetworks.audit.data;

import com.google.gson.reflect.TypeToken;
import com.marchnetworks.com.util.AuditLogDetailsHelper;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.common.serialization.CoreJsonSerializer;
import com.marchnetworks.common.utils.DateUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

public class AuditView
{
	private String eventName;
	private String userRemoteAddress;
	private String username;
	private Long startTime;
	private Long endTime;
	private List<Long> resourceIds;
	private Map<String, String> eventDetails = new HashMap<String, String>();
	private String eventTag;
	private String appId;

	private AuditView()
	{
	}

	private AuditView( Builder builder )
	{
		this.username = builder.username;
		this.userRemoteAddress = builder.remoteIpAddress;
		this.startTime = builder.startTime;
		this.endTime = builder.endTime;
		this.eventDetails = builder.details;
		this.eventName = builder.eventName;
		this.resourceIds = builder.resourceIds;
		this.eventTag = builder.eventTag;
		this.appId = builder.appId;
	}

	@XmlTransient
	public String getEventTag()
	{
		return eventTag;
	}

	public void setEventTag( String eventTag )
	{
		this.eventTag = eventTag;
	}

	public String getUserRemoteAddress()
	{
		return userRemoteAddress;
	}

	public void setUserRemoteAddress( String userRemoteAddress )
	{
		this.userRemoteAddress = userRemoteAddress;
	}

	public void setUsername( String username )
	{
		this.username = username;
	}

	@XmlElement( required = true )
	public String getUsername()
	{
		return username;
	}

	@XmlElement( required = true )
	public Long getStartTime()
	{
		return startTime;
	}

	public void setStartTime( Long startTime )
	{
		this.startTime = startTime;
	}

	@XmlElement( required = true, nillable = true )
	public Long getEndTime()
	{
		return endTime;
	}

	public void setEndTime( Long endTime )
	{
		this.endTime = endTime;
	}

	@XmlElement( required = true )
	public String getEventName()
	{
		return eventName;
	}

	public void setEventName( String eventName )
	{
		this.eventName = eventName;
	}

	public Map<String, String> getEventDetails()
	{
		return eventDetails;
	}

	public void setEventDetails( Map<String, String> eventDetails )
	{
		this.eventDetails = eventDetails;
	}

	public List<Long> getResourceIds()
	{
		return resourceIds;
	}

	public void setResourceIds( List<Long> resourceIds )
	{
		this.resourceIds = resourceIds;
	}

	public String getEventDetailsAsString()
	{
		return CoreJsonSerializer.toJson( eventDetails );
	}

	public void setEventDetailsFromString( String jsonInput )
	{
		Map<String, String> parsedDetails = CoreJsonSerializer.collectionFromJson( jsonInput, new TypeToken<HashMap<String, String>>()
		{
		} );

		setEventDetails( parsedDetails );
	}

	public void addDetailsPair( String key, String value )
	{
		eventDetails.put( key, value );
	}

	public String getAppId()
	{
		return appId;
	}

	public void setAppId( String appId )
	{
		this.appId = appId;
	}

	public static class Builder
	{
		private String eventName;
		private String username;
		private String remoteIpAddress;
		private Long startTime;
		private Long endTime;
		private List<Long> resourceIds = new ArrayList<Long>();
		private Map<String, String> details = new HashMap<String, String>();
		private String eventTag;
		private String appId;

		public Builder( String eventName )
		{
			this.eventName = eventName;
			username = CommonAppUtils.getUsernameFromSecurityContext();
			remoteIpAddress = CommonAppUtils.getRemoteIpAddressFromSecurityContext();
			startTime = DateUtils.getCurrentUTCTimeInMillis();
		}

		public Builder( String eventName, String userName, String remoteIpAddress, Long startTime )
		{
			this.eventName = eventName;
			username = userName;
			this.remoteIpAddress = remoteIpAddress;
			this.startTime = startTime;
		}

		public Builder addEventTag( String tag )
		{
			eventTag = tag;
			return this;
		}

		public Builder addResourceId( Long id )
		{
			resourceIds.add( id );
			return this;
		}

		public Builder addDetailsPair( String key, String value )
		{
			details.put( key, value );
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

		public Builder setEndTime( Long timestamp )
		{
			endTime = timestamp;
			return this;
		}

		public Builder setAppId( String appId )
		{
			this.appId = appId;
			return this;
		}

		public Builder addChildDeviceToAudit( String childDeviceId, String rootDeviceId )
		{
			Long childDeviceResourceId = AuditLogDetailsHelper.findDeviceResourceId( childDeviceId );
			if ( childDeviceResourceId != null )
			{
				addResourceToAudit( childDeviceResourceId );
				addRootDeviceToAudit( rootDeviceId, false );
			}
			return this;
		}

		public Builder addChannelToAudit( String deviceId, String channelId )
		{
			Long channelResourceId = AuditLogDetailsHelper.findChannelResourceId( deviceId, channelId );
			if ( channelResourceId != null )
			{
				addResourceToAudit( channelResourceId );
				addRootDeviceToAudit( deviceId, false );
			}
			return this;
		}

		public Builder addSwitchToAudit( String deviceId, String switchId )
		{
			Long switchResourceId = AuditLogDetailsHelper.findOutputResourceId( deviceId, switchId );
			if ( switchResourceId != null )
			{
				addResourceToAudit( switchResourceId );
				addRootDeviceToAudit( deviceId, false );
			}
			return this;
		}

		public Builder addAudioOutputToAudit( String deviceId, String audioOutId )
		{
			Long audioResourceId = AuditLogDetailsHelper.findOutputResourceId( deviceId, audioOutId );
			if ( audioResourceId != null )
			{
				addResourceToAudit( audioResourceId );
				addRootDeviceToAudit( deviceId, false );
			}
			return this;
		}

		public Builder addAlarmSourceToAudit( String deviceId, String deviceAlarmSourceId )
		{
			Long alarmSourceResourceId = AuditLogDetailsHelper.findAlarmSourceResourceId( deviceId, deviceAlarmSourceId );
			if ( alarmSourceResourceId != null )
			{
				addResourceToAudit( alarmSourceResourceId );
				addRootDeviceToAudit( deviceId, false );
			}
			return this;
		}

		public Builder addResourceToAudit( Long resourceId )
		{
			return addResourceToAudit( resourceId, true );
		}

		public Builder addResourceToAudit( Long resourceId, boolean addPath )
		{
			if ( resourceId != null )
			{
				resourceIds.add( resourceId );
				if ( addPath )
				{
					details.put( "resource_path", AuditLogDetailsHelper.findResourcePath( resourceId ) );
				}
			}
			return this;
		}

		public Builder setUsername( String username )
		{
			this.username = username;
			return this;
		}

		public Builder setRemoteIpAddress( String remoteIpAddress )
		{
			this.remoteIpAddress = remoteIpAddress;
			return this;
		}

		public AuditView build()
		{
			return new AuditView( this );
		}
	}
}
