package io.amelia.foundation;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import io.amelia.looper.queue.EntryRunnable;
import io.amelia.looper.queue.DefaultQueue;

public class EntryParcel extends EntryRunnable
{
	ParcelCarrier message;
	long when;

	EntryParcel( @Nonnull DefaultQueue queue, @Nonnull ParcelCarrier message, @Nonnegative long when )
	{
		super( queue );
		this.message = message;
		this.when = when;
	}

	@Override
	public long getWhen()
	{
		return when;
	}

	@Override
	public void recycle()
	{
		message.recycle();
	}

	@Override
	public boolean removesSafely()
	{
		return true;
	}

	@Override
	protected void run0()
	{
		// TODO
	}
}
