package com.marchnetworks.server.event;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.command.api.event.Notifiable;
import com.marchnetworks.command.api.event.TerritoryAware;
import com.marchnetworks.command.api.event.UserNotifiable;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.scheduling.TaskScheduler;
import com.marchnetworks.command.common.topology.TopologyException;
import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.command.common.user.UserException;
import com.marchnetworks.command.common.user.data.MemberView;
import com.marchnetworks.common.event.EventTypesEnum;
import com.marchnetworks.common.event.StateCacheable;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.management.topology.ResourceTopologyServiceIF;
import com.marchnetworks.management.user.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

class EventPusherImpl implements EventPusher
{
	private static final Logger LOG = LoggerFactory.getLogger( EventPusherImpl.class );

	private static final int EVENT_QUEUE_MAX_SIZE = 100000;

	private final AtomicInteger subscriptionNumberBuilder = new AtomicInteger( 1 );

	private Map<Integer, EventSubscription> subscriptionsMap = new ConcurrentHashMap<Integer, EventSubscription>();

	private volatile Multimap<Integer, EventNotification> eventQueueMap = LinkedHashMultimap.create(); //Multimaps.synchronizedMultimap( LinkedHashMultimap.create() );

	private UserService userService;

	private StateCacheService stateCacheService;

	private ResourceTopologyServiceIF topologyService;

	private EventRequestContainer eventRequestContainer;

	private TaskScheduler taskScheduler;

	public String subscribeToEvents( String[] eventTypes, long subscriptionTimeout, String sessionId )
	{
		int generatedId = subscriptionNumberBuilder.getAndIncrement();
		long subscriptionInMillis = subscriptionTimeout * 1000L;

		Set<String> subscriptionEvents = buildSubscriptionEventTopics( eventTypes );

		EventSubscription subscription = new EventSubscription.Builder( generatedId, subscriptionInMillis ).events( subscriptionEvents ).username( getAuthenticatedUser() ).sessionId( sessionId ).build();

		subscriptionsMap.put( subscription.getSubscriptionId(), subscription );
		LOG.debug( "New subscription created with id: {}", subscription.getSubscriptionId() );

		setupSubscription( subscription.getSubscriptionId() );

		return String.valueOf( subscription.getSubscriptionId() );
	}

	public List<EventNotification> getQueuedEvents( Integer subscriptionId ) throws EventNotificationException
	{
		EventSubscription subscription = getSubscription( subscriptionId );
		List<EventNotification> eventsToSubscriber = new LinkedList();

		LOG.debug( "getQueuedEvents Request from Subscription ID {}", new Object[] {subscriptionId} );

		if ( ( eventQueueMap.containsKey( Integer.valueOf( subscription.getSubscriptionId() ) ) ) && ( eventQueueMap.get( Integer.valueOf( subscription.getSubscriptionId() ) ).size() > 0 ) )
		{
			eventsToSubscriber.addAll( getAndClearSubscriptionQueuedEvents( subscription ) );
			LOG.debug( "Returned {} events to Subscription {}", new Object[] {Integer.valueOf( eventsToSubscriber.size() ), subscriptionId} );
		}

		if ( subscription.getState().equals( EventSubscription.EventSubscriptionState.READY ) )
		{
			subscription.setState( EventSubscription.EventSubscriptionState.ACTIVE );
		}

		return eventsToSubscriber;
	}

	public List<EventNotification> getCachedEvents( Integer subscriptionId, String[] eventPaths, String[] eventSources, Long[] deviceIds ) throws EventNotificationException
	{
		EventSubscription subscription = getSubscription( subscriptionId );

		Set<Long> visibleDeviceIds = new HashSet( getTopologyService().getDeviceResourcesFromIdSet( subscription.getTerritoryInfoSet() ) );

		visibleDeviceIds.add( StateCacheable.CES_EVENT );

		if ( ( deviceIds != null ) && ( deviceIds.length > 0 ) )
		{
			visibleDeviceIds.retainAll( Arrays.asList( deviceIds ) );
		}

		if ( eventSources == null )
		{
			eventSources = new String[0];
		}

		if ( eventPaths == null )
		{
			eventPaths = new String[0];
		}

		List<EventNotification> result = new ArrayList();
		Collection<StateCacheable> events = stateCacheService.getCachedEvents( visibleDeviceIds, new String[0], eventSources );

		boolean needsFiltering = eventPaths.length > 0;
		for ( StateCacheable stateCacheable : events )
		{
			if ( needsFiltering )
			{
				for ( String eventPath : eventPaths )
				{
					if ( stateCacheable.getEventNotificationType().startsWith( eventPath ) )
					{
						result.add( stateCacheable.getNotificationInfo() );
						break;
					}
				}
			}
		}
		return result;
	}

