package com.marchnetworks.management.topology.service;

import com.marchnetworks.command.api.topology.ArchiverAssociationCoreService;
import com.marchnetworks.command.common.topology.data.ArchiverAssociation;
import com.marchnetworks.management.topology.ArchiverAssociationService;
import com.marchnetworks.management.topology.dao.ArchiverAssociationDAO;
import com.marchnetworks.management.topology.events.ArchiverAssociationRemovedEvent;
import com.marchnetworks.management.topology.events.ArchiverAssociationUpdatedEvent;
import com.marchnetworks.management.topology.model.ArchiverAssociationEntity;
import com.marchnetworks.server.event.EventRegistry;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArchiverAssociationServiceImpl implements ArchiverAssociationCoreService, ArchiverAssociationService
{
	private static final Logger LOG = LoggerFactory.getLogger( ArchiverAssociationServiceImpl.class );

	private ArchiverAssociationDAO archiverAssociationDAO;

	private EventRegistry eventRegistry;

	public void updateArchiverAssociation( ArchiverAssociation archiverAssociation )
	{
		List<ArchiverAssociationEntity> entities = archiverAssociationDAO.findAll();

		ArchiverAssociationEntity archiverAssociationEntity = null;

		for ( ArchiverAssociationEntity entity : entities )
		{
			if ( entity.getArchiverResourceId().equals( archiverAssociation.getArchiverResourceId() ) )
			{
				archiverAssociationEntity = entity;
				break;
			}
		}

		if ( archiverAssociationEntity == null )
		{

			for ( Long deviceId : archiverAssociation.getDeviceResourceIds() )
			{
				removeExistingAssociation( deviceId, entities );
			}

			archiverAssociationEntity = new ArchiverAssociationEntity( archiverAssociation.getArchiverResourceId(), archiverAssociation.getDeviceResourceIds() );
			archiverAssociationDAO.create( archiverAssociationEntity );

			LOG.debug( "Created ArchiverAsssociationEntity for ArchiverId: {}", archiverAssociation.getArchiverResourceId() );
			eventRegistry.sendEventAfterTransactionCommits( new ArchiverAssociationUpdatedEvent( archiverAssociation.getArchiverResourceId(), archiverAssociation.getDeviceResourceIds() ) );
			return;
		}

		if ( archiverAssociation.getDeviceResourceIds() != null )
		{
			for ( Long deviceId : archiverAssociation.getDeviceResourceIds() )
			{
				removeExistingAssociation( deviceId, entities );
			}
		}

		archiverAssociationEntity.setAssociatedDevices( archiverAssociation.getDeviceResourceIds() );
		if ( archiverAssociation.getDeviceResourceIds() != null )
		{
			LOG.debug( "Updated ArchiverId: {}, devices: {}", archiverAssociationEntity.getArchiverResourceId(), Arrays.toString( archiverAssociation.getDeviceResourceIds() ) );
		}

		eventRegistry.sendEventAfterTransactionCommits( new ArchiverAssociationUpdatedEvent( archiverAssociation.getArchiverResourceId(), archiverAssociation.getDeviceResourceIds() ) );
	}

	public Set<Long> getAssociatedDeviceResourceIdsByArchiverId( Long archiverResourceId )
	{
		Set<Long> result = null;
		ArchiverAssociationEntity archiverAsssociationEntity = archiverAssociationDAO.findByArchiverId( archiverResourceId );
		if ( archiverAsssociationEntity != null )
		{
			result = archiverAsssociationEntity.getAssociatedDevices();
		}
		if ( result == null )
		{
			return Collections.emptySet();
		}
		return result;
	}

	public List<Long> getAllArchiverResourceIds()
	{
		List<Long> archiverResourceIds = archiverAssociationDAO.findAllArchiverIds();
		return archiverResourceIds;
	}

	public ArchiverAssociation[] getArchiverAssociations()
	{
		List<ArchiverAssociationEntity> archiverAsssociationEntities = archiverAssociationDAO.findAll();
		if ( ( archiverAsssociationEntities == null ) || ( archiverAsssociationEntities.isEmpty() ) )
		{
			return null;
		}

		ArchiverAssociation[] result = new ArchiverAssociation[archiverAsssociationEntities.size()];
		for ( int i = 0; i < archiverAsssociationEntities.size(); i++ )
		{
			ArchiverAssociationEntity archiverAsssociationEntity = ( ArchiverAssociationEntity ) archiverAsssociationEntities.get( i );
			result[i] = archiverAsssociationEntity.toDataObject();
		}
		return result;
	}

	public Long getPrimaryArchiverByDeviceresourceId( Long deviceResourceId )
	{
		LOG.debug( "getPrimaryArchiverByDeviceresourceId deviceid: " + deviceResourceId );
		List<ArchiverAssociationEntity> archiverAssociationEntities = archiverAssociationDAO.findAll();

		if ( ( archiverAssociationEntities == null ) || ( archiverAssociationEntities.isEmpty() ) )
		{
			return null;
		}

		for ( ArchiverAssociationEntity archiverAssociationEntitie : archiverAssociationEntities )
		{
			if ( ( archiverAssociationEntitie != null ) && ( archiverAssociationEntitie.getAssociatedDevices() != null ) && ( archiverAssociationEntitie.getAssociatedDevices().contains( deviceResourceId ) ) )
			{

				LOG.debug( "getPrimaryArchiverByDeviceresourceId  return ArchiverId: " + archiverAssociationEntitie.getArchiverResourceId() );
				return archiverAssociationEntitie.getArchiverResourceId();
			}
		}
		return null;
	}

	public void removeDevice( Long deviceId )
	{
		if ( deviceId == null )
		{
			return;
		}

		List<ArchiverAssociationEntity> archiverAssociationEntities = archiverAssociationDAO.findAll();

		if ( ( archiverAssociationEntities != null ) && ( !archiverAssociationEntities.isEmpty() ) )
		{
			Iterator<ArchiverAssociationEntity> it = archiverAssociationEntities.iterator();
			while ( it.hasNext() )
			{
				ArchiverAssociationEntity archiverAsssociationEntity = ( ArchiverAssociationEntity ) it.next();
				if ( archiverAsssociationEntity != null )
				{
					if ( archiverAsssociationEntity.getArchiverResourceId().longValue() == deviceId.longValue() )
					{
						archiverAssociationDAO.delete( archiverAsssociationEntity );
						LOG.debug( "ArchiverAssociation ArchiverId {} deleted ", deviceId );
						return;
					}

					if ( ( archiverAsssociationEntity.getAssociatedDevices() != null ) && ( archiverAsssociationEntity.getAssociatedDevices().contains( deviceId ) ) )
					{
						archiverAsssociationEntity.removeFromAssociatedDevices( deviceId );
						LOG.debug( " Removed the Association for deviceId: {} from ArchiverId: {}", deviceId, archiverAsssociationEntity.getArchiverResourceId() );
						return;
					}
				}
			}
		}
	}

	private void removeExistingAssociation( Long deviceId, List<ArchiverAssociationEntity> entities )
	{
		for ( Iterator<ArchiverAssociationEntity> iterator = entities.iterator(); iterator.hasNext(); )
		{
			ArchiverAssociationEntity entity = ( ArchiverAssociationEntity ) iterator.next();
			if ( ( entity.getAssociatedDevices() != null ) && ( entity.getAssociatedDevices().contains( deviceId ) ) )
			{
				entity.removeFromAssociatedDevices( deviceId );
				eventRegistry.sendEventAfterTransactionCommits( new ArchiverAssociationRemovedEvent( entity.getArchiverResourceId(), new Long[] {deviceId} ) );
				break;
			}
		}
	}

	public void setArchiverAssociationDAO( ArchiverAssociationDAO archiverAssociationDAO )
	{
		this.archiverAssociationDAO = archiverAssociationDAO;
	}

	public void setEventRegistry( EventRegistry eventRegistry )
	{
		this.eventRegistry = eventRegistry;
	}
}

