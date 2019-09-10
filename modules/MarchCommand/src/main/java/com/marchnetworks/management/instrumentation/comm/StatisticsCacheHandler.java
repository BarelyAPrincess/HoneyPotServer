package com.marchnetworks.management.instrumentation.comm;

import com.marchnetworks.command.common.device.data.RegistrationStatus;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.common.event.StateCacheable;
import com.marchnetworks.common.utils.DateUtils;
import com.marchnetworks.management.instrumentation.DeviceService;
import com.marchnetworks.management.instrumentation.events.DeviceChannelRemovedEvent;
import com.marchnetworks.management.instrumentation.events.DeviceRegistrationEvent;
import com.marchnetworks.management.instrumentation.events.DeviceStatisticsListEvent;
import com.marchnetworks.server.event.EventListener;
import com.marchnetworks.server.event.StateCacheService;

import java.util.Calendar;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatisticsCacheHandler implements EventListener
{
	private static final Logger LOG = LoggerFactory.getLogger( StatisticsCacheHandler.class );

	private StateCacheService m_StateCacheService;

	private DeviceService m_DeviceService;
	private Calendar periodicDebug = Calendar.getInstance( TimeZone.getTimeZone( "UTC" ) );

	public void process( Event aEvent )
	{
		if ( ( aEvent instanceof StateCacheable ) )
		{
			if ( ( LOG.isDebugEnabled() ) && ( periodicDebug.compareTo( DateUtils.getCurrentUTCTime() ) <= 0 ) )
			{
				periodicDebug = Calendar.getInstance( TimeZone.getTimeZone( "UTC" ) );
				periodicDebug.add( 12, 5 );
				LOG.debug( m_StateCacheService.toString() );
			}

			StateCacheable statEvent = ( StateCacheable ) aEvent;
			m_StateCacheService.putIntoCache( statEvent );

		}
		else if ( ( aEvent instanceof DeviceStatisticsListEvent ) )
		{
			if ( ( LOG.isDebugEnabled() ) && ( periodicDebug.compareTo( DateUtils.getCurrentUTCTime() ) <= 0 ) )
			{
				periodicDebug = Calendar.getInstance( TimeZone.getTimeZone( "UTC" ) );
				periodicDebug.add( 12, 5 );
				LOG.debug( m_StateCacheService.toString() );
			}

			DeviceStatisticsListEvent list = ( DeviceStatisticsListEvent ) aEvent;

			m_StateCacheService.putIntoCache( list.getStateEvents() );

		}
		else if ( ( aEvent instanceof DeviceRegistrationEvent ) )
		{
			DeviceRegistrationEvent deviceEvent = ( DeviceRegistrationEvent ) aEvent;
			String deviceID = deviceEvent.getDeviceId();
			RegistrationStatus registrationStatus = deviceEvent.getRegistrationStatus();
			if ( registrationStatus.equals( RegistrationStatus.UNREGISTERED ) )
			{
				m_StateCacheService.removeAllFromCache( Long.valueOf( Long.parseLong( deviceID ) ) );
			}
		}
		else if ( ( aEvent instanceof DeviceChannelRemovedEvent ) )
		{
			DeviceChannelRemovedEvent channelRemovedEvent = ( DeviceChannelRemovedEvent ) aEvent;
			String rootDeviceId = m_DeviceService.findRootDevice( channelRemovedEvent.getDeviceId() );

			m_StateCacheService.removeSourceStateFromCache( Long.valueOf( rootDeviceId ), channelRemovedEvent.getChannelId() );
		}
	}

	public String getListenerName()
	{
		return "StatisticsCacheHandler";
	}

	public void setStateCacheService( StateCacheService stateCacheService )
	{
		m_StateCacheService = stateCacheService;
	}

	public void setDeviceService( DeviceService deviceService )
	{
		m_DeviceService = deviceService;
	}
}

