/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import io.amelia.looper.LooperTask;
import io.amelia.support.Objs;

/**
 * Provides a runnable that blocks until the wrapped runnable is executed.
 * Be sure to pass this Runnable to the executor before calling {@link #postAndWait(long)} or else it will hang.
 */
public final class BlockingTask<E extends Exception> implements LooperTask<E>
{
	private final LooperTask<E> mTask;
	private boolean mDone;

	public BlockingTask( @Nonnull LooperTask<E> task )
	{
		mTask = task;
	}

	@Override
	public void execute() throws E
	{
		try
		{
			mTask.execute();
		}
		finally
		{
			synchronized ( this )
			{
				mDone = true;
				notifyAll();
			}
		}
	}

	public boolean postAndWait( @Nonnegative long timeout )
	{
		Objs.notNegative( timeout );

		synchronized ( this )
		{
			if ( timeout > 0 )
			{
				final long expirationTime = Kernel.uptime() + timeout;
				while ( !mDone )
				{
					long delay = expirationTime - Kernel.uptime();
					if ( delay <= 0 )
					{
						return false; // timeout
					}
					try
					{
						wait( delay );
					}
					catch ( InterruptedException ex )
					{
						// Ignore
					}
				}
			}
			else
			{
				while ( !mDone )
				{
					try
					{
						wait();
					}
					catch ( InterruptedException ex )
					{
						// Ignore
					}
				}
			}
		}
		return true;
	}

	public boolean postAndWait()
	{
		return postAndWait( 0L );
	}
}
