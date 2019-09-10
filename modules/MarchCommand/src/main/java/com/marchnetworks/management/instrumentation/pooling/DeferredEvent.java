package com.marchnetworks.management.instrumentation.pooling;

import com.marchnetworks.management.instrumentation.events.AbstractDeviceEvent;

public class DeferredEvent
{
	private static final long DEFAULT_EVICT_AGE = 60000L;
	private AbstractDeviceEvent event;
	private long createdTime;
	private long evictionAge;
	private String trigger;
	private boolean triggerOnEvict;
	private boolean evicted;

	public DeferredEvent( AbstractDeviceEvent event, String trigger )
	{
		this.event = event;
		this.trigger = trigger;
		createdTime = System.currentTimeMillis();
		evictionAge = 60000L;
		triggerOnEvict = false;
		evicted = false;
	}

	public DeferredEvent( AbstractDeviceEvent event, String trigger, long evictionAge )
	{
		this.event = event;
		this.trigger = trigger;
		createdTime = System.currentTimeMillis();
		this.evictionAge = evictionAge;
		triggerOnEvict = false;
		evicted = false;
	}

	public DeferredEvent( AbstractDeviceEvent event, String trigger, long evictionAge, boolean triggerOnEvict )
	{
		this.event = event;
		this.trigger = trigger;
		createdTime = System.currentTimeMillis();
		this.evictionAge = evictionAge;
		this.triggerOnEvict = triggerOnEvict;
		evicted = false;
	}

	public AbstractDeviceEvent getEvent()
	{
		return event;
	}

	public long getCreatedTime()
	{
		return createdTime;
	}

	public String getTrigger()
	{
		return trigger;
	}

	public long getEvictionAge()
	{
		return evictionAge;
	}

	public boolean isTriggerOnEvict()
	{
		return triggerOnEvict;
	}

	public void setEvicted( boolean evicted )
	{
		this.evicted = evicted;
	}

	public boolean isEvicted()
	{
		return evicted;
	}
}

