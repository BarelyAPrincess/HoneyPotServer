package com.marchnetworks.management.instrumentation.adaptation;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.device.data.DeviceView;
import com.marchnetworks.command.common.topology.data.DeviceResource;
import com.marchnetworks.common.transport.datamodel.DeviceException;
import com.marchnetworks.management.instrumentation.DeviceAdaptorFactory;
import com.marchnetworks.management.instrumentation.DeviceCapabilityService;
import com.marchnetworks.management.instrumentation.DeviceRegistry;
import com.marchnetworks.management.instrumentation.RemoteCompositeDeviceOperations;
import com.marchnetworks.management.topology.ResourceTopologyServiceIF;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DeviceCapabilityServiceImpl implements DeviceCapabilityService
{
	private static final Logger LOG = LoggerFactory.getLogger( DeviceCapabilityServiceImpl.class );

	private Multimap<Long, String> data = HashMultimap.create();

	private DeviceAdaptorFactory deviceAdaptorFactory;

	private ResourceTopologyServiceIF resourceTopologyService;

	private DeviceRegistry deviceRegistry;

	public boolean isCapabilityEnabled( long deviceId, String capabilityName )
	{
		return isCapabilityEnabled( deviceId, capabilityName, true );
	}

	public boolean isCapabilityEnabled( long deviceId, String capabilityName, boolean shouldPoll )
	{
		DeviceResource device = resourceTopologyService.getDeviceResourceByDeviceId( String.valueOf( deviceId ) );
		if ( ( device == null ) || ( !device.isRootDevice() ) )
		{
			LOG.info( "Device {} is not a root device or can no longer be found on the database.", Long.valueOf( deviceId ) );
			return false;
		}

		if ( !data.containsKey( Long.valueOf( deviceId ) ) )
		{
			if ( ( device.getDeviceView().getCapabilities() == null ) && ( shouldPoll ) )
			{
				List<String> capabilities = loadCapabilitiesFromDevice( device );
				updateCapabilities( device, capabilities );
			}
			else
			{
				storeCapabilitiesInCache( device, device.getDeviceView().getCapabilities() );
			}
		}

		boolean result = data.containsEntry( Long.valueOf( deviceId ), capabilityName );
		if ( !result )
		{
			LOG.debug( "Capability {} not enabled for device {}", new Object[] {capabilityName, Long.valueOf( deviceId )} );
		}

		return result;
	}

	public void updateCapabilities( long deviceId, List<String> deviceCapabilities )
	{
		DeviceResource device = resourceTopologyService.getDeviceResourceByDeviceId( String.valueOf( deviceId ) );

		if ( ( device == null ) || ( !device.isRootDevice() ) )
		{
			LOG.info( "Device {} is not a root device or can no longer be found on the database. Unable to update capabilities.", Long.valueOf( deviceId ) );
			return;
		}

		deviceRegistry.updateDeviceCapabilities( device.getDeviceId(), deviceCapabilities );
		storeCapabilitiesInCache( device, deviceCapabilities );
	}

	public void refreshDeviceCapabilities( long deviceId )
	{
		DeviceResource device = resourceTopologyService.getDeviceResourceByDeviceId( String.valueOf( deviceId ) );

		if ( ( device == null ) || ( !device.isRootDevice() ) )
		{
			LOG.info( "Device {} is not a root device or can no longer be found on the database. Unable to refresh capabilities.", Long.valueOf( deviceId ) );
			return;
		}

		List<String> capabilities = loadCapabilitiesFromDevice( device );
		updateCapabilities( device, capabilities );
	}

	public void clearCapabilities( long deviceId )
	{
		data.removeAll( Long.valueOf( deviceId ) );
	}

	private void updateCapabilities( DeviceResource device, List<String> deviceCapabilities )
	{
		if ( !CommonAppUtils.equalsWithNull( device.getDeviceView().getCapabilities(), deviceCapabilities ) )
		{
			deviceRegistry.updateDeviceCapabilities( device.getDeviceId(), deviceCapabilities );
			storeCapabilitiesInCache( device, deviceCapabilities );
		}
	}

	private void storeCapabilitiesInCache( DeviceResource device, List<String> deviceCapabilities )
	{
		data.removeAll( Long.valueOf( Long.parseLong( device.getDeviceId() ) ) );
		if ( deviceCapabilities != null )
		{
			data.putAll( Long.valueOf( Long.parseLong( device.getDeviceId() ) ), deviceCapabilities );
		}
	}

	private List<String> loadCapabilitiesFromDevice( DeviceResource device )
	{
		RemoteCompositeDeviceOperations adaptor = ( RemoteCompositeDeviceOperations ) deviceAdaptorFactory.getDeviceAdaptor( device );
		try
		{
			return adaptor.getDeviceCapabilities();
		}
		catch ( DeviceException e )
		{
			LOG.info( "Failed to refresh device capabilities information. Error: {}", e.getMessage() );
		}
		return null;
	}

	public void setDeviceAdaptorFactory( DeviceAdaptorFactory deviceAdaptorFactory )
	{
		this.deviceAdaptorFactory = deviceAdaptorFactory;
	}

	public void setResourceTopologyService( ResourceTopologyServiceIF resourceTopologyService )
	{
		this.resourceTopologyService = resourceTopologyService;
	}

	public void setDeviceRegistry( DeviceRegistry dr )
	{
		deviceRegistry = dr;
	}
}

