package com.marchnetworks.common.cache;

import com.marchnetworks.command.common.scheduling.TaskScheduler;
import com.marchnetworks.common.utils.DateUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class CacheImpl<S, T> implements Cache<S, T>
{
	private static final Logger LOG = LoggerFactory.getLogger( CacheImpl.class );
	private TaskScheduler taskScheduler;

	public CacheImpl()
	{
		objects = new ConcurrentHashMap();
		evictionTime = 60000L;
		evictionTaskDelay = 30000L;
		evictionTaskInterval = 30000L;
		needsEvictions = false;
	}

	public void init()
	{
		taskScheduler.scheduleWithFixedDelay( new EvictionTask(), evictionTaskDelay, evictionTaskInterval, TimeUnit.MILLISECONDS );
	}

	private Map<S, CachedObject<T>> objects;
	private long evictionTime;

	public T getObject( S id )
	{
		CachedObject<T> object = ( CachedObject ) objects.get( id );
		if ( object != null )
		{
			return ( T ) object.getCachedObject();
		}
		return null;
	}

	public String getTag( S id )
	{
		CachedObject<T> object = ( CachedObject ) objects.get( id );
		if ( object != null )
		{
			return object.getTag();
		}
		return null;
	}

	public synchronized String createTag( S id )
	{
		CachedObject<T> object = ( CachedObject ) objects.get( id );
		if ( object == null )
		{
			String newTag = UUID.randomUUID().toString();
			object = new CachedObject( newTag, null );
			objects.put( id, object );
			if ( LOG.isDebugEnabled() )
			{
				LOG.debug( "Created tag for object id " + id );
			}
		}
		return object.getTag();
	}

	private long evictionTaskDelay;
	private long evictionTaskInterval;
	private boolean needsEvictions;

	public synchronized void updateObject( S id, T object )
	{
		String newTag = UUID.randomUUID().toString();
		CachedObject<T> cachedObject = new CachedObject( newTag, object );
		objects.put( id, cachedObject );
		if ( LOG.isDebugEnabled() )
		{
			LOG.debug( "Object " + id + " updated in cache, cache size:" + getSize() );
		}
		needsEvictions = true;
	}

	public synchronized void returnObject( S id, T object )
	{
		CachedObject<T> cachedObject = ( CachedObject ) objects.get( id );
		if ( cachedObject == null )
		{
			updateObject( id, object );
		}
		else
		{
			cachedObject.setCachedObject( object );
			needsEvictions = true;
			if ( LOG.isDebugEnabled() )
			{
				LOG.debug( "Object " + id + " returned to cache, cache size:" + getSize() );
			}
		}
	}

	public synchronized void removeObject( S id )
	{
		objects.remove( id );
		if ( LOG.isDebugEnabled() )
		{
			LOG.debug( "Object " + id + " removed from cache, size:" + getSize() );
		}
	}

	public synchronized void evict()
	{
		LOG.debug( "Entered evict function" );
		if ( needsEvictions )
		{
			boolean foundCachedObject = false;
			long now = DateUtils.getCurrentUTCTimeInMillis();
			int evictCount = 0;

			for ( CachedObject<T> cachedObject : objects.values() )
			{
				if ( cachedObject.hasCachedObject() )
				{
					if ( now - cachedObject.getUpdatedTime() > evictionTime )
					{
						cachedObject.clearCachedObject();
						evictCount++;
					}
					else
					{
						foundCachedObject = true;
					}
				}
			}
			if ( !foundCachedObject )
			{
				needsEvictions = false;
			}
			if ( ( evictCount > 0 ) && ( LOG.isDebugEnabled() ) )
			{
				LOG.debug( "Evicted " + evictCount + " objects from cache, size:" + getSize() );
			}
		}
	}

	public void setEvictionTime( long evictionTime )
	{
		this.evictionTime = evictionTime;
	}

	public void setEvictionTaskDelay( long evictionTaskDelay )
	{
		this.evictionTaskDelay = evictionTaskDelay;
	}

	public void setEvictionTaskInterval( long evictionTaskInterval )
	{
		this.evictionTaskInterval = evictionTaskInterval;
	}

	private int getSize()
	{
		int result = 0;
		for ( CachedObject<T> cachedObject : objects.values() )
		{
			if ( cachedObject.hasCachedObject() )
			{
				result++;
			}
		}
		return result;
	}

	public void setTaskScheduler( TaskScheduler taskScheduler )
	{
		this.taskScheduler = taskScheduler;
	}

	class EvictionTask implements Runnable
	{
		EvictionTask()
		{
		}

		public void run()
		{
			evict();
		}
	}
}
