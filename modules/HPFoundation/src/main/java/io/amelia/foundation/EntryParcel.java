package io.amelia.foundation;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import io.amelia.foundation.parcel.ParcelCarrier;
import io.amelia.looper.queue.DefaultQueue;
import io.amelia.looper.queue.EntryRunnable;
import io.amelia.support.Objs;

public class EntryParcel extends EntryRunnable
{
	ParcelCarrier parcelCarrier;
	long when;

	EntryParcel( @Nonnull DefaultQueue queue, @Nonnull ParcelCarrier parcelCarrier, @Nonnegative long when )
	{
		super( queue );

		Objs.notNull( parcelCarrier );
		Objs.notNegative( when );

		parcelCarrier.markFinalized();

		this.parcelCarrier = parcelCarrier;
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
		parcelCarrier.recycle();
	}

	@Override
	public boolean isSafe()
	{
		return true;
	}

	@Override
	protected void run0()
	{
		// TODO
	}
}
