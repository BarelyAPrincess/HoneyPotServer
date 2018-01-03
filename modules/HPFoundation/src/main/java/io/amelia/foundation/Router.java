package io.amelia.foundation;

import io.amelia.looper.AbstractLooper;

public class Router extends AbstractLooper
{


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
