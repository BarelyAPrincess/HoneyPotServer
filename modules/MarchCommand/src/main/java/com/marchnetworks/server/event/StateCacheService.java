package com.marchnetworks.server.event;

import com.marchnetworks.common.event.StateCacheable;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface StateCacheService
{
	Collection<StateCacheable> getCachedEvents( Set<Long> paramSet, String[] paramArrayOfString1, String[] paramArrayOfString2 );

	StateCacheable getCachedEvent( StateCacheable paramStateCacheable );

	void putIntoCache( StateCacheable paramStateCacheable );

	void putIntoCache( List<StateCacheable> paramList );

	void removeAllFromCache( Long paramLong );

	void removeSourceStateFromCache( Long paramLong, String paramString );

	void removeStateFromCache( Set<Long> paramSet, String[] paramArrayOfString );

	void removeFromCache( StateCacheable paramStateCacheable );
}

