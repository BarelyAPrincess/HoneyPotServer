package io.amelia.foundation;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import io.amelia.foundation.parcel.ParcelCarrier;
import io.amelia.lang.ApplicationException;
import io.amelia.logcompat.LogBuilder;
import io.amelia.logcompat.Logger;
import io.amelia.looper.AbstractLooper;
import io.amelia.looper.LooperTaskTrait;
import io.amelia.looper.queue.DefaultQueue;
import io.amelia.looper.queue.EntryRunnable;
import io.amelia.support.Runlevel;

public class ApplicationLooper extends AbstractLooper<DefaultQueue> implements LooperTaskTrait
{
	public static final Logger L = LogBuilder.get( ApplicationLooper.class );

	public ApplicationLooper()
	{
		setQueue( new DefaultQueue( getLooperControl() ) );
	}

	boolean enqueueParcel( @Nonnull ParcelCarrier parcelCarrier, @Nonnegative long when )
	{
		if ( isQuitting() )
		{
			ApplicationLooper.L.warning( "Looper is quiting." );
			parcelCarrier.recycle();
			return false;
		}

		getQueue().postEntry( new EntryParcel( getQueue(), parcelCarrier, when ) );

		return true;
	}

	public boolean isDisposed()
	{
		return Foundation.isRunlevel( Runlevel.DISPOSED );
	}

	@Override
	protected void quit( boolean removePendingMessages )
	{
		if ( Foundation.getRunlevel() != Runlevel.DISPOSED )
			throw ApplicationException.runtime( "Application Looper is not permitted to quit." );

		super.quit( removePendingMessages );
	}

	@Override
	protected void quitFinal()
	{
		// Nothing
	}

	@Override
	protected void tick( long loopStartMillis )
	{
		// Call the actual loop logic.
		DefaultQueue.Result result = getQueue().next( loopStartMillis );

		// A queue entry was successful returned and can now be ran then recycled.
		if ( result == DefaultQueue.Result.SUCCESS )
		{
			// As of now, the only entry returned on the SUCCESS result is the EntryRunnable (or more so TaskEntry and ParcelEntry).
			EntryRunnable entry = ( EntryRunnable ) getQueue().getActiveEntry();

			entry.markFinalized();
			entry.run();
			entry.recycle();
		}
		// The queue is empty and this looper quits in such cases.
		else if ( result == DefaultQueue.Result.EMPTY && hasFlag( ApplicationLooper.Flag.AUTO_QUIT ) && !isQuitting() )
		{
			quitSafely();
		}
	}

	@Override
	protected void tickShutdown()
	{

	}
}
