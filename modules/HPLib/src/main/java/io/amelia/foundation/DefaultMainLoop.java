package io.amelia.foundation;

import io.amelia.config.ConfigRegistry;
import io.amelia.lang.ApplicationException;
import io.amelia.lang.Runlevel;
import io.amelia.lang.StartupException;
import io.amelia.android.AsyncTask;

/**
 * Created by amelia on 8/9/17.
 */
public final class DefaultMainLoop extends AsyncTask<Void, Void, Void>
{
	ApplicationInterface applicationInterface;
	float averageTick = -1;
	int currentTick = ( int ) ( System.currentTimeMillis() / 50 );

	public DefaultMainLoop( ApplicationInterface applicationInterface )
	{
		this.applicationInterface = applicationInterface;
	}

	@Override
	protected Void doInBackground( Void... params )
	{
		try
		{
			long i = System.currentTimeMillis();

			long q = 0L;
			long j = 0L;
			for ( ; ; )
			{
				long k = System.currentTimeMillis();
				long l = k - i;

				if ( l > 2000L && i - q >= 15000L )
				{
					if ( ConfigRegistry.warnOnOverload() )
						Kernel.L.warning( "Can't keep up! Did the system time change, or is the server overloaded?" );
					l = 2000L;
					q = i;
				}

				if ( l < 0L )
				{
					Kernel.L.warning( "Time ran backwards! Did the system time change?" );
					l = 0L;
				}

				j += l;
				i = k;

				while ( j > 50L )
				{
					currentTick = ( int ) ( System.currentTimeMillis() / 50 );
					averageTick = ( Math.min( currentTick, averageTick ) - Math.max( currentTick, averageTick ) ) / 2;
					j -= 50L;

					publishProgress();
				}

				if ( isCancelled() )
					break;
				Thread.sleep( 1L );
			}
		}
		catch ( Throwable t )
		{
			Kernel.handleExceptions( t );
		}
		return null;
	}

	@Override
	protected void onPostExecute( Void aVoid )
	{
		super.onPostExecute( aVoid );

		try
		{
			if ( Kernel.getRunlevel() != Runlevel.SHUTDOWN )
				Kernel.setRunlevel( Runlevel.SHUTDOWN );
		}
		catch ( ApplicationException e )
		{
			Kernel.handleExceptions( e );
		}
	}

	@Override
	protected void onPreExecute()
	{
		super.onPreExecute();

		if ( Kernel.getRunlevel() != Runlevel.DAEMON )
			throw new StartupException( "ApplicationMainLoop can only be started on RUNNING RunLevel." );
		if ( !Kernel.isPrimaryThread() )
			throw new StartupException( "ApplicationMainLoop can only be started from the PrimaryThread." );
	}

	@Override
	protected void onProgressUpdate( Void... values )
	{
		super.onProgressUpdate( values );

		try
		{
			applicationInterface.onTick( currentTick, averageTick );
		}
		catch ( ApplicationException e )
		{
			Kernel.handleExceptions( e );
		}
	}

	public boolean isRunning()
	{
		return getStatus() == Status.RUNNING;
	}
}
