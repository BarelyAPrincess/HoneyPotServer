package io.amelia.foundation;

import io.amelia.config.ConfigRegistry;
import io.amelia.lang.ApplicationException;
import io.amelia.lang.Runlevel;
import io.amelia.logcompat.LogBuilder;
import io.amelia.android.Looper;
import io.amelia.tasks.TaskDispatcher;

/**
 * Implements a basic application environment
 * <p>
 * APPLICATION MODULES:
 * ConfigRegistry (Static)
 * TaskDispatcher (Static)
 * EventDispatcher (Static)
 */
public abstract class DefaultApplication extends ApplicationInterface
{
	private static String stopReason = null;

	static
	{
		System.setProperty( "file.encoding", "utf-8" );
	}

	/**
	 * Return the Looper object associated with the current thread.  Returns
	 * null if the calling thread is not associated with a Looper.
	 */
	public static Looper myLooper()
	{
		return sThreadLocal.get();
	}

	private static void prepare( boolean quitAllowed )
	{
		if ( sThreadLocal.get() != null )
			throw new RuntimeException( "Only one Looper may be created per thread" );
		sThreadLocal.set( new Looper( quitAllowed ) );
	}

	public static void prepareMainLooper()
	{
		prepare( false );
		synchronized ( Looper.class )
		{
			if ( sMainLooper != null )
				throw new IllegalStateException( "The main Looper has already been prepared." );
			sMainLooper = myLooper();
		}
	}

	/**
	 * Returns the application's main looper, which lives in the main thread of the application.
	 */
	public Looper getMainLooper()
	{
		synchronized ( Looper.class )
		{
			return sMainLooper;
		}
	}

	@Override
	public void onRunlevelChange( Runlevel previousRunlevel, Runlevel currentRunlevel ) throws ApplicationException
	{
		if ( currentRunlevel == Runlevel.SHUTDOWN )
		{
			LogBuilder.get().info( "Shutting Down Task Manager..." );
			TaskDispatcher.shutdown();

			LogBuilder.get().info( "Saving Configuration..." );
			ConfigRegistry.save();

			try
			{
				Kernel.L.info( "Clearing Excess Cache..." );
				long keepHistory = ConfigRegistry.getLong( "advanced.execute.keepHistory" ).orElse( 30L );
				ConfigRegistry.clearCache( keepHistory );
			}
			catch ( IllegalArgumentException e )
			{
				Kernel.L.warning( "Cache directory is invalid!" );
			}
		}
	}

	@Override
	public void onTick( int currentTick, float averageTick ) throws ApplicationException
	{
		// CommandDispatch.handleCommands();
		TaskDispatcher.heartbeat( currentTick );
	}
}
