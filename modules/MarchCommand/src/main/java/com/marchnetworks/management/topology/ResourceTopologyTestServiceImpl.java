package com.marchnetworks.management.topology;

import com.marchnetworks.alarm.model.AlarmSourceEntity;
import com.marchnetworks.command.api.query.Criteria;
import com.marchnetworks.command.api.query.Restrictions;
import com.marchnetworks.command.common.topology.ResourceAssociationType;
import com.marchnetworks.command.common.topology.TopologyConstants;
import com.marchnetworks.command.common.topology.TopologyException;
import com.marchnetworks.command.common.topology.data.ChannelResource;
import com.marchnetworks.command.common.topology.data.DeviceResource;
import com.marchnetworks.command.common.topology.data.Group;
import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.management.instrumentation.model.Channel;
import com.marchnetworks.management.instrumentation.model.CompositeDevice;
import com.marchnetworks.management.instrumentation.model.Device;
import com.marchnetworks.management.topology.dao.ResourceAssociationDAO;
import com.marchnetworks.management.topology.dao.ResourceDAO;
import com.marchnetworks.management.topology.events.ResourceCreatedEvent;
import com.marchnetworks.management.topology.model.AlarmSourceResourceEntity;
import com.marchnetworks.management.topology.model.ChannelResourceEntity;
import com.marchnetworks.management.topology.model.DeviceResourceEntity;
import com.marchnetworks.management.topology.model.ResourceAssociationEntity;
import com.marchnetworks.management.topology.model.ResourceEntity;
import com.marchnetworks.management.topology.util.SimulatedDevicesKiller;
import com.marchnetworks.server.event.EventRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceTopologyTestServiceImpl implements ResourceTopologyTestService
{
	private static final Logger LOG = LoggerFactory.getLogger( ResourceTopologyTestServiceImpl.class );

	private static final ResourceTopologyServiceIF topologyService = ( ResourceTopologyServiceIF ) ApplicationContextSupport.getBean( "resourceTopologyService_internal" );
	private static final ResourceTopologyServiceIF topologyServiceProxy = ( ResourceTopologyServiceIF ) ApplicationContextSupport.getBean( "resourceTopologyServiceProxy_internal" );
	private static final EventRegistry eventRegistry = ( EventRegistry ) ApplicationContextSupport.getBean( "eventRegistry" );

	private static final int THREAD_NUM = 10;

	private ResourceDAO<ResourceEntity> resourceDAO;
	private ResourceAssociationDAO resourceAssociationDAO;

	public List<Long> createSimulatedDevices( Long parentId, List<CompositeDevice> rootDevices, Map<Long, List<AlarmSourceEntity>> alarms )
	{
		ResourceEntity parentResource = ( ResourceEntity ) resourceDAO.findById( parentId );
		List<Long> result = new ArrayList( rootDevices.size() );

		int i = 0;
		for ( CompositeDevice rootDevice : rootDevices )
		{
			i++;

			DeviceResourceEntity rootResource = new DeviceResourceEntity();
			rootResource.setDevice( rootDevice );
			rootResource.setName( rootDevice.getName() );
			fastCreateResource( rootResource, parentResource, ResourceAssociationType.DEVICE.name() );
			result.add( rootResource.getId() );

			for ( Channel channel : rootDevice.getChannels().values() )
			{
				ChannelResourceEntity channelResource = new ChannelResourceEntity();
				channelResource.setChannel( channel );
				channelResource.setName( channel.getName() );
				fastCreateResource( channelResource, rootResource, ResourceAssociationType.CHANNEL.name() );
			}

			DeviceResourceEntity childResource;

			for ( Device child : rootDevice.getChildDevices().values() )
			{
				childResource = new DeviceResourceEntity();
				childResource.setDevice( child );
				childResource.setName( child.getName() );
				fastCreateResource( childResource, rootResource, ResourceAssociationType.DEVICE.name() );

				for ( Channel channel : child.getChannels().values() )
				{
					ChannelResourceEntity channelResource = new ChannelResourceEntity();
					channelResource.setChannel( channel );
					channelResource.setName( channel.getName() );
					fastCreateResource( channelResource, childResource, ResourceAssociationType.CHANNEL.name() );
				}
			}

			for ( AlarmSourceEntity alarm : alarms.get( Long.parseLong( rootDevice.getDeviceId() ) ) )
			{
				AlarmSourceResourceEntity alarmResource = new AlarmSourceResourceEntity();
				alarmResource.setAlarmSource( alarm );
				alarmResource.setName( alarm.getName() );
				fastCreateResource( alarmResource, rootResource, ResourceAssociationType.ALARM_SOURCE.name() );
			}

			if ( i % 200 == 0 )
			{
				resourceDAO.flushAndClear();
			}
		}

		return result;
	}

	public void createLogicalTree( Integer cameraLinks )
	{
		List<DeviceResource> rootDevices = topologyService.getAllDeviceResources();

		for ( Iterator i$ = rootDevices.iterator(); i$.hasNext(); )
		{
			DeviceResource device = ( DeviceResource ) i$.next();

			Group folder = new Group();
			folder.setName( device.getName() );
			try
			{
				folder = ( Group ) topologyService.createResource( folder, TopologyConstants.LOGICAL_ROOT_ID, ResourceAssociationType.GROUP.name() );
			}
			catch ( TopologyException e )
			{
				LOG.error( "Could not create Logical Folder " + device.getName() );
			}

			List<Resource> channels = device.createFilteredResourceList( new Class[] {ChannelResource.class} );

			int i = 0;
			if ( ( i < cameraLinks.intValue() ) && ( i < channels.size() ) )
			{
				ChannelResource channel = ( ChannelResource ) channels.get( i );
				DeviceResource parentDevice = ( DeviceResource ) channel.getParentResource();
				try
				{
					topologyService.createChannelLinkResource( channel, folder.getId(), parentDevice.getId() );
				}
				catch ( TopologyException e )
				{
					LOG.error( "Could not create Channel Link " + channel.getChannelId() );
				}
				i++;
			}
		}
	}

	private void fastCreateResource( ResourceEntity resource, ResourceEntity parentResource, String associationType )
	{
		resourceDAO.create( resource );
		Resource ret = resource.toDataObject();

		ResourceCreatedEvent createdResource = new ResourceCreatedEvent( ret, parentResource.getId(), associationType, ret.getAllResourceAssociationIds() );
		eventRegistry.sendEventAfterTransactionCommits( createdResource );
		resourceAssociationDAO.create( new ResourceAssociationEntity( parentResource, resource, associationType ) );
	}

	public void removeSimulatedDevices() throws TopologyException
	{
		Long[] resourcesArray = getSimulatedDevices();

		ForkJoinPool pool = new ForkJoinPool( 10 );
		Semaphore semaphore = new Semaphore( 1 );
		pool.invoke( new SimulatedDevicesKiller( resourcesArray, semaphore ) );
	}

	private Long[] getSimulatedDevices()
	{
		List<Long> rootDevices = new ArrayList();

		Criteria criteria = new Criteria( DeviceResource.class );
		criteria.add( Restrictions.eq( "deviceView.parentDeviceId", null ) );
		criteria.add( Restrictions.eq( "deviceView.modelName", "Simulated" ) );
		List<Resource> deviceResources = topologyService.getResources( criteria );

		for ( Resource deviceResource : deviceResources )
		{
			rootDevices.add( deviceResource.getId() );
		}

		return ( Long[] ) rootDevices.toArray( new Long[rootDevices.size()] );
	}

	public Map<String, Long> runBenchmark()
	{
		Map<String, Long> result = new LinkedHashMap();

		long start = System.currentTimeMillis();
		List<DeviceResource> rootDevices = topologyService.getAllDeviceResources();
		result.put( "Get all registered device resources", Long.valueOf( System.currentTimeMillis() - start ) );

		if ( rootDevices.isEmpty() )
		{
			return result;
		}
		DeviceResource lastDevice = ( DeviceResource ) rootDevices.get( rootDevices.size() - 1 );
		Long id = lastDevice.getId();

		start = System.currentTimeMillis();
		DeviceResource deviceResource = topologyService.getDeviceResource( id );
		result.put( "Get last device resource by id", Long.valueOf( System.currentTimeMillis() - start ) );

		start = System.currentTimeMillis();
		Criteria criteria = new Criteria( DeviceResource.class );
		criteria.add( Restrictions.eq( "deviceView.deviceId", lastDevice.getDeviceId() ) );
		topologyService.getFirstResource( criteria );
		result.put( "Get last device resource by deviceId", Long.valueOf( System.currentTimeMillis() - start ) );

		criteria = new Criteria( DeviceResource.class );
		criteria.add( Restrictions.eq( "id", id ) );
		start = System.currentTimeMillis();
		topologyService.getResources( criteria );
		result.put( "Get last device resource by criteria", Long.valueOf( System.currentTimeMillis() - start ) );

		start = System.currentTimeMillis();
		topologyService.getDeviceResourceByDeviceId( lastDevice.getDeviceId() );
		result.put( "Get last device resource by device id", Long.valueOf( System.currentTimeMillis() - start ) );

		String channelId = getFirstChannelIdFromDevice( id );
		start = System.currentTimeMillis();
		topologyService.getChannelResource( deviceResource.getDeviceId(), channelId );
		result.put( "Get Channel by deviceId and channelId", Long.valueOf( System.currentTimeMillis() - start ) );

		criteria.clear();
		criteria.add( Restrictions.eq( "id", Long.valueOf( 0L ) ) );
		start = System.currentTimeMillis();
		topologyService.getResources( criteria );
		result.put( "Full topology cache scan", Long.valueOf( System.currentTimeMillis() - start ) );

		Resource logical;
		Resource system;

		try
		{
			system = topologyService.getResource( TopologyConstants.SYSTEM_ROOT_ID );
			logical = topologyService.getResource( TopologyConstants.LOGICAL_ROOT_ID );
		}
		catch ( TopologyException e )
		{
			throw new IllegalStateException( "Could not retrieve root resources for benchmark" );
		}

		start = System.currentTimeMillis();
		List<Resource> systemResourceList = system.createResourceList();
		result.put( "Get system list", Long.valueOf( System.currentTimeMillis() - start ) );

		Set<Long> systemIdList = new HashSet<>();
		Long[] idsSystem = {TopologyConstants.SYSTEM_ROOT_ID};
		for ( Resource res : systemResourceList )
		{
			systemIdList.add( res.getId() );
		}
		try
		{
			topologyService.getResources( idsSystem, -1, systemIdList );
		}
		catch ( TopologyException e )
		{
			throw new IllegalStateException( "Could not retrieve root resources for benchmark" );
		}
		result.put( "Get superadmin system list", Long.valueOf( System.currentTimeMillis() - start ) );

		start = System.currentTimeMillis();
		List<Resource> logicalResourceList = logical.createResourceList();
		result.put( "Get logical list", Long.valueOf( System.currentTimeMillis() - start ) );

		Set<Long> logicalIdList = new HashSet<>();
		Long[] idsLogical = {TopologyConstants.LOGICAL_ROOT_ID};
		for ( Resource res : logicalResourceList )
		{
			logicalIdList.add( res.getId() );
		}
		try
		{
			topologyService.getResources( idsLogical, -1, logicalIdList );
		}
		catch ( TopologyException e )
		{
			throw new IllegalStateException( "Could not retrieve root resources for benchmark" );
		}
		result.put( "Get superadmin logical list", Long.valueOf( System.currentTimeMillis() - start ) );

		start = System.currentTimeMillis();
		updateResourceBenchmark( lastDevice );
		result.put( "Update resource", Long.valueOf( System.currentTimeMillis() - start ) );

		start = System.currentTimeMillis();
		criteria = new Criteria( DeviceResource.class );
		criteria.add( Restrictions.in( "id", systemIdList ) );
		criteria.add( Restrictions.eq( "deviceView.parentDeviceId", null ) );
		topologyService.getResources( criteria );
		result.put( "Get devices by id list", Long.valueOf( System.currentTimeMillis() - start ) );

		return result;
	}

	public Map<Class<?>, Integer> getResourceCount( Class<?>... classes )
	{
		Map<Class<?>, Integer> results = new HashMap();
		for ( Class<?> clazz : classes )
		{
			Criteria criteria = new Criteria( clazz );
			List<Resource> resources = topologyService.getResources( criteria );
			results.put( clazz, Integer.valueOf( resources.size() ) );
		}
		return results;
	}

	public Integer getResourceCount( Criteria criteria )
	{
		List<Resource> resources = topologyService.getResources( criteria );
		return Integer.valueOf( resources.size() );
	}

	public DeviceResource getLastDeviceResource()
	{
		List<DeviceResource> rootDevices = topologyService.getAllDeviceResources();
		return ( DeviceResource ) rootDevices.get( rootDevices.size() - 1 );
	}

	public String getFirstChannelIdFromDevice( Long deviceResourceId )
	{
		try
		{
			List<String> allChannels = topologyService.getChannelIdsFromDevice( deviceResourceId );
			return ( String ) allChannels.get( 0 );
		}
		catch ( TopologyException e )
		{
			throw new IllegalStateException( "Could not retrieve channel from deviceResource specified id:" + deviceResourceId );
		}
	}

	private void updateResourceBenchmark( DeviceResource device )
	{
		try
		{
			topologyServiceProxy.updateResource( device );
		}
		catch ( TopologyException e )
		{
			throw new IllegalStateException( "Could not update resource for benchmark" );
		}
	}

	public void setResourceDAO( ResourceDAO<ResourceEntity> resourceDAO )
	{
		this.resourceDAO = resourceDAO;
	}

	public void setResourceAssociationDAO( ResourceAssociationDAO resourceAssociationDAO )
	{
		this.resourceAssociationDAO = resourceAssociationDAO;
	}
}

