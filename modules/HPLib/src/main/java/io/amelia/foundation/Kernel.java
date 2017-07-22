package io.amelia.foundation;

import io.amelia.foundation.injection.Libraries;
import io.amelia.foundation.injection.MavenReference;
import io.amelia.lang.ApplicationException;
import io.amelia.lang.StartupException;
import io.amelia.logcompat.LogBuilder;
import io.amelia.logcompat.Logger;
import io.amelia.support.LibIO;
import io.amelia.support.LibTime;
import io.amelia.support.Objs;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Kernel
{
	public static final Logger L = LogBuilder.get();
	public static long startTime = System.currentTimeMillis();
	private static ApplicationInterface app = null;
	private static Class<? extends ApplicationInterface> applicationInterface = null;
	private static boolean finished = false;
	private static Thread primaryThread;
	private static ExecutorService threadPool = Executors.newCachedThreadPool();

	@SuppressWarnings( "unchecked" )
	public static <T extends ApplicationInterface> T application()
	{
		if ( finished )
			throw ApplicationException.ignorable( "The Kernel has already been disposed!" );
		if ( app == null )
			app = Objs.initClass( applicationInterface );
		try
		{
			return ( T ) app;
		}
		catch ( Exception ex )
		{
			throw ApplicationException.ignorable( "Can't initialize the new instance of " + applicationInterface.getName(), ex );
		}
	}

	public static void dispose()
	{
		finished = true;
		threadPool.shutdown();
		threadPool = null;
		app = null;
	}

	public static boolean isPrimaryThread()
	{
		if ( primaryThread == null )
			throw new IllegalStateException( "Kernel has not been initialized!" );
		return primaryThread == Thread.currentThread();
	}

	private static void panic( String msg )
	{
		L.severe( msg );
		System.exit( 1 );
	}

	/**
	 * Loads the built-in dependencies written by the gradle script
	 */
	public static void prepare()
	{
		primaryThread = Thread.currentThread();

		LogBuilder.get().info( "Loading deployment libraries from " + Libraries.LIBRARY_DIR );
		try
		{
			/* Load Deployment Libraries */
			LogBuilder.get().info( "Loading deployment libraries defined in dependencies.txt." );
			for ( String depend : LibIO.resourceToString( "dependencies.txt" ).split( "\n" ) )
				Libraries.loadLibrary( new MavenReference( "builtin", depend ) );
		}
		catch ( IOException e )
		{
			throw new StartupException( "Failed to read the built-in dependencies file.", e );
		}
		LogBuilder.get().info( "Finished downloading deployment libraries." );
	}

	public static void registerRunnable( Runnable runnable )
	{
		threadPool.execute( runnable );
	}

	/**
	 * Sets the ApplicationInterface used
	 *
	 * @param applicationInterface The ApplicationInterface Implementation
	 */
	public static void setApplicationInterface( Class<? extends ApplicationInterface> applicationInterface )
	{
		Kernel.applicationInterface = applicationInterface;
	}

	public static long uptime()
	{
		return System.currentTimeMillis() - Kernel.startTime;
	}

	public static String uptimeDescribe()
	{
		return LibTime.formatDurection( System.currentTimeMillis() - Kernel.startTime );
	}

	private Kernel()
	{

	}
}
