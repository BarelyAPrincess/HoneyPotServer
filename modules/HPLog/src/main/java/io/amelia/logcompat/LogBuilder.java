/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.logcompat;

import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;

import io.amelia.foundation.Kernel;
import io.amelia.support.IO;

/**
 * Builder for Loggers
 */
public class LogBuilder
{
	private static final ConsoleHandler consoleHandler = new ConsoleHandler();
	private static final Set<Logger> loggers = new HashSet<>();
	private static final java.util.logging.Logger rootLogger = java.util.logging.Logger.getLogger( "" );

	static
	{
		for ( Handler h : rootLogger.getHandlers() )
			rootLogger.removeHandler( h );

		consoleHandler.setFormatter( new SimpleLogFormatter() );
		addHandler( consoleHandler );

		System.setOut( new PrintStream( new LoggerOutputStream( get( "SysOut" ), Level.INFO ), true ) );
		System.setErr( new PrintStream( new LoggerOutputStream( get( "SysErr" ), Level.SEVERE ), true ) );
	}


	public static void addFileHandler( String filename, boolean useColor, int archiveLimit, Level level )
	{
		try
		{
			File log = new File( Kernel.getPath( Kernel.PATH_LOGS ), filename + ".log" );

			if ( log.exists() )
			{
				if ( archiveLimit > 0 )
					IO.gzFile( log, new File( Kernel.getPath( Kernel.PATH_LOGS ), new SimpleDateFormat( "yyyy-MM-dd_HH-mm-ss" ).format( new Date() ) + "-" + filename + ".log.gz" ) );
				log.delete();
			}

			cleanupLogs( "-" + filename + ".log.gz", archiveLimit );

			FileHandler fileHandler = new FileHandler( log.getAbsolutePath() );
			fileHandler.setLevel( level );
			fileHandler.setFormatter( new DefaultLogFormatter( useColor ) );

			addHandler( fileHandler );
		}
		catch ( Exception e )
		{
			get().severe( "Failed to log to '" + filename + ".log'", e );
		}
	}

	public static void addHandler( Handler h )
	{
		rootLogger.addHandler( h );
	}

	private static void cleanupLogs( final String suffix, int limit )
	{
		File[] files = Kernel.getPath( Kernel.PATH_LOGS ).listFiles( new FilenameFilter()
		{
			public boolean accept( File dir, String name )
			{
				return name.toLowerCase().endsWith( suffix );
			}
		} );

		if ( files == null || files.length < 1 )
			return;

		// Delete all logs, no archiving!
		if ( limit < 1 )
		{
			for ( File f : files )
				f.delete();
			return;
		}

		IO.SortableFile[] sfiles = new IO.SortableFile[files.length];

		for ( int i = 0; i < files.length; i++ )
			sfiles[i] = new IO.SortableFile( files[i] );

		Arrays.sort( sfiles );

		if ( sfiles.length > limit )
			for ( int i = 0; i < sfiles.length - limit; i++ )
				sfiles[i].f.delete();
	}

	public static Logger get()
	{
		return get( "" );
	}

	/**
	 * Gets an instance of Log for provided loggerId. If the logger does not exist one will be created.
	 *
	 * @param id The logger id
	 *
	 * @return ConsoleLogger An empty loggerId will return the System Logger.
	 */
	public static Logger get( String id )
	{
		if ( id == null || id.length() == 0 )
			id = "Core";

		for ( Logger log : loggers )
			if ( log.getId().equals( id ) )
				return log;

		Logger log = new Logger( id );
		loggers.add( log );
		return log;
	}

	public static Logger get( Class<?> logClass )
	{
		return get( logClass.getSimpleName() );
	}

	public static java.util.logging.Logger getRootLogger()
	{
		return rootLogger;
	}

	public static void removeHandler( Handler h )
	{
		rootLogger.removeHandler( h );
	}

	public static void setConsoleFormatter( Formatter formatter )
	{
		consoleHandler.setFormatter( formatter );
	}

	/**
	 * Checks if the currently set Log Formatter, supports colored logs.
	 *
	 * @return true if it does
	 */
	public static boolean useColor()
	{
		return consoleHandler.getFormatter() instanceof DefaultLogFormatter && ( ( DefaultLogFormatter ) consoleHandler.getFormatter() ).useColor();
	}
}
