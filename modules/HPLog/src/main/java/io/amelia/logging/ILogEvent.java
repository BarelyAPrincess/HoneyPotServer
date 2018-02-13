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
 * Interface for {@link LogEvent} and {@link LogRecord}
 */
public interface ILogEvent
{
	void exceptions( Throwable... throwables );

	void log( Level level, String msg, Object... objs );

	void flush();

	void header( String msg, Object... objs );
}
