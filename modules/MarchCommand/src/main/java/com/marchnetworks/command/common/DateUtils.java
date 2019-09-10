package com.marchnetworks.command.common;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class DateUtils
{
	public static final long MILLISEC = 1L;
	public static final long SECOND = 1000L;
	public static final long MINUTE = 60000L;
	public static final long HOUR = 3600000L;
	public static final long DAY = 86400000L;
	public static final long WEEK = 604800000L;

	public static TimeZone getServerTimeZone()
	{
		TimeZone timeZone = Calendar.getInstance().getTimeZone();
		return timeZone;
	}

	public static String getDateStringFromMillis( long milliseconds, TimeZone timeZone, String format )
	{
		if ( milliseconds > 0L )
		{
			String defaultFormat = "dd-MMM-yyyy HH:mm:ss";
			if ( format != null )
			{
				defaultFormat = format;
			}

			LocalDate date = LocalDate.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern( defaultFormat );
			if ( timeZone != null )
				formatter = formatter.withZone( timeZone.toZoneId() );
			return date.format( formatter );
		}

		return "null";
	}

	public static String getDateStringFromMillis( long milliseconds )
	{
		return getDateStringFromMillis( milliseconds, null, null );
	}

	public static String getDateStringFromMillis( long milliseconds, String format )
	{
		return getDateStringFromMillis( milliseconds, null, format );
	}

	public static String getDateStringFromMillis( long milliseconds, TimeZone timeZone )
	{
		return getDateStringFromMillis( milliseconds, timeZone, null );
	}

	public static String getDateStringFromMicros( long microseconds )
	{
		return getDateStringFromMillis( microseconds / 1000L );
	}

	public static long currentTimeMillisRounded( long roundValue )
	{
		long time = System.currentTimeMillis();
		time -= time % roundValue;
		return time;
	}

	public static long getRoundedTime( long time, long roundValue )
	{
		time -= time % roundValue;
		return time;
	}

	public static long getLocalTimeMillis( long millisUTC )
	{
		TimeZone current = getServerTimeZone();
		return millisUTC + current.getOffset( millisUTC );
	}

	public static Long getTimestamp( Long days )
	{
		Long timestamp = Long.valueOf( System.currentTimeMillis() );
		Long removalDate = Long.valueOf( timestamp.longValue() - days.longValue() * 86400000L );
		return removalDate;
	}

	public static List<Long> getIntervals( Long start, Long end, Long interval )
	{
		List<Long> result = new ArrayList();

		for ( Long i = Long.valueOf( start.longValue() + interval.longValue() ); i.longValue() < end.longValue(); i = Long.valueOf( i.longValue() + interval.longValue() ) )
		{
			result.add( i );
		}
		result.add( end );

		return result;
	}

	public static String getHourMinuteSecFromMillis( long millis )
	{
		String result = String.format( "%02d:%02d:%02d", new Object[] {Long.valueOf( millis / 3600000L ), Long.valueOf( millis / 60000L % 60L ), Long.valueOf( millis / 1000L % 60L )} );
		return result;
	}

	public static String getDayHourMinuteSecFromMillis( long millis )
	{
		String result = String.format( "%02d %02d:%02d:%02d", new Object[] {Long.valueOf( millis / 86400000L ), Long.valueOf( millis / 3600000L % 24L ), Long.valueOf( millis / 60000L % 60L ), Long.valueOf( millis / 1000L % 60L )} );
		return result;
	}
}
