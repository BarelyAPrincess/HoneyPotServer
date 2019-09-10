package com.marchnetworks.management.initialization;

import java.text.DateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class InitLogFormatter extends Formatter
{
	private static final DateFormat m_DateFormat = DateFormat.getDateInstance();
	private static final String NEWLINE = System.getProperty( "line.separator" );

	public String format( LogRecord record )
	{
		Date d = new Date( record.getMillis() );

		StringBuilder Result = new StringBuilder();
		Result.append( m_DateFormat.format( d ) );
		Result.append( " [" + record.getLevel().getName() + "] " );
		Result.append( record.getSourceClassName() + "." + record.getSourceMethodName() );
		Result.append( ": " + record.getMessage() );

		Throwable t = record.getThrown();
		if ( t != null )
		{
			Result.append( NEWLINE + genStackTraceString( t ) );
		}

		Result.append( NEWLINE );
		String s = Result.toString();
		return s;
	}

	private static String genStackTraceString( Throwable t )
	{
		StringBuilder Result = new StringBuilder();
		Result.append( " " + t.toString() + " at" + NEWLINE );

		StackTraceElement[] stes = t.getStackTrace();
		for ( int i = 0; i < stes.length - 1; i++ )
		{
			Result.append( "\t" + stes[i].toString() );
			Result.append( " at" + NEWLINE );
		}

		Result.append( "\t" + stes[( stes.length - 1 )].toString() );

		return Result.toString();
	}
}
