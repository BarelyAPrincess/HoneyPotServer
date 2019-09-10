package com.marchnetworks.map.service;

import com.marchnetworks.common.cache.Cache;
import com.marchnetworks.common.crypto.CryptoException;
import com.marchnetworks.common.crypto.CryptoUtils;
import com.marchnetworks.map.dao.MapDAO;
import com.marchnetworks.map.model.MapEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class MapServiceImpl implements MapService
{
	private static final Logger LOG = LoggerFactory.getLogger( MapServiceImpl.class );

	private MapDAO mapDAO;
	private Cache<Long, byte[]> mapCache;

	public Long create( byte[] mapData ) throws MapException
	{
		Long result = null;
		if ( mapData != null )
		{
			if ( mapData.length == 0 )
			{
				String error = "Map data can not be empty";
				LOG.error( error );
				throw new MapException( MapExceptionTypeEnum.INVALID_MAP_DATA, error );
			}

			byte[] hash;

			try
			{
				hash = CryptoUtils.sha1( mapData );
			}
			catch ( CryptoException e )
			{
				String error = "Could not compute map data hash";
				LOG.error( error );
				throw new MapException( MapExceptionTypeEnum.INVALID_MAP_DATA, error );
			}

			MapEntity map = mapDAO.findByHash( hash );
			if ( map == null )
			{
				map = new MapEntity();
				map.setMapData( mapData );
				map.setHash( hash );
				mapDAO.create( map );
			}
			result = map.getId();

			mapCache.returnObject( map.getId(), mapData );
		}
		return result;
	}

	public Long update( Long resourceId, Long mapDataId, byte[] mapData ) throws MapException
	{
		Long result = null;
		if ( mapData != null )
		{
			if ( mapData.length == 0 )
			{
				String error = "Map data can not be empty";
				LOG.error( error );
				throw new MapException( MapExceptionTypeEnum.INVALID_MAP_DATA, error );
			}

			byte[] hash;

			try
			{
				hash = CryptoUtils.sha1( mapData );
			}
			catch ( CryptoException e )
			{
				String error = "Could not compute map data hash";
				LOG.error( error );
				throw new MapException( MapExceptionTypeEnum.INVALID_MAP_DATA, error );
			}

			MapEntity map = mapDAO.findByHash( hash );
			if ( map == null )
			{
				map = new MapEntity();
				map.setMapData( mapData );
				map.setHash( hash );
				mapDAO.create( map );
			}

			mapCache.returnObject( map.getId(), mapData );

			Long newMapId = map.getId();

			if ( !newMapId.equals( mapDataId ) )
			{

				removeReference( mapDataId, resourceId );

				addReference( map, resourceId );
			}
			result = newMapId;

		}
		else
		{
			removeReference( mapDataId, resourceId );
		}

		return result;
	}

	public byte[] getMapData( Long mapDataId ) throws MapException
	{
		byte[] result = ( byte[] ) mapCache.getObject( mapDataId );
		if ( result != null )
		{
			return result;
		}

		MapEntity map = ( MapEntity ) mapDAO.findById( mapDataId );
		if ( map == null )
		{
			String error = "Map id " + mapDataId + " not found when querying.";
			LOG.error( error );
			throw new MapException( MapExceptionTypeEnum.MAP_NOT_FOUND, error );
		}

		result = map.getMapData();

		mapCache.returnObject( mapDataId, result );
		return result;
	}

	public String getMapTag( Long mapDataId ) throws MapException
	{
		String tag = mapCache.getTag( mapDataId );
		if ( tag == null )
		{
			if ( !mapDAO.checkExists( mapDataId ) )
			{
				String error = "Map id " + mapDataId + " not found when querying.";
				LOG.error( error );
				throw new MapException( MapExceptionTypeEnum.MAP_NOT_FOUND, error );
			}
			tag = mapCache.createTag( mapDataId );
		}
		return tag;
	}

	public void removeReference( Long mapDataId, Long resourceId )
	{
		if ( mapDataId != null )
		{
			MapEntity oldMap = ( MapEntity ) mapDAO.findById( mapDataId );
			if ( oldMap != null )
			{
				Set<Long> references = oldMap.getReferences();
				references.remove( resourceId );
				oldMap.setReferences( references );

				if ( references.isEmpty() )
				{
					mapCache.removeObject( oldMap.getId() );
					mapDAO.delete( oldMap );
				}
			}
		}
	}

	public void addReference( Long mapDataId, Long resourceId )
	{
		if ( mapDataId != null )
		{
			MapEntity map = ( MapEntity ) mapDAO.findById( mapDataId );
			if ( map != null )
			{
				addReference( map, resourceId );
			}
		}
	}

	private void addReference( MapEntity map, Long resourceId )
	{
		Set<Long> references = map.getReferences();
		references.add( resourceId );
		map.setReferences( references );
	}

	public void setMapDAO( MapDAO mapDAO )
	{
		this.mapDAO = mapDAO;
	}

	public void setMapCache( Cache<Long, byte[]> mapCache )
	{
		this.mapCache = mapCache;
	}
}

