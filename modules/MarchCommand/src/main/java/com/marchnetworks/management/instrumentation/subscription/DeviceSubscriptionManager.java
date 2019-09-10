package com.marchnetworks.management.instrumentation.subscription;

import com.marchnetworks.alarm.service.AlarmService;
import com.marchnetworks.command.api.initialization.InitializationListener;
import com.marchnetworks.command.api.metrics.ApiMetricsTypes;
import com.marchnetworks.command.api.metrics.MetricsCoreService;
import com.marchnetworks.command.common.CollectionUtils;
import com.marchnetworks.command.common.device.DeviceEventsEnum;
import com.marchnetworks.command.common.device.data.ConnectState;
import com.marchnetworks.command.common.device.data.DeviceView;
import com.marchnetworks.command.common.device.data.RegistrationStatus;
import com.marchnetworks.command.common.scheduling.TaskScheduler;
import com.marchnetworks.command.common.topology.data.DeviceResource;
import com.marchnetworks.common.config.ConfigProperty;
import com.marchnetworks.common.diagnostics.metrics.MetricsHelper;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.spring.quartz.QuartzSchedulerSupport;
import com.marchnetworks.management.instrumentation.BaseDeviceScheduler;
import com.marchnetworks.management.instrumentation.DeviceService;
import com.marchnetworks.management.instrumentation.InstrumentationSettings;
import com.marchnetworks.management.instrumentation.data.DeviceSubscriptionType;
import com.marchnetworks.management.instrumentation.events.DeviceConnectionStateEvent;
import com.marchnetworks.management.instrumentation.events.DeviceModifySubscriptionEvent;
import com.marchnetworks.management.instrumentation.events.DeviceRestartEvent;
import com.marchnetworks.management.instrumentation.pooling.DeferredEvent;
import com.marchnetworks.management.instrumentation.pooling.DeferredEventPool;
import com.marchnetworks.management.instrumentation.task.DeviceFetchEventsTask;
import com.marchnetworks.management.instrumentation.task.DeviceModifyEventSubscriptionTask;
import com.marchnetworks.management.instrumentation.task.DeviceSubscribeEventsTask;
import com.marchnetworks.management.instrumentation.task.DeviceSynchronizerTask;
import com.marchnetworks.management.topology.ResourceTopologyServiceIF;
import com.marchnetworks.server.event.EventListener;
import com.marchnetworks.server.event.EventRegistry;
import com.marchnetworks.shared.config.CommonConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DeviceSubscriptionManager extends BaseDeviceScheduler implements EventListener, InitializationListener
{
	private Set<String> defaultSubscriptionPrefixes;
	private List<AdditionalPrefix> addedPrefixes = new ArrayList();
	private Map<String, DeviceSubscriptionType> deviceCurrentSubscriptionTypeMap = new HashMap();

	private ResourceTopologyServiceIF topologyService;
	private DeferredEventPool deferredEventPool;

	public void onAppInitialized()
	{
		initSubscriptionPrefixesArray();

		verifyDeviceSubscriptions( false );

		CommonConfiguration commonConfig = ( CommonConfiguration ) ApplicationContextSupport.getBean( "commonConfiguration" );

		int timeToOffline = commonConfig.getIntProperty( ConfigProperty.DEVICE_TIME_TO_OFFLINE, InstrumentationSettings.DEVICE_TIME_TO_OFFLINE );

		if ( timeToOffline != InstrumentationSettings.DEVICE_TIME_TO_OFFLINE )
		{
			timeToOffline = Math.max( timeToOffline, 100 );
			InstrumentationSettings.DEVICE_TIME_TO_OFFLINE = timeToOffline;

			InstrumentationSettings.DEVICE_NOTIFY_INTERVAL = ( int ) ( InstrumentationSettings.DEVICE_TIME_TO_OFFLINE * 0.75D );
			InstrumentationSettings.DEVICE_NOTIFY_INTERVAL = Math.max( InstrumentationSettings.DEVICE_NOTIFY_INTERVAL, 80 );

			InstrumentationSettings.DEVICE_OFFLINE_CHECK_INTERVAL = ( int ) ( InstrumentationSettings.DEVICE_TIME_TO_OFFLINE * 0.25D );
			InstrumentationSettings.DEVICE_OFFLINE_CHECK_INTERVAL = Math.max( InstrumentationSettings.DEVICE_OFFLINE_CHECK_INTERVAL, 30 );

			QuartzSchedulerSupport.updateSimpleTriggerRepeatInterval( "deviceSubscriptionTrigger", InstrumentationSettings.DEVICE_OFFLINE_CHECK_INTERVAL * 1000 );
		}
	}

	public void checkForOfflineDevices()
	{
		DeviceService deviceService = ( DeviceService ) ApplicationContextSupport.getBean( "deviceService" );
		long start = System.currentTimeMillis();
		List<String> offlineDevices = deviceService.findOfflineDevices();
		MetricsHelper.metrics.addBucketMinMaxAvg( ApiMetricsTypes.DEVICE_TASKS.getName(), "CheckOfflineDevices", System.currentTimeMillis() - start );

		if ( offlineDevices.isEmpty() )
		{
			return;
		}

		for ( String deviceId : offlineDevices )
		{

			getEventRegistry().send( new DeviceConnectionStateEvent( deviceId, ConnectState.OFFLINE ) );
		}
	}

	public String getListenerName()
	{
		return DeviceSubscriptionManager.class.getSimpleName();
	}

	public void process( Event aEvent )
	{
		if ( ( aEvent instanceof DeviceRestartEvent ) )
		{
			DeviceRestartEvent restartEvent = ( DeviceRestartEvent ) aEvent;

			String deviceId = restartEvent.getDeviceId();
			DeviceSubscribeEventsTask task = new DeviceSubscribeEventsTask( deviceId, restartEvent.getDeviceExtraInfo() );

			getTaskScheduler().executeFixedPoolSerial( task, deviceId );
		}
		else if ( ( aEvent instanceof DeviceModifySubscriptionEvent ) )
		{
			DeviceModifySubscriptionEvent modifySubscriptionEvent = ( DeviceModifySubscriptionEvent ) aEvent;

			DeviceModifyEventSubscriptionTask task = new DeviceModifyEventSubscriptionTask( modifySubscriptionEvent.getDeviceId(), modifySubscriptionEvent.getSubscriptionType() );
			getTaskScheduler().executeFixedPoolSerial( task, modifySubscriptionEvent.getDeviceId() );
		}
	}

	public void fetchEvents( String deviceId )
	{
		DeviceFetchEventsTask task = new DeviceFetchEventsTask( deviceId );
		getTaskScheduler().executeFixedPoolSerial( task, deviceId );
	}

	public void renewSubscription( String deviceId )
	{
		DeviceSubscribeEventsTask task = new DeviceSubscribeEventsTask( deviceId );
		getTaskScheduler().executeFixedPoolSerial( task, deviceId );
	}

	public void addSubscription( String[] additionalPrefixes )
	{
		if ( additionalPrefixes == null )
		{
			return;
		}

		for ( String additionalPrefix : additionalPrefixes )
		{

			boolean exists = false;
			for ( String prefix : defaultSubscriptionPrefixes )
			{
				if ( additionalPrefix.startsWith( prefix ) )
				{
					exists = true;
					break;
				}
			}
			if ( !exists )
			{

				exists = false;
				for ( AdditionalPrefix p : addedPrefixes )
				{
					if ( additionalPrefix.startsWith( p.getPrefix() ) )
					{
						p.addReference();
						exists = true;
						break;
					}
				}

				if ( !exists )
				{
					int references = 1;
					for ( Iterator<AdditionalPrefix> iterator = addedPrefixes.iterator(); iterator.hasNext(); )
					{
						AdditionalPrefix p = ( AdditionalPrefix ) iterator.next();
						if ( p.getPrefix().startsWith( additionalPrefix ) )
						{
							references++;
							iterator.remove();
						}
					}
					AdditionalPrefix newPrefix = new AdditionalPrefix( additionalPrefix, references );
					addedPrefixes.add( newPrefix );
					verifyDeviceSubscriptions( false );
				}
			}
		}
	}

	public void removeSubscription( String[] additionalPrefixes )
	{
		if ( additionalPrefixes == null )
		{
			return;
		}

		boolean verifySubscriptions = false;
		Iterator<AdditionalPrefix> iterator;
		for ( String additionalPrefix : additionalPrefixes )
		{
			for ( iterator = addedPrefixes.iterator(); iterator.hasNext(); )
			{
				AdditionalPrefix p = ( AdditionalPrefix ) iterator.next();
				if ( additionalPrefix.startsWith( p.getPrefix() ) )
				{
					p.removeReference();
					if ( p.isUnreferenced() )
					{
						iterator.remove();
						verifySubscriptions = true;
					}
				}
			}
		}
		if ( verifySubscriptions )
		{
			verifyDeviceSubscriptions( true );
		}
	}

	public void setDeviceEventSubscriptionsType( String deviceId, DeviceSubscriptionType type )
	{
		deviceCurrentSubscriptionTypeMap.put( deviceId, type );
	}

	public void removeDeviceEventSubscriptionsType( String deviceId )
	{
		deviceCurrentSubscriptionTypeMap.remove( deviceId );
	}

	public String[] getDeviceEventSubscriptionsByDeviceId( String deviceId )
	{
		String[] result = null;

		if ( deviceCurrentSubscriptionTypeMap.containsKey( deviceId ) )
		{
			result = getDeviceEventSubscriptionsByType( ( DeviceSubscriptionType ) deviceCurrentSubscriptionTypeMap.get( deviceId ) );
		}
		else
		{
			DeviceResource deviceResource = getTopologyService().getDeviceResourceByDeviceId( deviceId );
			result = deviceResource.getDeviceView().getDeviceEventSubscriptionPrefixes();
		}

		return result;
	}

	public String[] getDeviceEventSubscriptionsByType( DeviceSubscriptionType subscriptionType )
	{
		String[] result = null;

		if ( subscriptionType.equals( DeviceSubscriptionType.EVENTS_FOR_REPLACEMENT ) )
		{
			String[] replacementEventSubscriptions = {DeviceEventsEnum.SYSTEM_CONFIG.getPath(), DeviceEventsEnum.SYSTEM_LASTUPDATE.getPath(), DeviceEventsEnum.SYSTEM_LASTCONFIG.getPath()};
			result = replacementEventSubscriptions;
		}
		else
		{
			result = ( String[] ) defaultSubscriptionPrefixes.toArray( new String[defaultSubscriptionPrefixes.size() + addedPrefixes.size()] );

			for ( int i = 0; i < addedPrefixes.size(); i++ )
			{
				result[( defaultSubscriptionPrefixes.size() + i )] = ( ( AdditionalPrefix ) addedPrefixes.get( i ) ).getPrefix();
			}
		}

		return result;
	}

	public void resynchronizeData( String deviceId )
	{
		DeviceSynchronizerTask task = new DeviceSynchronizerTask( deviceId );
		getTaskScheduler().executeFixedPoolSerial( task, deviceId );
	}

	private void initSubscriptionPrefixesArray()
	{
		defaultSubscriptionPrefixes = new LinkedHashSet();
		defaultSubscriptionPrefixes.addAll( DeviceEventsEnum.getAllPathsByPrefix( DeviceEventsEnum.CHANNEL ) );
		defaultSubscriptionPrefixes.addAll( DeviceEventsEnum.getAllPathsByPrefix( DeviceEventsEnum.SYSTEM ) );
		defaultSubscriptionPrefixes.addAll( DeviceEventsEnum.getAllPathsByPrefix( DeviceEventsEnum.LICENSE ) );
		defaultSubscriptionPrefixes.add( DeviceEventsEnum.SWITCH.getPath() );
		defaultSubscriptionPrefixes.add( DeviceEventsEnum.AUDIO_OUT.getPath() );
		defaultSubscriptionPrefixes.addAll( DeviceEventsEnum.getAllPathsByPrefix( DeviceEventsEnum.CLIENT ) );
		defaultSubscriptionPrefixes.addAll( DeviceEventsEnum.getAllPathsByPrefix( DeviceEventsEnum.DISK ) );
		defaultSubscriptionPrefixes.add( DeviceEventsEnum.ALERT.getPath() );
		defaultSubscriptionPrefixes.add( DeviceEventsEnum.EXTRACTOR.getPath() );

		AlarmService alarmService = ( AlarmService ) ApplicationContextSupport.getBean( "alarmService_internal" );
		if ( alarmService.getAlarmsEnabled() )
		{
			defaultSubscriptionPrefixes.add( DeviceEventsEnum.ALARM.getPath() );
		}
	}

	private void verifyDeviceSubscriptions( boolean skipPrefixCheck )
	{
		List<DeviceResource> deviceResources = getTopologyService().getAllDeviceResources();
		for ( DeviceResource device : deviceResources )
		{
			DeviceSubscriptionType subscriptionType = DeviceSubscriptionType.FULL_EVENTS;
			if ( device.getDeviceView().getRegistrationStatus() != RegistrationStatus.REGISTERED )
			{
				subscriptionType = DeviceSubscriptionType.EVENTS_FOR_REPLACEMENT;
			}

			if ( skipPrefixCheck )
			{
				DeviceModifySubscriptionEvent deviceEvent = new DeviceModifySubscriptionEvent( device.getDeviceId(), subscriptionType );
				deferredEventPool.add( device.getDeviceId(), new DeferredEvent( deviceEvent, ConnectState.ONLINE.toString(), 172800L ) );
			}
			else
			{
				boolean shouldModify = false;
				if ( subscriptionType.equals( DeviceSubscriptionType.FULL_EVENTS ) )
				{
					List<String> currentEvents = Arrays.asList( device.getDeviceView().getDeviceEventSubscriptionPrefixes() );
					List<String> desiredEvents = Arrays.asList( getDeviceEventSubscriptionsByType( DeviceSubscriptionType.FULL_EVENTS ) );
					shouldModify = !currentEvents.containsAll( desiredEvents );
				}
				else
				{
					String[] markForReplacementEvents = getDeviceEventSubscriptionsByType( DeviceSubscriptionType.EVENTS_FOR_REPLACEMENT );
					shouldModify = !CollectionUtils.sortAndCompareArrays( device.getDeviceView().getDeviceEventSubscriptionPrefixes(), markForReplacementEvents );
				}

				if ( shouldModify )
				{
					DeviceModifySubscriptionEvent deviceEvent = new DeviceModifySubscriptionEvent( device.getDeviceId(), subscriptionType );
					deferredEventPool.add( device.getDeviceId(), new DeferredEvent( deviceEvent, ConnectState.ONLINE.toString(), 172800L ) );
				}
			}
		}
	}

	private EventRegistry getEventRegistry()
	{
		return ( EventRegistry ) ApplicationContextSupport.getBean( "eventRegistry" );
	}

	private ResourceTopologyServiceIF getTopologyService()
	{
		if ( topologyService == null )
		{
			topologyService = ( ( ResourceTopologyServiceIF ) ApplicationContextSupport.getBean( "resourceTopologyService_internal" ) );
		}
		return topologyService;
	}

	public void setDeferredEventPool( DeferredEventPool deferredEventPool )
	{
		this.deferredEventPool = deferredEventPool;
	}
}

