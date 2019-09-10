package com.marchnetworks.web.test;

import com.marchnetworks.command.api.provider.ContentProvider;
import com.marchnetworks.command.common.notification.data.Notification;
import com.marchnetworks.command.common.notification.data.NotificationContent;
import com.marchnetworks.common.utils.DateUtils;

import java.util.Collections;
import java.util.List;

public class TestNotificationProvider implements ContentProvider<List<NotificationContent>, Notification>
{
	public List<NotificationContent> getContent( Notification notification )
	{
		NotificationContent content = new NotificationContent();
		content.setRecipients( notification.getRecipients() );
		String emailMessage = "<!DOCTYPE html><html><body><h3>Test Email Message</h3><h4>Notification " + notification.toString() + " </h4>" + "<h4>Time: " + DateUtils.getDateStringFromMillis( System.currentTimeMillis() ) + "</h4>" + "<p>Email content</p>" + "</body>" + "</html>";

		content.setMessage( emailMessage );
		content.setSubject( "Test Email" );
		return Collections.singletonList( content );
	}
}
