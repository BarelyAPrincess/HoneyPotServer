/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.logging;

import java.util.logging.Level;

/**
 */
public class LogEvent implements ILogEvent
{
	final String id;
	final LogRecord record;

	LogEvent( String id, LogRecord record )
	{
		this.id = id;
		this.record = record;
	}

	public void close()
	{
		LogManager.close( this );
	}

	@Override
	public void exceptions( Throwable... throwables )
	{
		record.exceptions( throwables );
	}

	@Override
	public void flush()
	{
		record.flush();
	}

	public void flushAndClose()
	{
		flush();
		close();
	}

	@Override
	public void header( String msg, Object... objs )
	{
		record.header( msg, objs );
	}

	@Override
	public void log( Level level, String msg, Object... objs )
	{
		record.log( level, msg, objs );
	}
}
