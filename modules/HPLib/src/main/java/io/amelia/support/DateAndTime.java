/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.support;

import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateAndTime
{
	/*
	 * Epoch Add-able Seconds
	 */
	public static final int SECOND = 1;
	public static final int SECOND_15 = 15;
	public static final int SECOND_30 = 30;
	public static final int SECOND_45 = 45;
	public static final int MINUTE = 60;
	public static final int HOUR = MINUTE * 60;
	public static final int DAY = HOUR * 24;
	public static final int YEAR = DAY * 365;
	public static final int DAYS_31 = DAY * 31;
	public static final int DAYS_30 = DAY * 30;
	public static final int DAYS_28 = DAY * 28;
	public static final int DAYS_21 = DAY * 3;
	public static final int DAYS_14 = DAY * 14;
	public static final int DAYS_7 = DAY * 7;
	public static final int DAYS_3 = DAY * 3;
	public static final int HOUR_18 = HOUR * 18;
	public static final int HOUR_12 = HOUR * 12;
	public static final int HOUR_6 = HOUR * 6;
	public static final int HOUR_4 = HOUR * 4;
	public static final int HOUR_3 = HOUR * 3;
	public static final int HOUR_2 = HOUR * 2;
	public static final int MINUTE_45 = MINUTE * 45;
	public static final int MINUTE_30 = MINUTE * 30;
	public static final int MINUTE_15 = MINUTE * 15;
	public static final int MINUTE_10 = MINUTE * 10;
	public static final int MINUTE_5 = MINUTE * 5;
	private static final Pattern INTERVAL_PATTERN = Pattern.compile( "((?:\\d+)|(?:\\d+\\.\\d+))\\s*(second|minute|hour|day|week|month|year|s|m|h|d|w)", Pattern.CASE_INSENSITIVE );

	/**
	 * The current epoch since 1970
	 *
	 * @return The current epoch
	 */
	public static long epoch()
	{
		return System.currentTimeMillis() / 1000;
	}

	public static String formatDuration( long l )
	{
		Duration duration = new Duration( l );
		PeriodFormatter formatter = new PeriodFormatterBuilder().appendDays().appendSuffix( " Day(s) " ).appendHours().appendSuffix( " Hour(s) " ).appendMinutes().appendSuffix( " Minute(s) " ).appendSeconds().appendSuffix( " Second(s)" ).toFormatter();
		return formatter.print( duration.toPeriod() );
	}

	public static int getSecondsIn( String type )
	{
		type = type.toLowerCase();

		if ( "second".equals( type ) || "s".equals( type ) )
			return 1;
		else if ( "minute".equals( type ) || "m".equals( type ) )
			return 60;
		else if ( "hour".equals( type ) || "h".equals( type ) )
			return 3600;
		else if ( "day".equals( type ) || "d".equals( type ) )
			return 86400;
		else if ( "week".equals( type ) || "w".equals( type ) )
			return 604800;
		else if ( "month".equals( type ) )
			return 2592000;
		else if ( "year".equals( type ) )
			return 31104000;

		return 0;
	}

	/**
	 * The current millis since 1970
	 *
	 * @return The current millis
	 */
	public static long millis()
	{
		return System.currentTimeMillis();
	}

	public static String now()
	{
		return now( "" );
	}

	public static String now( Date date )
	{
		return now( "", date, null );
	}

	public static String now( String format )
	{
		return now( format, "" );
	}

	public static String now( String format, Date date )
	{
		return now( format, date, null );
	}

	public static String now( String format, Date date, String def )
	{
		return now( format, date == null ? "" : String.valueOf( date.getTime() / 1000 ), def );
	}

	public static String now( String format, Object data )
	{
		return now( format, data, null );
	}

	public static String now( String format, Object data, String def )
	{
		return now( format, data.toString(), def );
	}

	public static String now( String format, String data )
	{
		return now( format, data, null );
	}

	public static String now( String format, String data, String def )
	{
		Date date = new Date();

		if ( data != null && !data.isEmpty() )
		{
			data = data.trim();

			if ( data.length() > 10 )
				data = data.substring( 0, 10 );

			try
			{
				date = new Date( Long.parseLong( data ) * 1000 );
			}
			catch ( NumberFormatException e )
			{
				if ( def != null )
					return def;
			}
		}

		if ( format == null || format.isEmpty() )
			format = "MMM dx yyyy";

		if ( format.contains( "U" ) )
			format = format.replaceAll( "U", date.getTime() / 1000 + "" );

		if ( format.contains( "x" ) )
		{
			Calendar var1 = Calendar.getInstance();
			var1.setTime( date );
			int day = var1.get( Calendar.DAY_OF_MONTH );
			String suffix = "";

			if ( day >= 11 && day <= 13 )
				suffix = "'th'";
			else
				switch ( day % 10 )
				{
					case 1:
						suffix = "'st'";
						break;
					case 2:
						suffix = "'nd'";
						break;
					case 3:
						suffix = "'rd'";
						break;
					default:
						suffix = "'th'";
						break;
				}

			format = format.replaceAll( "x", suffix );
		}

		return new SimpleDateFormat( format ).format( date );
	}

	public static int parseInterval( String arg )
	{
		if ( arg.matches( "^\\d+$" ) )
			return Integer.parseInt( arg );

		Matcher match = INTERVAL_PATTERN.matcher( arg );

		int interval = 0;

		while ( match.find() )
			interval += Math.round( Float.parseFloat( match.group( 1 ) ) * getSecondsIn( match.group( 2 ) ) );

		return interval;
	}

	/**
	 * Converts the input value into a human readable string, e.g., 0 Day(s) 3 Hour(s) 13 Minutes(s) 42 Second(s).
	 *
	 * @param seconds The duration in seconds
	 *
	 * @return Human readable duration string
	 */
	public static String readoutDuration( Number seconds )
	{
		Duration duration = new Duration( seconds );
		PeriodFormatter formatter = new PeriodFormatterBuilder().appendDays().appendSuffix( " Day(s) " ).appendHours().appendSuffix( " Hour(s) " ).appendMinutes().appendSuffix( " Minute(s) " ).appendSeconds().appendSuffix( " Second(s)" ).toFormatter();
		return formatter.print( duration.toPeriod() );
	}

	/**
	 * See {@link #readoutDuration(Number)}
	 */
	public static String readoutDuration( String seconds )
	{
		return readoutDuration( Objs.castToInt( seconds ) );
	}

	private DateAndTime()
	{

	}
}
