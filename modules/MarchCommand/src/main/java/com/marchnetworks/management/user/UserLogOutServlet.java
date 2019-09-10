package com.marchnetworks.management.user;

import com.marchnetworks.audit.data.AuditEventNameEnum;
import com.marchnetworks.audit.data.AuditView;
import com.marchnetworks.audit.data.AuditView.Builder;
import com.marchnetworks.audit.events.AuditEvent;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.server.event.EventPusher;
import com.marchnetworks.server.event.EventRegistry;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserLogOutServlet extends HttpServlet
{
	private static final Logger LOG = LoggerFactory.getLogger( UserLogOutServlet.class );

	private static final long serialVersionUID = 1L;
	private static EventPusher eventPusher = ( EventPusher ) ApplicationContextSupport.getBean( "eventPusher" );
	private static EventRegistry eventRegistry = ( EventRegistry ) ApplicationContextSupport.getBean( "eventRegistry" );

	protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		HttpSession session = request.getSession( false );
		if ( session != null )
		{
			LOG.debug( "About to cancel subscriptions for client session {}", session.getId() );
			eventPusher.cancelSubscriptionsForSession( session.getId() );

			LOG.info( "About to invalidate client session {}", session.getId() );
			session.invalidate();

			AuditView audit = new Builder( AuditEventNameEnum.USER_LOGOUT.getName() ).build();
			eventRegistry.send( new AuditEvent( audit ) );
		}
	}

	protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		doGet( request, response );
	}
}
