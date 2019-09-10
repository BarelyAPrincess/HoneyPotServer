package com.marchnetworks.management.topology;

import com.marchnetworks.command.common.device.data.ChannelView;
import com.marchnetworks.command.common.device.data.DeviceView;
import com.marchnetworks.command.common.device.data.RegistrationStatus;
import com.marchnetworks.command.common.topology.ResourceAssociationType;
import com.marchnetworks.command.common.topology.TopologyException;
import com.marchnetworks.command.common.topology.data.ChannelLinkResource;
import com.marchnetworks.command.common.topology.data.ChannelResource;
import com.marchnetworks.command.common.topology.data.DeviceResource;
import com.marchnetworks.command.common.topology.data.LinkResource;
import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.command.common.topology.data.ResourceAssociation;
import com.marchnetworks.common.event.Event;
import com.marchnetworks.management.instrumentation.DeviceRegistry;
import com.marchnetworks.management.instrumentation.events.AbstractDeviceEvent;
import com.marchnetworks.management.instrumentation.events.ChannelConnectionStateEvent;
import com.marchnetworks.management.instrumentation.events.ChildDeviceRegistrationEvent;
import com.marchnetworks.management.instrumentation.events.DeviceChannelAddedEvent;
import com.marchnetworks.management.instrumentation.events.DeviceChannelChangedEvent;
import com.marchnetworks.management.instrumentation.events.DeviceChannelsInUseEvent;
import com.marchnetworks.management.instrumentation.events.DeviceChannelsMaxEvent;
import com.marchnetworks.management.instrumentation.events.DeviceConnectionStateChangeEvent;
import com.marchnetworks.management.instrumentation.events.DeviceIpChangedEvent;
import com.marchnetworks.management.instrumentation.events.DeviceRegistrationEvent;
import com.marchnetworks.management.instrumentation.events.DeviceSystemChangedEvent;
import com.marchnetworks.management.instrumentation.model.ChannelMBean;
import com.marchnetworks.management.instrumentation.model.CompositeDeviceMBean;
import com.marchnetworks.management.instrumentation.model.DeviceMBean;
import com.marchnetworks.management.topology.dao.ChannelResourceDAO;
import com.marchnetworks.management.topology.dao.DeviceResourceDAO;
import com.marchnetworks.management.topology.events.ResourceUpdatedEvent;
import com.marchnetworks.management.topology.model.ChannelResourceEntity;
import com.marchnetworks.management.topology.model.DeviceResourceEntity;
import com.marchnetworks.server.event.EventListener;
import com.marchnetworks.server.event.EventRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceChangeListener implements EventListener
{
	private static final Logger LOG = LoggerFactory.getLogger( DeviceChangeListener.class );

	private ResourceTopologyServiceIF topologyService;

	private DeviceResourceDAO deviceResourceDAO;

	private ChannelResourceDAO channelResourceDAO;

	private EventRegistry eventRegistry;
	private DeviceRegistry deviceRegistry;

	public String getListenerName()
	{
		return DeviceChangeListener.class.getSimpleName();
	}

	public void process( Event event )
	{
		LOG.debug( "Received event {}.", event );

		AbstractDeviceEvent deviceEvent = ( AbstractDeviceEvent ) event;
		String deviceId = deviceEvent.getDeviceId();

		if ( ( deviceEvent instanceof DeviceRegistrationEvent ) )
		{
			RegistrationStatus status = ( ( DeviceRegistrationEvent ) deviceEvent ).getRegistrationStatus();

			if ( ( status == RegistrationStatus.UNREGISTERED ) || ( status == RegistrationStatus.INITIAL ) )
			{
				return;
			}

			DeviceResource deviceResourceInCache = topologyService.getDeviceResourceByDeviceId( deviceId );
			DeviceResourceEntity deviceResource = deviceResourceDAO.findByDeviceId( deviceId );

			if ( ( deviceResourceInCache.getDeviceView().getRegistrationStatus() == RegistrationStatus.PENDING_REGISTRATION ) && ( status == RegistrationStatus.REGISTERED ) )
			{
				deviceResource.updateDeviceResourceName();
				for ( ChannelMBean channel : deviceResource.getDevice().getChannelMBeans().values() )
				{
					createChannelResource( channel, deviceResource.getId() );
				}
				DeviceResource deviceResourceView = ( DeviceResource ) deviceResource.createDataObject();
				deviceResourceView.setParentResource( deviceResourceInCache.getParentResource() );

				eventRegistry.sendEventAfterTransactionCommits( new ResourceUpdatedEvent( deviceResourceView, deviceResourceView.getAllResourceAssociationIds() ) );
			}
			else
			{
				updateTopologyForDeviceRegistration( status, deviceId, deviceResource );
			}
		}
		else if ( ( deviceEvent instanceof ChildDeviceRegistrationEvent ) )
		{
			RegistrationStatus status = ( ( ChildDeviceRegistrationEvent ) deviceEvent ).getRegistrationStatus();
			if ( status != RegistrationStatus.UNREGISTERED )
			{
				DeviceResourceEntity deviceResource = deviceResourceDAO.findByDeviceId( deviceId );
				updateTopologyForDeviceRegistration( status, deviceId, deviceResource );
			}
		}
		else if ( ( ( deviceEvent instanceof DeviceSystemChangedEvent ) ) || ( ( deviceEvent instanceof DeviceIpChangedEvent ) ) )
		{
			DeviceResourceEntity deviceResource = deviceResourceDAO.findByDeviceId( deviceId );
			if ( deviceResource != null )
			{
				LOG.debug( "Device {} changed. Sending ResourceUpdatedEvent for its DeviceResource {}.", deviceId, deviceResource.getId() );
				DeviceResource deviceResourceView = ( DeviceResource ) deviceResource.toDataObject();

				if ( deviceResourceView.getParentResourceId() == null )
				{
					deviceResourceView.setParentResource( topologyService.getDeviceResourceByDeviceId( deviceId ).getParentResource() );
				}

				eventRegistry.sendEventAfterTransactionCommits( new ResourceUpdatedEvent( deviceResourceView, deviceResourceView.getAllResourceAssociationIds() ) );
			}
			else
			{
				LOG.debug( "No DeviceResource for device {}.", deviceId );
			}
		}
		else if ( ( deviceEvent instanceof DeviceConnectionStateChangeEvent ) )
		{
			DeviceResource deviceResource = topologyService.getDeviceResourceByDeviceId( deviceId );
			if ( deviceResource != null )
			{
				eventRegistry.send( new ResourceUpdatedEvent( deviceResource, deviceResource.getAllResourceAssociationIds() ) );
			}
		}
		else if ( ( deviceEvent instanceof DeviceChannelChangedEvent ) )
		{
			DeviceResourceEntity deviceResource = deviceResourceDAO.findByDeviceId( deviceId );
			if ( deviceResource != null )
			{
				deviceResource.updateDeviceResourceName();

				DeviceChannelChangedEvent dccn = ( DeviceChannelChangedEvent ) deviceEvent;

				DeviceResource deviceResourceView = ( DeviceResource ) deviceResource.toDataObject();
				eventRegistry.sendEventAfterTransactionCommits( new ResourceUpdatedEvent( deviceResourceView, deviceResourceView.getAllResourceAssociationIds() ) );

				sendUpdateForChannelResource( dccn.getDeviceId(), dccn.getChannelId() );
			}
			else
			{
				LOG.warn( "No DeviceResource for device {} when processing DeviceChannelChangedEvent", deviceId );
			}
		}
		else if ( ( deviceEvent instanceof ChannelConnectionStateEvent ) )
		{
			ChannelConnectionStateEvent connectionStateEvent = ( ChannelConnectionStateEvent ) deviceEvent;
			sendUpdateForChannelResource( connectionStateEvent.getDeviceId(), connectionStateEvent.getChannelId() );
		}
		else if ( ( deviceEvent instanceof DeviceChannelAddedEvent ) )
		{

			DeviceChannelAddedEvent channelAddedEvent = ( DeviceChannelAddedEvent ) deviceEvent;

			DeviceMBean channelOwner = deviceRegistry.getDeviceByChannel( channelAddedEvent.getDeviceId(), channelAddedEvent.getChannelId() );
			if ( channelOwner == null )
			{
				LOG.warn( "Device {} owning channel {} does not exist. Won't create topology resources ...", new Object[] {channelAddedEvent.getDeviceId(), channelAddedEvent.getChannelId()} );
				return;
			}

			DeviceResourceEntity channelOwnerResource = deviceResourceDAO.findByDeviceId( channelOwner.getDeviceId() );

			if ( channelOwnerResource == null )
			{
				createDeviceResource( channelOwner );
			}
			else
			{
				updateDeviceResource( channelOwnerResource );
			}
		}
		else if ( ( deviceEvent instanceof DeviceChannelsInUseEvent ) )
		{
			DeviceResourceEntity deviceResource = deviceResourceDAO.findByDeviceId( deviceId );
			DeviceResource deviceResourceView = ( DeviceResource ) deviceResource.toDataObject();
			DeviceView deviceView = deviceResourceView.getDeviceView();
			deviceView.setChannelsInUse( Integer.valueOf( ( ( DeviceChannelsInUseEvent ) deviceEvent ).getInUse() ) );
			eventRegistry.sendEventAfterTransactionCommits( new ResourceUpdatedEvent( deviceResourceView, deviceResourceView.getAllResourceAssociationIds() ) );
		}
		else if ( ( deviceEvent instanceof DeviceChannelsMaxEvent ) )
		{
			DeviceResourceEntity deviceResource = deviceResourceDAO.findByDeviceId( deviceId );
			DeviceResource deviceResourceView = ( DeviceResource ) deviceResource.toDataObject();
			DeviceView deviceView = deviceResourceView.getDeviceView();
			deviceView.setChannelsMax( Integer.valueOf( ( ( DeviceChannelsMaxEvent ) deviceEvent ).getChannelsMax() ) );
			eventRegistry.sendEventAfterTransactionCommits( new ResourceUpdatedEvent( deviceResourceView, deviceResourceView.getAllResourceAssociationIds() ) );
		}
	}

	private void updateTopologyForDeviceRegistration( RegistrationStatus status, String deviceId, DeviceResourceEntity deviceResource )
	{
		if ( deviceResource == null )
		{
			DeviceMBean device = deviceRegistry.getDevice( deviceId );
			if ( device == null )
			{
				LOG.warn( "Device {} does not exist.", device );
				return;
			}

			LOG.debug( "Creating new DeviceResource for device {}.", deviceId );
			createDeviceResource( device );
		}
		else
		{
			if ( status == RegistrationStatus.REGISTERED )
			{
				LOG.debug( "Updating DeviceResource {} for device {}.", deviceResource.getId(), deviceId );
				updateDeviceResource( deviceResource );
			}
			LOG.debug( "Device {} registration result received. Sending ResourceUpdatedEvent for its DeviceResource {}.", deviceId, deviceResource.getId() );
			DeviceResource deviceResourceView = ( DeviceResource ) deviceResource.toDataObject();

			DeviceResource cachedDevice = topologyService.getDeviceResourceByDeviceId( deviceId );
			deviceResourceView.setParentResource( cachedDevice.getParentResource() );

			eventRegistry.sendEventAfterTransactionCommits( new ResourceUpdatedEvent( deviceResourceView, deviceResourceView.getAllResourceAssociationIds() ) );
		}
	}

	private void createDeviceResource( DeviceMBean device )
	{
		if ( device.getParentDevice() == null )
		{
			LOG.warn( "Child device {} is parentless. Its parent may have been removed.", device );
			return;
		}
		DeviceResourceEntity parentDeviceResource = deviceResourceDAO.findByDeviceId( device.getParentDevice().getDeviceId() );
		Long parentResourceId;

		if ( parentDeviceResource != null )
		{
			parentResourceId = parentDeviceResource.getId();
		}
		else
		{
			LOG.warn( "Parent device {} for device {} does not have a corresponding DeviceResource. It may have been removed.", device.getParentDevice().getDeviceId(), device );
			return;
		}

		DeviceResource deviceResourceData = new DeviceResource();
		deviceResourceData.setDeviceId( device.getDeviceId() );

		LOG.debug( "Creating new DeviceResource for device {} under parent resource {}.", device, parentResourceId );
		try
		{
			deviceResourceData = ( DeviceResource ) topologyService.createResource( deviceResourceData, parentResourceId, ResourceAssociationType.DEVICE.name() );
		}
		catch ( TopologyException ex )
		{
			LOG.error( "Error creating DeviceResource for device " + device + ".", ex );
			return;
		}

		LOG.debug( "Created new DeviceResource {} for device {} under parent resource {}.", new Object[] {deviceResourceData.getId(), device, parentResourceId} );

		for ( ChannelMBean channel : device.getChannelMBeans().values() )
		{
			createChannelResource( channel, deviceResourceData.getId() );
		}
	}

	private void createChannelResource( ChannelMBean channel, Long deviceResourceId )
	{
		ChannelResource channelResourceData = new ChannelResource();
		channelResourceData.setChannelId( channel.getChannelId() );
		ChannelView channelView = new ChannelView();
		channelView.setId( Long.valueOf( Long.parseLong( channel.getIdAsString() ) ) );
		channelResourceData.setChannelView( channelView );
		LOG.debug( "Creating new ChannelResource for Channel {} under parent resource {}.", channel.getChannelId(), deviceResourceId );
		try
		{
			channelResourceData = ( ChannelResource ) topologyService.createResource( channelResourceData, deviceResourceId, ResourceAssociationType.CHANNEL.name() );
		}
		catch ( TopologyException ex )
		{
			LOG.error( "Error creating ChannelResource for Channel " + channel.getChannelId() + " and device resource " + deviceResourceId + ".", ex );
		}
	}

	private void updateDeviceResource( DeviceResourceEntity deviceResource )
	{
		deviceResource.updateDeviceResourceName();

		Map<String, Long> currentChannelIdToResourceIdMap = new HashMap();

		Collection<ResourceAssociation> channelAssociations = deviceResource.toDataObject( 1, false ).getResourceAssociationsByType( ResourceAssociationType.CHANNEL.name() );
		if ( channelAssociations != null )
		{
			for ( ResourceAssociation association : channelAssociations )
			{
				ChannelResource channelResource = ( ChannelResource ) association.getResource();
				currentChannelIdToResourceIdMap.put( channelResource.getChannelId(), channelResource.getId() );
			}
		}

		for ( ChannelMBean channel : deviceResource.getDevice().getChannelMBeans().values() )
		{
			if ( currentChannelIdToResourceIdMap.remove( channel.getChannelId() ) == null )
			{
				createChannelResource( channel, deviceResource.getId() );
			}
		}

		for ( Long channelResourceId : currentChannelIdToResourceIdMap.values() )
		{
			try
			{
				LOG.debug( "Removing obsolete ChannelResource {}.", channelResourceId );
				topologyService.removeResource( channelResourceId );
			}
			catch ( TopologyException ex )
			{
				LOG.error( "Error removing ChannelResource " + channelResourceId + ".", ex );
			}
		}
	}

	private boolean canFind( Long[] array, long val )
	{
		boolean bFound = false;
		for ( int i = 0; ( !bFound ) && ( i < array.length ); i++ )
		{
			if ( array[i].longValue() == val )
			{
				bFound = true;
			}
		}

		return bFound;
	}

	private boolean isSame( Long[] array1, Long[] array2 )
	{
		boolean bSame = true;

		if ( array1.length == array2.length )
		{
			for ( int i = 0; ( bSame ) && ( i < array1.length ); i++ )
			{
				bSame = canFind( array2, array1[i].longValue() );
			}
		}
		else
		{
			bSame = false;
		}

		return bSame;
	}

	private void updateLinkedChannels( ChannelResource cr, String deviceId, String channelId, List<LinkResource> linksList )
	{
		String[] channelIdAssociations = cr.getChannelView().getAssocIds();
		List<Long> newLinkedIds = new ArrayList();

		ChannelResource channelResource = topologyService.getChannelResource( deviceId, channelId );
		newLinkedIds.add( channelResource.getId() );

		String rootDeviceId = deviceId;
		DeviceResource devRes = topologyService.getDeviceResourceByDeviceId( deviceId );
		if ( devRes == null )
		{
			LOG.warn( "Warn: cannot find the device resource for Device ID: " + deviceId );
			return;
		}

		if ( !devRes.isRootDevice() )
		{
			rootDeviceId = devRes.getDeviceView().getParentDeviceId();
		}
		for ( String chId : channelIdAssociations )
		{
			ChannelResource chRes = topologyService.getChannelResource( rootDeviceId, chId );
			if ( chRes != null )
			{
				newLinkedIds.add( chRes.getId() );
			}
			else
			{
				LOG.warn( "Warn: cannot find the channel resource for Device: {}, Channel: {} ", deviceId, chId );
			}
		}

		Long[] newIdsArray = ( Long[] ) newLinkedIds.toArray( new Long[newLinkedIds.size()] );
		for ( LinkResource res : linksList )
		{
			if ( ( res instanceof ChannelLinkResource ) )
			{
				Long[] curLinkedIds = res.getLinkedResourceIds();

				if ( curLinkedIds[0].equals( newLinkedIds.get( 0 ) ) )
				{

					if ( !isSame( curLinkedIds, newIdsArray ) )
					{
						res.setLinkedResourceIds( newIdsArray );
						try
						{
							LOG.debug( "Updating topology Resource " + res.getName() );
							topologyService.updateResource( res );
						}
						catch ( TopologyException e )
						{
							LOG.info( "Updating topology Resource " + res.getName() + " got exception " + e.getMessage() );
						}
					}
				}
			}
		}
	}

	private void sendUpdateForChannelResource( String deviceId, String channelId )
	{
		LOG.debug( "DeviceId=" + deviceId + " with ChannelId=" + channelId + " has changed. Sending ResourceUpdatedEvent for this resource." );

		ChannelResourceEntity cre = channelResourceDAO.getChannel( deviceId, channelId );
		if ( cre != null )
		{
			ChannelResource cr = ( ChannelResource ) cre.toDataObject();

			Set<Long> ids = new HashSet();
			ids.add( cr.getId() );
			List<LinkResource> linksList = topologyService.getLinkResources( cr.getId() );
			for ( LinkResource link : linksList )
			{
				ids.add( link.getId() );
			}

			if ( !linksList.isEmpty() )
			{
				updateLinkedChannels( cr, deviceId, channelId, linksList );
			}

			DeviceResource device = topologyService.getDeviceResourceByDeviceId( deviceId );
			cr.setParentResource( device );

			eventRegistry.sendEventAfterTransactionCommits( new ResourceUpdatedEvent( cr, ids ) );
		}
		else
		{
			LOG.debug( "No ChannelResource for DeviceId=" + deviceId + " with ChannelId=" + channelId );
		}
	}

	public ResourceTopologyServiceIF getTopologyService()
	{
		return topologyService;
	}

	public void setTopologyService( ResourceTopologyServiceIF topologyService )
	{
		this.topologyService = topologyService;
	}

	public DeviceResourceDAO getDeviceResourceDAO()
	{
		return deviceResourceDAO;
	}

	public void setDeviceResourceDAO( DeviceResourceDAO deviceResourceDAO )
	{
		this.deviceResourceDAO = deviceResourceDAO;
	}

	public EventRegistry getEventRegistry()
	{
		return eventRegistry;
	}

	public void setEventRegistry( EventRegistry eventRegistry )
	{
		this.eventRegistry = eventRegistry;
	}

	public DeviceRegistry getDeviceRegistry()
	{
		return deviceRegistry;
	}

	public void setDeviceRegistry( DeviceRegistry deviceRegistry )
	{
		this.deviceRegistry = deviceRegistry;
	}

	public void setChannelResourceDAO( ChannelResourceDAO channelResourceDAO )
	{
		this.channelResourceDAO = channelResourceDAO;
	}
}

