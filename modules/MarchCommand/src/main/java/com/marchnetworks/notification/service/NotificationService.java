package com.marchnetworks.notification.service;

import com.marchnetworks.command.api.notification.NotificationException;
import com.marchnetworks.command.api.provider.ContentProvider;
import com.marchnetworks.command.common.notification.data.Notification;
import com.marchnetworks.command.common.notification.data.NotificationContent;

import java.util.List;

public abstract interface NotificationService
{
	public abstract void setContentProvider( String paramString1, String paramString2, ContentProvider<List<NotificationContent>, Notification> paramContentProvider );

	public abstract void processNotification( Long paramLong );

	public abstract Notification updateNotification( Notification paramNotification, String paramString ) throws NotificationException;

	public abstract void deleteNotifications( Long[] paramArrayOfLong ) throws NotificationException;

	public abstract List<Notification> getAllNotifications( String paramString1, String paramString2, String paramString3 );

	public abstract List<Notification> getAllNotifications();

	public abstract void setLastSentTime( Long paramLong );

	public abstract void updateRecipientsAndUsername( String paramString1, String paramString2 );

	public abstract Long getNotificationLastSentTime( Long paramLong );
}

