package com.marchnetworks.command.api.notification;

import com.marchnetworks.command.api.provider.ContentProvider;
import com.marchnetworks.command.common.notification.data.Notification;
import com.marchnetworks.command.common.notification.data.NotificationContent;

import java.util.List;

public abstract interface NotificationCoreService
{
	public abstract List<Notification> getAllNotifications( String paramString1, String paramString2, String paramString3 );

	public abstract void setContentProvider( String paramString1, String paramString2, ContentProvider<List<NotificationContent>, Notification> paramContentProvider );
}
