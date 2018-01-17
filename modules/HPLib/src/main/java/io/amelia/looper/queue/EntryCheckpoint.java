package io.amelia.looper.queue;

import java.util.function.BiPredicate;

import javax.annotation.Nonnull;

import io.amelia.looper.AbstractLooper;

public class EntryCheckpoint extends AbstractEntry
{
	BiPredicate<AbstractLooper, Boolean> predicate;
	long when;

	EntryCheckpoint( @Nonnull DefaultQueue queue, @Nonnull BiPredicate<AbstractLooper, Boolean> predicate )
	{
		super( queue );
		this.predicate = predicate;
		when = queue.getLatestEntry() + 1L;
	}

	@Override
	public long getWhen()
	{
		return when;
	}

	@Override
	public void recycle()
	{
		// Still Does Nothing
	}

	@Override
	public boolean isSafe()
	{
		return true;
	}
}
