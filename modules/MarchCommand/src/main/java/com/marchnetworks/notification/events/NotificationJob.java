package com.marchnetworks.notification.events;

import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.notification.service.NotificationService;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class NotificationJob implements Job
{
	private static NotificationService notificationService = ( NotificationService ) ApplicationContextSupport.getBean( "notificationServiceProxy" );

	public void execute( JobExecutionContext paramJobExecutionContext ) throws JobExecutionException
	{
		Long id = Long.valueOf( Long.parseLong( paramJobExecutionContext.getJobDetail().getName() ) );
		notificationService.processNotification( id );
	}
}