	public void extendSubscriptionTime( Integer subscriptionId )
	{
		if ( subscriptionId == null )
		{
			throw new IllegalArgumentException( "subscriptionId can't be null" );
		}

		EventSubscription subscription = ( EventSubscription ) subscriptionsMap.get( subscriptionId );
		if ( subscription != null )
		{
			LOG.debug( "Extending Subscription id: {} with {} millis ", Integer.valueOf( subscription.getSubscriptionId() ), Long.valueOf( subscription.getExpirationTime() ) );
			if ( taskScheduler.cancelSchedule( subscription.getCleanUpTask() ) )
			{
				ScheduledFuture<?> cleanUpTask = taskScheduler.schedule( new EventPusherSubscriptionCleanupTask( subscription.getSubscriptionId() ), subscription.getExpirationTime(), TimeUnit.MILLISECONDS );

				subscription.setCleanUpTask( cleanUpTask );
			}
			else
			{
				LOG.warn( "An attempt to cancel clean up task for subscription {} has failed!", subscriptionId );
			}
		}
	}

	private Set<String> buildSubscriptionEventTopics( String[] eventTypes )
	{
		Set<String> subscriptionTopics = new HashSet();
		if ( eventTypes.length == 0 )
		{
			subscriptionTopics.addAll( EventTypesEnum.getFullPathEventSet() );
			return subscriptionTopics;
		}
		for ( String type : eventTypes )
		{
			if ( type.indexOf( "*" ) > -1 )
			{
				subscriptionTopics.addAll( EventTypesEnum.getFullPathEventSet() );
			}
			else
			{
				boolean found = false;
				for ( String prefix : subscriptionTopics )
				{
					if ( type.startsWith( prefix ) )
					{
						found = true;
					}
				}
				if ( !found )
				{

					subscriptionTopics.add( type );
				}
			}
		}
		return subscriptionTopics;
	}

	public void setupSubscription( int subscriptionId )
	{
		EventSubscription subscription = ( EventSubscription ) subscriptionsMap.get( Integer.valueOf( subscriptionId ) );
		if ( ( subscription == null ) || ( subscription.getState().equals( EventSubscription.EventSubscriptionState.READY ) ) )
		{
			LOG.info( " Subscription {} not found or in ready state. Aborting operation", Integer.valueOf( subscriptionId ) );
			return;
		}

		ScheduledFuture<?> cleanUpTask = taskScheduler.schedule( new EventPusherSubscriptionCleanupTask( subscriptionId ), subscription.getExpirationTime(), TimeUnit.MILLISECONDS );
		subscription.setCleanUpTask( cleanUpTask );

		Set<Long> territoryInfo = new HashSet();
		String user = subscription.getUsername();
		if ( user != null )
		{
			territoryInfo = getTerritoryInfoFromUser( user );
		}

		Set<Long> visibleDeviceIds = new HashSet( getTopologyService().getDeviceResourcesFromIdSet( territoryInfo ) );
		visibleDeviceIds.add( StateCacheable.CES_EVENT );

		Collection<StateCacheable> cachedEvents = stateCacheService.getCachedEvents( visibleDeviceIds, new String[0], new String[0] );
		boolean stateEventsAdded = cachedEvents != null && cachedEvents.size() > 0;

		subscription.setTerritoryInfoSet( territoryInfo );
		subscription.setState( EventSubscription.EventSubscriptionState.READY );
		LOG.debug( " Subscription {} is ready to receive events", Integer.valueOf( subscriptionId ) );

		if ( stateEventsAdded )
		{
			for ( StateCacheable stateCacheable : cachedEvents )
			{
				if ( subscription.hasEventNotificationPrefix( stateCacheable.getEventNotificationType() ) )
				{
					eventQueueMap.put( Integer.valueOf( subscriptionId ), stateCacheable.getNotificationInfo() );
				}
			}
			getEventRequestContainer().startRespondRequests( new HashSet( Collections.singleton( Integer.valueOf( subscription.getSubscriptionId() ) ) ) );
		}
	}

