package com.marchnetworks.command.api.event;

import com.marchnetworks.command.common.transport.data.Event;

import java.util.List;

public abstract interface AppEventCoreService
{
	public abstract void subscribe( AppSubscriptionPackage paramAppSubscriptionPackage );

	public abstract void send( EventNotification paramEventNotification );

	public abstract void send( EventNotification paramEventNotification, boolean paramBoolean );

	public abstract void send( List<EventNotification> paramList );

	public abstract void sendAfterTransactionCommits( EventNotification paramEventNotification );

	public abstract void injectDeviceEvent( Long paramLong, Event paramEvent );

	public abstract void injectDeviceEvents( Long paramLong, List<Event> paramList );

	public abstract void injectEventNotification( EventNotification paramEventNotification );
}
