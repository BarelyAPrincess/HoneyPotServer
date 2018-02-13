/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.support;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import javafx.util.Pair;

public class Maps
{
	/**
	 * Checks and converts the string key to an integer. Non-numeric keys are removed from the treemap.
	 *
	 * @param map The map to sort
	 * @param <T> The value type
	 *
	 * @return The sorted map as a TreeMap
	 */
	public static <T> Map<Integer, T> asNumericallySortedMap( final Map<String, T> map )
	{
		return new TreeMap<Integer, T>()
		{{
			for ( Map.Entry<String, T> entry : map.entrySet() )
			{
				if ( Maths.isNumber( entry.getKey() ) )
					put( Integer.parseInt( entry.getKey() ), entry.getValue() );
			}
		}};
	}

	public static <K, V> MapBuilder<K, V> builder()
	{
		return new MapBuilder<>();
	}

	public static <K, V> MapBuilder<K, V> builder( K key, V value )
	{
		return builder().put( key, value );
	}

	public static <K, V> MapBuilder<K, V> builder( Map<K, V> values )
	{
		return builder().putAll( values );
	}

	public static <K, V> MapBuilder<K, V> builder( Properties properties, Class<K> kClass, Class<V> vClass )
	{
		return builder().putAll( properties, kClass, vClass );
	}

	public static <V> MapBuilder<String, V> builder( Properties properties, Class<V> vClass )
	{
		return builder().putAll( properties, vClass );
	}

	public static MapBuilder<String, String> builder( Properties properties )
	{
		return builder().putAll( properties, String.class );
	}

	public static <K, V> boolean containsAll( Map<K, V> map, Collection<?> collection )
	{
		Iterator<?> it = collection.iterator();
		while ( it.hasNext() )
		{
			if ( !map.containsKey( it.next() ) )
			{
				return false;
			}
		}
		return true;
	}

	public static <K, V, M extends Map<K, V>> M copyEmpty( M map )
	{
		try
		{
			if ( map instanceof LinkedHashMap )
				return ( M ) new LinkedHashMap<K, V>();
			if ( map instanceof TreeMap )
				return ( M ) new TreeMap<K, V>();
			if ( map instanceof Hashtable )
				return ( M ) new Hashtable<K, V>();
			if ( map instanceof WeakHashMap )
				return ( M ) new WeakHashMap<K, V>();
			if ( map instanceof ConcurrentHashMap )
				return ( M ) new ConcurrentHashMap<K, V>();
			if ( map instanceof IdentityHashMap )
				return ( M ) new IdentityHashMap<K, V>();
		}
		catch ( ClassCastException e )
		{

		}
		return ( M ) new HashMap<K, V>();
	}

	public static <T> boolean equalsSet( Set<T> set, Object object )
	{
		if ( set == object )
		{
			return true;
		}
		if ( object instanceof Set )
		{
			Set<?> s = ( Set<?> ) object;

			try
			{
				return set.size() == s.size() && set.containsAll( s );
			}
			catch ( NullPointerException ignored )
			{
				return false;
			}
			catch ( ClassCastException ignored )
			{
				return false;
			}
		}
		return false;
	}

	@SuppressWarnings( "unchecked" )
	public static <T> Optional<T> first( Map<?, T> map )
	{
		return Optional.ofNullable( map.size() == 0 ? null : ( T ) map.values().toArray()[0] );
	}

	public static int firstKey( Map<Integer, ?> map )
	{
		int n = 0;
		while ( map.containsKey( n ) )
			n++;
		return n;
	}

	public static <V> int firstKeyAndPut( Map<Integer, V> map, V val )
	{
		int n = firstKey( map );
		map.put( n, val );
		return n;
	}

	public static Map<String, Object> flattenMap( Map<String, Object> map )
	{
		Map<String, Object> result = new HashMap<>();
		flattenMap( result, "", map );
		return result;
	}

	private static void flattenMap( Map<String, Object> result, String path, Map<String, Object> map )
	{
		for ( Map.Entry<String, Object> entry : map.entrySet() )
		{
			String key = path.isEmpty() ? entry.getKey() : path + "/" + entry.getKey();

			if ( entry.getValue() instanceof Map )
				flattenMap( result, key, ( Map<String, Object> ) entry.getValue() );
			else
				result.put( key, entry.getValue() );
		}
	}

	/**
	 * Flips the map Key and Value
	 */
	static <K, V> Map<V, K> flipKeyValue( final Map<K, V> origMap )
	{
		return origMap.entrySet().stream().collect( Collectors.toMap( Map.Entry::getValue, Map.Entry::getKey ) );
	}

