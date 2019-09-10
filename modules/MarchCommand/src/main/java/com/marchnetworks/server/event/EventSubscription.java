package com.marchnetworks.server.event;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

public class EventSubscription
{
	private final int subscriptionId;
	private final long creationTime;
	private final long expirationTime;
	private Set<String> eventNotificationsSet = new HashSet();
	private Set<Long> territoryInfoSet = new HashSet();
	private String username;
	private String sessionId;
	private EventSubscriptionState state;
	private ScheduledFuture<?> cleanUpTask;

	public static enum EventSubscriptionState
	{
		INITIALIZING,
		READY,
		ACTIVE;

		private EventSubscriptionState()
		{
		}
	}

	public static class Builder
	{
		private final int subscriptionId;
		private final long expirationTime;
		private final long creationTime;
		private Set<String> eventNotificationsSet = new HashSet();
		private Set<Long> territoryInfoSet = new HashSet();
		private String username;
		private EventSubscriptionState state;
		private String sessionId;

		public Builder( int subscriptionId, long expirationTime )
		{
			this.subscriptionId = subscriptionId;
			this.expirationTime = expirationTime;
			creationTime = Calendar.getInstance().getTimeInMillis();
			state = EventSubscriptionState.INITIALIZING;
		}

		public Builder events( Set<String> subscriptionEvents )
		{
			if ( ( subscriptionEvents != null ) && ( subscriptionEvents.size() > 0 ) )
			{
				eventNotificationsSet.addAll( subscriptionEvents );
			}
			return this;
		}

		public Builder territoryInfoSet( Set<Long> territoryInfo )
		{
			if ( territoryInfo != null )
			{
				territoryInfoSet.addAll( territoryInfo );
			}
			return this;
		}

		public Builder username( String username )
		{
			this.username = username;
			return this;
		}

		public Builder sessionId( String sessionId )
		{
			this.sessionId = sessionId;
			return this;
		}

		public EventSubscription build()
		{
			return new EventSubscription( this );
		}
	}

	private EventSubscription( Builder builder )
	{
		this.subscriptionId = builder.subscriptionId;
		this.creationTime = builder.creationTime;
		this.expirationTime = builder.expirationTime;
		this.eventNotificationsSet = builder.eventNotificationsSet;
		this.territoryInfoSet = builder.territoryInfoSet;
		this.username = builder.username;
		this.sessionId = builder.sessionId;
		this.state = builder.state;
	}

	public int getSubscriptionId()
	{
		return subscriptionId;
	}

	public String getSessionId()
	{
		return sessionId;
	}

	public long getCreationTime()
	{
		return creationTime;
	}

	public long getExpirationTime()
	{
		return expirationTime;
	}

	public Set<String> getEventNotificationsSet()
	{
		return eventNotificationsSet;
	}

	public void setEventNotificationsSet( Set<String> eventPathsSet )
	{
		eventNotificationsSet = eventPathsSet;
	}

	public boolean hasEventNotificationPrefix( String prefix )
	{
		for ( String event : eventNotificationsSet )
		{
			if ( prefix.startsWith( event ) )
			{
				return true;
			}
		}
		return false;
	}

	public Set<Long> getTerritoryInfoSet()
	{
		return territoryInfoSet;
	}

	public void setTerritoryInfoSet( Set<Long> territoryInfoSet )
	{
		this.territoryInfoSet = territoryInfoSet;
	}

	public String getUsername()
	{
		return username;
	}

	public int hashCode()
	{
		int prime = 31;
		int result = 1;
		result = 31 * result + subscriptionId;
		return result;
	}

	public boolean equals( Object obj )
	{
		if ( this == obj )
			return true;
		if ( obj == null )
			return false;
		if ( getClass() != obj.getClass() )
			return false;
		EventSubscription other = ( EventSubscription ) obj;
		if ( subscriptionId != subscriptionId )
			return false;
		return true;
	}

	public EventSubscriptionState getState()
	{
		return state;
	}

	public void setState( EventSubscriptionState state )
	{
		this.state = state;
	}

	public void setCleanUpTask( ScheduledFuture<?> cleanUpTask )
	{
		this.cleanUpTask = cleanUpTask;
	}

	public ScheduledFuture<?> getCleanUpTask()
	{
		return cleanUpTask;
	}
}

