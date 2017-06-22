/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.helpers;

import com.chiorichan.helpers.Pair;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sun.istack.internal.NotNull;
import io.amelia.lang.StartupException;
import io.amelia.lang.UncaughtException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Objs
{
	private Objs()
	{

	}

	@SuppressWarnings( "unchecked" )
	public static <O> O castTo( Object obj, Class<O> clz )
	{
		try
		{
			if ( clz == Integer.class )
				return ( O ) castToIntWithException( obj );
			if ( clz == Long.class )
				return ( O ) castToLongWithException( obj );
			if ( clz == Double.class )
				return ( O ) castToDoubleWithException( obj );
			if ( clz == Boolean.class )
				return ( O ) castToBoolWithException( obj );
			if ( clz == String.class )
				return ( O ) castToStringWithException( obj );
		}
		catch ( Exception e1 )
		{
			try
			{
				return ( O ) obj;
			}
			catch ( Exception e2 )
			{
				try
				{
					return ( O ) castToStringWithException( obj );
				}
				catch ( Exception e3 )
				{
					try
					{
						/*
						 * Last and final attempt to get something out of this
						 * object even if it results in the toString() method.
						 */
						return ( O ) ( "" + obj );
					}
					catch ( Exception e4 )
					{
						// Ignore
					}
				}
			}
		}

		return null;
	}

	public static Boolean castToBool( Object value )
	{
		return castToBool( value, false );
	}

	public static Boolean castToBool( Object value, Boolean def )
	{
		try
		{
			return castToBoolWithException( value );
		}
		catch ( Exception e )
		{
			return def;
		}
	}

	public static Boolean castToBoolWithException( Object value ) throws ClassCastException
	{
		if ( value == null )
			throw new ClassCastException( "Can't cast `null` to Boolean" );

		if ( value.getClass() == boolean.class || value.getClass() == Boolean.class )
			return ( boolean ) value;

		String val = castToStringWithException( value );

		if ( val == null )
			throw new ClassCastException( "Uncaught Convertion to Boolean of Type: " + value.getClass().getName() );

		switch ( val.trim().toLowerCase() )
		{
			case "yes":
				return true;
			case "no":
				return false;
			case "true":
				return true;
			case "false":
				return false;
			case "1":
				return true;
			case "0":
				return false;
			default:
				throw new ClassCastException( "Uncaught Convertion to Boolean of Type: " + value.getClass().getName() );
		}
	}

	public static Double castToDouble( Object value )
	{
		return castToDouble( value, 0D );
	}

	public static Double castToDouble( Object value, Double def )
	{
		try
		{
			return castToDoubleWithException( value );
		}
		catch ( Exception e )
		{
			return def;
		}
	}

	public static Double castToDoubleWithException( Object value )
	{
		if ( value == null )
			throw new ClassCastException( "Can't cast `null` to Double" );

		if ( value instanceof Long )
			return ( ( Long ) value ).doubleValue();
		if ( value instanceof String )
			return Double.parseDouble( ( String ) value );
		if ( value instanceof Integer )
			return ( ( Integer ) value ).doubleValue();
		if ( value instanceof Double )
			return ( Double ) value;
		if ( value instanceof Boolean )
			return ( boolean ) value ? 1D : 0D;
		if ( value instanceof BigDecimal )
			return ( ( BigDecimal ) value ).setScale( 0, BigDecimal.ROUND_HALF_UP ).doubleValue();

		throw new ClassCastException( "Uncaught Convertion to Integer of Type: " + value.getClass().getName() );
	}

	public static Integer castToInt( Object value )
	{
		return castToInt( value, -1 );
	}

	public static Integer castToInt( Object value, Integer def )
	{
		try
		{
			return castToIntWithException( value );
		}
		catch ( Exception e )
		{
			return def;
		}
	}

	public static Integer castToIntWithException( Object value )
	{
		if ( value == null )
			throw new ClassCastException( "Can't cast `null` to Integer" );

		if ( value instanceof Long )
			if ( ( long ) value < Integer.MIN_VALUE || ( long ) value > Integer.MAX_VALUE )
				return ( Integer ) value;
			else
				return null;
		if ( value instanceof String )
			return Integer.parseInt( ( String ) value );
		if ( value instanceof Integer )
			return ( Integer ) value;
		if ( value instanceof Double )
			return ( Integer ) value;
		if ( value instanceof Boolean )
			return ( boolean ) value ? 1 : 0;
		if ( value instanceof BigDecimal )
			return ( ( BigDecimal ) value ).setScale( 0, BigDecimal.ROUND_HALF_UP ).intValue();

		throw new ClassCastException( "Uncaught Conversion to Integer of Type: " + value.getClass().getName() );
	}

	public static Long castToLong( Object value )
	{
		return castToLong( value, 0L );
	}

	public static Long castToLong( Object value, Long def )
	{
		try
		{
			return castToLongWithException( value );
		}
		catch ( ClassCastException e )
		{
			return def;
		}
	}

	public static Long castToLongWithException( Object value )
	{
		if ( value == null )
			throw new ClassCastException( "Can't cast `null` to Long" );

		if ( value instanceof Long )
			return ( Long ) value;
		if ( value instanceof String )
			return Long.parseLong( ( String ) value );
		if ( value instanceof Integer )
			return Long.parseLong( "" + value );
		if ( value instanceof Double )
			return Long.parseLong( "" + value );
		if ( value instanceof Boolean )
			return ( boolean ) value ? 1L : 0L;
		if ( value instanceof BigDecimal )
			return ( ( BigDecimal ) value ).setScale( 0, BigDecimal.ROUND_HALF_UP ).longValue();

		throw new ClassCastException( "Uncaught Convertion to Long of Type: " + value.getClass().getName() );
	}

	public static String castToString( Object value )
	{
		return castToString( value, null );
	}

	public static String castToString( Object value, String def )
	{
		try
		{
			return castToStringWithException( value );
		}
		catch ( ClassCastException e )
		{
			return def;
		}
	}

	@SuppressWarnings( "rawtypes" )
	public static String castToStringWithException( final Object value ) throws ClassCastException
	{
		if ( value == null )
			return null;
		if ( value instanceof Long )
			return Long.toString( ( long ) value );
		if ( value instanceof String )
			return ( String ) value;
		if ( value instanceof Integer )
			return Integer.toString( ( int ) value );
		if ( value instanceof Double )
			return Double.toString( ( double ) value );
		if ( value instanceof Boolean )
			return ( boolean ) value ? "true" : "false";
		if ( value instanceof BigDecimal )
			return value.toString();
		if ( value instanceof Map )
			return ( ( Map<?, ?> ) value ).entrySet().stream().map( e -> castToString( e.getKey() ) + "=\"" + castToString( e.getValue() ) + "\"" ).collect( Collectors.joining( "," ) );
		if ( value instanceof List )
			return ( ( List<?> ) value ).stream().map( Objs::castToString ).collect( Collectors.joining( "," ) );
		throw new ClassCastException( "Uncaught Convertion to String of Type: " + value.getClass().getName() );
	}

	public static <T> T initClass( @NotNull Class<T> clz, Object... args ) throws UncaughtException
	{
		try
		{
			Class<?>[] argClasses = Arrays.stream( args ).map( Object::getClass ).toArray( Class[]::new );

			// Constructor<T> constructor = clz.getConstructor();
			Constructor<T> constructor = clz.getDeclaredConstructor( argClasses );
			constructor.setAccessible( true );
			return constructor.newInstance( args );
		}
		catch ( InvocationTargetException e )
		{
			if ( e.getTargetException() instanceof UncaughtException )
				throw ( UncaughtException ) e.getTargetException();
			else
				throw new StartupException( String.format( "Failed to initialize a new instance of %s, because it has thrown an exception.", clz.getSimpleName() ), e.getTargetException() );
		}
		catch ( NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException e )
		{
			String argClasses = Arrays.stream( args ).map( o -> o.getClass().getSimpleName() ).collect( Collectors.joining( ", " ) );
			if ( argClasses.length() == 0 )
				argClasses = "None";
			throw new StartupException( String.format( "Failed to initialize a new instance of %s, does the class have a constructor to match arguments '%s'?", clz.getSimpleName(), argClasses ), e );
		}
	}

	public static <T> void notFalse( T bool )
	{
		notFalse( bool, "Object is false" );
	}

	public static <T> void notFalse( T bool, String message, String... objects )
	{
		if ( !castToBool( bool ) )
			throw new IllegalArgumentException( objects == null || objects.length == 0 ? message : String.format( message, ( Object[] ) objects ) );
	}

	public static <T> boolean isTrue( T bool )
	{
		try
		{
			notFalse( bool );
			return true;
		}
		catch ( IllegalArgumentException e )
		{
			return false;
		}
	}

	public static boolean stackTraceAntiLoop( Class<?> cls, String method )
	{
		return stackTraceAntiLoop( cls.getCanonicalName(), method, 1 );
	}

	public static boolean stackTraceAntiLoop( Class<?> cls, String method, int max )
	{
		return stackTraceAntiLoop( cls.getCanonicalName(), method, max );
	}

	public static boolean stackTraceAntiLoop( String cls, String method )
	{
		return stackTraceAntiLoop( cls, method, 1 );
	}

	/**
	 * Detects if the specified class and method has been called in a previous stack trace event.
	 *
	 * @param cls    The class to check.
	 * @param method The method to check, null to ignore.
	 * @param max    The maximum number of recurrence until failure.
	 * @return True if no loop was detected.
	 */
	public static boolean stackTraceAntiLoop( String cls, String method, int max )
	{
		int cnt = 0;
		for ( StackTraceElement ste : Thread.currentThread().getStackTrace() )
			if ( ste.getClassName().equals( cls ) && ( method == null || ste.getMethodName().equals( method ) ) )
			{
				cnt++;
				if ( cnt >= max )
					return false;
			}
		return true;
	}

	public static <T extends CharSequence> T notEmpty( final T chars, final String message, final Object... values )
	{
		if ( chars == null )
			throw new NullPointerException( String.format( message, values ) );
		if ( chars.length() == 0 )
			throw new IllegalArgumentException( String.format( message, values ) );
		return chars;
	}

	public static <T> void notNull( final T object )
	{
		notNull( object, "Object is null" );
	}

	public static <T> void notNull( final T object, String message, Object... values )
	{
		if ( object == null )
			throw new NullPointerException( values == null || values.length == 0 ? message : String.format( message, values ) );
	}

	public static int safeLongToInt( long l )
	{
		if ( l < Integer.MIN_VALUE )
			return Integer.MIN_VALUE;
		if ( l > Integer.MAX_VALUE )
			return Integer.MAX_VALUE;
		return ( int ) l;
	}

	@SuppressWarnings( {"unchecked"} )
	public static <T> boolean instanceOf( Object obj, Class<T> castClass )
	{
		try
		{
			T testCast = ( T ) obj;
			return testCast != null && testCast.getClass().isInstance( castClass );
		}
		catch ( ClassCastException e )
		{
			return false;
		}
	}

	public static <T> T notEmpty( final T obj )
	{
		return notEmpty( obj, "Object is empty" );
	}

	public static <T> T notEmpty( final T obj, final String message, final Object... values )
	{
		if ( obj == null )
			throw new NullPointerException( String.format( message, values ) );

		Method methodIsEmpty = getMethodSafe( obj, "isEmpty" );
		Method methodLength = getMethodSafe( obj, "length" );
		Method methodSize = getMethodSafe( obj, "size" );

		if ( methodIsEmpty != null && invokeMethodSafe( methodIsEmpty, obj, false ) )
			throw new IllegalArgumentException( String.format( message, values ) );

		if ( methodLength != null && invokeMethodSafe( methodLength, obj, -1 ) == 0 )
			throw new IllegalArgumentException( String.format( message, values ) );

		if ( methodSize != null && invokeMethodSafe( methodSize, obj, -1 ) == 0 )
			throw new IllegalArgumentException( String.format( message, values ) );

		return obj;
	}

	public static int length( Object obj )
	{
		if ( obj == null )
			return 0;

		try
		{
			return castToStringWithException( obj ).length();
		}
		catch ( ClassCastException e )
		{
			// Ignore
		}

		Method methodIsEmpty = getMethodSafe( obj, "isEmpty" );
		Method methodLength = getMethodSafe( obj, "length" );
		Method methodSize = getMethodSafe( obj, "size" );

		if ( methodIsEmpty != null && invokeMethodSafe( methodIsEmpty, obj, false ) )
			return 0;

		if ( methodLength != null )
			return invokeMethodSafe( methodLength, obj, -1 );

		if ( methodSize != null )
			return invokeMethodSafe( methodSize, obj, -1 );

		return -1;
	}

	private static <T> T invokeMethodSafe( Method method, Object obj, T rtn, Object... args )
	{
		try
		{
			return ( T ) method.invoke( obj, args );
		}
		catch ( IllegalAccessException | InvocationTargetException e )
		{
			return rtn;
		}
	}

	private static <T> Method getMethodSafe( T obj, String methodName )
	{
		try
		{
			return obj.getClass().getMethod( methodName );
		}
		catch ( NoSuchMethodException e )
		{
			return null;
		}
	}

	public static <T> boolean isNull( T obj )
	{
		try
		{
			notNull( obj );
			return false;
		}
		catch ( Throwable t )
		{
			return true;
		}
	}

	public static <T> boolean isEmpty( T obj )
	{
		try
		{
			notEmpty( obj );
			return false;
		}
		catch ( Throwable t )
		{
			return true;
		}
	}

	public static <V> Collection<V> castCollection( Collection<?> col, Class<V> clz )
	{
		Collection<V> newCol = Lists.newLinkedList();

		for ( Object e : col )
		{
			V v = Objs.castTo( e, clz );

			if ( v != null )
				newCol.add( v );
		}

		return newCol;
	}

	public static <K, V> Map<K, V> castMap( Map<?, ?> map, Class<K> keyClz, Class<V> valClz )
	{
		Objs.notNull( map );

		Map<K, V> newMap = Maps.newLinkedHashMap();

		for ( Map.Entry<?, ?> e : map.entrySet() )
		{
			K k = Objs.castTo( e.getKey(), keyClz );
			V v = Objs.castTo( e.getValue(), valClz );

			if ( k != null && v != null )
				newMap.put( k, v );
		}

		return newMap;
	}

	public static boolean containsKeys( Map<String, ?> origMap, Collection<String> keys )
	{
		for ( String key : keys )
			if ( origMap.containsKey( key ) )
				return true;
		return false;
	}

	public static <K, V> Map<K, V> castToMap( Object map, Class<K> keyClz, Class<V> valClz )
	{
		if ( map instanceof Map )
			return castMap( ( Map<?, ?> ) map, keyClz, valClz );

		AtomicInteger i = new AtomicInteger();

		if ( map instanceof Collection )
			return ( ( Collection<?> ) map ).stream().map( e -> e == null ? "(null)" : e ).map( e -> new Pair<>( castTo( i.incrementAndGet(), keyClz ), castTo( e, valClz ) ) ).collect( Collectors.toMap( Pair::getKey, Pair::getValue ) );

		return Stream.of( castTo( map, valClz ) ).map( e -> e == null ? "(null)" : e ).map( e -> new Pair<>( castTo( i.incrementAndGet(), keyClz ), castTo( e, valClz ) ) ).collect( Collectors.toMap( Pair::getKey, Pair::getValue ) );
	}
}
