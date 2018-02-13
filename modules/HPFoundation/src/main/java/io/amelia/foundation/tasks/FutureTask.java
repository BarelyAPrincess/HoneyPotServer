/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation.tasks;

import io.amelia.foundation.RegistrarBase;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class FutureTask<T> extends Task implements Future<T>
{
	private final Callable<T> callable;
	private Exception exception = null;
	private T value;

	FutureTask( final Callable<T> callable, final RegistrarBase creator, final int id )
	{
		super( creator, null, id, -1L );
		this.callable = callable;
	}

	@Override
	public synchronized boolean cancel( final boolean mayInterruptIfRunning )
	{
		if ( getPeriod() != -1L )
		{
			return false;
		}
		setPeriod( -2L );
		return true;
	}

	@Override
	public boolean isCancelled()
	{
		return getPeriod() == -2L;
	}

	@Override
	public boolean isDone()
	{
		final long period = this.getPeriod();
		return period != -1L && period != -3L;
	}

	@Override
	public T get() throws CancellationException, InterruptedException, ExecutionException
	{
		try
		{
			return get( 0, TimeUnit.MILLISECONDS );
		}
		catch ( final TimeoutException e )
		{
			throw new Error( e );
		}
	}

	@Override
	public synchronized T get( long timeout, final TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException
	{
		timeout = unit.toMillis( timeout );
		long period = this.getPeriod();
		long timestamp = timeout > 0 ? System.currentTimeMillis() : 0L;
		while ( true )
		{
			if ( period == -1L || period == -3L )
			{
				this.wait( timeout );
				period = this.getPeriod();
				if ( period == -1L || period == -3L )
				{
					if ( timeout == 0L )
					{
						continue;
					}
					timeout += timestamp - ( timestamp = System.currentTimeMillis() );
					if ( timeout > 0 )
					{
						continue;
					}
					throw new TimeoutException();
				}
			}
			if ( period == -2L )
			{
				throw new CancellationException();
			}
			if ( period == -4L )
			{
				if ( exception == null )
				{
					return value;
				}
				throw new ExecutionException( exception );
			}
			throw new IllegalStateException( "Expected " + -1L + " to " + -4L + ", got " + period );
		}
	}

	@Override
	synchronized boolean cancel0()
	{
		if ( getPeriod() != -1L )
		{
			return false;
		}
		setPeriod( -2L );
		notifyAll();
		return true;
	}

	@Override
	public void run()
	{
		synchronized ( this )
		{
			if ( getPeriod() == -2L )
			{
				return;
			}
			setPeriod( -3L );
		}
		try
		{
			value = callable.call();
		}
		catch ( final Exception e )
		{
			exception = e;
		}
		finally
		{
			synchronized ( this )
			{
				setPeriod( -4L );
				this.notifyAll();
			}
		}
	}
}