	public static <T> Map<String, T> indexMap( List<T> list )
	{
		AtomicInteger inx = new AtomicInteger();
		return list.stream().filter( Objects::nonNull ).map( l -> new Pair<>( Integer.toString( inx.getAndIncrement() ), l ) ).collect( Collectors.toMap( Pair::getKey, Pair::getValue ) );
	}

	@SafeVarargs
	public static <Key, Value> Map<Key, Value> joinMaps( Map<Key, Value>... maps )
	{
		if ( Objs.isEmpty( maps ) )
			return new HashMap<>();

		return Arrays.stream( maps ).flatMap( m -> m.entrySet().stream() ).collect( Collectors.toMap( Map.Entry::getKey, Map.Entry::getValue ) );
	}

	public static <K, V> K keyOf( Map<K, V> map, @Nonnull V val )
	{
		if ( map == null )
			return null;
		Objs.notNull( val );
		for ( Map.Entry<K, V> entry : map.entrySet() )
			if ( entry.getValue() == val )
				return entry.getKey();
		return null;
	}

	@SuppressWarnings( "unchecked" )
	public static <T> T last( Map<?, T> map )
	{
		if ( map.size() == 0 )
			return null;
		return ( T ) map.values().toArray()[map.size() - 1];
	}

	@SuppressWarnings( "unchecked" )
	public static <T> T mapIndex( Map<?, T> map, int inx )
	{
		if ( inx < 0 || map.size() - inx < 1 )
			throw new IndexOutOfBoundsException( "Map index out of bounds" );
		if ( map.size() == 0 )
			return null;
		return ( T ) map.values().toArray()[inx];
	}

	public static <T> Map<Integer, List<T>> paginate( List<T> list, int perPage )
	{
		return IntStream.iterate( 0, i -> i + perPage ).limit( ( list.size() + perPage - 1 ) / perPage ).boxed().collect( Collectors.toMap( i -> i / perPage, i -> list.subList( i, Math.min( i + perPage, list.size() ) ) ) );
	}

	public static <K, V> boolean removeAll( Map<K, V> map, Collection<?> collection )
	{
		int oldSize = map.size();
		Iterator<?> it = collection.iterator();
		while ( it.hasNext() )
			map.remove( it.next() );
		return oldSize != map.size();
	}

	/**
	 * Removes all of the elements of map that satisfy the given predicate.
	 * Errors or runtime exceptions thrown during iteration or by the predicate
	 * are relayed to the caller.
	 *
	 * @param map    the map we try to remove from
	 * @param filter a predicate which returns {@code true} for elements to be removed
	 *
	 * @return {@code true} if any elements were removed
	 *
	 * @throws NullPointerException          if the specified map or filter is null
	 * @throws UnsupportedOperationException if elements cannot be removed from this collection.
	 *                                       Implementations may throw this exception if a
	 *                                       matching element cannot be removed or if, in general, removal is not
	 *                                       supported.
	 * @implSpec The default implementation traverses all elements of the collection using
	 * its {@link Set#iterator} method. Each matching element is removed using
	 * {@link Iterator#remove()}. If the collection's iterator does not
	 * support removal then an {@code UnsupportedOperationException} will be
	 * thrown on the first matching element.
	 * @since 1.8
	 */
	public static <K, V> boolean removeIf( @Nonnull Map<K, V> map, @Nonnull BiPredicate<K, V> filter )
	{
		Objs.notNull( map );
		Objs.notNull( filter );

		boolean removed = false;
		final Iterator<Map.Entry<K, V>> each = map.entrySet().iterator();
		while ( each.hasNext() )
		{
			Map.Entry<K, V> next = each.next();
			if ( filter.test( next.getKey(), next.getValue() ) )
			{
				each.remove();
				removed = true;
			}
		}
		return removed;
	}

	public static <K, V> boolean retainAll( @Nonnull Map<K, V> map, @Nonnull Collection<?> collection )
	{
		int oldSize = map.size();
		Iterator<K> it = map.keySet().iterator();
		while ( it.hasNext() )
			if ( !collection.contains( it.next() ) )
				it.remove();
		return oldSize != map.size();
	}

	private Maps()
	{

	}

	@SuppressWarnings( "unchecked" )
	public static class MapBuilder<CK, CV>
	{
		final TreeMap<CK, CV> map;

		private MapBuilder()
		{
			this.map = new TreeMap<>();
		}

		private MapBuilder( Map<CK, CV> map )
		{
			this.map = new TreeMap<>( map );
		}

