package com.marchnetworks.management.topology.events;

import com.marchnetworks.command.api.event.UserNotifiable;
import com.marchnetworks.command.common.topology.data.Store;

public class GenericStorageUserStoreEvent extends GenericStorageEvent implements UserNotifiable
{
	private String username;

	public GenericStorageUserStoreEvent( GenericStorageEventType eventType, String objectId, String appId, long size, String username )
	{
		super( eventType, Store.USER, objectId, appId, size );
		this.username = username;
	}

	public String getUser()
	{
		return username;
	}
}

