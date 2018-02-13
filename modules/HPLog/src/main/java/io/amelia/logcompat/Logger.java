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

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.logging.Level;

import io.amelia.foundation.Kernel;
import io.amelia.support.EnumColor;
import io.amelia.support.Objs;

/**
 * Logger Instance
 */
public class Logger
{
	public static final PrintStream FAILOVER_OUTPUT_STREAM = new PrintStream( new FileOutputStream( FileDescriptor.out ) );
	private final String id;
	private final java.util.logging.Logger logger;
	private boolean hasErrored = false;

	/**
	 * Attempts to find a logger based on the id provided. If you would like to use your own Logger, be sure to create it with the same id prior to using any of the built-in getLogger() methods or you will need to use the replaceLogger() method.
	 *
	 * @param id The logger id
	 */
	protected Logger( String id )
	{
		this.id = id;
		java.util.logging.Logger logger = java.util.logging.Logger.getLogger( id );

		if ( logger == null )
			logger = new ChildLogger( id );

		logger.setParent( LogBuilder.getRootLogger() );
		logger.setLevel( Level.ALL );

		this.logger = logger;
	}

	public void debug( String format, Object... args )
	{
		if ( !Kernel.isDevelopment() )
			return;

		log( Level.INFO, EnumColor.GOLD + "" + EnumColor.NEGATIVE + ">>>>   " + format + "   <<<< ", args );
	}

	public void debug( String msg, Throwable t )
	{
		log( Level.INFO, ">>>>   " + EnumColor.GOLD + "" + EnumColor.NEGATIVE + msg + "   <<<< ", t );
	}

	public void dev( String format, Object... args )
	{
		if ( !Kernel.isDevelopment() )
			return;

		log( Level.INFO, EnumColor.GOLD + "" + EnumColor.NEGATIVE + "[DEV NOTICE] " + format, args );
	}

	public void fine( String var1 )
	{
		log( Level.FINE, var1 );
	}

	public void fine( String var1, Object... args )
	{
		log( Level.FINE, var1, args );
	}

	public void finer( String var1 )
	{
		log( Level.FINER, var1 );
	}

	public void finest( String var1 )
	{
		log( Level.FINEST, var1 );
	}

	public String getId()
	{
		return id;
	}

	public java.util.logging.Logger getLogger()
	{
		return logger;
	}

	public boolean hasErrored()
	{
		return hasErrored;
	}

	public void info( String msg )
	{
		log( Level.INFO, msg );
	}

	public void info( String format, Object... arguments )
	{
		log( Level.INFO, format, arguments );
	}

	public void info( Throwable t )
	{
		log( Level.INFO, "Unexpected Exception", t );
	}

	public boolean isEnabled( Level level )
	{
		return logger.isLoggable( level );
	}

	public void log( Level l, String msg )
	{
		try
		{
			if ( !Objs.stackTraceAntiLoop( java.util.logging.Logger.class, "log" ) || hasErrored )
				FAILOVER_OUTPUT_STREAM.println( "Failover Logger [" + l.getName() + "] " + msg );
			else
				logger.log( l, ( LogBuilder.useColor() ? EnumColor.fromLevel( l ) : "" ) + msg );
		}
		catch ( Throwable t )
		{
			markError( t );
			if ( Kernel.isDevelopment() )
				throw t;
		}
	}

	public void log( Level l, String msg, Object... params )
	{
		try
		{
			if ( !Objs.stackTraceAntiLoop( java.util.logging.Logger.class, "log" ) || hasErrored )
				FAILOVER_OUTPUT_STREAM.println( "Failover Logger [" + l.getName() + "] " + msg );
			else
				logger.log( l, ( LogBuilder.useColor() ? EnumColor.fromLevel( l ) : "" ) + msg, params );
		}
		catch ( Throwable t )
		{
			markError( t );
			if ( Kernel.isDevelopment() )
				throw t;
		}
	}

	public void log( Level l, String msg, Throwable throwable )
	{
		try
		{
			if ( !Objs.stackTraceAntiLoop( java.util.logging.Logger.class, "log" ) || hasErrored )
			{
				FAILOVER_OUTPUT_STREAM.println( "Failover Logger [" + l.getName() + "] " + msg );
				throwable.printStackTrace( FAILOVER_OUTPUT_STREAM );
			}
			else
				logger.log( l, ( LogBuilder.useColor() ? EnumColor.fromLevel( l ) : "" ) + msg, throwable );
		}
		catch ( Throwable tt )
		{
			markError( tt );
			if ( Kernel.isDevelopment() )
				throw tt;
		}
	}

	private void markError( Throwable t )
	{
		hasErrored = true;

		FAILOVER_OUTPUT_STREAM.println( EnumColor.RED + "" + EnumColor.NEGATIVE + "The child logger \"" + getId() + "\" has thrown an unrecoverable exception!" );
		FAILOVER_OUTPUT_STREAM.println( EnumColor.RED + "" + EnumColor.NEGATIVE + "Please report the following stacktrace/log to the application developer." );
		if ( Kernel.isDevelopment() )
			FAILOVER_OUTPUT_STREAM.println( EnumColor.RED + "" + EnumColor.NEGATIVE + "Developer Node: Calling the method \"Log.get( [log name] ).unmarkError()\" will reset the errored log state." );
		t.printStackTrace( FAILOVER_OUTPUT_STREAM );
	}

	public String[] multilineColorRepeater( String var1 )
	{
		return multilineColorRepeater( var1.split( "\\n" ) );
	}

	public String[] multilineColorRepeater( String[] var1 )
	{
		try
		{
			String color = EnumColor.getLastColors( var1[0] );

			for ( int l = 0; l < var1.length; l++ )
				var1[l] = color + var1[l];
		}
		catch ( NoClassDefFoundError e )
		{
			// Ignore
		}

		return var1;
	}

	public void notice( String msg )
	{
		log( Level.WARNING, EnumColor.GOLD + "" + EnumColor.NEGATIVE + msg );
	}

	public void severe( String s )
	{
		log( Level.SEVERE, s );
	}

	public void severe( String s, Object... objs )
	{
		log( Level.SEVERE, s, objs );
	}

	public void severe( String s, Throwable t )
	{
		log( Level.SEVERE, s, t );
	}

	public void severe( Throwable t )
	{
		log( Level.SEVERE, "Severe Exception", t );
	}

	public void unmarkError()
	{
		hasErrored = false;
	}

	public void warning( String s )
	{
		log( Level.WARNING, s );
	}

	public void warning( String s, Object... objs )
	{
		log( Level.WARNING, s, objs );
	}

	public void warning( String s, Throwable t )
	{
		log( Level.WARNING, s, t );
	}

	public void warning( Throwable t )
	{
		log( Level.SEVERE, "Warning Exception", t );
	}
}
