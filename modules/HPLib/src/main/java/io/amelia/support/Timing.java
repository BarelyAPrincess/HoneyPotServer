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

import java.util.Map;
import java.util.WeakHashMap;

public class Timing
{
	/**
	 * Provides reference of context to start time.<br>
	 * We use a WeakHashMap to prevent a memory leak, in case {@link #finish(Object)} is never called and/or context was reclaimed by GC.
	 */
	private static final Map<Object, Long> timings = new WeakHashMap<>();

	/**
	 * Finds the total number of milliseconds it took and removes the context.
	 *
	 * @param context The context to reference the starting time.
	 * @return The time in milliseconds it took between calling {@link #start(Object)} and this method.<br>
	 * Returns {@code -1} if we have no record of ever starting.
	 */
	public static long finish( Object context )
	{
		Long start = timings.remove( context );

		if ( start == null )
			return -1;

		return System.currentTimeMillis() - start;
	}

	/**
	 * Finds the total number of milliseconds it took.
	 * Be sure to still call {@link #finish(Object)} as this method is only used for checkpoints.
	 *
	 * @param context The context to reference the starting time.
	 * @return The time in milliseconds it took between calling {@link #start(Object)} and this method.<br>
	 * Returns {@code -1} if we have no record of ever starting.
	 */
	public static long mark( Object context )
	{
		Long start = timings.get( context );

		if ( start == null )
			return -1;

		return System.currentTimeMillis() - start;
	}

	/**
	 * Starts the counter for the referenced context
	 *
	 * @param context The context to reference our start time with.
	 */
	public static void start( Object context )
	{
		timings.put( context, System.currentTimeMillis() );
	}
}
