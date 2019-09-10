package com.marchnetworks.schedule.events;

import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.schedule.service.ScheduleService;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Trigger;

public class ScheduleJob implements Job
{
	private static ScheduleService scheduleService = ( ScheduleService ) ApplicationContextSupport.getBean( "scheduleService" );

	public void execute( JobExecutionContext paramJobExecutionContext ) throws JobExecutionException
	{
		Long id = Long.valueOf( Long.parseLong( paramJobExecutionContext.getJobDetail().getName() ) );
		Trigger trigger = paramJobExecutionContext.getTrigger();
		boolean isStart = ( ( Boolean ) trigger.getJobDataMap().get( "isStart" ) ).booleanValue();
		scheduleService.processNotification( id, isStart );
	}
}

