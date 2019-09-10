package com.marchnetworks.web.test;

import com.marchnetworks.alarm.data.AlarmEntryView;
import com.marchnetworks.alarm.service.AlarmTestService;
import com.marchnetworks.command.common.alarm.data.AlarmSourceView;
import com.marchnetworks.command.common.alarm.data.AlarmState;
import com.marchnetworks.command.common.transport.data.Pair;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.management.instrumentation.adaptation.DeviceEventHandlerScheduler;
import com.marchnetworks.management.instrumentation.events.AbstractDeviceEvent;
import com.marchnetworks.management.instrumentation.events.DeviceAlarmEvent;
import com.marchnetworks.management.instrumentation.events.DeviceAlarmEventType;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet( name = "AlarmLoadTest", urlPatterns = {"/AlarmLoadTest"} )
public class AlarmLoadTestServlet extends HttpServlet
{
	private static final long serialVersionUID = 8187597591733661043L;
	private AlarmTestService alarmTestService = ( AlarmTestService ) ApplicationContextSupport.getBean( "alarmTestServiceProxy" );
	private DeviceEventHandlerScheduler deviceEventHandlerScheduler = ( DeviceEventHandlerScheduler ) ApplicationContextSupport.getBean( "deviceEventHandlerScheduler" );
	private static long alarmEntryID = 20000L;

	public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		response.setContentType( "text/html" );
		PrintWriter out = response.getWriter();
		createPageContent( request.getContextPath(), out, "Refresh complete" );
	}

	public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		response.setContentType( "text/html" );

		String status = "";
		if ( request.getParameter( "createManyAlarmSources" ) != null )
		{
			int alarmNum = Integer.parseInt( request.getParameter( "alarmSourceNumber" ) );
			boolean associateDevices = false;
			if ( request.getParameter( "massAssociateDevices" ) != null )
			{
				associateDevices = true;
			}
			alarmTestService.createManyAlarmSources( alarmNum, associateDevices );
			status = alarmNum + " Alarm Sources Created";
		}
		else if ( request.getParameter( "deleteAll" ) != null )
		{
			alarmTestService.deleteTestAlarmSourcesAndEntries();
			status = "Test Alarm Sources and Entries Deleted";
		}
		else if ( request.getParameter( "triggerAll" ) != null )
		{
			triggerTestAlarmSources();
			status = "All Test Alarm Sources Triggered";
		}

		PrintWriter out = response.getWriter();
		createPageContent( request.getContextPath(), out, status );
	}

	public void createPageContent( String path, PrintWriter out, String status )
	{
		out.println( "<html><head>" );

		out.println( "<title>Alarm Load Test</title></head><body>" );
		out.println( "<h2>Alarm Load Test</h2>" );

		out.println( "<form method='post' action ='" + path + "/AlarmLoadTest' >" );

		out.println( "<table border='0'>" );
		out.println( "<tr>" );
		out.println( "<td> Number of Sources: </td> " );
		out.println( "<td> <input type='text' name='alarmSourceNumber' value='1000' size='10'> </td>" );

		out.println( "<td> <input type='checkbox' name='massAssociateDevices' value='massAssociateDevices' />Associate cameras </td>" );

		out.println( "<td> <input type='submit' name='createManyAlarmSources' value='Create Alarm Sources'> </td>" );
		out.println( "</tr>" );
		out.println( "</table>" );
		out.println( "<br>" );

		out.println( "<table border='0'>" );
		out.println( "<tr>" );

		out.println( "<td> <input type='submit' name='deleteAll' value='Delete All Test Data'> </td>" );
		out.println( "</tr>" );
		out.println( "</table>" );
		out.println( "<br>" );

		out.println( "<table border='0'>" );
		out.println( "<tr>" );

		out.println( "<td> <input type='submit' name='triggerAll' value='Trigger All Test Sources'> </td>" );
		out.println( "</tr>" );
		out.println( "</table>" );
		out.println( "<br>" );

		out.println( "</form>" );
		out.println( "<h4>Status: " + status + "</h4>" );

		out.println( "</body></html>" );
	}

	private void triggerTestAlarmSources()
	{
		List<AlarmSourceView> alarmSources = alarmTestService.getAlarmSources();
		List<AlarmEntryView> openAlarmEntries = alarmTestService.getOpenAlarmEntries();

		Map<String, List<AbstractDeviceEvent>> eventMap = new HashMap();

		for ( AlarmSourceView alarmSource : alarmSources )
		{
			if ( alarmSource.getDeviceAlarmSourceId().startsWith( "testSource" ) )
			{
				String deviceId = alarmSource.getDeviceId();
				String deviceAlarmSourceId = alarmSource.getDeviceAlarmSourceId();

				boolean found = false;
				int count = 1;
				long firstInstance = 0L;
				String deviceAlarmEntryId = "";
				long currentTime = System.currentTimeMillis() * 1000L;

				for ( AlarmEntryView openAlarmEntry : openAlarmEntries )
				{
					if ( openAlarmEntry.getAlarmSourceId().equals( alarmSource.getId() ) )
					{
						found = true;
						count = openAlarmEntry.getCount() + 1;
						firstInstance = openAlarmEntry.getFirstInstanceTime();
						deviceAlarmEntryId = openAlarmEntry.getDeviceAlarmEntryId();
						break;
					}
				}

				if ( !found )
				{
					count = 1;
					firstInstance = currentTime;
					deviceAlarmEntryId = String.valueOf( "testEntry" + alarmEntryID++ );
				}

				List<AbstractDeviceEvent> eventList = ( List ) eventMap.get( deviceId );
				if ( eventList == null )
				{
					eventList = new ArrayList();
					eventMap.put( deviceId, eventList );
				}

				DeviceAlarmEvent alarmEvent = new DeviceAlarmEvent( DeviceAlarmEventType.ALARM_STATE, deviceId, currentTime, deviceAlarmSourceId, AlarmState.ON.getValue(), null );
				eventList.add( alarmEvent );

				Pair[] details = new Pair[2];
				Pair pair = new Pair();
				pair.setName( "count" );
				pair.setValue( String.valueOf( count ) );
				details[0] = pair;
				pair = new Pair();
				pair.setName( "first" );
				pair.setValue( String.valueOf( firstInstance ) );
				details[1] = pair;

				alarmEvent = new DeviceAlarmEvent( DeviceAlarmEventType.ALARM_ENTRY, deviceId, currentTime, deviceAlarmSourceId, deviceAlarmEntryId, details );
				eventList.add( alarmEvent );
			}
		}

		for ( Entry<String, List<AbstractDeviceEvent>> eventEntry : eventMap.entrySet() )
		{
			deviceEventHandlerScheduler.scheduleDeviceEventHandling( ( String ) eventEntry.getKey(), ( List ) eventEntry.getValue() );
		}
	}
}
