package com.marchnetworks.management.config.util;

import com.marchnetworks.management.config.DeviceImageState;
import com.marchnetworks.management.config.DeviceSnapshotState;
import com.marchnetworks.management.config.model.DeviceConfig;
import com.marchnetworks.management.config.model.DeviceImage;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class DeviceConfigCollection implements List<DeviceConfig>
{
	List<DeviceConfig> configCollection;

	public DeviceConfigCollection( List<DeviceConfig> deviceConfigList )
	{
		configCollection = deviceConfigList;
	}

	public DeviceConfigCollection()
	{
		configCollection = new java.util.ArrayList();
	}

	public void setImage( DeviceImage val )
	{
		for ( DeviceConfig aConfig : configCollection )
		{
			aConfig.setImage( val );
		}
	}

	public void setAssignState( DeviceImageState val )
	{
		for ( DeviceConfig aConfig : configCollection )
		{
			aConfig.setAssignState( val );
		}
	}

	public void clearSnapshots()
	{
		for ( DeviceConfig aConfig : configCollection )
		{
			aConfig.setSnapshot( null );
		}
	}

	public void setSnapshotState( DeviceSnapshotState snapshotState )
	{
		for ( DeviceConfig aConfig : configCollection )
		{
			aConfig.setSnapshotState( snapshotState );
		}
	}

	public boolean isEmpty()
	{
		return configCollection.isEmpty();
	}

	public DeviceConfig get( int i )
	{
		return ( DeviceConfig ) configCollection.get( i );
	}

	public boolean add( DeviceConfig e )
	{
		return configCollection.add( e );
	}

	public void add( int index, DeviceConfig element )
	{
		configCollection.add( index, element );
	}

	public boolean addAll( Collection<? extends DeviceConfig> c )
	{
		return configCollection.addAll( c );
	}

	public boolean addAll( int index, Collection<? extends DeviceConfig> c )
	{
		return configCollection.addAll( index, c );
	}

	public void clear()
	{
		configCollection.clear();
	}

	public boolean contains( Object o )
	{
		return configCollection.contains( o );
	}

	public boolean containsAll( Collection<?> c )
	{
		return configCollection.containsAll( c );
	}

	public int indexOf( Object o )
	{
		return configCollection.indexOf( o );
	}

	public Iterator<DeviceConfig> iterator()
	{
		return configCollection.iterator();
	}

	public int lastIndexOf( Object o )
	{
		return configCollection.lastIndexOf( o );
	}

	public ListIterator<DeviceConfig> listIterator()
	{
		return configCollection.listIterator();
	}

	public ListIterator<DeviceConfig> listIterator( int index )
	{
		return configCollection.listIterator();
	}

	public boolean remove( Object o )
	{
		return configCollection.remove( o );
	}

	public DeviceConfig remove( int index )
	{
		return ( DeviceConfig ) configCollection.remove( index );
	}

	public boolean removeAll( Collection<?> c )
	{
		return configCollection.removeAll( c );
	}

	public boolean retainAll( Collection<?> c )
	{
		return configCollection.retainAll( c );
	}

	public DeviceConfig set( int index, DeviceConfig element )
	{
		return ( DeviceConfig ) configCollection.set( index, element );
	}

	public int size()
	{
		return configCollection.size();
	}

	public List<DeviceConfig> subList( int fromIndex, int toIndex )
	{
		return configCollection.subList( fromIndex, toIndex );
	}

	public Object[] toArray()
	{
		return configCollection.toArray();
	}

	public <T> T[] toArray( T[] a )
	{
		return configCollection.toArray( a );
	}
}
