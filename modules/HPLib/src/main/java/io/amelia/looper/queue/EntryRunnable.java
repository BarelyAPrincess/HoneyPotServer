package io.amelia.looper.queue;

import javax.annotation.Nonnull;

import io.amelia.lang.ApplicationException;
import io.amelia.looper.AbstractLooper;

public abstract class EntryRunnable extends AbstractEntry implements Runnable
{
	EntryRunnable( @Nonnull DefaultQueue queue )
	{
		super( queue );
	}

	EntryRunnable( @Nonnull DefaultQueue queue, boolean async )
	{
		super( queue, async );
	}

	@Override
	public synchronized void run()
	{
		if ( queue.lastEntry != this )
			throw ApplicationException.runtime( "Entry must only be ran while it's the active entry for the parcel queue!" );

		if ( isAsync() || queue.looper.hasFlag( AbstractLooper.Flag.ASYNC ) )
			queue.looper.runAsync( this::run0 );
		else
			run0();
	}

	protected abstract void run0() throws ApplicationException;
}
