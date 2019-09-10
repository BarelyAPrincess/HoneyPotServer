package com.marchnetworks.notification.service;

import com.marchnetworks.notification.data.NotificationMessage;

import org.springframework.mail.MailSendException;

import java.util.List;

public abstract interface SendNotificationService
{
	public abstract void sendMessages( Long paramLong, List<NotificationMessage> paramList );

	public abstract void doSendMessage( NotificationMessage paramNotificationMessage ) throws MailSendException;
}

