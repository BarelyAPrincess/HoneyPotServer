package com.marchnetworks.server.event;

import com.marchnetworks.common.spring.ApplicationContextSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventPusherSubscriptionCleanupTask implements Runnable
{
	private static final Logger LOG = LoggerFactory.getLogger( EventPusherSubscriptionCleanupTask.class );
	private int subscriptionId;

	public EventPusherSubscriptionCleanupTask( int subscriptionId )
	{
		this.subscriptionId = subscriptionId;
		LOG.debug( "New instance of EventPusherSubscriptionCleanupTask with subscription id {}", Integer.valueOf( subscriptionId ) );
	}

	public void run()
	{
		LOG.info( "Subscription id:{} has expired.", Integer.valueOf( subscriptionId ) );
		try
		{
			getEventPusher().cancelSubscription( subscriptionId );
		}
		catch ( EventNotificationException e )
		{
			LOG.error( "Exception removing subscription of {}", Integer.valueOf( subscriptionId ) );
		}
	}

	private EventPusher getEventPusher()
	{
		return ( EventPusher ) ApplicationContextSupport.getBean( "eventPusher" );
	}
}

