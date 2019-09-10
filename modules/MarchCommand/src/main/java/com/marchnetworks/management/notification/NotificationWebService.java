package com.marchnetworks.management.notification;

import com.marchnetworks.command.api.notification.NotificationException;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.notification.data.Notification;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.notification.service.NotificationService;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

@WebService( serviceName = "NotificationService", name = "NotificationService", portName = "NotificationPort" )
public class NotificationWebService
{
	private NotificationService notificationService = ( NotificationService ) ApplicationContextSupport.getBean( "notificationServiceProxy" );

	@WebMethod( operationName = "updateNotification" )
	public Notification updateNotification( @WebParam( name = "notification" ) Notification notification ) throws NotificationException
	{
		String username = CommonAppUtils.getUsernameFromSecurityContext();
		return notificationService.updateNotification( notification, username );
	}

	@WebMethod( operationName = "deleteNotification" )
	public void deleteNotification( @WebParam( name = "ids" ) Long[] ids ) throws NotificationException
	{
		notificationService.deleteNotifications( ids );
	}

	@WebMethod( operationName = "getAllNotifications" )
	public List<Notification> getAllNotifications( @WebParam( name = "group" ) String group, @WebParam( name = "appId" ) String appId ) throws NotificationException
	{
		String username = CommonAppUtils.getUsernameFromSecurityContext();
		return notificationService.getAllNotifications( group, appId, username );
	}
}
