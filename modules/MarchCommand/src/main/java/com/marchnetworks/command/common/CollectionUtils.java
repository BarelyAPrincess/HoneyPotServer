package com.marchnetworks.command.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class CollectionUtils
{
	public static <T> List<List<T>> split( List<T> list, int subLength )
	{
		List<List<T>> parts = new ArrayList();

		if ( subLength > 0 )
		{
			int length = list.size();

			for ( int i = 0; i < length; i += subLength )
			{
				parts.add( list.subList( i, Math.min( length, i + subLength ) ) );
			}
		}

		return parts;
	}

	public static <T> List<T> difference( List<T> first, List<T> second )
	{
		List<T> diff = new ArrayList( first );
		diff.removeAll( second );

		return diff;
	}

	public static <T> Set<T> difference( Set<T> first, Set<T> second )
	{
		Set<T> diff = new LinkedHashSet( first );
		diff.removeAll( second );
		return diff;
	}

	public static <T> boolean sortAndCompareArrays( T[] firstArray, T[] secondArray )
	{
		if ( ( firstArray == null ) || ( secondArray == null ) )
		{
			return false;
		}
		Arrays.sort( firstArray );
		Arrays.sort( secondArray );
		return Arrays.equals( firstArray, secondArray );
	}

	public static <T> void sortList( List<T> list, final String propertyName )
	{
		if ( list.size() > 0 )
		{
			Collections.sort( list, new Comparator<T>()
			{
				@Override
				public int compare( T object1, T object2 )
				{
					String property1 = ( String ) ReflectionUtils.getSpecifiedFieldValue( propertyName, object1 );
					String property2 = ( String ) ReflectionUtils.getSpecifiedFieldValue( propertyName, object2 );
					return property1.compareToIgnoreCase( property2 );
				}
			} );
		}
	}

	public static <T> String getStringFromList( List<T> list, String propertyName )
	{
		String result = "";
		for ( int i = 0; i < list.size(); i++ )
		{
			String property = ( String ) ReflectionUtils.getSpecifiedFieldValue( propertyName, list.get( i ) );
			if ( !CommonAppUtils.isNullOrEmptyString( property ) )
			{

				result = result + property;
				if ( i < list.size() - 1 )
					result = result + ",";
			}
		}
		return result;
	}

	public static <T> T[] getDifferenceOfArrays( T[] firstArray, T[] secondArray )
	{
		if ( firstArray == null )
		{
			return secondArray;
		}
		if ( secondArray == null )
		{
			return firstArray;
		}

		List<T> diffElements = new ArrayList();
		Arrays.sort( firstArray );
		for ( T element : secondArray )
		{
			if ( Arrays.binarySearch( firstArray, element ) < 0 )
			{
				diffElements.add( element );
			}
		}

		return diffElements.toArray( Arrays.copyOf( firstArray, 0 ) );
	}

	public static <T> T[] getIntersectionOfArrays( T[] firstArray, T[] secondArray )
	{
		if ( ( firstArray == null ) || ( secondArray == null ) )
		{
			return null;
		}

		List<T> commonElements = new ArrayList();
		Arrays.sort( firstArray );
		for ( T element : secondArray )
		{
			int i = Arrays.binarySearch( firstArray, element );
			if ( i >= 0 )
			{
				commonElements.add( firstArray[i] );
			}
		}
		return commonElements.toArray( Arrays.copyOf( firstArray, 0 ) );
	}

	public static <T> boolean containsDuplicates( T[] array )
	{
		Set<T> set = new HashSet();
		for ( T element : array )
		{
			if ( set.contains( element ) )
			{
				return true;
			}
			set.add( element );
		}
		return false;
	}

	public static byte[] concatenate( byte[] array1, byte[] array2 )
	{
		int array1Length = array1.length;
		int array2Length = array2.length;

		byte[] result = new byte[array1Length + array2Length];
		System.arraycopy( array1, 0, result, 0, array1Length );
		System.arraycopy( array2, 0, result, array1Length, array2Length );

		return result;
	}

	public static <T> T[] concatenate( T[] array1, T[] array2 )
	{
		if ( ( array1 == null ) && ( array2 != null ) )
			return array2;
		if ( ( array1 != null ) && ( array2 == null ) )
			return array1;
		if ( ( array1 == null ) && ( array2 == null ) )
		{
			return null;
		}
		T[] result = Arrays.copyOf( array1, array1.length + array2.length );
		System.arraycopy( array2, 0, result, array1.length, array2.length );
		return result;
	}

	public static List<String> stringToList( String commaSeparated )
	{
		List<String> items = Arrays.asList( commaSeparated.split( "\\s*,\\s*" ) );
		return items;
	}

	public static Set<Long> convertStringToLongSet( Set<String> collection )
	{
		Set<Long> converted = new HashSet( collection.size() );
		for ( String value : collection )
		{
			converted.add( Long.valueOf( Long.parseLong( value ) ) );
		}
		return converted;
	}

	public static <T> Set<String> convertToStringSet( Set<T> collection )
	{
		Set<String> converted = new HashSet( collection.size() );
		for ( T value : collection )
		{
			converted.add( String.valueOf( value ) );
		}
		return converted;
	}

	public static boolean containsIgnoreCase( List<String> list, String s )
	{
		for ( String item : list )
		{
			if ( ( item == null ) && ( s == null ) )
			{
				return true;
			}
			if ( ( item != null ) && ( item.equalsIgnoreCase( s ) ) )
			{
				return true;
			}
		}
		return false;
	}

	public static <T> boolean contains( T[] array, T value )
	{
		for ( T item : array )
		{
			if ( item.equals( value ) )
			{
				return true;
			}
		}
		return false;
	}

	public static <T> void addAll( Collection<T> collection, T[] array )
	{
		for ( T item : array )
		{
			collection.add( item );
		}
	}

	public static <T> String collectionToString( Collection<T> list, String delimiter )
	{
		if ( ( list == null ) || ( list.isEmpty() ) )
		{
			return "";
		}
		StringBuilder sb = new StringBuilder();

		String loopDelimiter = "";

		for ( T s : list )
		{
			sb.append( loopDelimiter );
			sb.append( s.toString() );

			loopDelimiter = delimiter;
		}

		return sb.toString();
	}

	public static <T> String arrayToString( T[] array, String delimiter, boolean enclose )
	{
		if ( array == null )
		{
			return "";
		}
		StringBuilder result = new StringBuilder();
		for ( int i = 0; i < array.length; i++ )
		{
			result.append( array[i] );
			if ( i < array.length - 1 )
			{
				result.append( delimiter );
			}
		}
		if ( enclose )
		{
			result.insert( 0, "[" );
			result.append( "]" );
		}
		return result.toString();
	}

	public static <T, S> String mapToString( Map<T, S> map )
	{
		if ( map == null )
		{
			return "";
		}
		return map.toString();
	}

	public static <T, S> List<String> mapToStringList( Map<T, S> map, int itemsPerLine )
	{
		if ( map == null )
		{
			return Collections.emptyList();
		}

		List<String> result = new ArrayList();

		StringBuilder s = new StringBuilder();
		int i = 0;
		Set<Entry<T, S>> set = map.entrySet();
		for ( Entry<T, S> entry : set )
		{
			s.append( entry.getKey() ).append( "=" ).append( entry.getValue() );
			if ( i < set.size() - 1 )
			{
				s.append( ", " );
			}
			i++;
			if ( ( itemsPerLine > 0 ) && ( i % itemsPerLine == 0 ) )
			{
				result.add( s.toString() );
				s = new StringBuilder();
			}
		}
		if ( s.length() > 0 )
		{
			result.add( s.toString() );
		}
		return result;
	}

	public static <T> List<T> intersectLists( List<T> list1, List<T> list2 )
	{
		if ( ( list1 == null ) && ( list2 == null ) )
		{
			return null;
		}
		if ( list1 == null )
		{
			return list2;
		}
		if ( list2 == null )
		{
			return list1;
		}
		list1.retainAll( list2 );
		return list1;
	}

	public static boolean containsAny( String string, Collection<String> values )
	{
		if ( CommonAppUtils.isNullOrEmptyString( string ) )
		{
			return false;
		}

		for ( String value : values )
		{
			if ( string.contains( value ) )
			{
				return true;
			}
		}
		return false;
	}

	public static <T> void incrementMapLongValue( T key, Map<T, Long> map )
	{
		Long value = ( Long ) map.get( key );
		if ( value == null )
		{
			value = Long.valueOf( 0L );
		}
		Long localLong1 = value;
		Long localLong2 = value = Long.valueOf( value.longValue() + 1L );
		map.put( key, value );
	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue( Map<K, V> map, int topValues, final boolean descending )
	{
		List<Entry<K, V>> list = new ArrayList<Entry<K, V>>( map.entrySet() );

		Collections.sort( list, new Comparator<Entry<K, V>>()
		{
			@Override
			public int compare( Entry<K, V> o1, Entry<K, V> o2 )
			{
				if ( descending )
					return o2.getValue().compareTo( o1.getValue() );
				return o1.getValue().compareTo( o2.getValue() );
			}
		} );

		int size = map.size();

		if ( ( topValues != 0 ) && ( topValues < size ) )
			size = topValues;

		Map<K, V> result = new LinkedHashMap<K, V>( size );

		for ( int i = 0; i < size; i++ )

		{
			Entry<K, V> entry = list.get( i );
			result.put( entry.getKey(), entry.getValue() );
		}
		return result;
	}

	public static <T> Set<T> exclusiveOR( Collection<T> listA, Collection<T> listB )
	{
		if ( ( listA == null ) || ( listA.isEmpty() ) )
		{
			return new HashSet( listB );
		}

		if ( ( listB == null ) || ( listB.isEmpty() ) )
		{
			return new HashSet( listA );
		}

		Set<T> results = new HashSet();
		for ( T item : listA )
		{
			if ( !listB.contains( item ) )
			{
				results.add( item );
			}
		}
		for ( T item : listB )
		{
			if ( !listA.contains( item ) )
			{
				results.add( item );
			}
		}
		return results;
	}

	public static <T> T getNextFromSet( Set<T> set )
	{
		if ( set == null )
		{
			return null;
		}
		Iterator<T> iterator = set.iterator();
		if ( iterator.hasNext() )
		{
			return ( T ) set.iterator().next();
		}
		return null;
	}

	public static <T> Set<T> mergeSets( Set<T> set1, Set<T> set2 )
	{
		if ( set1 == null )
			return set2;
		if ( set2 == null )
		{
			return set1;
		}
		Set<T> result = new HashSet( set1 );
		result.addAll( set2 );
		return result;
	}
}
