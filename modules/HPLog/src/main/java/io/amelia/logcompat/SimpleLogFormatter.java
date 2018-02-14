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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import io.amelia.support.EnumColor;

public class SimpleLogFormatter extends Formatter
{
	public static boolean debugMode = false;
	public static int debugModeHowDeep = 1;
	private SimpleDateFormat dateFormat;
	private SimpleDateFormat timeFormat;

	public SimpleLogFormatter()
	{
		dateFormat = new SimpleDateFormat( "MM-dd" );
		timeFormat = new SimpleDateFormat( "HH:mm:ss.SSS" );
	}

	@Override
	public String format( LogRecord record )
	{
		StringBuilder msg = new StringBuilder();
		msg.append( EnumColor.RESET );
		msg.append( EnumColor.GRAY );
		msg.append( dateFormat.format( record.getMillis() ) );
		msg.append( " " );
		msg.append( timeFormat.format( record.getMillis() ) );
		msg.append( " [" );
		msg.append( EnumColor.fromLevel( record.getLevel() ) );
		msg.append( EnumColor.BOLD );
		msg.append( record.getLevel().getLocalizedName().toUpperCase() );
		msg.append( EnumColor.RESET );
		msg.append( EnumColor.GRAY );
		msg.append( "] " );
		msg.append( EnumColor.fromLevel( record.getLevel() ) );
		msg.append( formatMessage( record ) );

		if ( !msg.toString().endsWith( "\r" ) )
			msg.append( "\n" );

		Throwable ex = record.getThrown();
		if ( ex != null )
		{
			StringWriter writer = new StringWriter();
			ex.printStackTrace( new PrintWriter( writer ) );
			msg.append( writer );
		}

		return EnumColor.transAltColors( msg.toString() );
	}
}
