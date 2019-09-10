package com.marchnetworks.common.scheduling.asynchronous;

import com.marchnetworks.common.event.Event;

import java.util.UUID;

public abstract class AsynchronousEvent extends Event
{
	private UUID uuid = null;

	private Object payload = null;

	private boolean isLastEvent = false;

	public AsynchronousEvent( Object payload )
	{
		setPayload( payload );
	}

	public void setUUID( UUID uuid )
	{
		this.uuid = uuid;
	}

	public UUID getUUID()
	{
		return uuid;
	}

	public void setPayload( Object payload )
	{
		this.payload = payload;
	}

	public Object getPayload()
	{
		return payload;
	}

	public void setIsLastEvent( boolean isLast )
	{
		isLastEvent = isLast;
	}

	public boolean getIsLastEvent()
	{
		return isLastEvent;
	}
}
