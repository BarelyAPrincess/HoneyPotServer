package com.marchnetworks.management.instrumentation;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.marchnetworks.command.common.topology.TopologyException;
import com.marchnetworks.command.common.topology.data.DeviceResource;
import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.command.common.transport.data.Pair;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.transport.datamodel.DeviceException;
import com.marchnetworks.common.types.DeviceExceptionTypes;
import com.marchnetworks.management.instrumentation.dao.DeviceOutputDAO;
import com.marchnetworks.management.instrumentation.events.DeviceOutputEvent;
import com.marchnetworks.management.instrumentation.events.DeviceOutputEventType;
import com.marchnetworks.management.instrumentation.model.DeviceOutputEntity;
import com.marchnetworks.management.instrumentation.model.DeviceOutputMBean;
import com.marchnetworks.management.topology.ResourceTopologyServiceIF;
import com.marchnetworks.server.communications.transport.datamodel.DeviceOutput;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DeviceOutputServiceImpl<T extends DeviceOutputEntity> implements DeviceOutputService
{
	private static final Logger LOG = LoggerFactory.getLogger( DeviceOutputServiceImpl.class );

	private ResourceTopologyServiceIF topologyService;

	protected DeviceOutputDAO<T> outputDAO;

	protected abstract List<? extends DeviceOutput> getOutputs( Long paramLong ) throws DeviceException;

	protected abstract Resource createResource( T paramT );

	protected abstract T createEntity( Long paramLong, DeviceOutput paramDeviceOutput );

	protected abstract String getResourceAssociationType();

	protected abstract void updateState( T paramT, String paramString );

	protected abstract boolean isDisabled( String paramString );

	public DeviceOutputMBean getDeviceOutputById( Long deviceOutputId )
	{
		DeviceOutputMBean result = ( DeviceOutputMBean ) outputDAO.findById( deviceOutputId );
		return result;
	}

	public void processDeviceOutputEvent( DeviceOutputEvent outputEvent )
	{
		if ( outputEvent.getType().equals( DeviceOutputEventType.OUTPUT_CONFIG ) )
		{
			refreshOutputs( Long.valueOf( outputEvent.getDeviceId() ) );
		}
		else
		{
			updateOutputState( Long.valueOf( outputEvent.getDeviceId() ), outputEvent.getDeviceOutputId(), outputEvent.getState(), outputEvent.getExtraInfo() );
		}
	}

	private void refreshOutputs( Long deviceId )
	{
		Map<String, DeviceOutput> outputsFromDevice = new HashMap();
		try
		{
			List<? extends DeviceOutput> outputs = getOutputs( deviceId );
			if ( outputs != null )
			{
				for ( DeviceOutput deviceOutput : outputs )
				{
					outputsFromDevice.put( deviceOutput.getId(), deviceOutput );
				}
			}
		}
		catch ( DeviceException de )
		{
			if ( de.getDetailedErrorType() == DeviceExceptionTypes.FEATURE_NOT_SUPPORTED )
			{
				LOG.debug( "Output feature is not supported on device {}. Error details: {}", deviceId, de.getMessage() );
			}
			else
			{
				LOG.warn( "Error when requesting output information from device {}. Error details: {}", deviceId, de.getMessage() );
			}
			return;
		}

		Map<String, T> persistedDeviceOutputs = new HashMap();
		List<T> deviceOutputs = outputDAO.findAllByDeviceId( deviceId );
		for ( T deviceOutput : deviceOutputs )
		{
			persistedDeviceOutputs.put( deviceOutput.getOutputId(), deviceOutput );
		}

		SetView<String> removedDeviceOutputs = Sets.difference( persistedDeviceOutputs.keySet(), outputsFromDevice.keySet() );
		for ( String deviceOutputId : removedDeviceOutputs )
		{
			T deviceOutput = ( T ) persistedDeviceOutputs.get( deviceOutputId );
			if ( deviceOutput == null )
			{
				LOG.info( "DeviceOutputId {} for device {} could not be found. Aborting deletion...", deviceOutputId, deviceId );
			}
			else
			{
				removeDeviceOutput( deviceOutput );
			}
		}
		SetView<String> newDeviceOutputs = Sets.difference( outputsFromDevice.keySet(), persistedDeviceOutputs.keySet() );
		for ( String deviceOutputId : newDeviceOutputs )
		{
			DeviceOutput deviceOutputFromDevice = ( DeviceOutput ) outputsFromDevice.get( deviceOutputId );
			String state = deviceOutputFromDevice.getState();
			if ( !isDisabled( state ) )
			{
				createDeviceOutput( deviceId, deviceOutputFromDevice );
			}
		}

		persistedDeviceOutputs.keySet().retainAll( outputsFromDevice.keySet() );
		for ( T deviceOutputEntity : persistedDeviceOutputs.values() )
		{
			DeviceOutput deviceOutput = ( DeviceOutput ) outputsFromDevice.get( deviceOutputEntity.getOutputId() );

			if ( deviceOutputEntity.readFromTransportObject( deviceOutput ) )
			{
				try
				{
					Resource deviceOutputResource = createResource( deviceOutputEntity );
					Long resourceId = deviceOutputEntity.getResourceId();

					deviceOutputResource.setId( resourceId );
					deviceOutputResource.setName( deviceOutputEntity.getName() );
					getTopologyService().updateResource( deviceOutputResource );
				}
				catch ( TopologyException ex )
				{
					LOG.error( "Error updating device output topology, id:" + deviceOutputEntity.getOutputId(), ex );
				}
			}
		}
	}

	protected void createDeviceOutput( Long deviceId, DeviceOutput deviceOutputFromDevice )
	{
		T deviceOutputEntity = createEntity( deviceId, deviceOutputFromDevice );
		outputDAO.create( deviceOutputEntity );

		Resource deviceOutputResource = createResource( deviceOutputEntity );

		Long parentResourceId = getTopologyService().getResourceIdByDeviceId( deviceId.toString() );
		try
		{
			Resource result = getTopologyService().createResource( deviceOutputResource, parentResourceId, getResourceAssociationType() );
			deviceOutputEntity.setResourceId( result.getId() );
		}
		catch ( TopologyException ex )
		{
			LOG.error( "Error creating Device Output resource for deviceId:{} outputId:{} Error details: {}", new Object[] {deviceId, deviceOutputEntity.getOutputId(), ex.getMessage()} );
		}
	}

	private void updateOutputState( Long deviceId, String deviceOutputId, String state, Pair[] info )
	{
		T deviceOutput = outputDAO.findByDeviceAndOutputId( deviceId, deviceOutputId );

		if ( deviceOutput == null )
		{
			if ( !isDisabled( state ) )
			{
				List<? extends DeviceOutput> outputs;

				try
				{
					outputs = getOutputs( deviceId );
				}
				catch ( DeviceException e )
				{
					LOG.warn( "Error when requesting outputs from device {}. error details {}", new Object[] {deviceId, e.getMessage()} );
					return;
				}

				for ( DeviceOutput output : outputs )
				{
					if ( output.getId().equals( deviceOutputId ) )
					{
						LOG.info( "Device output " + deviceOutputId + " created." );
						createDeviceOutput( deviceId, output );
						return;
					}
				}
			}
			return;
		}

		updateState( deviceOutput, state );
		deviceOutput.setInfo( info );

		try
		{
			Resource deviceOutputResource = createResource( deviceOutput );
			Long resourceId = deviceOutput.getResourceId();

			deviceOutputResource.setId( resourceId );
			deviceOutputResource.setName( deviceOutput.getName() );
			getTopologyService().updateResource( deviceOutputResource );
		}
		catch ( TopologyException ex )
		{
			LOG.error( "Error updating device output topology, outputId:{} Error details:", deviceOutput.getOutputId(), ex.getMessage() );
		}
	}

	protected void removeDeviceOutput( T deviceOutput )
	{
		Long resourceId = deviceOutput.getResourceId();
		if ( resourceId != null )
		{
			try
			{
				getTopologyService().removeResource( resourceId );
			}
			catch ( TopologyException ex )
			{
				LOG.error( "Error removing Output Resource id:{} Error details:{}", resourceId, ex.getMessage() );
			}
		}

		outputDAO.delete( deviceOutput );
	}

	public void processDeviceRegistration( Long deviceId )
	{
		refreshOutputs( deviceId );
	}

	public void processDeviceUnregistration( Long deviceId )
	{
		outputDAO.deleteByDeviceId( deviceId );
	}

	public RemoteCompositeDeviceOperations getAdaptor( Long deviceId )
	{
		DeviceResource dev = getTopologyService().getDeviceResourceByDeviceId( deviceId.toString() );
		if ( dev == null )
		{
			return null;
		}

		if ( !dev.isRootDevice() )
		{
			LOG.warn( "Can only retrieve outputs from RootDevices. deviceId={}", deviceId );
			return null;
		}
		RemoteCompositeDeviceOperations adaptor = ( RemoteCompositeDeviceOperations ) getDeviceAdaptorFactory().getDeviceAdaptor( dev );
		return adaptor;
	}

	public void setOutputDAO( DeviceOutputDAO<T> outputDAO )
	{
		this.outputDAO = outputDAO;
	}

	public ResourceTopologyServiceIF getTopologyService()
	{
		if ( topologyService == null )
		{
			topologyService = ( ( ResourceTopologyServiceIF ) ApplicationContextSupport.getBean( "resourceTopologyService_internal" ) );
		}
		return topologyService;
	}

	private DeviceAdaptorFactory getDeviceAdaptorFactory()
	{
		return ( DeviceAdaptorFactory ) ApplicationContextSupport.getBean( "deviceAdaptorFactory" );
	}
}

