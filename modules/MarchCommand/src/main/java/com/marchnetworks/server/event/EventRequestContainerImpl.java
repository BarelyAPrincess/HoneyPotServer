package com.marchnetworks.server.event;

import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.command.common.scheduling.TaskScheduler;
import com.marchnetworks.common.config.ConfigProperty;
import com.marchnetworks.server.communications.serialization.ClientJsonSerializer;
import com.marchnetworks.shared.config.CommonConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class EventRequestContainerImpl implements EventRequestContainer
{
	private static final Logger LOG = LoggerFactory.getLogger( EventRequestContainerImpl.class );

	private static final String EMPTY_RESPONSE = "[]";
	private static final String EXECUTOR_ID = EventRequestContainer.class.getSimpleName();

	private static final long LONG_TIMEOUT = 1200000L;
	private static final int TIMEOUT_KEEPALIVE = 10;
	private static final int RESPONSE_KEEPALIVE = 60;
	private static final int RESPONSE_POOLSIZE = 64;
	private static final int TIMEOUT_POOLSIZE = 32;
	private static final int EVENT_SEND_DELAY_DEFAULT = 250;
	private EventPusher eventPusher;
	private TaskScheduler taskScheduler;
	private CommonConfiguration configuration;
	private Map<Integer, EventRequest> requests = new ConcurrentHashMap();
	private boolean waitingToSendEvents = false;

	private EventRequestTask sendEventsTask;

	public void addRequest( Integer id, long timeout, HttpServletRequest request, HttpServletResponse response ) throws IOException
	{
		List<EventNotification> events;

		try
		{
			events = eventPusher.getQueuedEvents( id );
			LOG.debug( "Founded {} events for id {}", Integer.valueOf( events.size() ), id );
		}
		catch ( EventNotificationException e )
		{
			LOG.error( "Sending error response " );
			sendErrorResponse( response, 400, EventRequestExceptionType.SUBSCRIPTION_INVALID, e.getMessage() );
			return;
		}

		eventPusher.extendSubscriptionTime( id );

		if ( !events.isEmpty() )
		{
			String jsonResponse = ClientJsonSerializer.toJson( events );

			PrintWriter out = response.getWriter();
			out.print( jsonResponse );
			out.close();

			LOG.debug( "Response returned: {}", jsonResponse );

		}
		else
		{
			AsyncContext ctx = request.startAsync();

			ctx.setTimeout( timeout < 1200000L ? 1200000L : timeout * 2L );

			ScheduledFuture<?> timeoutTask = taskScheduler.scheduleFixedPool( new EventRequestTimeoutTask( id.intValue() ), EXECUTOR_ID, 32, timeout, TimeUnit.SECONDS, 10 );
			requests.put( id, new EventRequest( id, ctx, timeoutTask ) );
		}
	}

	public void timeoutRequest( Integer id )
	{
		EventRequest request = ( EventRequest ) requests.remove( id );

		if ( request != null )
		{
			request.setTimeoutTask( null );
			AsyncContext context = request.getAsyncContext();
			try
			{
				PrintWriter out = context.getResponse().getWriter();
				Throwable localThrowable2 = null;
				try
				{
					out.write( "[]" );
				}
				catch ( Throwable localThrowable1 )
				{
					localThrowable2 = localThrowable1;
					throw localThrowable1;
				}
				finally
				{
					if ( out != null )
						if ( localThrowable2 != null )
							try
							{
								out.close();
							}
							catch ( Throwable x2 )
							{
								localThrowable2.addSuppressed( x2 );
							}
						else
							out.close();
				}
			}
			catch ( IOException e )
			{
				LOG.error( "Error processing event timeout response: " + e.getMessage() );
			}

			context.complete();

			LOG.debug( "----------------    Returned empty response --------------------- " );
		}
	}

	public synchronized void startRespondRequests( Set<Integer> ids )
	{
		ids.retainAll( requests.keySet() );
		if ( ids.isEmpty() )
		{
			return;
		}
		if ( waitingToSendEvents )
		{
			sendEventsTask.addIds( ids );
		}
		else
		{
			sendEventsTask = new EventRequestTask( ids );
			waitingToSendEvents = true;
			taskScheduler.schedule( sendEventsTask, getEventSendDelay(), TimeUnit.MILLISECONDS );
		}
	}

	public void respondRequests( Set<Integer> ids )
	{
		List<EventRequest> requestsToRespond = new ArrayList();

		synchronized ( this )
		{
			for ( Integer id : ids )
			{
				EventRequest request = ( EventRequest ) requests.remove( id );
				if ( request != null )
				{
					requestsToRespond.add( request );
				}
			}
			waitingToSendEvents = false;
		}

		for ( EventRequest request : requestsToRespond )
		{

			taskScheduler.cancelFixedPoolSchedule( EXECUTOR_ID, request.getTimeoutTask() );
			request.setTimeoutTask( null );
			try
			{
				List<EventNotification> eventList = eventPusher.getQueuedEvents( request.getId() );

				String result = ClientJsonSerializer.toJson( eventList );
				taskScheduler.executeFixedPool( new EventRequestResponseTask( request, result ), EXECUTOR_ID, 64, 60 );
			}
			catch ( EventNotificationException e )
			{
				LOG.error( "Error processing event response due to missing subscription: " + e.getMessage() );
			}
		}
	}

	public void respondRequest( EventRequest request, String response )
	{
		AsyncContext context = request.getAsyncContext();
		try
		{
			PrintWriter out = context.getResponse().getWriter();
			out.write( response );
		}
		catch ( IOException e )
		{
			LOG.error( "Error processing event response: " + e.getMessage() );
		}
		context.complete();
	}

	private int getEventSendDelay()
	{
		return configuration.getIntProperty( ConfigProperty.EVENT_SEND_DELAY, 250 );
	}

	private void sendErrorResponse( HttpServletResponse response, int status, EventRequestExceptionType code, String message )
	{
		response.setStatus( status );
		response.setHeader( "x-reason", code.name() );
		try
		{
			PrintWriter out = response.getWriter();
			out.print( message );
		}
		catch ( IOException e )
		{
			LOG.error( "Error while sending event error response: " + e.getMessage() );
		}
	}

	public void setEventPusher( EventPusher eventPusher )
	{
		this.eventPusher = eventPusher;
	}

	public void setTaskScheduler( TaskScheduler taskScheduler )
	{
		this.taskScheduler = taskScheduler;
	}

	public void setConfiguration( CommonConfiguration configuration )
	{
		this.configuration = configuration;
	}
}

