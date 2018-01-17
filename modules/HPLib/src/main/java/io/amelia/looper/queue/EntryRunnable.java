package io.amelia.looper.queue;

import javax.annotation.Nonnull;

import io.amelia.lang.ApplicationException;

public abstract class EntryRunnable extends AbstractEntry implements Runnable
{
	public EntryRunnable( @Nonnull DefaultQueue queue )
	{
		super( queue );
	}

	public EntryRunnable( @Nonnull DefaultQueue queue, boolean async )
	{
		super( queue, async );
	}

	@Override
	public synchronized void run()
	{
		if ( queue.getActiveEntry() != this )
			throw ApplicationException.runtime( "Entry can only be ran while it's the active entry for the queue!" );

		Runnable runnable = () -> {
			try
			{
				run0();
			}
			catch ( ApplicationException.Error error )
			{
				queue.getLooperControl().handleException( error );
			}
		};

		if ( isAsync() || queue.hasFlag( AbstractQueue.Flag.ASYNC ) )
			queue.getLooperControl().runAsync( runnable );
		else
			runnable.run();
	}

	protected abstract void run0() throws ApplicationException.Error;
}
