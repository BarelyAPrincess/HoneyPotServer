package io.amelia.looper.queue;

import java.util.function.Predicate;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import io.amelia.looper.AbstractLooper;

public class EntryBarrier extends EntryRunnable
{
	private long id = AbstractLooper.getGloballyUniqueId();
	private Predicate<AbstractLooper> predicate;
	private long when;

	EntryBarrier( @Nonnull DefaultQueue queue, @Nonnull Predicate<AbstractLooper> predicate, @Nonnegative long when )
	{
		super( queue );
		this.predicate = predicate;
		this.when = when;
	}

	public long getId()
	{
		return id;
	}

	@Override
	public long getWhen()
	{
		return when;
	}

	@Override
	public void recycle()
	{
		// Do Nothing
	}

	@Override
	public boolean removesSafely()
	{
		return true;
	}

	@Override
	protected void run0()
	{
		if ( !predicate.test( queue.looper ) )
			cancel();
	}
}
