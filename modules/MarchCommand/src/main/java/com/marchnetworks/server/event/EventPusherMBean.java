package com.marchnetworks.server.event;

public abstract interface EventPusherMBean
{
	public abstract long getLastEventNumber();

	public abstract int getEventQueueSize();

	public abstract int getPullersSize();

	public abstract void sendFakeEvent();

	public abstract long getMaxEnvelopeAgeMillis();

	public abstract void setMaxEnvelopeAgeMillis( long paramLong );

	public abstract int getMaxQueueSize();

	public abstract void setMaxQueueSize( int paramInt );

	public abstract long getMaxPullerAgeMillis();

	public abstract void setMaxPullerAgeMillis( long paramLong );

	public abstract int getMaxPullerListSize();

	public abstract void setMaxPullerListSize( int paramInt );
}

