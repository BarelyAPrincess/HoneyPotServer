package io.amelia.log;

import io.amelia.foundation.InternalMessage;
import io.amelia.foundation.Kernel;
import io.amelia.foundation.Looper;
import io.amelia.foundation.LooperReceiver;

public class LogProcessing
{
	private Looper selfLooper;

	public LogProcessing()
	{
		selfLooper = Looper.Factory.obtain();
		Kernel.getExecutorParallel().execute( selfLooper::joinLoop );
	}

	private class LogHandler extends LooperReceiver
	{
		public LogHandler( Looper.Factory logisticsFactory )
		{
			super( logisticsFactory );
		}

		@Override
		public void handleMessage( InternalMessage msg )
		{

		}
	}
}
