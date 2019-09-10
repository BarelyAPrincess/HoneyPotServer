package com.marchnetworks.common.event;

import com.marchnetworks.common.utils.DateUtils;

public class Event
{
	private String eventType;
	protected long timestamp;

	public Event()
	{
	}

	public Event( String eventType )
	{
		this.eventType = eventType;
		timestamp = DateUtils.getCurrentUTCTimeInMillis();
	}

	public Event( String eventType, long timestamp )
	{
		this.eventType = eventType;
		this.timestamp = timestamp;
	}

	public String getEventType()
	{
		if ( eventType != null )
		{
			return eventType;
		}

		return getClass().getName();
	}

	protected String valuesToString()
	{
		return "type=" + eventType;
	}

	public String toString()
	{
		return getClass().getSimpleName() + ": " + valuesToString();
	}

	public long getTimestamp()
	{
		return timestamp;
	}
}
