package com.marchnetworks.management.instrumentation.task;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.marchnetworks.command.common.scheduling.NonConcurrentRunnable;
import com.marchnetworks.command.common.topology.data.DeviceResource;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.transport.datamodel.DeviceException;
import com.marchnetworks.management.instrumentation.DeviceAdaptorFactory;
import com.marchnetworks.management.instrumentation.DeviceRegistry;
import com.marchnetworks.management.instrumentation.RemoteCompositeDeviceOperations;
import com.marchnetworks.management.instrumentation.adaptation.DeviceEventHandlerScheduler;
import com.marchnetworks.management.instrumentation.events.AbstractDeviceEvent;
import com.marchnetworks.management.instrumentation.events.DeviceAlarmReconciliationEvent;
import com.marchnetworks.management.instrumentation.events.DeviceAudioOutputConfigEvent;
import com.marchnetworks.management.instrumentation.events.DeviceChannelAddedEvent;
import com.marchnetworks.management.instrumentation.events.DeviceChannelChangedEvent;
import com.marchnetworks.management.instrumentation.events.DeviceChannelRemovedEvent;
import com.marchnetworks.management.instrumentation.events.DeviceHealthReconciliationEvent;
import com.marchnetworks.management.instrumentation.events.DeviceSwitchConfigEvent;
import com.marchnetworks.management.instrumentation.events.DeviceSystemChangedEvent;
import com.marchnetworks.management.instrumentation.events.ServerIdHashEvent;
import com.marchnetworks.management.instrumentation.model.Channel;
import com.marchnetworks.management.instrumentation.model.CompositeDevice;
import com.marchnetworks.management.instrumentation.model.Device;
import com.marchnetworks.management.topology.ResourceTopologyServiceIF;
import com.marchnetworks.server.communications.transport.datamodel.ChannelDetails;
import com.marchnetworks.server.communications.transport.datamodel.DeviceDetails;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceSynchronizerTask implements NonConcurrentRunnable
{
	private static final Logger LOG = LoggerFactory.getLogger( DeviceSynchronizerTask.class );
	private String deviceId;

	public DeviceSynchronizerTask( String deviceId )
	{
		this.deviceId = deviceId;
	}

	public String getTaskId()
	{
		StringBuilder sb = new StringBuilder( DeviceSynchronizerTask.class.getSimpleName() );
		sb.append( ":" );
		sb.append( deviceId );
		return sb.toString();
	}

	public void run()
	{
		LOG.info( "Running Device Synchronization Task for device {}.", deviceId );

		DeviceResource device = getTopologyService().getDeviceResourceByDeviceId( deviceId );

		if ( device == null )
		{
			LOG.info( "DeviceId={} not found on DB store. Aborting task.", deviceId );
			return;
		}

		synchronizeDeviceInfo( deviceId );

		try
		{
			DeviceRegistry deviceRegistry = ( DeviceRegistry ) ApplicationContextSupport.getBean( "deviceRegistryProxy" );
			DeviceAdaptorFactory adaptorFactory = ( DeviceAdaptorFactory ) ApplicationContextSupport.getBean( "deviceAdaptorFactory" );
			RemoteCompositeDeviceOperations adaptor = ( RemoteCompositeDeviceOperations ) adaptorFactory.getDeviceAdaptor( device );
			DeviceDetails rootDeviceDescriptor = adaptor.retrieveAllChannelDetails();
			CompositeDevice pd = ( CompositeDevice ) deviceRegistry.getDeviceEagerDetached( deviceId );
			synchronizeChannelInfo( pd, rootDeviceDescriptor );
		}
		catch ( DeviceException e )
		{
			LOG.warn( "Error retrieveAllChannelDetails: ", e );
		}
	}

	private void synchronizeDeviceInfo( String deviceId )
	{
		List<AbstractDeviceEvent> rootDeviceEvents = new ArrayList();

		AbstractDeviceEvent compositeDeviceEvent = new DeviceSystemChangedEvent( deviceId );
		AbstractDeviceEvent alarmReconciliationEvent = new DeviceAlarmReconciliationEvent( deviceId );
		AbstractDeviceEvent switchConfigEvent = new DeviceSwitchConfigEvent( deviceId );
		AbstractDeviceEvent audioOutputConfigEvent = new DeviceAudioOutputConfigEvent( deviceId );
		AbstractDeviceEvent healthReconiliationEvent = new DeviceHealthReconciliationEvent( deviceId );
		AbstractDeviceEvent serverIdHashEvent = new ServerIdHashEvent( deviceId );
		DeviceEventHandlerScheduler dehs = ( DeviceEventHandlerScheduler ) ApplicationContextSupport.getBean( "deviceEventHandlerScheduler" );

		Collections.addAll( rootDeviceEvents, new AbstractDeviceEvent[] {compositeDeviceEvent, alarmReconciliationEvent, switchConfigEvent, audioOutputConfigEvent, healthReconiliationEvent, serverIdHashEvent} );

		dehs.scheduleDeviceEventHandling( deviceId, rootDeviceEvents );
	}

	private void synchronizeChannelInfo( CompositeDevice rootDevice, DeviceDetails deviceDetails )
	{
		Set<String> currentChannelSet = new HashSet();
		for ( Channel channel : rootDevice.getChannels().values() )
		{
			currentChannelSet.add( channel.getChannelId() );
		}

		for ( Device childDevice : rootDevice.getChildDevices().values() )
		{
			for ( Channel channel : childDevice.getChannels().values() )
			{
				currentChannelSet.add( channel.getChannelId() );
			}
		}

		Set<String> deviceChannelSet = new HashSet();
		for ( ChannelDetails channelDetails : deviceDetails.getDeviceChannels() )
		{
			if ( !ChannelDetails.isChannelStateUnknown( channelDetails ) )
			{

				deviceChannelSet.add( channelDetails.getId() );
			}
		}
		for ( DeviceDetails childDeviceDetails : deviceDetails.getChildDevices() )
		{
			for ( ChannelDetails channelDetails : childDeviceDetails.getDeviceChannels() )
			{
				if ( !ChannelDetails.isChannelStateUnknown( channelDetails ) )
				{

					deviceChannelSet.add( channelDetails.getId() );
				}
			}
		}
		List<AbstractDeviceEvent> channelEventsList = new ArrayList();

		SetView<String> newChannelsSet = Sets.difference( deviceChannelSet, currentChannelSet );
		for ( String newChannel : newChannelsSet )
		{
			AbstractDeviceEvent event = new DeviceChannelAddedEvent( rootDevice.getDeviceId(), newChannel );
			channelEventsList.add( event );
		}

		SetView<String> deletedChannelSet = Sets.difference( currentChannelSet, deviceChannelSet );
		for ( String deletedChannel : deletedChannelSet )
		{
			AbstractDeviceEvent event = new DeviceChannelRemovedEvent( rootDevice.convertToDeviceIdFromChannelId( deletedChannel ), deletedChannel );
			channelEventsList.add( event );
		}

		currentChannelSet.retainAll( deviceChannelSet );
		for ( String changedChannel : currentChannelSet )
		{
			AbstractDeviceEvent event = new DeviceChannelChangedEvent( rootDevice.convertToDeviceIdFromChannelId( changedChannel ), changedChannel );
			channelEventsList.add( event );
		}
		DeviceEventHandlerScheduler dehs = ( DeviceEventHandlerScheduler ) ApplicationContextSupport.getBean( "deviceEventHandlerScheduler" );
		dehs.scheduleDeviceEventHandling( rootDevice.getDeviceId(), channelEventsList );
	}

	private ResourceTopologyServiceIF getTopologyService()
	{
		return ( ResourceTopologyServiceIF ) ApplicationContextSupport.getBean( "resourceTopologyServiceProxy_internal" );
	}
}

