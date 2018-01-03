package io.amelia.foundation;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import io.amelia.logcompat.LogBuilder;
import io.amelia.logcompat.Logger;
import io.amelia.looper.AbstractLooper;
import io.amelia.looper.LooperTaskTrait;
import io.amelia.looper.queue.AbstractQueue;
import io.amelia.looper.queue.DefaultQueue;
import io.amelia.support.Objs;

/**
 * The Looper is intended to be interfaced by the thread that intends to execute tasks or oversee the process.
 */
public class ApplicationLooper extends AbstractLooper<DefaultQueue> implements LooperTaskTrait
{
	public static final Logger L = LogBuilder.get( ApplicationLooper.class );

	ApplicationLooper( Flag... flags )
	{
		super( flags );
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
		return !loopers.contains( this );
	}

	@Override
	protected void tick( long loopStartMillis )
	{

	}

	@Override
	protected void tickShutdown()
	{

	}
}
