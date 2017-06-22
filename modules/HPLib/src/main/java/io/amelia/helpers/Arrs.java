package io.amelia.helpers;

import com.sun.istack.internal.NotNull;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Manipulates arrays typically using Java 8 Streams
 */
@SuppressWarnings( "unchecked" )
public class Arrs
{
	public static <T> T[] append( @NotNull T[] arr, T first )
	{
		return ( T[] ) Stream.concat( Arrays.stream( arr ), Stream.of( first ) ).toArray();
	}

	public static <T> T[] array( T... objs )
	{
		return objs;
	}

	public static <T> T[] limit( @NotNull T[] arr, int limit )
	{
		return ( T[] ) Arrays.stream( arr ).limit( limit ).toArray();
	}

	public static <T> T[] merge( T[]... arrs )
	{
		return ( T[] ) Arrays.stream( arrs ).flatMap( Arrays::stream ).toArray();
	}

	public static <T> T[] pop( @NotNull T[] arr )
	{
		return trimEnd( arr, 1 );
	}

	public static <T> T[] prepend( @NotNull T[] arr, T first )
	{
		return ( T[] ) Stream.concat( Stream.of( first ), Arrays.stream( arr ) ).toArray();
	}

	public static <T> T[] push( @NotNull T[] arr, @NotNull T obj )
	{
		return ( T[] ) ( obj == null ? arr : Stream.concat( Arrays.stream( arr ), Stream.of( obj ) ).toArray() );
	}

	public static <T> T[] skip( @NotNull T[] arr, int skip )
	{
		return ( T[] ) Arrays.stream( arr ).skip( skip ).toArray();
	}

	public static <T> T[] trim( @NotNull T[] arr, int start, int end )
	{
		return arr.length == 0 ? arr : ( T[] ) Arrays.stream( arr ).limit( arr.length - end ).skip( start ).toArray();
	}

	public static <T> T[] trimEnd( @NotNull T[] arr, int inx )
	{
		return arr.length == 0 ? arr : ( T[] ) Arrays.stream( arr ).limit( arr.length - inx ).toArray();
	}

	public static <T> T[] trimStart( @NotNull T[] arr, int inx )
	{
		return arr.length == 0 ? arr : ( T[] ) Arrays.stream( arr ).skip( inx ).toArray();
	}

	private Arrs()
	{

	}
}
