package io.amelia.support;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Helper for writing standard Java collection interfaces to a data
 * structure like {@link ArrayMap}.
 *
 * @hide
 */
abstract class MapCollections<K, V>
{
	EntrySet mEntrySet;
	KeySet mKeySet;
	ValuesCollection mValues;

	protected abstract void colClear();

	protected abstract Object colGetEntry( int index, int offset );

	protected abstract Map<K, V> colGetMap();

	protected abstract int colGetSize();

	protected abstract int colIndexOfKey( Object key );

	protected abstract int colIndexOfValue( Object key );

	protected abstract void colPut( K key, V value );

	protected abstract void colRemoveAt( int index );

	protected abstract V colSetValue( int index, V value );

	public Set<Map.Entry<K, V>> getEntrySet()
	{
		if ( mEntrySet == null )
		{
			mEntrySet = new EntrySet();
		}
		return mEntrySet;
	}

	public Set<K> getKeySet()
	{
		if ( mKeySet == null )
		{
			mKeySet = new KeySet();
		}
		return mKeySet;
	}

	public Collection<V> getValues()
	{
		if ( mValues == null )
		{
			mValues = new ValuesCollection();
		}
		return mValues;
	}

	public Object[] toArrayHelper( int offset )
	{
		final int N = colGetSize();
		Object[] result = new Object[N];
		for ( int i = 0; i < N; i++ )
		{
			result[i] = colGetEntry( i, offset );
		}
		return result;
	}

	public <T> T[] toArrayHelper( T[] array, int offset )
	{
		final int N = colGetSize();
		if ( array.length < N )
		{
			@SuppressWarnings( "unchecked" )
			T[] newArray = ( T[] ) Array.newInstance( array.getClass().getComponentType(), N );
			array = newArray;
		}
		for ( int i = 0; i < N; i++ )
		{
			array[i] = ( T ) colGetEntry( i, offset );
		}
		if ( array.length > N )
		{
			array[N] = null;
		}
		return array;
	}

	final class ArrayIterator<T> implements Iterator<T>
	{
		final int mOffset;
		boolean mCanRemove = false;
		int mIndex;
		int mSize;

		ArrayIterator( int offset )
		{
			mOffset = offset;
			mSize = colGetSize();
		}

		@Override
		public boolean hasNext()
		{
			return mIndex < mSize;
		}

		@Override
		public T next()
		{
			Object res = colGetEntry( mIndex, mOffset );
			mIndex++;
			mCanRemove = true;
			return ( T ) res;
		}

		@Override
		public void remove()
		{
			if ( !mCanRemove )
			{
				throw new IllegalStateException();
			}
			mIndex--;
			mSize--;
			mCanRemove = false;
			colRemoveAt( mIndex );
		}
	}

	final class EntrySet implements Set<Map.Entry<K, V>>
	{
		@Override
		public int hashCode()
		{
			int result = 0;
			for ( int i = colGetSize() - 1; i >= 0; i-- )
			{
				final Object key = colGetEntry( i, 0 );
				final Object value = colGetEntry( i, 1 );
				result += ( ( key == null ? 0 : key.hashCode() ) ^ ( value == null ? 0 : value.hashCode() ) );
			}
			return result;
		}

		@Override
		public int size()
		{
			return colGetSize();
		}

		@Override
		public boolean add( Map.Entry<K, V> object )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean addAll( Collection<? extends Map.Entry<K, V>> collection )
		{
			int oldSize = colGetSize();
			for ( Map.Entry<K, V> entry : collection )
			{
				colPut( entry.getKey(), entry.getValue() );
			}
			return oldSize != colGetSize();
		}

		@Override
		public void clear()
		{
			colClear();
		}

		@Override
		public boolean contains( Object o )
		{
			if ( !( o instanceof Map.Entry ) )
				return false;
			Map.Entry<?, ?> e = ( Map.Entry<?, ?> ) o;
			int index = colIndexOfKey( e.getKey() );
			if ( index < 0 )
			{
				return false;
			}
			Object foundVal = colGetEntry( index, 1 );
			return Objs.equals( foundVal, e.getValue() );
		}

		@Override
		public boolean containsAll( Collection<?> collection )
		{
			Iterator<?> it = collection.iterator();
			while ( it.hasNext() )
			{
				if ( !contains( it.next() ) )
				{
					return false;
				}
			}
			return true;
		}

		@Override
		public boolean isEmpty()
		{
			return colGetSize() == 0;
		}

		@Override
		public Iterator<Map.Entry<K, V>> iterator()
		{
			return new MapIterator();
		}

		@Override
		public boolean remove( Object object )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean removeAll( Collection<?> collection )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean retainAll( Collection<?> collection )
		{
			throw new UnsupportedOperationException();
		}



		@Override
		public Object[] toArray()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public <T> T[] toArray( T[] array )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean equals( Object object )
		{
			return Maps.equalsSet( this, object );
		}


	}

	final class KeySet implements Set<K>
	{
		@Override
		public boolean add( K object )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean addAll( Collection<? extends K> collection )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void clear()
		{
			colClear();
		}

		@Override
		public boolean contains( Object object )
		{
			return colIndexOfKey( object ) >= 0;
		}

		@Override
		public boolean containsAll( Collection<?> collection )
		{
			return Maps.containsAll( colGetMap(), collection );
		}

		@Override
		public boolean isEmpty()
		{
			return colGetSize() == 0;
		}

		@Override
		public Iterator<K> iterator()
		{
			return new ArrayIterator<K>( 0 );
		}

