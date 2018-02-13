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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * WORK IN PROGRESS!!!
 *
 * Do not use, API will likely change.
 */
public class Reflection
{
	private static final ThreadLocal<List<StackTraceElement>> methodCallEnforcements = new ThreadLocal<>();

	public static Method getMethod( Class<?> aClass, String methodName )
	{
		try
		{
			return aClass.getDeclaredMethod( methodName );
		}
		catch ( NoSuchMethodException e )
		{
			return null;
		}
	}

	public static Method getMethod( String aClass, String methodName )
	{
		try
		{
			return Class.forName( aClass ).getDeclaredMethod( methodName );
		}
		catch ( ClassNotFoundException | NoSuchMethodException e )
		{
			return null;
		}
	}

	public static boolean hasAnnotation( Class<?> classToCheck, Class<? extends Annotation> annotation )
	{
		return classToCheck.getAnnotation( annotation ) != null;
	}

	/**
	 * Used to signal method calls and record a max count of 10.
	 *
	 * @param enclosingMethod
	 */
	public static void methodCall( Method enclosingMethod )
	{
		Objs.notNull( enclosingMethod );

		List<StackTraceElement> callHistory = methodCallEnforcements.get();

		if ( callHistory == null )
		{
			callHistory = new ArrayList<>();
			methodCallEnforcements.set( callHistory );
		}

		if ( callHistory.size() > 10 )
		{
			callHistory = Lists.subList( callHistory, 0, 9 );
			methodCallEnforcements.set( callHistory );
		}

		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		if ( stackTrace == null || stackTrace.length < 3 )
			throw new IllegalStateException( "Could not retrieve the Thread StackTrace." );

		callHistory.add( stackTrace[2] );
	}

	public static void wasSuperCalled( Method method )
	{
		wasSuperCalled( method, 1 );
	}

	public static void wasSuperCalled( Method method, int maxDepth )
	{
		Objs.notNegative( maxDepth );
		Objs.notZero( maxDepth );

		List<StackTraceElement> callHistory = methodCallEnforcements.get();

		if ( callHistory == null )
			return;

		// callHistory.stream().limit( maxDepth ).forEach( s -> s. );
	}

	public static void wasSuperCalledAny( Method method )
	{
		wasSuperCalled( method, 999 );
	}

	private Reflection()
	{

	}
}
