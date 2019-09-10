package com.marchnetworks.web.test;

import com.marchnetworks.app.data.App;
import com.marchnetworks.app.service.AppManager;
import com.marchnetworks.command.api.notification.NotificationException;
import com.marchnetworks.command.common.notification.data.Notification;
import com.marchnetworks.command.common.notification.data.NotificationFrequency;
import com.marchnetworks.command.common.timezones.TimezonesDictionary;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.utils.ServletUtils;
import com.marchnetworks.notification.service.NotificationService;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet( name = "TestNotification", urlPatterns = {"/TestNotification"} )
public class NotificationTestServlet extends HttpServlet
{
	private static final String TEST_GROUP = "TEST";
	private NotificationService notificationService = ( NotificationService ) ApplicationContextSupport.getBean( "notificationServiceProxy" );
	private AppManager appService = ( AppManager ) ApplicationContextSupport.getBean( "appManagerProxy_internal" );
	private TestNotificationProvider testNotificationProvider = new TestNotificationProvider();

	public NotificationTestServlet()
	{
		notificationService.setContentProvider( "TEST", null, testNotificationProvider );
	}

	public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		createResponse( request, response, null, "Refresh Complete" );
	}

	public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		response.setContentType( "text/html" );

		String status = "";

		Long deleteNotificationId = ServletUtils.getParameterId( request.getParameterNames(), "deleteNotification" );
		List<Notification> notifications = null;

		if ( request.getParameter( "updateNotification" ) != null )
		{
			Long id = ServletUtils.getLongParameterValue( request.getParameter( "id" ) );
			String name = ServletUtils.getStringParameterValue( request.getParameter( "name" ) );
			String group = ServletUtils.getStringParameterValue( request.getParameter( "group" ) );
			String appId = ServletUtils.getStringParameterValue( request.getParameter( "appId" ) );
			String username = ServletUtils.getStringParameterValue( request.getParameter( "username" ) );

			NotificationFrequency frequency = NotificationFrequency.valueOf( ServletUtils.getStringParameterValue( request.getParameter( "notificationFrequency" ) ) );
			Integer dayOfWeek = ServletUtils.getIntegerParameterValue( request.getParameter( "dayOfWeek" ) );
			Integer sendTime = ServletUtils.getIntegerParameterValue( request.getParameter( "sendTime" ) );
			String timeZone = ServletUtils.getStringParameterValue( request.getParameter( "notificationTimeZone" ) );
			List<String> recipients = ServletUtils.getStringList( request.getParameter( "recipients" ) );
			Boolean enhancedMailTemplate = ServletUtils.getBooleanParameterValue( request.getParameter( "enhanced" ) );

			Notification notification = new Notification();
			notification.setId( id );
			notification.setName( name );
			notification.setGroup( group );
			notification.setAppId( appId );
			notification.setFrequency( frequency );
			notification.setDayOfWeek( dayOfWeek );
			notification.setSendTime( sendTime );
			notification.setTimeZone( timeZone );
			notification.setRecipients( recipients );
			notification.setEnhancedMailTemplate( enhancedMailTemplate );
			try
			{
				notificationService.updateNotification( notification, username );
				status = "Notification Created";
			}
			catch ( NotificationException e )
			{
				status = "Error creating notification, Exception: " + e.getMessage();
			}
		}
		else if ( deleteNotificationId != null )
		{
			try
			{
				Long[] ids = {deleteNotificationId};
				notificationService.deleteNotifications( ids );
				status = "Notification " + deleteNotificationId + " deleted";
			}
			catch ( NotificationException e )
			{
				status = "Error deleting notification, Exception: " + e.getMessage();
			}
		}
		else if ( request.getParameter( "getNotifications" ) != null )
		{
			String group = ServletUtils.getStringParameterValue( request.getParameter( "groupNotifications" ) );
			String appId = ServletUtils.getStringParameterValue( request.getParameter( "appIdNotifications" ) );
			String username = ServletUtils.getStringParameterValue( request.getParameter( "usernameNotifications" ) );
			notifications = notificationService.getAllNotifications( group, appId, username );
		}
		else
		{
			status = "Refresh complete";
		}

		createResponse( request, response, notifications, status );
	}

	private void createResponse( HttpServletRequest request, HttpServletResponse response, List<Notification> notifications, String status ) throws ServletException, IOException
	{
		response.setContentType( "text/html" );
		if ( notifications == null )
		{
			notifications = notificationService.getAllNotifications();
		}

		App[] apps = appService.getApps();
		request.setAttribute( "notifications", notifications );
		request.setAttribute( "apps", apps );
		request.setAttribute( "status", status );
		request.setAttribute( "timezones", TimezonesDictionary.getAllWindowsTimezones() );
		request.setAttribute( "notificationFrequencies", NotificationFrequency.values() );

		getServletContext().getRequestDispatcher( "/WEB-INF/pages/NotificationTest.jsp" ).forward( request, response );
	}
}
