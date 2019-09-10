package com.marchnetworks.command.api.schedule;

import com.marchnetworks.command.common.schedule.data.Schedule;

import java.util.List;

public abstract interface ScheduleCoreService
{
	public abstract List<Schedule> getAllSchedules( String paramString1, String paramString2, String paramString3 );

	public abstract Schedule getById( Long paramLong ) throws ScheduleException;
}
