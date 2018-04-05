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

import java.util.logging.Level;

import io.netty.util.internal.logging.InternalLogLevel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

public class DefaultLogFactory extends InternalLoggerFactory
{
	public Level level( InternalLogLevel level )
	{
		switch ( level )
		{
			case DEBUG:
				return Level.FINE;
			case ERROR:
				return Level.SEVERE;
			case INFO:
				return Level.INFO;
			case TRACE:
				return Level.CONFIG;
			case WARN:
				return Level.WARNING;
			default:
				return Level.INFO;
		}
	}

	@Override
	protected InternalLogger newInstance( String name )
	{
		return new DefaultLog( name );
	}

	private class DefaultLog implements InternalLogger
	{
		Logger l;

		DefaultLog( String name )
		{
			l = LogBuilder.get( name );
		}

		@Override
		public void debug( String msg )
		{
			l.fine( msg );
		}

		@Override
		public void debug( String format, Object arg )
		{
			l.fine( format, arg );
		}

		@Override
		public void debug( String format, Object... args )
		{
			l.fine( format, args );
		}

		@Override
		public void debug( String format, Object argA, Object argB )
		{
			l.fine( format, argA, argB );
		}

		@Override
		public void debug( String msg, Throwable t )
		{
			l.log( Level.FINE, msg, t );
		}

		@Override
		public void debug( Throwable t )
		{
			l.log( Level.FINE, "Debugged Exception", t );
		}

		@Override
		public void error( String msg )
		{
			l.severe( msg );
		}

		@Override
		public void error( String format, Object arg )
		{
			l.severe( format, arg );
		}

		@Override
		public void error( String format, Object... arguments )
		{
			l.severe( format, arguments );
		}

		@Override
		public void error( String format, Object argA, Object argB )
		{
			l.severe( format, argA, argB );
		}

		@Override
		public void error( String msg, Throwable t )
		{
			l.severe( msg, t );
		}

		@Override
		public void error( Throwable t )
		{
			l.severe( t );
		}

		@Override
		public void info( String msg )
		{
			l.info( msg );
		}

		@Override
		public void info( String format, Object arg )
		{
			l.info( format, arg );
		}

		@Override
		public void info( String format, Object... arguments )
		{
			l.info( format, arguments );
		}

		@Override
		public void info( String format, Object argA, Object argB )
		{
			l.info( format, argA, argB );
		}

		@Override
		public void info( String msg, Throwable t )
		{
			l.info( msg, t );
		}

		@Override
		public void info( Throwable t )
		{
			l.info( t );
		}

		@Override
		public boolean isDebugEnabled()
		{
			return false;
		}

		@Override
		public boolean isEnabled( InternalLogLevel level )
		{
			return l.isEnabled( level( level ) );
		}

		@Override
		public boolean isErrorEnabled()
		{
			return l.isEnabled( Level.SEVERE );
		}

		@Override
		public boolean isInfoEnabled()
		{
			return l.isEnabled( Level.INFO );
		}

		@Override
		public boolean isTraceEnabled()
		{
			return false;
			// return l.isEnabled( Level.CONFIG );
		}

		@Override
		public boolean isWarnEnabled()
		{
			return l.isEnabled( Level.WARNING );
		}

		@Override
		public void log( InternalLogLevel level, String msg )
		{
			l.log( level( level ), msg );
		}

		@Override
		public void log( InternalLogLevel level, String format, Object arg )
		{
			l.log( level( level ), format, arg );
		}

		@Override
		public void log( InternalLogLevel level, String format, Object... arguments )
		{
			l.log( level( level ), format, arguments );
		}

		@Override
		public void log( InternalLogLevel level, String format, Object argA, Object argB )
		{
			l.log( level( level ), format, argA, argB );
		}

		@Override
		public void log( InternalLogLevel level, String msg, Throwable t )
		{
			l.log( level( level ), msg, t );
		}

		@Override
		public void log( InternalLogLevel level, Throwable t )
		{
			l.log( level( level ), "Encountered Exception", t );
		}

		@Override
		public String name()
		{
			return l.getId();
		}

		@Override
		public void trace( String msg )
		{
			l.log( Level.CONFIG, msg );
		}

		@Override
		public void trace( String format, Object arg )
		{
			l.log( Level.CONFIG, format, arg );
		}

		@Override
		public void trace( String format, Object... arguments )
		{
			l.log( Level.CONFIG, format, arguments );
		}

		@Override
		public void trace( String format, Object argA, Object argB )
		{
			l.log( Level.CONFIG, format, argA, argB );
		}

		@Override
		public void trace( String msg, Throwable t )
		{
			l.log( Level.CONFIG, msg, t );
		}

		@Override
		public void trace( Throwable t )
		{
			l.log( Level.CONFIG, "Encountered Exception", t );
		}

		@Override
		public void warn( String msg )
		{
			l.warning( msg );
		}

		@Override
		public void warn( String format, Object arg )
		{
			l.warning( format, arg );
		}

		@Override
		public void warn( String format, Object... arguments )
		{
			l.warning( format, arguments );
		}

		@Override
		public void warn( String format, Object argA, Object argB )
		{
			l.warning( format, argA, argB );
		}

		@Override
		public void warn( String msg, Throwable t )
		{
			l.warning( msg, t );
		}

		@Override
		public void warn( Throwable t )
		{
			l.warning( "Encountered Exception", t );
		}

	}
}
