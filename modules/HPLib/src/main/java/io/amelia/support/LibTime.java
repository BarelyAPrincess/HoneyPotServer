package io.amelia.support;

import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

public class LibTime
{
	public static String formatDurection( long l )
	{
		Duration duration = new Duration( l );
		PeriodFormatter formatter = new PeriodFormatterBuilder().appendDays().appendSuffix( " Day(s) " ).appendHours().appendSuffix( " Hour(s) " ).appendMinutes().appendSuffix( " Minute(s) " ).appendSeconds().appendSuffix( " Second(s)" ).toFormatter();
		return formatter.print( duration.toPeriod() );
	}
}
