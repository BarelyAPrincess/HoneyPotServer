package com.marchnetworks.common.transaction;

import com.marchnetworks.common.diagnostics.metrics.MetricsHelper;
import com.marchnetworks.common.diagnostics.metrics.MetricsTypes;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.server.event.ChainedEventListener;
import com.marchnetworks.server.event.EventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TransactionalEventProcessor implements ChainedEventListener
{
	private static final Logger LOG = LoggerFactory.getLogger( TransactionalEventProcessor.class );

	public void processChain( Event event, List<EventListener> listeners )
	{
		LOG.debug( "started processing chain for event {}", event.toString() );
		for ( EventListener eventListener : listeners )
		{
			LOG.debug( "processing chained listener : " + eventListener );
			long start = System.currentTimeMillis();
			eventListener.process( event );
			MetricsHelper.metrics.addBucketMinMaxAvg( MetricsTypes.EVENTS_CHAINED.getName(), eventListener.getListenerName() + "." + event.getClass().getSimpleName(), System.currentTimeMillis() - start );
		}
	}
}
