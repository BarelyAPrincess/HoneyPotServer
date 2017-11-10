package io.amelia.foundation;

import io.amelia.support.Objs;

/**
 * Provides a runnable that blocks until the wrapped runnable is executed.
 * Be sure to pass this Runnable to the executor before calling {@link #postAndWait(long)} or else it will hang.
 */
public final class BlockingRunnable implements Runnable
{
	private final Runnable mTask;
	private boolean mDone;

	public BlockingRunnable( Runnable task )
	{
		mTask = task;
	}

	public boolean postAndWait()
	{
		return postAndWait( 0L );
	}

	public boolean postAndWait( long timeout )
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
					}
				}
			}
		}
		return true;
	}

	@Override
	public void run()
	{
		try
		{
			mTask.run();
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
}
