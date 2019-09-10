package com.marchnetworks.command.common.extractor.data.image;

import java.util.List;

public class Schedule
{
	private List<Integer> DaysOfTheWeek;
	private int StartTimeMinutes;
	private int EndTimeMinutes;
	private int RepeatIntervalMinutes;

	public Schedule( List<Integer> daysOfWeek, int startTimeMinutes, int endTimeMinutes, int repeatIntervalMinutes )
	{
		DaysOfTheWeek = daysOfWeek;
		StartTimeMinutes = startTimeMinutes;
		EndTimeMinutes = endTimeMinutes;
		RepeatIntervalMinutes = repeatIntervalMinutes;
	}

	public List<Integer> getDaysOfTheWeek()
	{
		return DaysOfTheWeek;
	}

	public void setDaysOfTheWeek( List<Integer> daysOfTheWeek )
	{
		DaysOfTheWeek = daysOfTheWeek;
	}

	public int getStartTimeMinutes()
	{
		return StartTimeMinutes;
	}

	public void setStartTimeMinutes( int startTimeMinutes )
	{
		StartTimeMinutes = startTimeMinutes;
	}

	public int getEndTimeMinutes()
	{
		return EndTimeMinutes;
	}

	public void setEndTimeMinutes( int endTimeMinutes )
	{
		EndTimeMinutes = endTimeMinutes;
	}

	public int getRepeatIntervalMinutes()
	{
		return RepeatIntervalMinutes;
	}

	public void setRepeatIntervalMinutes( int repeatIntervalMinutes )
	{
		RepeatIntervalMinutes = repeatIntervalMinutes;
	}
}
