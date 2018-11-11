/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation.tasks;

/**
 * Provides tick constants
 *
 * Ticks values are only estimates, if the server is running slow or, GOD forbid, fast then exact times will vary.
 */
public class Ticks
{
	// Milliseconds
	public static final long MILLIS_50 = 1;
	public static final long MILLIS_100 = 2;
	public static final long MILLIS_150 = 3;
	public static final long MILLIS_200 = 4;
	public static final long MILLIS_250 = 5;
	public static final long MILLIS_300 = 6;
	public static final long MILLIS_400 = 8;
	public static final long MILLIS_500 = 10;
	public static final long MILLIS_600 = 12;
	public static final long MILLIS_700 = 14;
	public static final long MILLIS_750 = 15;
	public static final long MILLIS_800 = 16;
	public static final long MILLIS_900 = 18;

	// Seconds
	public static final long SECOND = 20;
	public static final long SECOND_2 = SECOND * 2;
	public static final long SECOND_2_5 = 50;
	public static final long SECOND_5 = SECOND * 5;
	public static final long SECOND_10 = SECOND * 10;
	public static final long SECOND_15 = SECOND * 15;
	public static final long SECOND_30 = SECOND * 30;
	public static final long SECOND_45 = SECOND * 45;

	// Minutes
	public static final long MINUTE = SECOND * 60;
	public static final long MINUTE_2 = MINUTE * 2;
	public static final long MINUTE_3 = MINUTE * 3;
	public static final long MINUTE_5 = MINUTE * 5;
	public static final long MINUTE_10 = MINUTE * 10;
	public static final long MINUTE_15 = MINUTE * 15;
	public static final long MINUTE_30 = MINUTE * 30;
	public static final long MINUTE_45 = MINUTE * 45;

	// Hours
	public static final long HOUR = MINUTE * 60;
	public static final long HOUR_2 = HOUR * 2;
	public static final long HOUR_3 = HOUR * 3;
	public static final long HOUR_4 = HOUR * 4;
	public static final long HOUR_5 = HOUR * 5;
	public static final long HOUR_6 = HOUR * 6;
	public static final long HOUR_12 = HOUR * 12;
	public static final long HOUR_18 = HOUR * 18;

	// Days
	public static final long DAY = HOUR * 24;
	public static final long DAYS_3 = DAY * 3;
	public static final long DAYS_7 = DAY * 7;
	public static final long DAYS_14 = DAY * 14;
	public static final long DAYS_21 = DAY * 3;
	public static final long DAYS_28 = DAY * 28;
	public static final long DAYS_30 = DAY * 30;
	public static final long DAYS_31 = DAY * 31;
}
