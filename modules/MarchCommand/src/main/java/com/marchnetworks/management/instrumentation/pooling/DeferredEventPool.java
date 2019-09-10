package com.marchnetworks.management.instrumentation.pooling;

public abstract interface DeferredEventPool
{
	public abstract void add( String paramString, DeferredEvent paramDeferredEvent );

	public abstract void trigger( String paramString1, String paramString2 );

	public abstract void evictEvents();

	public abstract void set( String paramString, DeferredEvent paramDeferredEvent );
}

