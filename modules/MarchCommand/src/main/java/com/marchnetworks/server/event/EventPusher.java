package com.marchnetworks.server.event;

import com.marchnetworks.command.api.event.EventNotification;
import com.marchnetworks.command.api.event.Notifiable;

import java.util.List;

public abstract interface EventPusher
{
	public abstract String subscribeToEvents( String[] paramArrayOfString, long paramLong, String paramString );

	public abstract List<EventNotification> getQueuedEvents( Integer paramInteger ) throws EventNotificationException;

	public abstract List<EventNotification> getCachedEvents( Integer paramInteger, String[] paramArrayOfString1, String[] paramArrayOfString2, Long[] paramArrayOfLong ) throws EventNotificationException;

	public abstract void cancelSubscription( int paramInt ) throws EventNotificationException;

	public abstract void cancelSubscriptionsForSession( String paramString );

	public abstract void modifySubscription( int paramInt, String[] paramArrayOfString ) throws EventNotificationException;

	public abstract void extendSubscriptionTime( Integer paramInteger );

	public abstract void setupSubscription( int paramInt );

	public abstract void processNotifiables( List<Notifiable> paramList );

	public abstract void cancelAllSubscriptions();

	public abstract int getTotalSubscriptions();
}

