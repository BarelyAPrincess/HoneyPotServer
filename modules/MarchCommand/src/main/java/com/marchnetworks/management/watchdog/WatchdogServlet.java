package com.marchnetworks.management.watchdog;

import com.marchnetworks.common.diagnostics.DiagnosticError;
import com.marchnetworks.common.diagnostics.DiagnosticResult;
import com.marchnetworks.common.diagnostics.DiagnosticResultType;
import com.marchnetworks.common.diagnostics.WatchdogNotification;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.utils.XmlUtils;
import com.marchnetworks.monitoring.diagnostics.DiagnosticsServiceIF;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@WebServlet( name = "WatchdogServlet", urlPatterns = {"/WatchdogServlet"} )
public class WatchdogServlet extends HttpServlet
{
	private static final long serialVersionUID = -6334779224825525658L;
	private static final Logger LOG = LoggerFactory.getLogger( WatchdogServlet.class );
	private static final String NOTIFICATION = "notification";
	private static final String NOTIFICATION_RESTART = "restart";
	private static final String NOTIFICATION_LOG = "log";
	private DiagnosticsServiceIF diagnosticsService = ( DiagnosticsServiceIF ) ApplicationContextSupport.getBean( "diagnosticsService" );

	public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		response.setContentType( "text/xml" );

		DiagnosticResult result = diagnosticsService.checkFailures();

		if ( result != DiagnosticResult.RESULT_OK )
		{
			constructErrorResponse( response, result );
		}
	}

	public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		response.setContentType( "text/xml" );
		String notificationParameter = request.getParameter( "notification" );
		if ( ( notificationParameter == null ) || ( ( !notificationParameter.equals( "restart" ) ) && ( !notificationParameter.equals( "log" ) ) ) )
		{
			response.sendError( 400, "Only restart notifications are allowed." );
			LOG.error( "Invalid url from watchdog notification, URL: " + request.getRequestURL() + ", query: " + request.getQueryString() );
			return;
		}

		if ( notificationParameter.equals( "restart" ) )
		{
			List<WatchdogNotification> notifications = new ArrayList<>();
			try
			{
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document doc = db.parse( request.getInputStream() );

				NodeList listOfNotifications = doc.getElementsByTagName( "notification" );

				for ( int i = 0; i < listOfNotifications.getLength(); i++ )
				{
					Element notificationEl = ( Element ) listOfNotifications.item( i );

					String reason = XmlUtils.getStringValue( notificationEl, "reason" );
					Boolean processRestarted = XmlUtils.getBooleanValue( notificationEl, "processRestarted" );
					Integer dependenciesRestarted = XmlUtils.getIntValue( notificationEl, "dependenciesRestarted" );
					Long timestamp = XmlUtils.getLongValue( notificationEl, "timestamp" );

					if ( ( reason == null ) || ( processRestarted == null ) || ( dependenciesRestarted == null ) || ( timestamp == null ) )
					{
						response.sendError( 400, "Missing required fields in restart notification." );
						LOG.error( "Error reading notification from watchdog, Missing required fields in restart notification" );
						return;
					}
					WatchdogNotification notification = new WatchdogNotification( reason, processRestarted.booleanValue(), dependenciesRestarted.intValue(), timestamp.longValue() );
					notifications.add( notification );
				}
			}
			catch ( Exception e )
			{
				response.sendError( 400, "Invalid xml in restart notification." );
				LOG.error( "Error reading notification from watchdog, Exception: " + e.getMessage() );
				return;
			}

			LOG.info( "Received {} notifications from watchdog ", Integer.valueOf( notifications.size() ) );

			for ( WatchdogNotification notification : notifications )
			{
				diagnosticsService.notifyRestartComplete( notification );
			}
		}
		else if ( notificationParameter.equals( "log" ) )
		{

			String logString = IOUtils.toString( request.getInputStream(), "UTF-8" );
			LOG.info( "Log message received from watchdog: " + logString );
			System.out.println( "Log notification received from watchdog: " + logString );
		}
	}

	private void constructErrorResponse( HttpServletResponse response, DiagnosticResult result ) throws IOException
	{
		response.setStatus( 500 );
		PrintWriter out = response.getWriter();
		out.println( "<error><type>" + result.getType().name() + "</type><code>" + result.getError().getCode() + "</code><description>" + result.getMessage() + "</description></error>" );
	}
}
