package com.marchnetworks.schedule.service;

import com.marchnetworks.command.api.schedule.ScheduleException;
import com.marchnetworks.command.common.schedule.data.Schedule;

import java.util.List;

public abstract interface ScheduleService
{
	public abstract Schedule updateSchedule( Schedule paramSchedule, String paramString ) throws ScheduleException;

	public abstract void deleteSchedule( Long paramLong, boolean paramBoolean, String paramString ) throws ScheduleException;

	public abstract List<Schedule> getAllSchedules( String paramString1, String paramString2, String paramString3 );

	public abstract Schedule getById( Long paramLong ) throws ScheduleException;

	public abstract List<Schedule> getAllSchedules();

	public abstract void processNotification( Long paramLong, boolean paramBoolean );

	public abstract boolean checkDeletions( List<Long> paramList, boolean paramBoolean );
}

