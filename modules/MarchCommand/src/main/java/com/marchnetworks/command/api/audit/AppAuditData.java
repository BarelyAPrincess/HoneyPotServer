package com.marchnetworks.command.api.audit;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class AppAuditData
{
	private String eventName;
	private Long startTime;
	private Long endTime;
	private Map<String, String> eventDetails;
	private String appId;
	private String user;
	private String address;

	private AppAuditData( Builder builder )
	{
		this.eventName = builder.eventName;
		this.startTime = builder.startTime;
		this.endTime = builder.endTime;
		this.eventDetails = builder.details;
		this.user = builder.user;
		this.address = builder.address;
	}

	public Long getStartTime()
	{
		return startTime;
	}

	public void setStartTime( Long startTime )
	{
		this.startTime = startTime;
	}

	public Long getEndTime()
	{
		return endTime;
	}

	public void setEndTime( Long endTime )
	{
		this.endTime = endTime;
	}

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

	public String getEventDetailsAsString()
	{
		Gson gson = new Gson();
		return gson.toJson( eventDetails );
	}

	public String getAppId()
	{
		return appId;
	}

	public void setAppId( String appId )
	{
		this.appId = appId;
	}

	public String getUser()
	{
		return user;
	}

	public void setUser( String user )
	{
		this.user = user;
	}

	public String getAddress()
	{
		return address;
	}

	public void setAddress( String address )
	{
		this.address = address;
	}

	public static class Builder
	{
		private String user;
		private String address;
		private String eventName;
		private Long startTime;
		private Long endTime;
		private Map<String, String> details;

		public Builder( String eventName )
		{
			this.eventName = eventName;
			startTime = Long.valueOf( System.currentTimeMillis() );
		}

		public Builder( String eventName, String user, String address )
		{
			this.eventName = eventName;
			startTime = Long.valueOf( System.currentTimeMillis() );
			this.user = user;
			this.address = address;
		}

		public void addDetailsPair( String key, String value )
		{
			if ( details == null )
			{
				details = new HashMap( 4 );
			}
			details.put( key, value );
		}

		public AppAuditData build()
		{
			return new AppAuditData( this );
		}
	}
}
