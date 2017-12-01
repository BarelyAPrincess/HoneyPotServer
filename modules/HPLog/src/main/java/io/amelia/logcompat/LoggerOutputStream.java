/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.logcompat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;

public class LoggerOutputStream extends ByteArrayOutputStream
{
	private final String separator = System.getProperty( "line.separator" );
	private final Logger log;
	private final Level level;

	public LoggerOutputStream( Logger log, Level level )
	{
		super();
		this.log = log;
		this.level = level;
	}

	@Override
	public void flush() throws IOException
	{
		synchronized ( this )
		{
			super.flush();
			String record = this.toString();
			super.reset();

			if ( record.length() > 0 && !record.equals( separator ) )
				log.log( level, record );
		}
	}
}
