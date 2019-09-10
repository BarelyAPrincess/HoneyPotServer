package com.marchnetworks.server.event;

import com.marchnetworks.command.api.event.AppNotifiable;
import com.marchnetworks.command.api.event.Notifiable;
import com.marchnetworks.command.common.scheduling.TaskScheduler;
import com.marchnetworks.common.diagnostics.metrics.MetricsHelper;
import com.marchnetworks.common.diagnostics.metrics.MetricsTypes;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.transaction.TransactionalBeanInterceptor;
import com.marchnetworks.shared.config.CommonConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EventRegistryImpl implements EventRegistry
{
	private static Logger LOG = LoggerFactory.getLogger( EventRegistryImpl.class );

	public EventRegistryImpl()
	{
		listenerMap = new HashMap();
	}

	private Map<Object, List<EventListenerWrapper>> listenerMap;
	private CommonConfiguration configuration;
	private TaskScheduler taskScheduler;

	private List<EventListenerWrapper> getOrCreateListeners( Object a_EventType )
	{
		List<EventListenerWrapper> listeners = ( List ) listenerMap.get( a_EventType );

		if ( listeners == null )
		{
			listeners = new LinkedList();
			listenerMap.put( a_EventType, listeners );
		}

		return listeners;
	}

	public void register( Object eventType, EventListener listener, EventProcessingType processingType, int poolSize )
	{
		if ( eventType == null )
		{
			LOG.warn( "register: NULL event type=" + eventType + " for listener=" + listener );
		}
		else if ( listener == null )
		{
			LOG.warn( "register: NULL listener=" + listener + " for event type=" + eventType );
		}
		else
		{
			if ( ( eventType instanceof String ) )
			{

				eventType = ( ( String ) eventType ).trim();
			}

			List<EventListenerWrapper> listeners = getOrCreateListeners( eventType );
			if ( !listeners.contains( listener ) )
			{
				listeners.add( new EventListenerWrapper( listener, processingType, poolSize ) );
			}

			LOG.debug( "Registered listener for events of type=" + eventType );
		}
	}

	public void setAddListeners( EventListenersRegistration[] registrations )
	{
		HashMap<Object, LinkedList<EventListener>> map;
		Iterator i$;
		Object eventType;

		for ( EventListenersRegistration reg : registrations )
		{
			map = reg.getListenerMap();
			for ( i$ = map.keySet().iterator(); i$.hasNext(); )
			{
				eventType = i$.next();
				for ( EventListener listener : map.get( eventType ) )
					register( eventType, listener, reg.getProcessingType(), reg.getPoolSize() );
			}
		}
	}

	public void sendEventAfterTransactionCommits( Event event )
	{
		TransactionalBeanInterceptor.sendEventAfterTransactionCommits( event );
	}

	public void send( List<Event> eventList )
	{
		List<AppNotifiable> appNotifiables = new ArrayList<AppNotifiable>( 1 );
		List<Notifiable> notifiables = new ArrayList<Notifiable>( 1 );

		for ( Event event : eventList )
		{
			if ( event == null )
			{
				LOG.warn( "send: NULL event" );
			}
			else
			{
				List<EventListenerWrapper> listeners = ( List ) listenerMap.get( event.getEventType() );
				checkEventListeners( event, listeners );

				if ( ( event instanceof AppNotifiable ) )
					appNotifiables.add( ( AppNotifiable ) event );

				if ( ( event instanceof Notifiable ) )
					notifiables.add( ( Notifiable ) event );
			}
		}

		if ( !appNotifiables.isEmpty() )
		{
			AppNotifiableEventTask notifiableTask = new AppNotifiableEventTask( appNotifiables );
			taskScheduler.executeSerial( notifiableTask, AppNotifiable.class.getSimpleName() );
		}

		if ( !notifiables.isEmpty() )
		{
			NotifiableEventTask notifiableTask = new NotifiableEventTask( notifiables );
			taskScheduler.executeSerial( notifiableTask, EventPusher.class.getSimpleName() );
		}
	}

	public void send( Event event )
	{
		if ( event == null )
		{
			LOG.warn( "send: NULL event" );
			return;
		}

		List<EventListenerWrapper> listeners = ( List ) listenerMap.get( event.getEventType() );
		checkEventListeners( event, listeners );

		if ( ( event instanceof AppNotifiable ) )
		{
			AppNotifiableEventTask notifiableTask = new AppNotifiableEventTask( Collections.singletonList( ( AppNotifiable ) event ) );
			taskScheduler.executeSerial( notifiableTask, AppNotifiable.class.getSimpleName() );
		}

		if ( ( event instanceof Notifiable ) )
		{
			NotifiableEventTask notifiableTask = new NotifiableEventTask( Collections.singletonList( ( Notifiable ) event ) );
			taskScheduler.executeSerial( notifiableTask, EventPusher.class.getSimpleName() );
		}
	}

	public void sendNotifiable( Notifiable event )
	{
		NotifiableEventTask notifiableTask = new NotifiableEventTask( Collections.singletonList( event ) );
		taskScheduler.executeSerial( notifiableTask, EventPusher.class.getSimpleName() );
	}

	public void sendNotifiable( List<Notifiable> events )
	{
		NotifiableEventTask notifiableTask = new NotifiableEventTask( events );
		taskScheduler.executeSerial( notifiableTask, EventPusher.class.getSimpleName() );
	}

	private void checkEventListeners( Event event, List<EventListenerWrapper> listeners )
	{
		List<EventListener> chainedListeners = new ArrayList();
		List<EventListenerWrapper> regularListeners = new ArrayList();

		if ( listeners != null )
		{
			for ( EventListenerWrapper eventListenerWrapper : listeners )
			{
				if ( eventListenerWrapper.getEventProcessingType() == EventProcessingType.SYNCHRONOUS_CHAINED )
				{
					chainedListeners.add( eventListenerWrapper.getListener() );
				}
				else
				{
					regularListeners.add( eventListenerWrapper );
				}
			}
		}

		if ( !chainedListeners.isEmpty() )
		{
			ChainedEventListener processor = ( ChainedEventListener ) ApplicationContextSupport.getBean( "transactionalEventProcessorProxy" );
			try
			{
				processor.processChain( event, chainedListeners );
			}
			catch ( Exception ex )
			{
				LOG.warn( " Error during processing chain of event" + event + " Aborting Event execution and rolling back all db operations." );
				return;
			}
		}

		for ( EventListenerWrapper eventListenerWrapper : regularListeners )
		{
			EventProcessingType processingType = eventListenerWrapper.getEventProcessingType();

			if ( processingType == EventProcessingType.SYNCHRONOUS )
			{
				long start = System.currentTimeMillis();
				try
				{
					eventListenerWrapper.getListener().process( event );
				}
				catch ( Exception ex )
				{
					LOG.debug( "Error processing synchronous event" + event + ", Exception: " + ex.getMessage() );
				}
				MetricsHelper.metrics.addBucketMinMaxAvg( MetricsTypes.EVENTS_SYNC.getName(), eventListenerWrapper.getListener().getListenerName() + "." + event.getClass().getSimpleName(), System.currentTimeMillis() - start );
			}
			else
			{
				SendEventTask eventTask = new SendEventTask( event, eventListenerWrapper.getListener() );

				if ( processingType == EventProcessingType.ASYNC_SERIAL_PER_LISTENER )
				{
					taskScheduler.executeSerial( eventTask, eventListenerWrapper.getListener().getListenerName() );
				}
				else if ( processingType == EventProcessingType.ASYNC_PARALLEL_PER_LISTENER )
				{
					taskScheduler.executeFixedPool( eventTask, eventListenerWrapper.getListener().getListenerName(), eventListenerWrapper.getPoolSize() );
				}
				else if ( processingType == EventProcessingType.ASYNC_PARALLEL )
				{
					taskScheduler.executeNow( eventTask );
				}
			}
		}
	}

	private class SendEventTask implements Runnable
	{
		private Event event;
		private EventListener listener;

		public SendEventTask( Event event, EventListener listener )
		{
			this.event = event;
			this.listener = listener;
		}

		public void run()
		{
			long start = System.currentTimeMillis();
			try
			{
				listener.process( event );
			}
			catch ( Exception ex )
			{
				EventRegistryImpl.LOG.warn( "Error passing event={} to listener {}. Error Details {}", new Object[] {event, listener, ex} );
			}
			finally
			{
				MetricsHelper.metrics.addBucketMinMaxAvg( MetricsTypes.EVENTS_ASYNC.getName(), listener.getListenerName() + "." + event.getClass().getSimpleName(), System.currentTimeMillis() - start );
			}
		}
	}

	private class NotifiableEventTask implements Runnable
	{
		private List<Notifiable> notifiableEvents;

		public NotifiableEventTask( List<Notifiable> notifiableEvents )
		{
			this.notifiableEvents = notifiableEvents;
		}

		public void run()
		{
			try
			{
				EventPusher eventPusher = ( EventPusher ) ApplicationContextSupport.getBean( "eventPusher" );
				eventPusher.processNotifiables( notifiableEvents );
			}
			catch ( Exception e )
			{
				EventRegistryImpl.LOG.warn( "Error when sending events to EventPusher. Error details {}", e.getMessage() );
			}
		}
	}

	private class AppNotifiableEventTask implements Runnable
	{
		private List<AppNotifiable> notifiableEvents;

		public AppNotifiableEventTask( List<AppNotifiable> notifiableEvents )
		{
			this.notifiableEvents = notifiableEvents;
		}

		public void run()
		{
			try
			{
				AppEventService appManager = ( AppEventService ) ApplicationContextSupport.getBean( "appEventCoreService" );
				appManager.processAppNotifiables( notifiableEvents );
			}
			catch ( Exception e )
			{
				EventRegistryImpl.LOG.warn( "Error when sending event notifiableEvents to EventPusher. Error details {}", e.getMessage() );
			}
		}
	}

	private class EventListenerWrapper
	{
		private EventListener listener;
		EventProcessingType processingType = EventProcessingType.SYNCHRONOUS;
		private int poolSize = 1;

		public EventListenerWrapper( EventListener listener, EventProcessingType processingType, int poolSize )
		{
			this.listener = listener;

			this.processingType = processingType;
			this.poolSize = poolSize;
		}

		public EventListener getListener()
		{
			return listener;
		}

		public String toString()
		{
			return "EventListenerWrapper: type=" + processingType + " listener=" + listener;
		}

		public int getPoolSize()
		{
			return poolSize;
		}

		public EventProcessingType getEventProcessingType()
		{
			return processingType;
		}
	}

	public CommonConfiguration getCommonConfiguration()
	{
		return configuration;
	}

	public void setCommonConfiguration( CommonConfiguration commonConfiguration )
	{
		configuration = commonConfiguration;
	}

	public TaskScheduler getTaskScheduler()
	{
		return taskScheduler;
	}

	public void setTaskScheduler( TaskScheduler taskScheduler )
	{
		this.taskScheduler = taskScheduler;
	}
}

