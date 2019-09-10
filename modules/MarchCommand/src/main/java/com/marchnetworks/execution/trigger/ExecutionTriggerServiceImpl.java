package com.marchnetworks.execution.trigger;

import com.marchnetworks.command.api.execution.trigger.ExecutionTriggerCoreService;
import com.marchnetworks.command.api.execution.trigger.ExecutionTriggerServiceException;
import com.marchnetworks.command.common.execution.trigger.ExecutionFrequency;
import com.marchnetworks.command.common.execution.trigger.ExecutionTrigger;
import com.marchnetworks.command.common.timezones.TimezonesDictionary;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecutionTriggerServiceImpl implements ExecutionTriggerService, ExecutionTriggerCoreService
{
	private Scheduler scheduler;
	private static final Logger LOG = LoggerFactory.getLogger( ExecutionTriggerServiceImpl.class );

	public void schedule( ExecutionTrigger execution, boolean create, String groupName, Class<?> clazz ) throws ExecutionTriggerServiceException
	{
		String jobName = String.valueOf( execution.getId() );
		CronTrigger trigger = new CronTrigger();
		trigger.setName( jobName );
		trigger.setGroup( groupName );
		trigger.setJobName( jobName );
		trigger.setJobGroup( groupName );

		ExecutionFrequency frequency = execution.getFrequency();
		Integer executionTime = execution.getExecutionTime();

		int hours = 0;
		int minutes = 0;

		if ( executionTime != null )
		{
			hours = executionTime / 60;
			minutes = executionTime % 60;
		}

		String cronExpression;
		if ( frequency == ExecutionFrequency.HOURLY )
		{
			cronExpression = String.format( "0 %s * * * ?", new Object[] {minutes} );
		}
		else
		{
			if ( frequency == ExecutionFrequency.DAILY )
			{
				cronExpression = String.format( "0 %s %s * * ?", new Object[] {minutes, hours} );
			}
			else
			{
				cronExpression = String.format( "0 %s %s ? * %s", new Object[] {minutes, hours, Integer.valueOf( execution.getDayOfWeek().intValue() + 1 )} );
			}
		}

		trigger.setTimeZone( TimezonesDictionary.fromWindowToTimeZone( execution.getTimeZone() ) );

		try
		{
			trigger.setCronExpression( cronExpression );

			if ( create )
			{
				JobDetail job = new JobDetail();
				job.setName( jobName );
				job.setGroup( groupName );
				job.setJobClass( clazz );

				scheduler.scheduleJob( job, trigger );
			}
			else
			{
				scheduler.rescheduleJob( jobName, groupName, trigger );
			}

		}
		catch ( Exception e )
		{
			String errorMessage = "Failed to schedule ExecutionTrigger, Exception " + e.getMessage();
			LOG.error( errorMessage );
			throw new ExecutionTriggerServiceException( errorMessage, e );
		}
	}

	public void unscheduleJob( String jobName, String jobGroup ) throws ExecutionTriggerServiceException
	{
		try
		{
			scheduler.unscheduleJob( jobName, jobGroup );
		}
		catch ( SchedulerException e )
		{
			throw new ExecutionTriggerServiceException( "Unable to unschedule job " + jobName + " of group " + jobGroup, e );
		}
	}

	public void setScheduler( Scheduler scheduler )
	{
		this.scheduler = scheduler;
	}
}
