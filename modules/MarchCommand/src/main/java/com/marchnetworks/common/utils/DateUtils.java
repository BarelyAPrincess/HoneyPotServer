package com.marchnetworks.common.utils;

import org.apache.commons.lang.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import sun.util.calendar.ZoneInfo;

public class DateUtils
{
	public static final long MILLISEC = 1L;
	public static final long SECOND = 1000L;
	public static final long MINUTE = 60000L;
	public static final long HOUR = 3600000L;
	public static final long DAY = 86400000L;
	public static final long SECOND_MICROSECONDS = 1000000L;
	public static final long MINUTE_MICROSECONDS = 60000000L;
	public static final long HOUR_MICROSECONDS = 3600000000L;
	public static final long DAY_MICROSECONDS = 86400000000L;
	private static final Logger LOG = LoggerFactory.getLogger( DateUtils.class );

	private static String[] DATE_FORMATS = {"EEE, dd MMM yyyy HH:mm:ss zzz", "EEE, MMM dd yyyy HH:mm:ss zzz", "EEEE, dd-MMM-yy HH:mm:ss zzz", "EEE MMM d HH:mm:ss yyyy"};
	private static Calendar TWO_DIGIT_YEAR_START_CALENDAR;

	static
	{
		Calendar calendar = Calendar.getInstance();
		calendar.set( 2000, 0, 1, 0, 0 );
		TWO_DIGIT_YEAR_START_CALENDAR = calendar;
	}

	public static Calendar getCurrentUTCTime()
	{
		Calendar calendar = Calendar.getInstance( TimeZone.getTimeZone( "UTC" ) );
		return calendar;
	}

	public static long getCurrentUTCTimeInMillis()
	{
		return System.currentTimeMillis();
	}

	public static long getCurrentUTCTimeInMicros()
	{
		return System.currentTimeMillis() * 1000L;
	}

	public static GregorianCalendar getGregorianCalendarFromTimeInMillis( long timestamp )
	{
		GregorianCalendar calendar = new GregorianCalendar( TimeZone.getTimeZone( "UTC" ) );
		calendar.setTimeInMillis( timestamp );

		return calendar;
	}

	public static long getUTCMicrosFromDate( Date date )
	{
		return date.getTime() * 1000L;
	}

	public static boolean isTimeDifferenceLarge( Calendar c1, Calendar c2, long amount )
	{
		long l1 = c1.getTimeInMillis();
		long l2 = c2.getTimeInMillis();
		long lDiff = Math.abs( l1 - l2 );

		return lDiff >= amount;
	}

	public static String calendar2String( Calendar c )
	{
		if ( c == null )
		{
			return "";
		}
		StringBuilder sb = new StringBuilder();
		sb.append( c.get( 1 ) );
		sb.append( '-' );
		sb.append( c.get( 2 ) + 1 );
		sb.append( '-' );
		sb.append( c.get( 5 ) );
		sb.append( ' ' );
		sb.append( c.get( 11 ) );
		sb.append( ':' );
		sb.append( c.get( 12 ) );
		sb.append( ':' );
		sb.append( c.get( 13 ) );
		sb.append( '.' );
		sb.append( c.get( 14 ) );
		return sb.toString();
	}

	public static TimeZone getServerTimeZone()
	{
		TimeZone timeZone = Calendar.getInstance().getTimeZone();
		return timeZone;
	}

