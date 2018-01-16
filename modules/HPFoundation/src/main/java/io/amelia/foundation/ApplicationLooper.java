package io.amelia.foundation;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import io.amelia.foundation.parcel.ParcelCarrier;
import io.amelia.lang.ApplicationException;
import io.amelia.logcompat.LogBuilder;
import io.amelia.logcompat.Logger;
import io.amelia.looper.AbstractLooper;
import io.amelia.looper.LooperTaskTrait;
import io.amelia.looper.queue.AbstractQueue;
import io.amelia.looper.queue.DefaultQueue;
import io.amelia.support.Objs;
import io.amelia.support.Runlevel;

/**
 * The Looper is intended to be interfaced by the thread that intends to execute tasks or oversee the process.
 */
public class ApplicationLooper extends AbstractLooper<DefaultQueue> implements LooperTaskTrait
{
	public static final Logger L = LogBuilder.get( ApplicationLooper.class );

	public ApplicationLooper()
	{
		super( new DefaultQueue() );
	}

	boolean enqueueMessage( @Nonnull ParcelCarrier msg, @Nonnegative long when )
	{
		Objs.notNull( msg );
		Objs.notNegative( when );

		if ( msg.isInUse() )
			throw new IllegalStateException( "Message is already in use." );

		if ( isQuitting() )
		{
			ApplicationLooper.L.warning( "Looper is quiting." );
			msg.recycle();
			return false;
		}

		DefaultQueue queue = getQueue();

		synchronized ( queue.entries )
		{
			msg.markInUse( true );

			// TODO
			boolean needWake = queue.getActiveResult() == AbstractQueue.Result.EMPTY || when == 0 || when < queue.getEarliestEntry();

			queue.postEntry( new EntryParcel( this, msg, when ) );

			if ( needWake )
				queue.wake();
		}

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
		LooperQueue.Result result = queue.next( loopStartMillis );

		// A queue entry was successful returned and can now be ran then recycled.
		if ( result == LooperQueue.Result.SUCCESS )
		{
			// As of now, the only entry returned on the SUCCESS result is the RunnableEntry (or more so TaskEntry and ParcelEntry).
			LooperQueue.RunnableEntry entry = ( LooperQueue.RunnableEntry ) queue.getLastEntry();

			entry.markFinalized();
			entry.run();
			entry.recycle();
		}
		// The queue is empty and this looper quits in such cases.
		else if ( result == LooperQueue.Result.EMPTY && hasFlag( ApplicationLooper.Flag.AUTO_QUIT ) && !isQuitting() )
		{
			quitSafely();
		}
	}

	@Override
	protected void tickShutdown()
	{
		queue.clearState();
	}
}
