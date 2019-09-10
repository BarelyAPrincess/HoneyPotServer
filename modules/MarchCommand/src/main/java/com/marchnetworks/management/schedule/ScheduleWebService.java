package com.marchnetworks.management.schedule;

import com.marchnetworks.command.api.schedule.ScheduleException;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.schedule.data.Schedule;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.schedule.service.ScheduleService;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

@WebService( serviceName = "ScheduleService", name = "ScheduleService", portName = "SchedulePort" )
public class ScheduleWebService
{
	private ScheduleService scheduleService = ( ScheduleService ) ApplicationContextSupport.getBean( "scheduleServiceProxy" );

	@WebMethod( operationName = "updateSchedule" )
	public Schedule updateSchedule( @WebParam( name = "schedule" ) Schedule schedule ) throws ScheduleException
	{
		String username = CommonAppUtils.getUsernameFromSecurityContext();
		return scheduleService.updateSchedule( schedule, username );
	}

	@WebMethod( operationName = "deleteSchedule" )
	public void deleteSchedule( @WebParam( name = "id" ) Long id, @WebParam( name = "forceDelete" ) boolean forceDelete ) throws ScheduleException
	{
		String username = CommonAppUtils.getUsernameFromSecurityContext();
		scheduleService.deleteSchedule( id, forceDelete, username );
	}

	@WebMethod( operationName = "getAllSchedules" )
	public List<Schedule> getAllSchedules( @WebParam( name = "group" ) String group, @WebParam( name = "appId" ) String appId ) throws ScheduleException
	{
		String username = CommonAppUtils.getUsernameFromSecurityContext();
		return scheduleService.getAllSchedules( group, appId, username );
	}
}