	public void modifySubscription( int subscriptionId, String[] eventPrefixes ) throws EventNotificationException
	{
		EventSubscription subscription = getSubscription( Integer.valueOf( subscriptionId ) );
		Set<String> newEventPrefixes = buildSubscriptionEventTopics( eventPrefixes );
		subscription.setEventNotificationsSet( newEventPrefixes );

		Set<Long> deviceIds = new HashSet( getTopologyService().getDeviceResourcesFromIdSet( subscription.getTerritoryInfoSet() ) );

		deviceIds.add( StateCacheable.CES_EVENT );
		Collection<StateCacheable> cachedEvents = stateCacheService.getCachedEvents( deviceIds, new String[0], new String[0] );

		boolean stateEventsAdded = cachedEvents != null && cachedEvents.size() > 0;

		if ( stateEventsAdded )
		{
			for ( StateCacheable stateCacheable : cachedEvents )
			{
				if ( subscription.hasEventNotificationPrefix( stateCacheable.getEventNotificationType() ) )
				{
					eventQueueMap.put( Integer.valueOf( subscriptionId ), stateCacheable.getNotificationInfo() );
				}
			}
			getEventRequestContainer().startRespondRequests( new HashSet( Collections.singleton( Integer.valueOf( subscription.getSubscriptionId() ) ) ) );
		}
	}

	public void cancelSubscription( int subscriptionId ) throws EventNotificationException
	{
		EventSubscription subscription = getSubscription( Integer.valueOf( subscriptionId ) );
		LOG.info( "Subscription id:{} is going to be cancelled. removing from subscriptionsMap", Integer.valueOf( subscriptionId ) );

		synchronized ( subscriptionsMap )
		{
			subscriptionsMap.remove( Integer.valueOf( subscription.getSubscriptionId() ) );
		}

		eventQueueMap.removeAll( Integer.valueOf( subscription.getSubscriptionId() ) );
	}

	public void cancelSubscriptionsForSession( String sessionId )
	{
		List<EventSubscription> subscriptions = getSubscriptionsForSession( sessionId );
		for ( EventSubscription subscription : subscriptions )
		{
			try
			{
				cancelSubscription( subscription.getSubscriptionId() );
			}
			catch ( EventNotificationException ex )
			{
				LOG.info( "Failed to cancel subscription {}.", Integer.valueOf( subscription.getSubscriptionId() ) );
			}
		}
	}

	public void cancelAllSubscriptions()
	{
		List<Integer> list = new ArrayList();
		for ( EventSubscription subscription : subscriptionsMap.values() )
		{
			list.add( Integer.valueOf( subscription.getSubscriptionId() ) );
		}
		for ( Integer i : list )
		{
			try
			{
				cancelSubscription( i.intValue() );
			}
			catch ( EventNotificationException ex )
			{
				LOG.info( "Failed to cancel subscription {}.", i );
			}
		}
	}

	public int getTotalSubscriptions()
	{
		return subscriptionsMap.size();
	}

	private EventSubscription getSubscription( Integer subscriptionId ) throws EventNotificationException
	{
		if ( subscriptionId == null )
		{
			throw new IllegalArgumentException( "subscriptionId can't be null" );
		}

		EventSubscription subscription = ( EventSubscription ) subscriptionsMap.get( subscriptionId );
		if ( subscription == null )
		{
			LOG.info( "Subscription id {} not found in list of subscribers.", subscriptionId );
			throw new EventNotificationException( "Subscription id " + subscriptionId + " not found in list of subscribers" );
		}
		return subscription;
	}

