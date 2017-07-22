package io.amelia.foundation;

import com.sun.istack.internal.NotNull;
import io.amelia.config.ConfigRegistry;
import io.amelia.lang.ApplicationException;
import io.amelia.lang.EnumColor;
import io.amelia.lang.ExceptionContext;
import io.amelia.lang.ExceptionReport;
import io.amelia.lang.IException;
import io.amelia.lang.RunLevel;
import io.amelia.lang.StartupAbortException;
import io.amelia.logcompat.LogBuilder;
import io.amelia.serializable.AsyncTask;
import io.amelia.serializable.Looper;
import io.amelia.support.Lists;
import io.amelia.support.Objs;
import io.amelia.support.Timings;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract class ApplicationInterface implements ExceptionContext
{
	// sThreadLocal.get() will return null unless you've called prepare().
	private static final ThreadLocal<Looper> sThreadLocal = new ThreadLocal<>();
	private static boolean hasErrored = false;
	private static Looper sMainLooper;  // guarded by Looper.class
	private static String stopReason = null;
	private static boolean willRestart = false;

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

	private MainLoop mainLoop = new MainLoop();

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

	public void handleExceptions( @NotNull Throwable throwable )
	{
		handleExceptions( Lists.newArrayList( throwable ) );
	}

	public void handleExceptions( @NotNull List<? extends Throwable> throwables )
	{
		handleExceptions( throwables, true );
	}

	public void handleExceptions( @NotNull Throwable throwable, boolean crashOnError )
	{
		handleExceptions( Lists.newArrayList( throwable ), crashOnError );
	}

	public void handleExceptions( @NotNull List<? extends Throwable> throwables, boolean crashOnError )
	{
		ExceptionReport report = new ExceptionReport();

		for ( Throwable t : throwables )
		{
			t.printStackTrace();
			if ( report.handleException( t, this ) )
				hasErrored = true;
		}

		/* Non-Ignorable Exceptions */

		Supplier<Stream<IException>> errorStream = report::getNotIgnorableExceptions;

		LogBuilder.get().severe( "We Encountered " + errorStream.get().count() + " Non-Ignorable Exception(s):" );

		errorStream.get().forEach( e ->
		{
			if ( e instanceof Throwable )
				LogBuilder.get().severe( ( Throwable ) e );
			else
				LogBuilder.get().severe( e.getClass() + ": " + e.getMessage() );
		} );

		/* Ignorable Exceptions */

		Supplier<Stream<IException>> debugStream = report::getIgnorableExceptions;

		if ( debugStream.get().count() > 0 )
		{
			LogBuilder.get().severe( "In Addition, We Encountered " + debugStream.get().count() + " Ignorable Exception(s):" );

			debugStream.get().forEach( e ->
			{
				if ( e instanceof Throwable )
					LogBuilder.get().warning( ( Throwable ) e );
				else
					LogBuilder.get().warning( e.getClass() + ": " + e.getMessage() );
			} );
		}

		if ( hasErrored )
			LogBuilder.get().fine( "The AppController has reached an errored state!" );

		/* TODO Pass crash information */
		if ( crashOnError && hasErrored )
			stopApplication( "CRASHED" );
	}

	public void reloadApplication( String reason )
	{
		if ( Objs.isEmpty( reason ) )
			reason = String.format( "Server %s is restarting, be back soon... :D", ConfigRegistry.getString("env.instance-id") );

		Kernel.L.notice( reason );

		stopReason = reason;
		willRestart = true;

		mainLoop.cancel( false );
	}

	public void restartApplication( String reason )
	{
		if ( reason == null )
			LogBuilder.get().notice( "Restarting!" );
		else if ( !reason.isEmpty() )
			LogBuilder.get().notice( "Restarting for Reason: " + reason );

		stopReason = reason;
		willRestart = true;

		if ( !isRunning )
			throw new StartupAbortException();

		mainLoop.cancel( false );
	}

	private void setRunLevel( RunLevel level ) throws ApplicationException
	{
		runlevel.setRunLevel( level );
		// TODO Throw runlevel change events
	}

	private void shutdownKernelFinal() throws ApplicationException
	{
		Object timing = new Object();
		Timings.start( timing );

		setRunLevel( RunLevel.SHUTDOWN );

		THREAD_POOL.shutdown();

		LogBuilder.get().info( "Shutting Down Plugin Manager..." );
		if ( PluginManager.instanceWithoutException() != null )
			PluginManager.instanceWithoutException().shutdown();

		LogBuilder.get().info( "Shutting Down Permission Manager..." );
		if ( PermissionDispatcher.instanceWithoutException() != null )
			PermissionDispatcher.instanceWithoutException().saveData();

		LogBuilder.get().info( "Shutting Down Account Manager..." );
		ModuleDispatcher.i().moduleDestroyByClass( AccountManager.class, stopReason );

		LogBuilder.get().info( "Shutting Down Task Manager..." );
		if ( TaskManager.instanceWithoutException() != null )
			TaskManager.instanceWithoutException().shutdown();

		LogBuilder.get().info( "Saving Configuration..." );
		ConfigRegistry.save();

		try
		{
			LogBuilder.get().info( "Clearing Excess Cache..." );
			long keepHistory = ConfigRegistry.i().getLong( "advanced.execute.keepHistory", 30L );
			ConfigRegistry.clearCache( keepHistory );
		}
		catch ( IllegalArgumentException e )
		{
			LogBuilder.get().warning( "Cache directory is invalid!" );
		}

		setRunLevel( RunLevel.DISPOSED );

		LogBuilder.get().info( EnumColor.GOLD + "" + EnumColor.NEGATIVE + "Shutdown Completed! It took " + Timings.finish( timing ) + "ms!" );

		if ( willRestart )
			System.exit( 99 );
		else
			System.exit( 0 );
	}

	public void stopApplication( String reason )
	{
		if ( reason == null )
			LogBuilder.get().notice( "Stopping... Goodbye!" );
		else if ( !reason.isEmpty() )
			LogBuilder.get().notice( "Stopping for Reason: " + reason );

		stopReason = reason;
		willRestart = false;

		// if ( !isRunning )
		// A shutdown was requested but the server never reached the running state!
		// throw new StartupAbortException();

		mainLoop.cancel( false );
	}

	public abstract void tick( int currentTick, float averageTick );

	private final class MainLoop extends AsyncTask<Void, Void, Void>
	{
		float averageTick = -1;
		int currentTick = ( int ) ( System.currentTimeMillis() / 50 );

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
							LogBuilder.get().warning( "Can't keep up! Did the system time change, or is the server overloaded?" );
						l = 2000L;
						q = i;
					}

					if ( l < 0L )
					{
						LogBuilder.get().warning( "Time ran backwards! Did the system time change?" );
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
				handleExceptions( t );
			}
			finally
			{
				try
				{
					shutdownKernelFinal();
				}
				catch ( ApplicationException e )
				{
					handleExceptions( e );
				}
			}
		}

		@Override
		protected void onProgressUpdate( Void... values )
		{
			super.onProgressUpdate( values );

			// CommandDispatch.handleCommands();
			TaskManager.instance().heartbeat( currentTick );

			ApplicationInterface.this.tick( currentTick, averageTick );
		}

		public boolean isRunning()
		{
			return getStatus() == Status.RUNNING;
		}
	}
}
