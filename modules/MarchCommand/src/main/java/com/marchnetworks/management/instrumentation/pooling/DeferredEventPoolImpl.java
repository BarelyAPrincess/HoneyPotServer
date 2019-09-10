package com.marchnetworks.management.instrumentation.pooling;

import com.marchnetworks.command.common.device.data.ChannelState;
import com.marchnetworks.command.common.device.data.ConnectState;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.common.utils.DateUtils;
import com.marchnetworks.management.instrumentation.adaptation.DeviceEventHandlerScheduler;
import com.marchnetworks.management.instrumentation.events.AbstractDeviceEvent;
import com.marchnetworks.management.instrumentation.events.ChannelConnectionStateEvent;
import com.marchnetworks.management.instrumentation.events.DeviceConnectionStateEvent;
import com.marchnetworks.server.event.EventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeferredEventPoolImpl implements DeferredEventPool, EventListener
{
	private static final Logger LOG = LoggerFactory.getLogger( DeferredEventPoolImpl.class );
	private DeviceEventHandlerScheduler deviceEventHandlerScheduler;
	private Map<String, List<DeferredEvent>> deferredEvents = new ConcurrentHashMap();

	public synchronized void add( String id, DeferredEvent event )
	{
		List<DeferredEvent> events = ( List ) deferredEvents.get( id );
		if ( events == null )
		{
			events = new ArrayList();
			deferredEvents.put( id, events );
		}

		synchronized ( events )
		{
			LOG.debug( "Added new deferred event, trigger:" + event.getTrigger() + ", id:" + id );
			events.add( event );
		}
	}

	public void trigger( String id, String trigger )
	{
		List<DeferredEvent> events = ( List ) deferredEvents.get( id );

		if ( ( events != null ) && ( !events.isEmpty() ) )
		{
			synchronized ( events )
			{
				List<AbstractDeviceEvent> eventList = new ArrayList();

				Iterator<DeferredEvent> it = events.iterator();

				while ( it.hasNext() )
				{
					DeferredEvent deferredEvent = ( DeferredEvent ) it.next();

					if ( deferredEvent.getTrigger().equals( trigger ) )
					{
						LOG.debug( "Triggered deferred event, trigger:" + deferredEvent.getTrigger() + ", id:" + id );
						eventList.add( deferredEvent.getEvent() );
						it.remove();
					}
				}
				deviceEventHandlerScheduler.scheduleDeviceEventHandling( id, eventList );
			}
		}
	}

	public void evictEvents()
	{
		long now = DateUtils.getCurrentUTCTimeInMillis();

		for ( List<DeferredEvent> events : deferredEvents.values() )
		{
			synchronized ( events )
			{
				Iterator<DeferredEvent> it = events.iterator();
				while ( it.hasNext() )
				{
					DeferredEvent deferredEvent = ( DeferredEvent ) it.next();

					long age = now - deferredEvent.getCreatedTime();
					if ( age > deferredEvent.getEvictionAge() )
					{
						if ( deferredEvent.isTriggerOnEvict() )
						{
							deferredEvent.setEvicted( true );
							deviceEventHandlerScheduler.scheduleDeviceEventHandling( deferredEvent.getEvent().getDeviceId(), Collections.singletonList( deferredEvent.getEvent() ) );
							LOG.debug( "Triggered deferred event before evict, trigger:" + deferredEvent.getTrigger() + ", id:" + deferredEvent.getEvent().getDeviceId() );
						}
						LOG.debug( "Evicted deferred event, trigger:" + deferredEvent.getTrigger() + ", age:" + age + " ms" );
						it.remove();
					}
				}
			}
		}
	}

	public synchronized void set( String id, DeferredEvent event )
	{
		List<DeferredEvent> events = ( List ) deferredEvents.get( id );
		if ( events == null )
		{
			events = new ArrayList();
			deferredEvents.put( id, events );
		}

		synchronized ( events )
		{
			boolean bFound = false;
			String eventClassName = event.getEvent().getClass().getName();
			for ( DeferredEvent dEvent : events )
			{
				if ( eventClassName.equalsIgnoreCase( dEvent.getEvent().getClass().getName() ) )
				{
					bFound = true;
					dEvent = event;
					break;
				}
			}

			if ( !bFound )
			{
				events.add( event );
			}
		}
	}

	public void process( Event aEvent )
	{
		if ( ( aEvent instanceof DeviceConnectionStateEvent ) )
		{
			DeviceConnectionStateEvent connectStateEvent = ( DeviceConnectionStateEvent ) aEvent;
			if ( ConnectState.ONLINE.equals( connectStateEvent.getConnectState() ) )
			{
				trigger( connectStateEvent.getDeviceId(), connectStateEvent.getConnectState().toString() );
			}
		}
		else if ( ( aEvent instanceof ChannelConnectionStateEvent ) )
		{
			ChannelConnectionStateEvent channelConnectStateEvent = ( ChannelConnectionStateEvent ) aEvent;
			if ( ChannelState.ONLINE.equals( channelConnectStateEvent.getConnectionState() ) )
			{
				trigger( channelConnectStateEvent.getDeviceId(), channelConnectStateEvent.getConnectionState().toString() );
			}
		}
	}

	public String getListenerName()
	{
		return DeferredEventPoolImpl.class.getSimpleName();
	}

	public void setDeviceEventHandlerScheduler( DeviceEventHandlerScheduler deviceEventHandlerScheduler )
	{
		this.deviceEventHandlerScheduler = deviceEventHandlerScheduler;
	}
}