	private List<EventSubscription> getSubscriptionsForSession( String sessionId )
	{
		if ( sessionId == null )
		{
			throw new IllegalArgumentException( "sessionId can't be null" );
		}

		List<EventSubscription> subscriptions = new ArrayList();
		for ( EventSubscription subscription : subscriptionsMap.values() )
		{
			if ( sessionId.equals( subscription.getSessionId() ) )
			{
				subscriptions.add( subscription );
			}
		}
		return subscriptions;
	}

	private String getAuthenticatedUser()
	{
		return CommonAppUtils.getUsernameFromSecurityContext();
	}

	private Set<Long> getTerritoryInfoFromUser( String username )
	{
		Set<Long> territoryInfo = new HashSet();
		try
		{
			MemberView memberView = getUserService().getUser( username );
			if ( memberView != null )
			{
				LOG.debug( "Loading up resources with All associations" );
				for ( Long id : memberView.getAllRoots( true ) )
				{
					territoryInfo.addAll( getRelatedResourceIdSet( id ) );
				}
			}
		}
		catch ( TopologyException e )
		{
			LOG.error( "Could not get territory info for user " + username + ". Exception = " + e.getMessage() );
		}
		catch ( UserException e )
		{
			LOG.error( "Could not get territory info for user " + username + ". Exception = " + e.getMessage() );
		}

		return territoryInfo;
	}

	private Set<Long> getRelatedResourceIdSet( Long resourceId ) throws TopologyException
	{
		Set<Long> relatedResourceIdSet = new HashSet<Long>();
		Resource territoryResource = getTopologyService().getResource( resourceId );
		if ( territoryResource != null )
		{
			relatedResourceIdSet.addAll( territoryResource.getAllResourceAssociationIds() );
		}
		return relatedResourceIdSet;
	}

	private List<EventNotification> getAndClearSubscriptionQueuedEvents( EventSubscription subscription )
	{
		List<EventNotification> events = new LinkedList<EventNotification>();

		events.addAll( eventQueueMap.removeAll( subscription.getSubscriptionId() ) );
		if ( events.size() > 0 )
		{
			LOG.debug( "{} events waiting for Subscription ID {}", new Object[] {Integer.valueOf( events.size() ), Integer.valueOf( subscription.getSubscriptionId() )} );
		}
		return events;
	}

	public void processNotifiables( List<Notifiable> notifiableList )
	{
		Set<Integer> subscriptionsToNotify = new LinkedHashSet();
		for ( Notifiable event : notifiableList )
		{
			EventNotification eventNotification = event.getNotificationInfo();

			if ( LOG.isDebugEnabled() )
			{
				LOG.debug( "processing notification for event: path=" + eventNotification.getPath() + " source=" + eventNotification.getSource() + " value=" + eventNotification.getValue() );
			}

			if ( LOG.isDebugEnabled() )
			{
				LOG.debug( "filter: currentSubscribers size()=" + subscriptionsMap.size() + " a_Event=" + event );
			}

			refreshSubscriptionsTerritory( event );

			List<Integer> overQueueLimitIds = new ArrayList();
			for ( EventSubscription subscription : subscriptionsMap.values() )
			{
				if ( subscription.hasEventNotificationPrefix( eventNotification.getPath() ) )
				{
					if ( ( !( event instanceof TerritoryAware ) ) ||

							( checkEventTerritory( subscription, ( TerritoryAware ) event ) ) )
					{

						if ( ( event instanceof UserNotifiable ) )
						{
							String user = ( ( UserNotifiable ) event ).getUser();
							if ( !subscription.getUsername().equals( user ) )
							{
							}

						}
						else
						{
							eventQueueMap.put( Integer.valueOf( subscription.getSubscriptionId() ), eventNotification );

							LOG.debug( "EventPath {} queued for subscription {}", new Object[] {eventNotification.getPath(), Integer.valueOf( subscription.getSubscriptionId() )} );

							if ( ( subscription.getState().equals( EventSubscription.EventSubscriptionState.ACTIVE ) ) && ( eventQueueMap.get( Integer.valueOf( subscription.getSubscriptionId() ) ).size() > 100000 ) )
							{
								LOG.info( "Subscription {} has gone over the allowed queue limit with {} events.", Integer.valueOf( subscription.getSubscriptionId() ), Integer.valueOf( eventQueueMap.get( Integer.valueOf( subscription.getSubscriptionId() ) ).size() ) );
								overQueueLimitIds.add( Integer.valueOf( subscription.getSubscriptionId() ) );
							}
							else
							{
								subscriptionsToNotify.add( Integer.valueOf( subscription.getSubscriptionId() ) );
							}
						}
					}
				}
			}
			for ( Integer subscriptionId : overQueueLimitIds )
			{
				try
				{
					cancelSubscription( subscriptionId.intValue() );
				}
				catch ( EventNotificationException e )
				{
					LOG.info( "Could not cancel subscription {}. Subscription not found.", subscriptionId );
				}
			}
		}

		if ( !subscriptionsToNotify.isEmpty() )
		{
			getEventRequestContainer().startRespondRequests( subscriptionsToNotify );
		}
	}

