package com.marchnetworks.management.statistics;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.marchnetworks.command.api.initialization.InitializationListener;
import com.marchnetworks.command.common.device.DeviceEventsEnum;
import com.marchnetworks.common.event.StateCacheable;
import com.marchnetworks.device.event.dao.DeviceStateEventDAO;
import com.marchnetworks.device.event.dao.DeviceStateEventEntity;
import com.marchnetworks.management.instrumentation.events.GenericDeviceStateEvent;
import com.marchnetworks.server.event.StateCacheService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StateCacheServiceImpl implements StateCacheService, InitializationListener
{
	private static final Logger LOG = LoggerFactory.getLogger( StateCacheServiceImpl.class );

	private DeviceStateEventDAO persistence = new DeviceStateEventDAO();

	protected final Map<Long, Table<String, String, StateCacheable>> mData = new HashMap<>();

	public void onAppInitialized()
	{
		synchronized ( mData )
		{
			for ( DeviceStateEventEntity entity : persistence.findAllDetached() )
			{
				Table<String, String, StateCacheable> tbl = HashBasedTable.create();
				List<GenericDeviceStateEvent> deviceEvents = entity.getEvents();

				for ( GenericDeviceStateEvent event : deviceEvents )
					tbl.put( event.getNotificationInfo().getPath(), event.getNotificationInfo().getSource(), event );

				mData.put( entity.getDeviceId(), tbl );
			}
		}
	}

	public void putIntoCache( StateCacheable state )
	{
		synchronized ( mData )
		{
			putIntoCacheInternal( state );

			Long deviceId = state.getDeviceIdLong();

			if ( ( state.getEventNotificationType().equals( DeviceEventsEnum.SYSTEM_EXPORT_QUEUE.getPath() ) ) || ( state.getEventNotificationType().equals( DeviceEventsEnum.EXTRACTOR_STORAGE_FREE.getPath() ) ) )
			{
				if ( state.isDeleteEvent() )
				{
					persistence.delete( deviceId );
					// deviceStateEventDAO.deleteDetached( deviceId );
					return;
				}

				if ( ( state instanceof GenericDeviceStateEvent ) )
				{
					GenericDeviceStateEvent genericDeviceStateEvent = ( GenericDeviceStateEvent ) state;

					DeviceStateEventEntity entity = ( DeviceStateEventEntity ) deviceStateEventDAO.findById( deviceId );
					List<GenericDeviceStateEvent> events = Collections.singletonList( genericDeviceStateEvent );

					if ( entity == null )
					{
						entity = new DeviceStateEventEntity();
						entity.setDeviceId( deviceId );

						entity.setEvents( events );

						deviceStateEventDAO.create( entity );
					}
					else
					{
						entity.setEvents( events );
					}
				}
			}
		}
	}

	public void putIntoCache( List<StateCacheable> list )
	{
		synchronized ( mData )
		{
			for ( StateCacheable state : list )
			{
				putIntoCacheInternal( state );
			}
		}
	}

	private void putIntoCacheInternal( StateCacheable state )
	{
		Long deviceId = state.getDeviceIdLong();
		String source = state.getNotificationInfo().getSource();

		Table<String, String, StateCacheable> tbl = ( Table ) mData.get( deviceId );
		if ( tbl == null )
		{
			tbl = HashBasedTable.create();
			mData.put( deviceId, tbl );
		}

		if ( state.isDeleteEvent() )
		{
			tbl.remove( state.getEventNotificationType(), source );
			return;
		}

		StateCacheable cachedEntry = ( StateCacheable ) tbl.get( state.getEventNotificationType(), source );
		if ( ( cachedEntry != null ) && ( cachedEntry.getTimestamp() > state.getTimestamp() ) )
		{
			return;
		}

		tbl.put( state.getEventNotificationType(), source, state );

		if ( LOG.isDebugEnabled() )
		{
			LOG.debug( "Stats Cache, size of the collection for device Id " + state.getDeviceIdLong() + " is " + tbl.size() );
		}
	}

	public Collection<StateCacheable> getCachedEvents( Set<Long> deviceIds, String[] eventPathNames, String[] eventSources )
	{
		ArrayList<StateCacheable> result = new ArrayList( 8 );

		for ( Long deviceId : deviceIds )
		{
			Table<String, String, StateCacheable> tbl = ( Table ) mData.get( deviceId );
			if ( tbl != null )
			{
				if ( ( ( eventPathNames != null ? 1 : 0 ) & ( eventPathNames.length > 0 ? 1 : 0 ) ) == 0 )
				{
				}
				boolean needsFiltering = ( ( eventSources != null ? 1 : 0 ) & ( eventSources.length > 0 ? 1 : 0 ) ) != 0;
				if ( needsFiltering )
				{
					Arrays.sort( eventPathNames );
					Arrays.sort( eventSources );
					for ( StateCacheable stateCacheable : tbl.values() )
					{
						if ( ( ( eventPathNames.length <= 0 ) || ( Arrays.binarySearch( eventPathNames, stateCacheable.getEventNotificationType() ) >= 0 ) ) && (

								( eventSources.length <= 0 ) || ( Arrays.binarySearch( eventSources, stateCacheable.getNotificationInfo().getSource() ) >= 0 ) ) )
						{
							result.add( stateCacheable );
						}
					}
				}
				else
				{
					result.addAll( tbl.values() );
				}
			}
		}

		return result;
	}

	public StateCacheable getCachedEvent( StateCacheable state )
	{
		StateCacheable result = null;
		Table<String, String, StateCacheable> tbl = ( Table ) mData.get( state.getDeviceIdLong() );

		if ( tbl != null )
		{
			result = ( StateCacheable ) tbl.get( state.getEventNotificationType(), state.getNotificationInfo().getSource() );
		}
		return result;
	}

	public void removeStateFromCache( Set<Long> deviceIds, String[] eventPathNames )
	{
		Collection<StateCacheable> events = getCachedEvents( deviceIds, eventPathNames, new String[0] );
		Iterator<StateCacheable> iterator;
		if ( events != null )
		{
			for ( iterator = events.iterator(); iterator.hasNext(); )
			{
				StateCacheable event = ( StateCacheable ) iterator.next();
				removeFromCache( event );
			}
		}
	}

	public String toString()
	{
		StringBuffer logInfo = new StringBuffer( "---Beginning Statistics Cache---\n" );

		for ( Long l : mData.keySet() )
		{
			logInfo.append( "Device I.D. " + l + " with " + ( ( Table ) mData.get( l ) ).size() + " statistics cached.\n" );
		}
		logInfo.append( "---Ending Statistics Cache---\n" );
		return logInfo.toString();
	}

	public void removeAllFromCache( Long deviceId )
	{
		synchronized ( mData )
		{
			mData.remove( deviceId );
		}

		deviceStateEventDAO.deleteDetached( deviceId );
	}

	public void removeSourceStateFromCache( Long deviceId, String sourceId )
	{
		synchronized ( mData )
		{
			if ( mData.containsKey( deviceId ) )
			{
				Map<String, StateCacheable> eventsMap = ( ( Table ) mData.get( deviceId ) ).column( sourceId );
				if ( eventsMap != null )
				{
					eventsMap.clear();
				}
			}
		}
	}

	public void removeFromCache( StateCacheable state )
	{
		synchronized ( mData )
		{
			Table<String, String, StateCacheable> tbl = ( Table ) mData.get( state.getDeviceIdLong() );

			if ( tbl != null )
			{
				tbl.remove( state.getEventNotificationType(), state.getNotificationInfo().getSource() );
			}
		}
	}
}

