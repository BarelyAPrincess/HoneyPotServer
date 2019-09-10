package com.marchnetworks.management.instrumentation.comm;

import com.marchnetworks.command.common.device.data.DeviceView;
import com.marchnetworks.command.common.topology.data.DeviceResource;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.management.instrumentation.dao.DeviceDAO;
import com.marchnetworks.management.instrumentation.events.DeviceSequenceIdUpdateEvent;
import com.marchnetworks.management.instrumentation.model.CompositeDevice;
import com.marchnetworks.management.instrumentation.model.Device;
import com.marchnetworks.management.topology.ResourceTopologyServiceIF;
import com.marchnetworks.server.event.EventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceSequenceIdUpdateEventHandler implements EventListener
{
	private static final Logger LOG = LoggerFactory.getLogger( DeviceSequenceIdUpdateEvent.class );

	private DeviceDAO deviceDAO;

	public void process( Event event )
	{
		if ( ( event instanceof DeviceSequenceIdUpdateEvent ) )
		{
			DeviceSequenceIdUpdateEvent sequenceUpdateEvent = ( DeviceSequenceIdUpdateEvent ) event;
			Device compositeDevice = deviceDAO.findById( sequenceUpdateEvent.getDeviceId() );
			if ( !( compositeDevice instanceof CompositeDevice ) )
			{
				LOG.warn( "Device {} is not a composite device. Can't update sequence id {}.", new Object[] {compositeDevice.getAddress(), sequenceUpdateEvent.getEventSequenceId()} );
				return;
			}
			LOG.debug( "Updating event sequence id number {} for device {}", new Object[] {sequenceUpdateEvent.getEventSequenceId(), compositeDevice.getAddress()} );

			( ( CompositeDevice ) compositeDevice ).setDeviceEventSequenceId( sequenceUpdateEvent.getEventSequenceId() );

			DeviceResource device = getTopologyService().getDeviceResourceByDeviceId( sequenceUpdateEvent.getDeviceId() );
			device.getDeviceView().setDeviceEventSequenceId( sequenceUpdateEvent.getEventSequenceId() );
		}
	}

	public String getListenerName()
	{
		return DeviceSequenceIdUpdateEventHandler.class.getSimpleName();
	}

	public void setDeviceDAO( DeviceDAO deviceDAO )
	{
		this.deviceDAO = deviceDAO;
	}

	private ResourceTopologyServiceIF getTopologyService()
	{
		return ( ResourceTopologyServiceIF ) ApplicationContextSupport.getBean( "resourceTopologyService_internal" );
	}
}

