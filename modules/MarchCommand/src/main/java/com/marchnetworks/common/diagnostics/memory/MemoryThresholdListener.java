package com.marchnetworks.common.diagnostics.memory;

import java.lang.management.MemoryNotificationInfo;

import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;

public class MemoryThresholdListener implements NotificationListener
{
	private MemoryPoolWatcher watcher;

	public MemoryThresholdListener( MemoryPoolWatcher watcher )
	{
		this.watcher = watcher;
	}

	public void handleNotification( Notification notification, Object handback )
	{
		String notifyType = notification.getType();

		if ( ( notifyType.equals( "java.management.memory.threshold.exceeded" ) ) || ( notifyType.equals( "java.management.memory.collection.threshold.exceeded" ) ) )
		{

			CompositeData cd = ( CompositeData ) notification.getUserData();
			MemoryNotificationInfo info = MemoryNotificationInfo.from( cd );

			watcher.notifyListeners( info );
		}
	}
}

