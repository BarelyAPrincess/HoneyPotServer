package com.marchnetworks.web.test;

import com.marchnetworks.app.data.App;
import com.marchnetworks.app.service.AppManager;
import com.marchnetworks.command.api.schedule.ScheduleException;
import com.marchnetworks.command.common.schedule.data.DaySchedule;
import com.marchnetworks.command.common.schedule.data.Schedule;
import com.marchnetworks.command.common.user.data.MemberView;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.utils.ServletUtils;
import com.marchnetworks.management.user.UserService;
import com.marchnetworks.schedule.service.ScheduleService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet( name = "TestSchedule", urlPatterns = {"/TestSchedule"} )
public class ScheduleTestServlet extends HttpServlet
{
	private ScheduleService schedulerService = ( ScheduleService ) ApplicationContextSupport.getBean( "scheduleServiceProxy" );
	private UserService userService = ( UserService ) ApplicationContextSupport.getBean( "userServiceProxy" );
	private AppManager appService = ( AppManager ) ApplicationContextSupport.getBean( "appManagerProxy_internal" );

	public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		createResponse( request, response, null, "Refresh Complete" );
	}

	public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		response.setContentType( "text/html" );

		String status = "";

		Long deleteScheduleId = ServletUtils.getParameterId( request.getParameterNames(), "deleteSchedule" );
		List<Schedule> schedules = null;

		if ( request.getParameter( "updateSchedule" ) != null )
		{
			Long id = ServletUtils.getLongParameterValue( request.getParameter( "id" ) );
			String name = ServletUtils.getStringParameterValue( request.getParameter( "name" ) );
			String group = ServletUtils.getStringParameterValue( request.getParameter( "group" ) );
			String appId = ServletUtils.getStringParameterValue( request.getParameter( "appId" ) );

			List<MemberView> members = userService.listAllMembers();
			String adminUser = "";
			for ( MemberView member : members )
			{
				if ( userService.isSuperAdmin( member.getName() ) )
				{
					adminUser = member.getName();
					break;
				}
			}

			String[] daysOfWeek = request.getParameterValues( "dayOfWeek" );
			String[] starts = request.getParameterValues( "startTime" );
			String[] ends = request.getParameterValues( "endTime" );

			String systemIdInput = ServletUtils.getStringParameterValue( request.getParameter( "systemId" ) );
			String logicalIdInput = ServletUtils.getStringParameterValue( request.getParameter( "logicalId" ) );

			Schedule schedule = new Schedule();
			schedule.setId( id );
			schedule.setName( name );
			schedule.setGroup( group );
			schedule.setAppId( appId );

			if ( systemIdInput != null )
			{
				schedule.setSystemRoots( Collections.singleton( Long.valueOf( Long.parseLong( systemIdInput ) ) ) );
			}
			if ( logicalIdInput != null )
			{
				schedule.setLogicalRoots( Collections.singleton( Long.valueOf( Long.parseLong( logicalIdInput ) ) ) );
			}

			for ( int i = 0; i < daysOfWeek.length; i++ )
			{
				DaySchedule daySchedule = new DaySchedule();
				daySchedule.setDayOfWeek( Integer.valueOf( Integer.parseInt( daysOfWeek[i] ) ) );
				daySchedule.setStartTime( Integer.valueOf( Integer.parseInt( starts[i] ) ) );
				daySchedule.setEndTime( Integer.valueOf( Integer.parseInt( ends[i] ) ) );
				schedule.addDaySchedule( daySchedule );
			}
			try
			{
				schedulerService.updateSchedule( schedule, adminUser );
				status = "Schedule Created";
			}
			catch ( ScheduleException e )
			{
				status = "Error creating schedule, Exception: " + e.getMessage();
			}
		}
		else if ( deleteScheduleId != null )
		{
			List<MemberView> members = userService.listAllMembers();
			String adminUser = "";
			for ( MemberView member : members )
			{
				if ( userService.isSuperAdmin( member.getName() ) )
				{
					adminUser = member.getName();
					break;
				}
			}
			try
			{
				schedulerService.deleteSchedule( deleteScheduleId, true, adminUser );
				status = "Schedule " + deleteScheduleId + " deleted";
			}
			catch ( ScheduleException e )
			{
				status = "Error deleting schedule, Exception: " + e.getMessage();
			}
		}
		else if ( request.getParameter( "getSchedules" ) != null )
		{
			String group = ServletUtils.getStringParameterValue( request.getParameter( "groupSchedules" ) );
			String appId = ServletUtils.getStringParameterValue( request.getParameter( "appIdSchedules" ) );
			String user = ServletUtils.getStringParameterValue( request.getParameter( "usernameSchedules" ) );
			schedules = schedulerService.getAllSchedules( group, appId, user );
		}
		else
		{
			status = "Refresh complete";
		}

		createResponse( request, response, schedules, status );
	}

	private void createResponse( HttpServletRequest request, HttpServletResponse response, List<Schedule> schedules, String status ) throws ServletException, IOException
	{
		response.setContentType( "text/html" );
		if ( schedules == null )
		{
			schedules = schedulerService.getAllSchedules();
		}

		App[] apps = appService.getApps();
		request.setAttribute( "schedules", schedules );
		request.setAttribute( "apps", apps );
		request.setAttribute( "status", status );

		getServletContext().getRequestDispatcher( "/WEB-INF/pages/ScheduleTest.jsp" ).forward( request, response );
	}
}