	public static String formatDSTDate( TimeZone timeZoneData, boolean isStartDate )
	{
		StringBuilder sb = new StringBuilder();
		if ( timeZoneData != null )
		{
			SimpleTimeZone simpleTZ = null;
			if ( ( timeZoneData instanceof ZoneInfo ) )
			{
				simpleTZ = ( ( ZoneInfo ) timeZoneData ).getLastRuleInstance();
			}
			else if ( ( timeZoneData instanceof SimpleTimeZone ) )
			{
				simpleTZ = ( SimpleTimeZone ) timeZoneData;
			}
			String NO_DATA = "0 00/00/0000 00:00:00";
			if ( simpleTZ == null )
			{
				LOG.debug( "Timezone details are in a non-readable format {}", timeZoneData.getClass() );
				return "0 00/00/0000 00:00:00";
			}

			if ( !simpleTZ.useDaylightTime() )
			{
				return "0 00/00/0000 00:00:00";
			}
			String prefix;
			if ( isStartDate )
			{
				prefix = "start";
			}
			else
			{
				prefix = "end";
			}

			try
			{
				int dayOfWeek = getIntValueThroughReflection( SimpleTimeZone.class, simpleTZ, prefix.concat( "DayOfWeek" ) );

				sb.append( dayOfWeek - 1 );
				sb.append( " " );

				int day = getIntValueThroughReflection( SimpleTimeZone.class, simpleTZ, prefix.concat( "Day" ) );
				int month = getIntValueThroughReflection( SimpleTimeZone.class, simpleTZ, prefix.concat( "Month" ) );
				int year = getIntValueThroughReflection( SimpleTimeZone.class, simpleTZ, "startYear" );
				int mode = getIntValueThroughReflection( SimpleTimeZone.class, simpleTZ, prefix.concat( "Mode" ) );

				Calendar localCalendar = Calendar.getInstance();
				localCalendar.set( 2, month );

				if ( year > 0 )
					localCalendar.set( 1, year );
				int i;
				switch ( mode )
				{
					case 1:
						localCalendar.set( 5, day );
						break;
					case 2:
						int weekOfMonth = day;
						if ( day < 0 )
						{
							weekOfMonth = localCalendar.getActualMaximum( 4 );
						}
						localCalendar.set( 7, dayOfWeek );
						localCalendar.set( 4, weekOfMonth );
						break;
					case 3:
						localCalendar.set( 5, day );

						for ( i = localCalendar.get( 5 ); localCalendar.get( 7 ) != dayOfWeek; )
						{
							i++;
							localCalendar.set( 5, i );
						}
						break;
					case 4:
						localCalendar.set( 5, day );

						for ( i = localCalendar.get( 5 ); localCalendar.get( 7 ) != dayOfWeek; )
						{
							i--;
							localCalendar.set( 5, i );
						}
				}

				int weekInMonth = localCalendar.get( 8 );

				sb.append( "0" + weekInMonth );
				sb.append( "/" );

				month++;
				sb.append( month < 10 ? "0" + month : Integer.valueOf( month ) );
				sb.append( "/" );

				if ( year == 0 )
				{
					sb.append( "0000" );
				}
				else
				{
					sb.append( year );
				}
				sb.append( " " );

				int time = getIntValueThroughReflection( SimpleTimeZone.class, simpleTZ, prefix.concat( "Time" ) );
				int timeMode = getIntValueThroughReflection( SimpleTimeZone.class, simpleTZ, prefix.concat( "TimeMode" ) );
				Calendar tempCalendar = null;
				switch ( timeMode )
				{
					case 0:
						tempCalendar = getCurrentUTCTime();

						tempCalendar.setTimeInMillis( time );
						break;

					case 1:
						int dstOffSet = 0;
						if ( !isStartDate )
						{
							dstOffSet = localCalendar.get( 16 );
						}
						tempCalendar = getCurrentUTCTime();
						tempCalendar.setTimeInMillis( time + dstOffSet );
						break;

					case 2:
						tempCalendar = Calendar.getInstance();
						int zoneOffSet = tempCalendar.get( 15 );
						if ( !isStartDate )
						{
							zoneOffSet += tempCalendar.get( 16 );
						}

						tempCalendar = getCurrentUTCTime();
						tempCalendar.setTimeInMillis( time + zoneOffSet );
				}

				int hour = tempCalendar.get( 11 );
				sb.append( hour < 10 ? "0" + hour : Integer.valueOf( hour ) );
				sb.append( ":" );
				int minutes = tempCalendar.get( 12 );
				sb.append( minutes < 10 ? "0" + minutes : Integer.valueOf( minutes ) );
				sb.append( ":" );
				int seconds = tempCalendar.get( 13 );
				sb.append( seconds < 10 ? "0" + seconds : Integer.valueOf( seconds ) );
			}
			catch ( SecurityException e )
			{
				LOG.debug( "Failed to obtain member out of SimpleTimeZone instance through Reflection. Error details {}", e );
			}
			catch ( NoSuchFieldException e )
			{
				LOG.debug( "SimpleTimeZone instance doesn't have desired member. Error details {}", e );
			}
			catch ( IllegalArgumentException e )
			{
				LOG.debug( "IllegalArgument when retrieving value of SimpleTimeZone member. Error details {}", e );
			}
			catch ( IllegalAccessException e )
			{
				LOG.debug( "IllegalAccess when retrieving value of SimpleTimeZone member. Error details {}", e );
			}
		}

		return sb.toString();
	}

	public static long getUTCTimeFromDateString( String dateValue )
	{
		Date date = null;
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat( DATE_FORMATS[0], Locale.US );
		simpleDateFormat.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
		simpleDateFormat.set2DigitYearStart( TWO_DIGIT_YEAR_START_CALENDAR.getTime() );
		for ( int i = 0; i < DATE_FORMATS.length; i++ )
		{
			if ( i > 0 )
			{
				simpleDateFormat.applyPattern( DATE_FORMATS[i] );
			}
			try
			{
				date = simpleDateFormat.parse( dateValue );
			}
			catch ( ParseException e )
			{
				LOG.debug( "Could not parse date string {} to one of the expected date formats", dateValue );
			}
		}
		if ( date == null )
		{
			return 0L;
		}
		return date.getTime();
	}

	public static String getDateStringFromMillis( long milliseconds )
	{
		if ( milliseconds > 0L )
		{
			return FastDateFormat.getInstance( "dd-MMM-yyyy HH:mm:ss" ).format( milliseconds );
		}
		return "null";
	}

	public static String getFileTimestampFromMillis( long milliseconds )
	{
		if ( milliseconds > 0L )
		{
			SimpleDateFormat dateFormat = new SimpleDateFormat( "dd-MMM-yyyy HH-mm-ss" );
			Date date = new Date( milliseconds );
			return dateFormat.format( date );
		}
		return "null";
	}

	public static String getDateStringFromMicros( long microseconds )
	{
		return getDateStringFromMillis( microseconds / 1000L );
	}

	public static String getDateInRFC1123( Date date )
	{
		SimpleDateFormat simpleFormatter = new SimpleDateFormat( DATE_FORMATS[0] );
		simpleFormatter.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
		return simpleFormatter.format( date );
	}

	private static int getIntValueThroughReflection( Class<?> clazz, Object instance, String memberName ) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException
	{
		Field field = clazz.getDeclaredField( memberName );
		field.setAccessible( true );
		return field.getInt( instance );
	}
}

