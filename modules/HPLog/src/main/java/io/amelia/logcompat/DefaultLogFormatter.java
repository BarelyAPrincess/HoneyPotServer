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

import io.amelia.foundation.ConfigRegistry;
import io.amelia.support.EnumColor;
import io.amelia.support.Strs;

public class DefaultLogFormatter extends Formatter
{
	public static boolean debugMode = false;
	public static int debugModeHowDeep = 1;
	private SimpleDateFormat dateFormat;
	private boolean fancyConsole;
	private boolean formatConfigLoaded = false;
	private SimpleDateFormat timeFormat;

	public DefaultLogFormatter()
	{
		this( true );
	}

	public DefaultLogFormatter( boolean fancyConsole )
	{
		this.fancyConsole = fancyConsole;
		dateFormat = new SimpleDateFormat( "MM-dd" );
		timeFormat = new SimpleDateFormat( "HH:mm:ss.SSS" );
	}

	@Override
	public String format( LogRecord record )
	{
		if ( ConfigRegistry.isConfigLoaded() && !formatConfigLoaded )
		{
			dateFormat = new SimpleDateFormat( ConfigRegistry.config.getString( "console.dateFormat" ).orElse( "MM-dd" ) );
			timeFormat = new SimpleDateFormat( ConfigRegistry.config.getString( "console.timeFormat" ).orElse( "HH:mm:ss.SSS" ) );
			formatConfigLoaded = true;
		}

		String style = ConfigRegistry.isConfigLoaded() ? ConfigRegistry.config.getString( "console.style" ).orElse( "&r&7[&d%ct&7] %dt %tm [%lv&7]&f" ) : "&r&7%dt %tm [%lv&7]&f";

		Throwable ex = record.getThrown();

		if ( style.contains( "%ct" ) )
		{
			String threadName = Thread.currentThread().getName();

			if ( threadName.length() > 10 )
				threadName = threadName.substring( 0, 2 ) + ".." + threadName.substring( threadName.length() - 6 );
			else if ( threadName.length() < 10 )
				threadName = threadName + Strs.repeat( " ", 10 - threadName.length() );

			style = style.replaceAll( "%ct", threadName );
		}

		style = style.replaceAll( "%dt", dateFormat.format( record.getMillis() ) );
		style = style.replaceAll( "%tm", timeFormat.format( record.getMillis() ) );

		int howDeep = debugModeHowDeep;

		if ( debugMode )
		{
			StackTraceElement[] var1 = Thread.currentThread().getStackTrace();

			for ( StackTraceElement var2 : var1 )
				if ( !var2.getClassName().toLowerCase().contains( "java" ) && !var2.getClassName().toLowerCase().contains( "sun" ) && !var2.getClassName().toLowerCase().contains( "log" ) && !var2.getMethodName().equals( "sendMessage" ) && !var2.getMethodName().equals( "sendRawMessage" ) )
				{
					howDeep--;

					if ( howDeep <= 0 )
					{
						style += " " + var2.getClassName() + "$" + var2.getMethodName() + ":" + var2.getLineNumber();
						break;
					}
				}
		}

		if ( style.contains( "%lv" ) )
			style = style.replaceAll( "%lv", EnumColor.fromLevel( record.getLevel() ) + record.getLevel().getLocalizedName().toUpperCase() );

		style += " " + formatMessage( record );

		if ( !style.endsWith( "\r" ) )
			style += "\n";

		if ( ex != null )
		{
			StringWriter writer = new StringWriter();
			ex.printStackTrace( new PrintWriter( writer ) );
			style += writer;
		}

		if ( !fancyConsole )
			return EnumColor.removeAltColors( style );
		else
			return EnumColor.transAltColors( style );
	}

	public boolean useColor()
	{
		return fancyConsole;
	}
}