		private MapBuilder( Map<?, ?> oldMap, Class<CK> keyClass, Class<CV> valueClass )
		{
			map = new TreeMap<>();
			for ( Map.Entry<?, ?> entry : oldMap.entrySet() )
				putNotNull( Objs.castTo( entry.getKey(), keyClass ), Objs.castTo( entry.getValue(), valueClass ) );
		}

		private MapBuilder( Map<?, ?> oldMap, CK key, CV value )
		{
			map = new TreeMap<>();
			for ( Map.Entry<?, ?> entry : oldMap.entrySet() )
				putNotNull( ( CK ) entry.getKey(), ( CV ) entry.getValue() );
			putNotNull( key, value );
		}

		private MapBuilder( Map<?, ?> oldMap, Map<CK, CV> newMap )
		{
			map = new TreeMap<>();
			for ( Map.Entry<?, ?> entry : oldMap.entrySet() )
				putNotNull( ( CK ) entry.getKey(), ( CV ) entry.getValue() );
			for ( Map.Entry<CK, CV> entry : newMap.entrySet() )
				putNotNull( entry.getKey(), entry.getValue() );
		}

		public MapBuilder( Map<?, ?> oldMap, BiFunction<CK, CV, Boolean> function )
		{
			map = new TreeMap<>();
			for ( Map.Entry<?, ?> entry : oldMap.entrySet() )
				if ( function.apply( ( CK ) entry.getKey(), ( CV ) entry.getValue() ) )
					putNotNull( ( CK ) entry.getKey(), ( CV ) entry.getValue() );
		}

		public <K, V> MapBuilder<K, V> castTo( Class<K> keyClass, Class<V> valueClass )
		{
			return new MapBuilder<>( map, keyClass, valueClass );
		}

		public ConcurrentHashMap<CK, CV> concurrentHashMap()
		{
			return new ConcurrentHashMap<>( map );
		}

		public <K, V> MapBuilder<K, V> fill( @Nonnull List<K> keys )
		{
			return new MapBuilder<>( map, keys.stream().collect( Collectors.toMap( v -> v, null ) ) );
		}

		public MapBuilder<CK, CV> filter( BiFunction<CK, CV, Boolean> function )
		{
			return new MapBuilder<>( map, function );
		}

		public HashMap<CK, CV> hashMap()
		{
			return new HashMap<>( map );
		}

		public <V> MapBuilder<Integer, V> increment( @Nonnull List<V> values )
		{
			AtomicInteger i = new AtomicInteger( 0 );
			Map<Integer, V> newMap = new TreeMap<>();

			for ( V value : values )
			{
				while ( map.containsKey( i.get() ) )
					i.incrementAndGet();
				newMap.put( i.get(), value );
			}

			return new MapBuilder<>( map, newMap );
		}

		public <M extends Map> M map( Supplier<M> mapSupplier )
		{
			M newMap = mapSupplier.get();
			newMap.putAll( map );
			return newMap;
		}

		public <K, V> MapBuilder<K, V> put( K key, V value )
		{
			return new MapBuilder<>( map, key, value );
		}

		public <K, V> MapBuilder<K, V> putAll( Map<K, V> values )
		{
			return new MapBuilder<>( map, values );
		}

		public <V> MapBuilder<String, V> putAll( Properties properties )
		{
			return new MapBuilder<>( map, properties.stringPropertyNames().stream().collect( Collectors.toMap( s -> s, s -> ( V ) properties.getProperty( s ) ) ) );
		}

		public <V> MapBuilder<String, V> putAll( Properties properties, Class<V> vClass )
		{
			return new MapBuilder<>( map, properties.stringPropertyNames().stream().collect( Collectors.toMap( s -> s, s -> Objs.castTo( properties.getProperty( s ), vClass ) ) ) );
		}

		public <K, V> MapBuilder<K, V> putAll( Properties properties, Class<K> kClass, Class<V> vClass )
		{
			return new MapBuilder<>( map, properties.stringPropertyNames().stream().collect( Collectors.toMap( s -> Objs.castTo( s, kClass ), s -> Objs.castTo( properties.getProperty( s ), vClass ) ) ) );
		}

		private void putNotNull( CK key, CV value )
		{
			if ( !Objs.isNull( key ) && !Objs.isNull( value ) )
				map.put( key, value );
		}

		public Stream<Pair<CK, CV>> stream()
		{
			return map.entrySet().stream().map( e -> new Pair<>( e.getKey(), e.getValue() ) );
		}

		public void to( Map<CK, CV> surrogateMap )
		{
			surrogateMap.putAll( map );
		}

		public TreeMap<CK, CV> treeMap()
		{
			return map;
		}
	}
}