	private void refreshSubscriptionsTerritory( Notifiable event )
	{
		if ( ( event.getEventNotificationType().equals( EventTypesEnum.TOPOLOGY_CREATED.getFullPathEventName() ) ) || ( event.getEventNotificationType().equals( EventTypesEnum.TOPOLOGY_MOVED.getFullPathEventName() ) ) )
		{
			LOG.debug( "Refreshing subscriptions territory. " );
			EventNotification notification = event.getNotificationInfo();

			Long parentId;

			if ( event.getEventNotificationType().equals( EventTypesEnum.TOPOLOGY_CREATED.getFullPathEventName() ) )
			{
				parentId = Long.valueOf( Long.parseLong( notification.getInfo( "CES_PARENT_RESOURCE_ID" ) ) );
			}
			else
			{
				parentId = ( Long ) notification.getValue();
			}

			Long resourceId = Long.valueOf( notification.getSource() );

			synchronized ( subscriptionsMap )
			{
				for ( EventSubscription subscription : subscriptionsMap.values() )
				{
					if ( subscription.getTerritoryInfoSet().contains( parentId ) )
					{
						try
						{
							Set<Long> resources = getRelatedResourceIdSet( resourceId );
							subscription.getTerritoryInfoSet().addAll( resources );
						}
						catch ( TopologyException e )
						{
							LOG.warn( "Could not get Territory info for Resource " + resourceId );
						}
					}
				}
			}
		}
	}

	private boolean checkEventTerritory( EventSubscription subscription, TerritoryAware event )
	{
		boolean isVisible = true;
		EventSubscription.EventSubscriptionState state = subscription.getState();
		if ( ( event.getTerritoryInfo() != null ) && ( ( state == EventSubscription.EventSubscriptionState.READY ) || ( state == EventSubscription.EventSubscriptionState.ACTIVE ) ) )
		{

			if ( Collections.disjoint( event.getTerritoryInfo(), subscription.getTerritoryInfoSet() ) )
			{
				isVisible = false;
			}
		}
		return isVisible;
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append( "EventPusherImpl:" );

		sb.append( " #Subscriptions=" );
		sb.append( subscriptionsMap.size() );

		return sb.toString();
	}

	public Map<Integer, EventSubscription> getSubscriptionsMap()
	{
		return subscriptionsMap;
	}

	public void setTaskScheduler( TaskScheduler taskScheduler )
	{
		this.taskScheduler = taskScheduler;
	}

	public void setStateCacheService( StateCacheService stateCacheService )
	{
		this.stateCacheService = stateCacheService;
	}

	private UserService getUserService()
	{
		if ( userService == null )
		{
			userService = ( ( UserService ) ApplicationContextSupport.getBean( "userServiceProxy_internal" ) );
		}
		return userService;
	}

	private ResourceTopologyServiceIF getTopologyService()
	{
		if ( topologyService == null )
		{
			topologyService = ( ( ResourceTopologyServiceIF ) ApplicationContextSupport.getBean( "resourceTopologyServiceProxy_internal" ) );
		}
		return topologyService;
	}

	private EventRequestContainer getEventRequestContainer()
	{
		if ( eventRequestContainer == null )
		{
			eventRequestContainer = ( ( EventRequestContainer ) ApplicationContextSupport.getBean( "eventRequestContainer" ) );
		}
		return eventRequestContainer;
	}
}

