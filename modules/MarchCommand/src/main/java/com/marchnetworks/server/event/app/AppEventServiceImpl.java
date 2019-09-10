package com.marchnetworks.server.event.app;

import com.marchnetworks.command.api.event.AppEventCoreService;
import com.marchnetworks.command.api.event.AppNotifiable;
import com.marchnetworks.command.api.event.AppSubscriptionPackage;
import com.marchnetworks.command.api.event.AppSubscriptionPackage.EventProcessingDetails;
import com.marchnetworks.command.api.event.EventListener;
import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.command.api.event.Notifiable;
import com.marchnetworks.command.common.CollectionUtils;
import com.marchnetworks.command.common.transaction.AppTransactionalBeanInterceptor;
import com.marchnetworks.command.common.transport.data.Event;
import com.marchnetworks.management.instrumentation.DeviceService;
import com.marchnetworks.management.instrumentation.DeviceTestService;
import com.marchnetworks.server.event.AppEventService;
import com.marchnetworks.server.event.EventRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class AppEventServiceImpl implements AppEventCoreService, AppEventService
{
	private static final Logger LOG = LoggerFactory.getLogger( AppEventServiceImpl.class );
	private static final int ASYNC_POOL_SIZE = 5;
	private static final String APP_EVENT_PATH_PREFIX = "generic.";
	private DeviceService deviceService;
	private DeviceTestService deviceTestService;
	private EventRegistry eventRegistry;
	private List<AppSubscriptionPackage> subscribers;

	public AppEventServiceImpl()
	{
		subscribers = new CopyOnWriteArrayList();
	}

	public void subscribe( AppSubscriptionPackage subscriptionPackage )
	{
		if ( subscriptionPackage.hasDeviceEvents() )
		{
			Set<String> allDeviceEvents = subscriptionPackage.getDeviceEvents();
			deviceService.addSubscription( ( String[] ) allDeviceEvents.toArray( new String[allDeviceEvents.size()] ) );
		}

		subscribers.add( subscriptionPackage );
	}

	public void send( EventNotification event )
	{
		send( event, false );
	}

	public void send( EventNotification event, boolean outOnly )
	{
		if ( ( outOnly ) || ( event.getAppId() != null ) )
		{
			eventRegistry.sendNotifiable( new NotifiableDecorator( event ) );
		}

		if ( !outOnly )
		{
			for ( AppSubscriptionPackage subscription : subscribers )
			{
				if ( subscription.hasEvent( event.getPath() ) )
				{
					dispatchEventToSubscriber( subscription, Collections.singletonList( event ) );
				}
			}
		}
	}

	public void send( List<EventNotification> events )
	{
		List<Notifiable> notifiables = new ArrayList();
		for ( EventNotification eventNotification : events )
		{
			if ( eventNotification.getAppId() != null )
			{
				notifiables.add( new NotifiableDecorator( eventNotification ) );
			}
		}

		if ( !notifiables.isEmpty() )
		{
			eventRegistry.sendNotifiable( notifiables );
		}

		for ( AppSubscriptionPackage subscription : subscribers )
		{
			List<EventNotification> filteredList = new ArrayList( events.size() );
			for ( EventNotification eventNotification : events )
			{
				if ( subscription.hasEvent( eventNotification.getPath() ) )
				{
					filteredList.add( eventNotification );
				}
			}
			dispatchEventToSubscriber( subscription, filteredList );
		}
	}

	public void sendAfterTransactionCommits( EventNotification event )
	{
		AppTransactionalBeanInterceptor.sendEventAfterTransactionCommits( event );
	}

	public void processAppNotifiables( List<AppNotifiable> events )
	{
		for ( AppSubscriptionPackage subscription : subscribers )
		{
			List<EventNotification> filteredList = new ArrayList( events.size() );
			for ( AppNotifiable notifiable : events )
			{
				if ( subscription.hasEvent( notifiable.getEventNotificationType() ) )
				{
					filteredList.add( notifiable.getNotificationInfo() );
				}
			}
			dispatchEventToSubscriber( subscription, filteredList );
		}
	}

	public void processAppStopped( String appid )
	{
		List<AppSubscriptionPackage> subscriptionsToRemove = new ArrayList();
		for ( AppSubscriptionPackage subscription : subscribers )
		{
			if ( subscription.getAppId().equals( appid ) )
			{
				Set<String> eventsInSubscription = subscription.getAllEvents();
				LOG.info( "App unsubscribed for id:{} , events: {}", appid, CollectionUtils.arrayToString( eventsInSubscription.toArray( new String[eventsInSubscription.size()] ), ",", true ) );
				Set<String> deviceEvents = subscription.getDeviceEvents();
				if ( !deviceEvents.isEmpty() )
				{
					deviceService.removeSubscription( ( String[] ) deviceEvents.toArray( new String[deviceEvents.size()] ) );
				}
				subscriptionsToRemove.add( subscription );
			}
		}

		subscribers.removeAll( subscriptionsToRemove );
	}

	public boolean subscriptionExists( String path )
	{
		for ( AppSubscriptionPackage subscriptionPackage : subscribers )
		{
			for ( String eventPath : subscriptionPackage.getDeviceEvents() )
			{
				if ( path.startsWith( eventPath ) )
				{
					return true;
				}
			}
		}
		return false;
	}

	public void injectDeviceEvent( Long deviceResourceId, Event event )
	{
		deviceTestService.injectDeviceEvent( deviceResourceId, event );
	}

	public void injectDeviceEvents( Long deviceResourceId, List<Event> events )
	{
		deviceTestService.injectDeviceEvents( deviceResourceId, events );
	}

	public void injectEventNotification( EventNotification eventNotification )
	{
		for ( AppSubscriptionPackage subscriber : subscribers )
		{
			if ( subscriber.getAllEvents().contains( eventNotification.getPath() ) )
			{
				dispatchEventToSubscriber( subscriber, Collections.singletonList( eventNotification ) );
			}
		}
	}

	private void dispatchEventToSubscriber( AppSubscriptionPackage subscription, List<EventNotification> events )
	{
		if ( events.isEmpty() )
		{
			return;
		}
		EventListener delegate = subscription.getEventListener();

		Map<EventProcessingDetails, List<EventNotification>> processingTypeMap = new HashMap();
		for ( EventNotification eventNotification : events )
		{
			EventProcessingDetails processingDetails = subscription.getEventProcessingDetails( eventNotification.getPath() );
			List<EventNotification> processingTypeEvents = ( List ) processingTypeMap.get( processingDetails );
			if ( processingTypeEvents == null )
			{
				processingTypeEvents = new ArrayList();
				processingTypeMap.put( processingDetails, processingTypeEvents );
			}
			processingTypeEvents.add( eventNotification );
		}

		for ( Iterator i$ = processingTypeMap.entrySet().iterator(); i$.hasNext(); )
		{
			Entry<EventProcessingDetails, List<EventNotification>> entry = ( Entry ) i$.next();
			switch ( entry.getKey().getEventProcessingType() )
			{
				case SYNCHRONOUS:
					delegate.processEvents( entry.getValue() );
					break;
				case ASYNCHRONOUS:
					for ( EventNotification event : entry.getValue() )
					{
						subscription.getTaskScheduler().executeNow( new RunnableWrapper( delegate, Collections.singletonList( event ) ) );
					}
					break;
				case ASYNCHRONOUS_SERIAL:
					subscription.getTaskScheduler().executeSerial( new RunnableWrapper( delegate, ( List ) entry.getValue() ), ( ( EventProcessingDetails ) entry.getKey() ).getProcessorName() );
					break;
				case ASYNCHRONOUS_POOLED:
					for ( EventNotification event : entry.getValue() )
					{
						subscription.getTaskScheduler().executeFixedPool( new RunnableWrapper( delegate, Collections.singletonList( event ) ), ( ( EventProcessingDetails ) entry.getKey() ).getProcessorName(), 5 );
					}
			}

		}
	}

	private class NotifiableDecorator extends EventNotification implements Notifiable
	{
		NotifiableDecorator( EventNotification notification )
		{
			id = notification.getId();
			path = ( "generic." + notification.getAppId() + "." + notification.getPath() );
			source = notification.getSource();
			value = notification.getValue();
			timestamp = notification.getTimestamp();
			appId = notification.getAppId();
			info = notification.getInfo();
		}

		public EventNotification getNotificationInfo()
		{
			return this;
		}

		public String getEventNotificationType()
		{
			return path;
		}
	}

	private class RunnableWrapper implements Runnable
	{
		EventListener listener;
		List<EventNotification> events;

		RunnableWrapper( EventListener listener, List<EventNotification> events )
		{
			this.listener = listener;
			this.events = events;
		}

		public void run()
		{
			listener.processEvents( events );
		}
	}

	public void setDeviceService( DeviceService deviceService )
	{
		this.deviceService = deviceService;
	}

	public void setEventRegistry( EventRegistry eventRegistry )
	{
		this.eventRegistry = eventRegistry;
	}

	public void setDeviceTestService( DeviceTestService deviceTestService )
	{
		this.deviceTestService = deviceTestService;
	}
}

