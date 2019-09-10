package com.marchnetworks.command.common.extractor.data.image;

import com.marchnetworks.command.common.extractor.data.Channel;
import com.marchnetworks.command.common.schedule.data.DaySchedule;

import java.util.ArrayList;
import java.util.List;

public class ScheduledImageExtractionJob extends ImageExtractionJobBase
{
	private String TimeZone;
	private List<Schedule> Schedules;
	private List<Channel> Channels;

	public ScheduledImageExtractionJob()
	{
	}

	public ScheduledImageExtractionJob( String timezone )
	{
		TimeZone = timezone;
	}

	public void addChannels( List<String> channelIds )
	{
		if ( Channels == null )
		{
			Channels = new ArrayList();
		}
		for ( String string : channelIds )
		{
			Channel channel = new Channel();
			channel.setId( string );
			Channels.add( channel );
		}
	}

	public void clearChannels()
	{
		Channels.clear();
	}

	public void clearSchedules()
	{
		Schedules.clear();
	}

	public void addSchedules( List<DaySchedule> daySchedules, Integer downloadInterval )
	{
		if ( Schedules == null )
		{
			Schedules = new ArrayList();
		}

		for ( DaySchedule daySchedule : daySchedules )
		{
			boolean newSchedule = true;
			for ( Schedule schedule : Schedules )
			{
				if ( ( daySchedule.getStartTime().equals( Integer.valueOf( schedule.getStartTimeMinutes() ) ) ) && ( daySchedule.getEndTime().equals( Integer.valueOf( schedule.getEndTimeMinutes() ) ) ) )
				{
					schedule.getDaysOfTheWeek().add( daySchedule.getDayOfWeek() );
					newSchedule = false;
					break;
				}
			}
			if ( newSchedule )
			{
				List<Integer> days = new ArrayList();
				days.add( daySchedule.getDayOfWeek() );
				Schedule schedule = new Schedule( days, daySchedule.getStartTime().intValue(), daySchedule.getEndTime().intValue(), downloadInterval.intValue() );
				Schedules.add( schedule );
			}
		}
	}

	public String getTimeZone()
	{
		return TimeZone;
	}

	public void setTimeZone( String timeZone )
	{
		TimeZone = timeZone;
	}

	public List<Schedule> getSchedules()
	{
		return Schedules;
	}

	public void setSchedules( List<Schedule> schedules )
	{
		Schedules = schedules;
	}

	public List<Channel> getChannels()
	{
		return Channels;
	}

	public void setChannels( List<Channel> channels )
	{
		Channels = channels;
	}
}
