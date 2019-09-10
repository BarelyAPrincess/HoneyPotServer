package com.marchnetworks.notification.task;

import com.marchnetworks.command.common.DateUtils;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.notification.data.NotificationMessage;
import com.marchnetworks.notification.service.NotificationService;
import com.marchnetworks.notification.service.SendNotificationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailSendException;

import java.util.List;

public class MailTask implements Runnable
{
	private static final Logger LOG = LoggerFactory.getLogger( MailTask.class );

	private static SendNotificationService sendNotificationService = ( SendNotificationService ) ApplicationContextSupport.getBean( "sendNotificationService" );
	private static NotificationService notificationService = ( NotificationService ) ApplicationContextSupport.getBean( "notificationServiceProxy" );
	private Long notificationId;
	private List<NotificationMessage> messages;

	public MailTask( Long notificationId, List<NotificationMessage> messages )
	{
		this.notificationId = notificationId;
		this.messages = messages;
	}

	private void sendMessages()
	{
		boolean error = false;

		for ( NotificationMessage message : messages )
		{
			try
			{
				sendNotificationService.doSendMessage( message );
			}
			catch ( MailSendException e )
			{
				LOG.error( "Failed to send eMail due to the exception: " + e.getMessage() );
				error = true;
			}
		}

		LOG.debug( "*** Notification: {} set last sent time to {}.", notificationId, DateUtils.getDateStringFromMillis( System.currentTimeMillis() ) );
		Long notificationLastSentTime = notificationService.getNotificationLastSentTime( notificationId );
		if ( ( notificationLastSentTime.equals( Long.valueOf( 0L ) ) ) && ( error ) )
		{
			notificationService.setLastSentTime( Long.valueOf( -1L ) );
		}
		else
			notificationService.setLastSentTime( notificationId );
	}

	public void run()
	{
		sendMessages();
	}
}