		@Override
		public boolean remove( Object object )
		{
			int index = colIndexOfKey( object );
			if ( index >= 0 )
			{
				colRemoveAt( index );
				return true;
			}
			return false;
		}

		@Override
		public boolean removeAll( Collection<?> collection )
		{
			return Maps.removeAll( colGetMap(), collection );
		}

		@Override
		public boolean retainAll( Collection<?> collection )
		{
			return Maps.retainAll( colGetMap(), collection );
		}

		@Override
		public int size()
		{
			return colGetSize();
		}

		@Override
		public Object[] toArray()
		{
			return toArrayHelper( 0 );
		}

		@Override
		public <T> T[] toArray( T[] array )
		{
			return toArrayHelper( array, 0 );
		}

		@Override
		public boolean equals( Object object )
		{
			return Maps.equalsSet( this, object );
		}

		@Override
		public int hashCode()
		{
			int result = 0;
			for ( int i = colGetSize() - 1; i >= 0; i-- )
			{
				Object obj = colGetEntry( i, 0 );
				result += obj == null ? 0 : obj.hashCode();
			}
			return result;
		}
	}

	final class MapIterator implements Iterator<Map.Entry<K, V>>, Map.Entry<K, V>
	{
		int mEnd;
		boolean mEntryValid = false;
		int mIndex;

		MapIterator()
		{
			mEnd = colGetSize() - 1;
			mIndex = -1;
		}

		@Override
		public K getKey()
		{
			if ( !mEntryValid )
			{
				throw new IllegalStateException( "This container does not support retaining Map.Entry objects" );
			}
			return ( K ) colGetEntry( mIndex, 0 );
		}

		@Override
		public V getValue()
		{
			if ( !mEntryValid )
			{
				throw new IllegalStateException( "This container does not support retaining Map.Entry objects" );
			}
			return ( V ) colGetEntry( mIndex, 1 );
		}

		@Override
		public V setValue( V object )
		{
			if ( !mEntryValid )
			{
				throw new IllegalStateException( "This container does not support retaining Map.Entry objects" );
			}
			return colSetValue( mIndex, object );
		}

		@Override
		public boolean hasNext()
		{
			return mIndex < mEnd;
		}



		@Override
		public Map.Entry<K, V> next()
		{
			mIndex++;
			mEntryValid = true;
			return this;
		}



		@Override
		public void remove()
		{
			if ( !mEntryValid )
			{
				throw new IllegalStateException();
			}
			colRemoveAt( mIndex );
			mIndex--;
			mEnd--;
			mEntryValid = false;
		}

		@Override
		public final int hashCode()
		{
			if ( !mEntryValid )
			{
				throw new IllegalStateException( "This container does not support retaining Map.Entry objects" );
			}
			final Object key = colGetEntry( mIndex, 0 );
			final Object value = colGetEntry( mIndex, 1 );
			return ( key == null ? 0 : key.hashCode() ) ^ ( value == null ? 0 : value.hashCode() );
		}



		@Override
		public final boolean equals( Object o )
		{
			if ( !mEntryValid )
			{
				throw new IllegalStateException( "This container does not support retaining Map.Entry objects" );
			}
			if ( !( o instanceof Map.Entry ) )
			{
				return false;
			}
			Map.Entry<?, ?> e = ( Map.Entry<?, ?> ) o;
			return Objs.equals( e.getKey(), colGetEntry( mIndex, 0 ) ) && Objs.equals( e.getValue(), colGetEntry( mIndex, 1 ) );
		}



		@Override
		public final String toString()
		{
			return getKey() + "=" + getValue();
		}
	}

	final class ValuesCollection implements Collection<V>
	{

		@Override
		public int size()
		{
			return colGetSize();
		}

		@Override
		public boolean isEmpty()
		{
			return colGetSize() == 0;
		}

		@Override
		public boolean contains( Object object )
		{
			return colIndexOfValue( object ) >= 0;
		}

		@Override
		public Iterator<V> iterator()
		{
			return new ArrayIterator<V>( 1 );
		}

		@Override
		public Object[] toArray()
		{
			return toArrayHelper( 1 );
		}

		@Override
		public <T> T[] toArray( T[] array )
		{
			return toArrayHelper( array, 1 );
		}

		@Override
		public boolean add( V object )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean remove( Object object )
		{
			int index = colIndexOfValue( object );
			if ( index >= 0 )
			{
				colRemoveAt( index );
				return true;
			}
			return false;
		}

		@Override
		public boolean containsAll( Collection<?> collection )
		{
			Iterator<?> it = collection.iterator();
			while ( it.hasNext() )
			{
				if ( !contains( it.next() ) )
				{
					return false;
				}
			}
			return true;
		}

		@Override
		public boolean addAll( Collection<? extends V> collection )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean removeAll( Collection<?> collection )
		{
			int N = colGetSize();
			boolean changed = false;
			for ( int i = 0; i < N; i++ )
			{
				Object cur = colGetEntry( i, 1 );
				if ( collection.contains( cur ) )
				{
					colRemoveAt( i );
					i--;
					N--;
					changed = true;
				}
			}
			return changed;
		}

		@Override
		public boolean retainAll( Collection<?> collection )
		{
			int N = colGetSize();
			boolean changed = false;
			for ( int i = 0; i < N; i++ )
			{
				Object cur = colGetEntry( i, 1 );
				if ( !collection.contains( cur ) )
				{
					colRemoveAt( i );
					i--;
					N--;
					changed = true;
				}
			}
			return changed;
		}

		@Override
		public void clear()
		{
			colClear();
		}
	}
}
