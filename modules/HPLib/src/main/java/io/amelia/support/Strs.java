/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.support;

import com.sun.istack.internal.NotNull;
import io.amelia.foundation.Kernel;

import java.awt.Color;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Strs
{
	public static String capitalizeWords( String str )
	{
		return capitalizeWords( str, ' ' );
	}

	public static String capitalizeWords( String str, char delimiter )
	{
		if ( Objs.isEmpty( str ) )
			return str;

		Objs.notNull( delimiter );

		final char[] buffer = str.toCharArray();
		boolean capitalizeNext = true;
		for ( int i = 0; i < buffer.length; i++ )
		{
			final char ch = buffer[i];
			if ( ch == delimiter )
				capitalizeNext = true;
			else if ( capitalizeNext )
			{
				buffer[i] = Character.toTitleCase( ch );
				capitalizeNext = false;
			}
		}
		return new String( buffer );
	}

	public static String capitalizeWordsFully( String str )
	{
		return capitalizeWordsFully( str, ' ' );
	}

	public static String capitalizeWordsFully( String str, char delimiter )
	{
		if ( Objs.isEmpty( str ) )
			return str;

		return capitalizeWords( str.toLowerCase(), delimiter );
	}

	/**
	 * Returns true if either array shares ANY elements
	 */
	public static boolean comparable( Object[] arrayLeft, Object[] arrayRight )
	{
		for ( Object objLeft : arrayLeft )
			for ( Object objRight : arrayRight )
				if ( objLeft != null && objLeft.equals( objRight ) )
					return true;
		return false;
	}

	public static boolean containsValidChars( String ref )
	{
		return ref.matches( "[a-z0-9_]*" );
	}

	/**
	 * Copies all elements from the iterable collection of originals to the collection provided.
	 *
	 * @param token      String to search for
	 * @param originals  An iterable collection of strings to filter.
	 * @param collection The collection to add matches to
	 * @return the collection provided that would have the elements copied into
	 * @throws UnsupportedOperationException if the collection is immutable and originals contains a string which starts with the specified search
	 *                                       string.
	 * @throws IllegalArgumentException      if any parameter is is null
	 * @throws IllegalArgumentException      if originals contains a null element. <b>Note: the collection may be modified before this is thrown</b>
	 */
	public static <T extends Collection<String>> T copyPartialMatches( final String token, final Iterable<String> originals, final T collection ) throws UnsupportedOperationException, IllegalArgumentException
	{
		Objs.notNull( token, "Search token cannot be null" );
		Objs.notNull( collection, "Collection cannot be null" );
		Objs.notNull( originals, "Originals cannot be null" );

		for ( String string : originals )
			if ( startsWithIgnoreCase( string, token ) )
				collection.add( string );

		return collection;
	}

	public static int countInstances( String str, char chr )
	{
		int cnt = 0;
		for ( int i = 0; i < str.length(); i++ )
			if ( str.charAt( i ) == chr )
				cnt++;
		return cnt;
	}

	public static String escapeHtml( String str )
	{
		return EscapeTranslator.HTML_ESCAPE().translate( str );
	}

	public static String fixQuotes( String var )
	{
		try
		{
			var = var.replaceAll( "\\\\\"", "\"" );
			var = var.replaceAll( "\\\\'", "'" );

			if ( var.startsWith( "\"" ) || var.startsWith( "'" ) )
				var = var.substring( 1 );
			if ( var.endsWith( "\"" ) || var.endsWith( "'" ) )
				var = var.substring( 0, var.length() - 1 );
		}
		catch ( Exception ignore )
		{

		}

		return var;
	}

	public static boolean isCamelCase( String var )
	{
		Objs.notNull( var );
		return var.matches( "[a-z0-9]+(?:[A-Z]{1,2}[a-z0-9]+)*" );
	}

	public static boolean isCapitalizedWords( String str )
	{
		Objs.notNull( str );
		return str.equals( capitalizeWords( str ) );
	}

	/**
	 * Determines if a string is all lowercase using the toLowerCase() method.
	 *
	 * @param str The string to check
	 * @return Is it all lowercase?
	 */
	public static boolean isLowercase( String str )
	{
		return str.toLowerCase().equals( str );
	}

	/**
	 * Determines if a string is all uppercase using the toUpperCase() method.
	 *
	 * @param str The string to check
	 * @return Is it all uppercase?
	 */
	public static boolean isUppercase( String str )
	{
		return str.toUpperCase().equals( str );
	}

	public static String join( @NotNull Map<String, String> args, @NotNull String glue )
	{
		return join( args, glue, "=" );
	}

	public static String join( @NotNull Map<String, String> args )
	{
		return join( args, ", ", "=" );
	}

	public static String join( @NotNull Map<String, String> args, @NotNull String glue, @NotNull String keyValueSeparator )
	{
		return args.entrySet().stream().map( e -> e.getKey() + keyValueSeparator + e.getValue() ).collect( Collectors.joining( glue ) );
	}

	public static String join( @NotNull Collection<String> args )
	{
		return join( args, ", " );
	}

	public static String join( @NotNull Collection<String> args, @NotNull String glue )
	{
		return args.stream().collect( Collectors.joining( glue ) );
	}

	public static String join( @NotNull String[] args )
	{
		return join( args, ", " );
	}

	public static String join( @NotNull String[] args, @NotNull String glue )
	{
		return Arrays.stream( args ).collect( Collectors.joining( glue ) );
	}

	public static String lcfirst( String value )
	{
		return value.substring( 0, 1 ).toLowerCase() + value.substring( 1 );
	}

	public static String limitLength( String text, int max )
	{
		if ( text.length() <= max )
			return text;
		return text.substring( 0, max ) + "...";
	}

	public static Color parseColor( String color )
	{
		Pattern c = Pattern.compile( "rgb *\\( *([0-9]+), *([0-9]+), *([0-9]+) *\\)" );
		Matcher m = c.matcher( color );

		// First try to parse RGB(0,0,0);
		if ( m.matches() )
			return new Color( Integer.valueOf( m.group( 1 ) ), // r
					Integer.valueOf( m.group( 2 ) ), // g
					Integer.valueOf( m.group( 3 ) ) ); // b

		try
		{
			Field field = Class.forName( "java.awt.Color" ).getField( color.trim().toUpperCase() );
			return ( Color ) field.get( null );
		}
		catch ( Exception e )
		{
			// Ignore
		}

		try
		{
			return Color.decode( color );
		}
		catch ( Exception e )
		{
			// Ignore
		}

		return null;
	}

	public static Map<String, String> queryToMap( String query ) throws UnsupportedEncodingException
	{
		Map<String, String> result = new HashMap<>();

		if ( query == null )
			return result;

		for ( String param : query.split( "&" ) )
		{
			String[] pair = param.split( "=" );
			try
			{
				if ( pair.length > 1 )
					result.put( URLDecoder.decode( Strs.trimEnd( pair[0], '%' ), "ISO-8859-1" ), URLDecoder.decode( Strs.trimEnd( pair[1], '%' ), "ISO-8859-1" ) );
				else if ( pair.length == 1 )
					result.put( URLDecoder.decode( Strs.trimEnd( pair[0], '%' ), "ISO-8859-1" ), "" );
			}
			catch ( IllegalArgumentException e )
			{
				Kernel.L.warning( "Malformed URL exception was thrown, key: `" + pair[0] + "`, val: '" + pair[1] + "'" );
			}
		}
		return result;
	}

	public static String randomChars( String seed, int length )
	{
		Objs.notEmpty( seed );

		StringBuilder sb = new StringBuilder();

		for ( int i = 0; i < length; i++ )
			sb.append( seed.toCharArray()[new Random().nextInt( seed.length() )] );

		return sb.toString();
	}

	public static String regexCapture( String var, String regex )
	{
		return regexCapture( var, regex, 1 );
	}

	public static String regexCapture( String var, String regex, int group )
	{
		Pattern p = Pattern.compile( regex );
		Matcher m = p.matcher( var );

		if ( !m.find() )
			return null;

		return m.group( group );
	}

	public static String removeInvalidChars( String ref )
	{
		return ref.replaceAll( "[^a-zA-Z0-9!#$%&'*+-/=?^_`{|}~@\\. ]", "" );
	}

	public static String removeLetters( String input )
	{
		return input.replaceAll( "[a-zA-Z]", "" );
	}

	public static String removeLettersLower( String input )
	{
		return input.replaceAll( "[a-z]", "" );
	}

	public static String removeLettersUpper( String input )
	{
		return input.replaceAll( "[A-Z]", "" );
	}

	public static String removeNumbers( String input )
	{
		return input.replaceAll( "\\d", "" );
	}

	public static String removeSpecial( String input )
	{
		return input.replaceAll( "\\W", "" );
	}

	public static String removeWhitespace( String input )
	{
		return input.replaceAll( "\\s", "" );
	}

	public static String repeat( String string, int count )
	{
		Objs.notNull( string );

		if ( count <= 1 )
			return count == 0 ? "" : string;

		final int len = string.length();
		final long longSize = ( long ) len * ( long ) count;
		final int size = ( int ) longSize;
		if ( size != longSize )
			throw new ArrayIndexOutOfBoundsException( "Required array size too large: " + longSize );

		final char[] array = new char[size];
		string.getChars( 0, len, array, 0 );
		int n;
		for ( n = len; n < size - n; n <<= 1 )
			System.arraycopy( array, 0, array, n, n );
		System.arraycopy( array, 0, array, n, size - n );
		return new String( array );
	}

	public static List<String> repeatToList( String chr, int length )
	{
		List<String> list = Lists.newArrayList();
		for ( int i = 0; i < length; i++ )
			list.add( chr );
		return list;
	}

	public static String replaceAt( String par, int at, String rep )
	{
		StringBuilder sb = new StringBuilder( par );
		sb.setCharAt( at, rep.toCharArray()[0] );
		return sb.toString();
	}

	public static Stream<String> split( String str, @NotNull String delimiter, int limit )
	{
		if ( Objs.isEmpty( str ) )
			return Stream.empty();
		return Arrays.stream( str.split( delimiter, limit ) );
	}

	public static Stream<String> split( String str, @NotNull String delimiter )
	{
		if ( Objs.isEmpty( str ) )
			return Stream.empty();
		return Arrays.stream( str.split( delimiter ) );
	}

	public static Stream<String> split( String str, @NotNull Pattern delimiter )
	{
		if ( Objs.isEmpty( str ) )
			return Stream.empty();
		return Arrays.stream( delimiter.split( str ) );
	}

	/**
	 * This method uses a substring to check case-insensitive equality. This means the internal array does not need to be
	 * copied like a toLowerCase() call would.
	 *
	 * @param string   String to check
	 * @param prefixes Prefix of string to compare
	 * @return true if provided string starts with, ignoring case, the prefix provided
	 * @throws NullPointerException if prefix is null
	 */
	public static boolean startsWithIgnoreCase( @NotNull final String string, @NotNull final String... prefixes ) throws NullPointerException
	{
		Objs.notNull( string, "Cannot check a null string for a match" );
		for ( String prefix : prefixes )
		{
			if ( string.length() < prefix.length() )
				return false;
			if ( string.substring( 0, prefix.length() ).equalsIgnoreCase( prefix ) )
				return true;
		}
		return false;
	}

	public static byte[] stringToBytesASCII( String str )
	{
		byte[] b = new byte[str.length()];
		for ( int i = 0; i < b.length; i++ )
			b[i] = ( byte ) str.charAt( i );
		return b;
	}

	public static byte[] stringToBytesUTF( String str )
	{
		byte[] b = new byte[str.length() << 1];
		for ( int i = 0; i < str.length(); i++ )
		{
			char strChar = str.charAt( i );
			int bytePos = i << 1;
			b[bytePos] = ( byte ) ( ( strChar & 0xFF00 ) >> 8 );
			b[bytePos + 1] = ( byte ) ( strChar & 0x00FF );
		}
		return b;
	}

	/**
	 * Convert a value to camel case.
	 *
	 * @param value
	 * @return String
	 */
	public static String toCamelCase( String value )
	{
		return lcfirst( toStudlyCase( value ) );
	}

	/**
	 * Scans a string list for entries that are not lower case.
	 *
	 * @param strings The original list to check.
	 * @return Lowercase string array.
	 */
	public static List<String> toLowerCase( List<String> strings )
	{
		return strings.stream().filter( v -> !Objs.isNull( v ) ).map( String::toLowerCase ).collect( Collectors.toList() );
	}

	public static Set<String> toLowerCase( Set<String> strings )
	{
		return strings.stream().filter( v -> !Objs.isNull( v ) ).map( String::toLowerCase ).collect( Collectors.toSet() );
	}

	public static String[] toLowerCase( String[] strings )
	{
		return Arrays.stream( strings ).filter( v -> !Objs.isNull( v ) ).map( String::toLowerCase ).toArray( String[]::new );
	}

	/**
	 * Convert a value to studly caps case.
	 *
	 * @param value
	 * @return String
	 */
	public static String toStudlyCase( String value )
	{
		return Strs.capitalizeWordsFully( value.replaceAll( "-_", " " ) ).replaceAll( " ", "" );
	}

	/**
	 * Trim specified character from both ends of a String
	 *
	 * @param text      Text
	 * @param character Character to remove
	 * @return Trimmed text
	 */
	public static String trimAll( String text, char character )
	{
		String normalizedText = trimFront( text, character );

		return trimEnd( normalizedText, character );
	}

	/**
	 * Trim specified character from end of string
	 *
	 * @param text      Text
	 * @param character Character to remove
	 * @return Trimmed text
	 */
	public static String trimEnd( String text, char character )
	{
		String normalizedText;
		int index;

		if ( text == null || text.isEmpty() )
			return text;

		normalizedText = text.trim();
		index = normalizedText.length() - 1;

		while ( normalizedText.charAt( index ) == character )
			if ( --index < 0 )
				return "";
		return normalizedText.substring( 0, index + 1 ).trim();
	}

	/**
	 * Trim specified character from front of string
	 *
	 * @param text      Text
	 * @param character Character to remove
	 * @return Trimmed text
	 */
	public static String trimFront( String text, char character )
	{
		String normalizedText;
		int index;

		if ( text == null || text.isEmpty() )
			return text;

		normalizedText = text.trim();
		index = -1;

		do
			index++;
		while ( index < normalizedText.length() && normalizedText.charAt( index ) == character );

		return normalizedText.substring( index ).trim();
	}

	public static String unescapeHtml( String str )
	{
		return EscapeTranslator.HTML_UNESCAPE().translate( str );
	}

	public static Map<String, String> wrap( Map<String, String> map )
	{
		return wrap( map, '`', '\'' );
	}

	public static Collection<String> wrap( Collection<String> col )
	{
		return wrap( col, '`' );
	}

	public static Collection<String> wrap( final Collection<String> col, char wrap )
	{
		synchronized ( col )
		{
			String[] strings = col.toArray( new String[0] );
			col.clear();
			for ( int i = 0; i < strings.length; i++ )
				col.add( wrap( strings[i], wrap ) );
		}

		return col;
	}

	public static List<String> wrap( List<String> list )
	{
		return wrap( list, '`' );
	}

	public static List<String> wrap( final List<String> list, char wrap )
	{
		List<String> newList;
		if ( list instanceof ArrayList )
			newList = new ArrayList<>();
		else if ( list instanceof CopyOnWriteArrayList )
			newList = new CopyOnWriteArrayList<>();
		else if ( list instanceof LinkedList )
			newList = new LinkedList<>();
		else
			newList = new LinkedList<>();

		for ( String str : list )
			newList.add( wrap( str, wrap ) );

		return newList;
	}

	public static Map<String, String> wrap( final Map<String, String> map, char keyWrap, char valueWrap )
	{
		Map<String, String> newMap;
		if ( map instanceof HashMap )
			newMap = new HashMap<>();
		if ( map instanceof ConcurrentMap )
			newMap = new ConcurrentHashMap<>();
		if ( map instanceof IdentityHashMap )
			newMap = new IdentityHashMap<>();
		if ( map instanceof LinkedHashMap )
			newMap = new LinkedHashMap<>();
		if ( map instanceof TreeMap )
			newMap = new TreeMap<>();
		else
			newMap = new LinkedHashMap<>();

		for ( Map.Entry<String, String> e : map.entrySet() )
			if ( e.getKey() != null && !e.getKey().isEmpty() )
				newMap.put( keyWrap + e.getKey() + keyWrap, valueWrap + ( e.getValue() == null ? "" : e.getValue() ) + valueWrap );

		return newMap;
	}

	public static Set<String> wrap( Set<String> set )
	{
		return wrap( set, '`' );
	}

	public static Set<String> wrap( final Set<String> set, char wrap )
	{
		Set<String> newSet;
		if ( set instanceof HashSet )
			newSet = new HashSet<>();
		else if ( set instanceof TreeSet )
			newSet = new TreeSet<>();
		else if ( set instanceof LinkedHashSet )
			newSet = new LinkedHashSet<>();
		else if ( set instanceof CopyOnWriteArraySet )
			newSet = new CopyOnWriteArraySet<>();
		else if ( set.getClass() == new HashMap<>().keySet().getClass() ) // Really nasty way of comparing it to a private class
			newSet = new LinkedHashSet<>();
		else
			newSet = new LinkedHashSet<>();

		for ( String str : set )
			newSet.add( wrap( str, wrap ) );

		return newSet;
	}

	public static String wrap( String str, char wrap )
	{
		return String.format( "%s%s%s", wrap, str, wrap );
	}

	private Strs()
	{

	}
}
