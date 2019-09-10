package com.marchnetworks.management.event;

import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.server.communications.serialization.ClientJsonSerializer;
import com.marchnetworks.server.event.EventNotificationException;
import com.marchnetworks.server.event.EventPusher;
import com.marchnetworks.server.event.EventRequestContainer;
import com.marchnetworks.server.event.EventRequestExceptionType;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet( value = {"/rest/eventServiceRest/*"}, asyncSupported = true )
public class EventRestServlet extends HttpServlet
{
	private static final Logger LOG = LoggerFactory.getLogger( EventRestServlet.class );

	private static final long serialVersionUID = -8143011494070967473L;

	private static EventPusher eventPusher = ( EventPusher ) ApplicationContextSupport.getBean( "eventPusher" );
	private static EventRequestContainer eventRequestContainer = ( EventRequestContainer ) ApplicationContextSupport.getBean( "eventRequestContainer" );

	protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		LOG.trace( "  -------- doget: request received! --------------" );

		String pathInfo = request.getPathInfo();

		response.setCharacterEncoding( "UTF-8" );
		response.setContentType( "application/json" );

		if ( "/events".equals( pathInfo ) )
		{
			Integer id = Integer.valueOf( Integer.parseInt( request.getParameter( "subscriptionId" ) ) );
			long timeout = Long.parseLong( request.getParameter( "waitTimeout" ) );

			LOG.debug( "Adding request to the eventRequestContainer for subscription id {} with a timeout of {}", id, Long.valueOf( timeout ) );

			eventRequestContainer.addRequest( id, timeout, request, response );
		}
		else if ( "/events/state".equals( pathInfo ) )
		{
			Integer id = Integer.valueOf( Integer.parseInt( request.getParameter( "subscriptionId" ) ) );

			String eventPrefixes = request.getParameter( "eventPrefixes" );
			String[] events = ( String[] ) deserializeArrayParam( eventPrefixes, String[].class );
			String eventSources = request.getParameter( "eventSources" );
			String[] sources = ( String[] ) deserializeArrayParam( eventSources, String[].class );
			String deviceIds = request.getParameter( "deviceIds" );
			Long[] devices = ( Long[] ) deserializeArrayParam( deviceIds, Long[].class );

			OutputStream out = response.getOutputStream();
			try
			{
				LOG.debug( "Getting cached events for subscription id {}", id );
				List<EventNotification> cachedEvents = eventPusher.getCachedEvents( id, events, sources, devices );
				String jsonResult = ClientJsonSerializer.toJson( cachedEvents );
				out.write( jsonResult.getBytes() );
			}
			catch ( EventNotificationException e )
			{
				LOG.error( "Response with bad request because {}", e.getMessage() );
				response.setStatus( 400 );
				response.setHeader( "x-reason", EventRequestExceptionType.SUBSCRIPTION_INVALID.name() );
			}
		}
	}

	protected void doPut( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		String pathInfo = request.getPathInfo();

		if ( "/subscription".equals( pathInfo ) )
		{
			Integer subscriptionId = request.getParameter( "subscriptionId" ) == null ? null : Integer.valueOf( Integer.parseInt( request.getParameter( "subscriptionId" ) ) );

			String eventPrefixes = request.getParameter( "eventPrefixes" );
			String[] events = ( String[] ) deserializeArrayParam( eventPrefixes, String[].class );

			Long timeout = request.getParameter( "subscriptionTimeout" ) == null ? null : Long.valueOf( Long.parseLong( request.getParameter( "subscriptionTimeout" ) ) );
			HttpSession session = request.getSession( false );
			if ( session == null )
			{
				response.setStatus( 400 );
				response.setHeader( "x-reason", EventRequestExceptionType.SUBSCRIPTION_INVALID.name() );
				return;
			}
			String sessionId = session.getId();
			if ( subscriptionId == null )
			{
				String id = eventPusher.subscribeToEvents( events, timeout.longValue(), sessionId );

				OutputStream out = response.getOutputStream();
				out.write( id.getBytes() );
			}
			else
			{
				try
				{
					eventPusher.modifySubscription( subscriptionId.intValue(), events );
				}
				catch ( EventNotificationException ex )
				{
					response.setStatus( 400 );
					response.setHeader( "x-reason", EventRequestExceptionType.SUBSCRIPTION_INVALID.name() );
				}
			}
		}
	}

	protected void doDelete( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		String pathInfo = request.getPathInfo();
		if ( "/subscription".equals( pathInfo ) )
		{
			Integer subscriptionId = Integer.valueOf( Integer.parseInt( request.getParameter( "subscriptionId" ) ) );
			try
			{
				eventPusher.cancelSubscription( subscriptionId.intValue() );
			}
			catch ( EventNotificationException e )
			{
				response.setStatus( 400 );
				response.setHeader( "x-reason", EventRequestExceptionType.SUBSCRIPTION_INVALID.name() );
			}
		}
	}

	private <T> T deserializeArrayParam( String input, Class<T> arrayType )
	{
		T array = null;
		if ( input == null )
		{
			try
			{
				return ( T ) arrayType.newInstance();

			}
			catch ( InstantiationException localInstantiationException )
			{
			}
			catch ( IllegalAccessException localIllegalAccessException )
			{
			}
		}
		else
		{
			array = ClientJsonSerializer.fromJson( input, arrayType );
		}
		return array;
	}
}
