package com.marchnetworks.health.system;

import com.marchnetworks.command.api.alert.AlertDefinitionEnum;
import com.marchnetworks.command.api.initialization.InitializationListener;
import com.marchnetworks.command.api.query.Criteria;
import com.marchnetworks.command.api.query.Restrictions;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.device.data.ConnectState;
import com.marchnetworks.command.common.topology.data.DeviceResource;
import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.common.config.ConfigProperty;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.common.scheduling.PeriodicTransactionalTask;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.spring.quartz.QuartzSchedulerSupport;
import com.marchnetworks.health.input.AlertInput;
import com.marchnetworks.health.input.DeviceAlertInput;
import com.marchnetworks.health.service.HealthServiceIF;
import com.marchnetworks.management.instrumentation.events.AbstractDeviceEvent;
import com.marchnetworks.management.instrumentation.events.DeviceChannelChangedEvent;
import com.marchnetworks.management.instrumentation.events.DeviceRestartEvent;
import com.marchnetworks.management.topology.ResourceTopologyServiceIF;
import com.marchnetworks.server.event.EventListener;
import com.marchnetworks.shared.config.CommonConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DeviceOfflineHealthAlertMonitor implements EventListener, PeriodicTransactionalTask, InitializationListener
{
	private static final Logger LOG = LoggerFactory.getLogger( DeviceOfflineHealthAlertMonitor.class );

	private HealthServiceIF healthService;

	private ResourceTopologyServiceIF topologyService;

	private int maxDisconnectionTime = 2880;
	private int monitorDefaultCheckInterval = 3600000;

	public void onAppInitialized()
	{
		CommonConfiguration commonConfig = ( CommonConfiguration ) ApplicationContextSupport.getBean( "commonConfiguration" );

		String configuredMaxDisconnectionTime = commonConfig.getProperty( ConfigProperty.DEVICE_OFFLINE_MONITOR_MAX_DISCONNECTION_TIME );
		if ( !CommonAppUtils.isNullOrEmptyString( configuredMaxDisconnectionTime ) )
		{
			int disconnectionTime = Integer.valueOf( configuredMaxDisconnectionTime ).intValue();
			if ( disconnectionTime < 3 )
			{
				disconnectionTime = 3;
			}
			maxDisconnectionTime = disconnectionTime;
		}

		String configuredTriggerInterval = commonConfig.getProperty( ConfigProperty.DEVICE_OFFLINE_MONITOR_TRIGGER_INTERVAL );
		if ( !CommonAppUtils.isNullOrEmptyString( configuredTriggerInterval ) )
		{
			int triggerInterval = Integer.valueOf( configuredTriggerInterval ).intValue();
			triggerInterval = triggerInterval * 60 * 1000;
			if ( triggerInterval != monitorDefaultCheckInterval )
			{
				QuartzSchedulerSupport.updateSimpleTriggerRepeatInterval( "deviceOfflineTrigger", triggerInterval );
			}
		}
	}

	public void handlePeriodicTransactionalTask()
	{
		LOG.debug( "Checking for devices that have been offline for more than {} minutes...", Integer.valueOf( maxDisconnectionTime ) );

		Long nowMillis = Long.valueOf( System.currentTimeMillis() );

		Long onlineWindow = Long.valueOf( nowMillis.longValue() - maxDisconnectionTime * 60 * 1000 );

		Criteria criteria = new Criteria( DeviceResource.class );
		criteria.add( Restrictions.eq( "deviceView.parentDeviceId", null ) );
		criteria.add( Restrictions.eq( "deviceView.connectState", ConnectState.OFFLINE ) );
		criteria.add( Restrictions.lt( "deviceView.lastCommunicationTime", onlineWindow ) );

		List<Resource> resources = null;
		resources = getTopologyService().getResources( criteria );

		if ( resources == null )
		{
			return;
		}
		for ( Resource resource : resources )
		{
			DeviceResource dr = ( DeviceResource ) resource;

			long timeStamp = System.currentTimeMillis();

			AlertInput alert = new DeviceAlertInput( dr.getDeviceId(), AlertDefinitionEnum.DEVICE_DISCONNECTED, dr.getName(), timeStamp, timeStamp, timeStamp, "", "", true );

			healthService.processHealthAlert( alert );
		}
	}

	public void process( Event event )
	{
		AbstractDeviceEvent deviceEvent = ( AbstractDeviceEvent ) event;
		if ( ( event instanceof DeviceRestartEvent ) )
		{
			DeviceResource device = getTopologyService().getDeviceResourceByDeviceId( deviceEvent.getDeviceId() );

			if ( ( device != null ) && ( device.isRootDevice() ) )
			{
				clearHealthAlert( device );
			}

		}
		else if ( ( event instanceof DeviceChannelChangedEvent ) )
		{
			DeviceResource device = getTopologyService().getDeviceResourceByDeviceId( deviceEvent.getDeviceId() );
			ConnectState cs = device.getDeviceView().getConnectState();

			if ( cs == ConnectState.ONLINE )
			{
				clearHealthAlert( device );
			}
		}
	}

	private void clearHealthAlert( DeviceResource d )
	{
		long timeStamp = System.currentTimeMillis();

		AlertInput alert = new DeviceAlertInput( d.getDeviceId(), AlertDefinitionEnum.DEVICE_DISCONNECTED, d.getName(), timeStamp, timeStamp, timeStamp, "", "", false );

		healthService.processHealthAlert( alert );
	}

	public String getListenerName()
	{
		return DeviceOfflineHealthAlertMonitor.class.getSimpleName();
	}

	private ResourceTopologyServiceIF getTopologyService()
	{
		if ( topologyService == null )
		{
			topologyService = ( ( ResourceTopologyServiceIF ) ApplicationContextSupport.getBean( "resourceTopologyService_internal" ) );
		}
		return topologyService;
	}

	public void setMaxDisconnectionTime( int maxDisconnectionTime )
	{
		this.maxDisconnectionTime = maxDisconnectionTime;
	}

	public void setHealthService( HealthServiceIF healthService )
	{
		this.healthService = healthService;
	}
}
