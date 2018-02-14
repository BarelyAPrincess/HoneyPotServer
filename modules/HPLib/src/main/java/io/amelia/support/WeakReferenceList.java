package io.amelia.support;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

/**
 * Wraps an {@link ArrayList<WeakReference>>} and provides methods that auto remove dereferenced values.
 *
 * @param <T> The Value Type
 */
public class WeakReferenceList<T> implements List<T>
{
	List<WeakReference<T>> list = new ArrayList<>();

	@Override
	public boolean add( T value )
	{
		return list.add( new WeakReference<>( value ) );
	}

	@Override
	public void add( int index, T value )
	{
		list.add( index, new WeakReference<>( value ) );
	}

	@Override
	public boolean addAll( @Nonnull Collection<? extends T> values )
	{
		return list.addAll( values.stream().map( value -> new WeakReference<>( ( T ) value ) ).collect( Collectors.toList() ) );
	}

	@Override
	public boolean addAll( int index, @Nonnull Collection<? extends T> values )
	{
		return list.addAll( index, values.stream().map( value -> new WeakReference<>( ( T ) value ) ).collect( Collectors.toList() ) );
	}

	@Override
	public void clear()
	{
		list.clear();
	}

	private <R> R collect( Function<List<T>, R> function )
	{
		List<T> tempList = list.stream().map( WeakReference::get ).filter( Objects::nonNull ).collect( Collectors.toList() );
		R result = function.apply( tempList );
		list = tempList.stream().map( WeakReference::new ).collect( Collectors.toList() );
		return result;
	}

	/**
	 * We remove references from the list that contain null values, meaning they were dereferenced.
	 *
	 * @return The computed list
	 */
	private List<T> collect()
	{
		filter();
		return list.stream().map( WeakReference::get ).collect( Collectors.toList() );
	}

	@Override
	public boolean contains( Object obj )
	{
		return collect().contains( obj );
	}

	@Override
	public boolean containsAll( @Nonnull Collection<?> collection )
	{
		return collect().containsAll( collection );
	}

	@Override
	public boolean equals( Object o )
	{
		return this == o;
	}

	private void filter()
	{
		list = list.stream().filter( ref -> ref.get() != null ).collect( Collectors.toList() );
	}

	@Override
	public T get( int index )
	{
		filter();
		return list.get( index ).get();
	}

	@Override
	public int hashCode()
	{
		return list.hashCode();
	}

	@Override
	public int indexOf( Object obj )
	{
		return collect().indexOf( obj );
	}

	@Override
	public boolean isEmpty()
	{
		return list.isEmpty();
	}

	@Nonnull
	@Override
	public Iterator<T> iterator()
	{
		return collect().iterator();
	}

	@Override
	public int lastIndexOf( Object obj )
	{
		return collect().lastIndexOf( obj );
	}

	@Nonnull
	@Override
	public ListIterator<T> listIterator()
	{
		return collect().listIterator();
	}

	@Nonnull
	@Override
	public ListIterator<T> listIterator( int index )
	{
		return collect().listIterator( index );
	}

	@Override
	public T remove( int index )
	{
		filter();
		return list.remove( index ).get();
	}

	@Override
	public boolean remove( Object obj )
	{
		return collect( list -> list.remove( obj ) );
	}

	@Override
	public boolean removeAll( @Nonnull Collection<?> collection )
	{
		return collect( list -> list.removeAll( collection ) );
	}

	@Override
	public boolean retainAll( @Nonnull Collection<?> collection )
	{
		return collect( list -> list.retainAll( collection ) );
	}

	@Override
	public T set( int index, T element )
	{
		filter();
		return list.set( index, new WeakReference<>( element ) ).get();
	}

	@Override
	public int size()
	{
		return collect().size();
	}

	@Override
	public Stream<T> stream()
	{
		filter();
		return list.stream().map( WeakReference::get );
	}

	@Nonnull
	@Override
	public List<T> subList( int fromIndex, int toIndex )
	{
		return collect().subList( fromIndex, toIndex );
	}

	@Nonnull
	@Override
	public Object[] toArray()
	{
		return stream().toArray();
	}

	@Nonnull
	@Override
	public <V> V[] toArray( @Nonnull V[] array )
	{
		// TODO Is there a better way to do this with the provided argument?
		return collect().toArray( array );
	}
}
