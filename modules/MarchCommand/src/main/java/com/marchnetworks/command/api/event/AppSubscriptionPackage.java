package com.marchnetworks.command.api.event;

import com.marchnetworks.command.common.scheduling.TaskScheduler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AppSubscriptionPackage
{
	private String appId;
	private EventListener listener;
	private TaskScheduler taskScheduler;
	private Map<String, EventProcessingDetails> eventsPackage;
	private Map<String, EventProcessingDetails> deviceEventsPackage;

	public AppSubscriptionPackage()
	{
		eventsPackage = new HashMap();
		deviceEventsPackage = new HashMap();
	}

	public static class Builder
	{
		AppSubscriptionPackage subscriptionPackage;

		public Builder( String appId, EventListener listener, TaskScheduler scheduler )
		{
			subscriptionPackage = new AppSubscriptionPackage();
			subscriptionPackage.appId = appId;
			subscriptionPackage.taskScheduler = scheduler;
			subscriptionPackage.listener = listener;
		}

		public Builder addEvents( String... eventPaths )
		{
			for ( String string : eventPaths )
			{
				EventProcessingDetails ep = new EventProcessingDetails( EventProcessingType.SYNCHRONOUS, subscriptionPackage.listener.getClass().getSimpleName() );
				subscriptionPackage.eventsPackage.put( string, ep );
			}
			return this;
		}

		public Builder addEvents( EventProcessingType processingType, String... eventPaths )
		{
			for ( String string : eventPaths )
			{
				EventProcessingDetails ep = new EventProcessingDetails( processingType, subscriptionPackage.listener.getClass().getSimpleName() );
				subscriptionPackage.eventsPackage.put( string, ep );
			}
			return this;
		}

		public Builder addEvents( String processorName, EventProcessingType processingType, String... eventPaths )
		{
			for ( String string : eventPaths )
			{
				EventProcessingDetails ep = new EventProcessingDetails( processingType, processorName );
				subscriptionPackage.eventsPackage.put( string, ep );
			}
			return this;
		}

		public Builder addDeviceEvents( String... eventPaths )
		{
			for ( String string : eventPaths )
			{
				EventProcessingDetails ep = new EventProcessingDetails( EventProcessingType.SYNCHRONOUS, subscriptionPackage.listener.getClass().getSimpleName() );
				subscriptionPackage.deviceEventsPackage.put( string, ep );
			}
			return this;
		}

		public Builder addDeviceEvents( EventProcessingType processingType, String... eventPaths )
		{
			for ( String string : eventPaths )
			{
				EventProcessingDetails ep = new EventProcessingDetails( processingType, subscriptionPackage.listener.getClass().getSimpleName() );
				subscriptionPackage.deviceEventsPackage.put( string, ep );
			}
			return this;
		}

		public Builder addDeviceEvents( EventProcessingType processingType, String processorName, List<String> eventPaths )
		{
			for ( String string : eventPaths )
			{
				EventProcessingDetails ep = new EventProcessingDetails( processingType, processorName );
				subscriptionPackage.deviceEventsPackage.put( string, ep );
			}
			return this;
		}

		public AppSubscriptionPackage build()
		{
			return subscriptionPackage;
		}
	}

	public EventProcessingDetails getEventProcessingDetails( String eventPath )
	{
		for ( String subscribedPath : eventsPackage.keySet() )
		{
			if ( eventPath.startsWith( subscribedPath ) )
			{
				return ( EventProcessingDetails ) eventsPackage.get( subscribedPath );
			}
		}

		for ( String subscribedPath : deviceEventsPackage.keySet() )
		{
			if ( eventPath.startsWith( subscribedPath ) )
			{
				return ( EventProcessingDetails ) deviceEventsPackage.get( subscribedPath );
			}
		}

		return null;
	}

	public Set<String> getEvents()
	{
		return new HashSet( eventsPackage.keySet() );
	}

	public Set<String> getDeviceEvents()
	{
		return new HashSet( deviceEventsPackage.keySet() );
	}

	public Set<String> getAllEvents()
	{
		Set<String> allEvents = new HashSet();
		allEvents.addAll( eventsPackage.keySet() );
		allEvents.addAll( deviceEventsPackage.keySet() );
		return allEvents;
	}

	public TaskScheduler getTaskScheduler()
	{
		return taskScheduler;
	}

	public String getAppId()
	{
		return appId;
	}

	public EventListener getEventListener()
	{
		return listener;
	}

	public boolean hasDeviceEvents()
	{
		return !deviceEventsPackage.isEmpty();
	}

	public boolean hasEvent( String eventPath )
	{
		for ( String subscribedPath : eventsPackage.keySet() )
		{
			if ( eventPath.startsWith( subscribedPath ) )
			{
				return true;
			}
		}

		for ( String subscribedPath : deviceEventsPackage.keySet() )
		{
			if ( eventPath.startsWith( subscribedPath ) )
			{
				return true;
			}
		}
		return false;
	}

	public static class EventProcessingDetails
	{
		private EventProcessingType eventProcessingType;

		private String processorName;

		public EventProcessingDetails( EventProcessingType processingType, String processorName )
		{
			eventProcessingType = processingType;
			this.processorName = processorName;
		}

		public EventProcessingType getEventProcessingType()
		{
			return eventProcessingType;
		}

		public String getProcessorName()
		{
			return processorName;
		}

		public String toString()
		{
			return eventProcessingType.name() + ":" + processorName;
		}
	}
}
