package com.marchnetworks.web.test;

import com.marchnetworks.alarm.alarmdetails.AlarmDetailEnum;
import com.marchnetworks.alarm.data.AlarmEntryView;
import com.marchnetworks.alarm.data.DeletedSourceAlarmEntry;
import com.marchnetworks.alarm.service.AlarmEntryCloseRecord;
import com.marchnetworks.alarm.service.AlarmException;
import com.marchnetworks.alarm.service.AlarmService;
import com.marchnetworks.alarm.service.AlarmTestService;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.alarm.data.AlarmExtendedState;
import com.marchnetworks.command.common.alarm.data.AlarmSourceView;
import com.marchnetworks.command.common.alarm.data.AlarmState;
import com.marchnetworks.command.common.transport.data.Pair;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.utils.CommonUtils;
import com.marchnetworks.common.utils.DateUtils;
import com.marchnetworks.management.instrumentation.DeviceService;
import com.marchnetworks.management.instrumentation.adaptation.DeviceEventHandlerScheduler;
import com.marchnetworks.management.instrumentation.events.AbstractDeviceEvent;
import com.marchnetworks.management.instrumentation.events.DeviceAlarmEvent;
import com.marchnetworks.management.instrumentation.events.DeviceAlarmEventType;
import com.marchnetworks.management.instrumentation.model.CompositeDeviceMBean;
import com.marchnetworks.management.instrumentation.model.DeviceMBean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet( name = "AlarmTest", urlPatterns = {"/AlarmTest"} )
public class AlarmTestServlet extends HttpServlet
{
	private static final long serialVersionUID = 6814357323970544714L;
	private static final Logger LOG = LoggerFactory.getLogger( AlarmTestServlet.class );

	private static final int STARTING_SOURCE_ID = 3000;

	private static final int STARTING_ENTRY_ID = 10000;

	private static final String ALARM_TEST_USERNAME = "localTestUser";
	private AlarmTestService alarmTestService = ( AlarmTestService ) ApplicationContextSupport.getBean( "alarmTestServiceProxy" );
	private AlarmService alarmService = ( AlarmService ) ApplicationContextSupport.getBean( "alarmServiceProxy_internal" );
	private DeviceService deviceService = ( DeviceService ) ApplicationContextSupport.getBean( "deviceServiceProxy" );
	private DeviceEventHandlerScheduler deviceEventHandlerScheduler = ( DeviceEventHandlerScheduler ) ApplicationContextSupport.getBean( "deviceEventHandlerScheduler" );

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

		if ( request.getParameter( "createAlarmSource" ) != null )
		{
			String alarmType = request.getParameter( "alarmType" );
			String alarmSubtype = request.getParameter( "alarmSubtype" );
			String alarmTypeString = alarmType;
			if ( !alarmSubtype.equals( "none" ) )
			{
				alarmTypeString = alarmTypeString + "." + alarmSubtype;
			}
			String alarmSourceName = request.getParameter( "alarmSourceName" );
			boolean associateDevices = false;
			if ( request.getParameter( "associateDevices" ) != null )
			{
				associateDevices = true;
			}
			String deviceId = request.getParameter( "deviceId" );

			int nextAlarmSourceId = getNextDeviceAlarmSourceId( deviceId );
			alarmTestService.createAlarmSource( deviceId, String.valueOf( nextAlarmSourceId ), alarmTypeString, alarmSourceName, associateDevices );

			status = "Created Alarm Source " + alarmSourceName;
		}
		else if ( request.getParameter( "setON" ) != null )
		{
			String alarmSourceId = request.getParameter( "alarmSourceId" );
			handleAlarmSourceOn( alarmSourceId );
			status = "Alarm " + alarmSourceId + " set to ON, refresh to see changes";
		}
		else if ( request.getParameter( "setOFF" ) != null )
		{
			String alarmSourceId = request.getParameter( "alarmSourceId" );
			handleAlarmSourceOff( alarmSourceId );
			status = "Alarm " + alarmSourceId + " set to OFF, refresh to see changes";
		}
		else if ( request.getParameter( "deleteAlarmSource" ) != null )
		{
			String alarmSourceId = request.getParameter( "alarmSourceId" );
			alarmService.deleteAlarmSource( alarmSourceId );
			status = "Alarm " + alarmSourceId + " deleted";
		}
		else if ( request.getParameter( "disableAlarmSource" ) != null )
		{
			String alarmSourceId = request.getParameter( "alarmSourceId" );
			handleAlarmSourceDisabled( alarmSourceId );
			status = "Alarm " + alarmSourceId + " disabled, refresh to see changes";
		}
		else if ( request.getParameter( "cutAlarmSource" ) != null )
		{
			String alarmSourceId = request.getParameter( "alarmSourceId" );
			handleAlarmSourceCut( alarmSourceId );
			status = "Alarm " + alarmSourceId + " cut, refresh to see changes";
		}
		else if ( request.getParameter( "setONRandom" ) != null )
		{
			String alarmSourceId = request.getParameter( "alarmSourceId" );
			handleAlarmSourceOnRandomAssociations( alarmSourceId );
			status = "Alarm " + alarmSourceId + " set to ON with random associations, refresh to see changes";
		}
		else if ( request.getParameter( "closeOnDevice" ) != null )
		{
			String alarmEntry = request.getParameter( "alarmEntry" );
			if ( handleAlarmEntryClosedOnDevice( alarmEntry ) )
			{
				status = "Alarm entry " + alarmEntry + " closed on device, refresh to see changes";
			}
			else
			{
				status = "Alarm entry " + alarmEntry + " can not be closed on the device, alarm source was deleted";
			}
		}
		else if ( request.getParameter( "closeOnServer" ) != null )
		{
			String alarmEntry = request.getParameter( "alarmEntry" );
			String user = request.getParameter( "handleByName" );

			if ( handleAlarmEntryClosedOnServer( alarmEntry, user ) )
			{
				status = "Alarm entry " + alarmEntry + " closed on Server, refresh to see changes";
			}
			else
			{
				status = "Failed to close Alarm Entry " + alarmEntry + " on server. See server logs for detailed information regarding the error";
			}
		}
		else if ( request.getParameter( "handle" ) != null )
		{
			String alarmEntry = request.getParameter( "alarmEntry" );
			String handleByName = request.getParameter( "handleByName" );
			setAlarmHandling( alarmEntry, true, handleByName );

			status = "Alarm entry " + alarmEntry + " handled";
		}
		else if ( request.getParameter( "unhandle" ) != null )
		{
			String alarmEntry = request.getParameter( "alarmEntry" );
			String unhandleByName = request.getParameter( "unhandleByName" );
			setAlarmHandling( alarmEntry, false, unhandleByName );

			status = "Alarm entry " + alarmEntry + " unhandled";
		}
		else if ( request.getParameter( "sendCustomEntry" ) != null )
		{
			String deviceAlarmSourceId = request.getParameter( "deviceAlarmSourceId" );
			String deviceAlarmEntryId = request.getParameter( "deviceAlarmEntryId" );
			String deviceId = request.getParameter( "customDeviceId" );
			String count = request.getParameter( "customCount" );
			handleSendCustomEntry( deviceAlarmSourceId, deviceAlarmEntryId, deviceId, count );

			status = "Custom alarm entry for source " + deviceAlarmSourceId + " sent";
		}
		else if ( request.getParameter( "sendCustomState" ) != null )
		{
			String deviceAlarmSourceId = request.getParameter( "stateDeviceAlarmSourceId" );
			String deviceId = request.getParameter( "customStateDeviceId" );
			String state = request.getParameter( "customState" );
			String extendedState = request.getParameter( "customExtendedState" );

			handleSendCustomState( deviceAlarmSourceId, deviceId, state, extendedState );

			status = "Custom alarm state for source " + deviceAlarmSourceId + " sent";
		}
		else if ( request.getParameter( "purgeEntries" ) != null )
		{
			String age = request.getParameter( "purgeAge" );
			alarmService.purgeOldAlarms( Long.parseLong( age ) );

			status = "Purged alarms older than " + age + " days";
		}
		else if ( request.getParameter( "alarmDetails" ) != null )
		{
			String alarmEntryId = request.getParameter( "alarmEntry" );

			updateAlarmDetails( alarmEntryId, request );
		}
		else
		{
			status = "Refresh complete";
		}

		PrintWriter out = response.getWriter();
		createPageContent( request.getContextPath(), out, status );
	}

	private void updateAlarmDetails( String alarmEntryId, HttpServletRequest request )
	{
		Set<AlarmDetailEnum> newDetails = new HashSet();

		String note = alarmTestService.getAlarmEntry( alarmEntryId ).getClosedText();

		if ( request.getParameter( "policeInvolved" ) != null )
			newDetails.add( AlarmDetailEnum.PARTY_INVOLVED_POLICE );
		if ( request.getParameter( "fireInvolved" ) != null )
			newDetails.add( AlarmDetailEnum.PARTY_INVOLVED_FIRE );
		if ( request.getParameter( "ambulanceInvolved" ) != null )
			newDetails.add( AlarmDetailEnum.PARTY_INVOLVED_AMBULANCE );
		if ( request.getParameter( "otherInvolved" ) != null )
			newDetails.add( AlarmDetailEnum.PARTY_INVOLVED_OTHER );
		if ( request.getParameter( "unspecifiedSeverity" ) != null )
			newDetails.add( AlarmDetailEnum.SEVERITY_UNSPECIFIED );
		if ( request.getParameter( "criticialSeverity" ) != null )
			newDetails.add( AlarmDetailEnum.SEVERITY_CRITICAL );
		if ( request.getParameter( "severeSeverity" ) != null )
			newDetails.add( AlarmDetailEnum.SEVERITY_SEVERE );
		if ( request.getParameter( "minorSeverity" ) != null )
			newDetails.add( AlarmDetailEnum.SEVERITY_MINOR );
		if ( request.getParameter( "falseSeverity" ) != null )
			newDetails.add( AlarmDetailEnum.SEVERITY_FALSE );
		if ( request.getParameter( "unspecifiedInjury" ) != null )
			newDetails.add( AlarmDetailEnum.VICTIM_UNSPECIFIED );
		if ( request.getParameter( "employeeInjury" ) != null )
			newDetails.add( AlarmDetailEnum.VICTIM_EMPLOYEE );
		if ( request.getParameter( "customerInjury" ) != null )
			newDetails.add( AlarmDetailEnum.VICTIM_CUSTOMER );
		if ( request.getParameter( "otherInjury" ) != null )
			newDetails.add( AlarmDetailEnum.VICTIM_OTHER );
		if ( request.getParameter( "noInjury" ) != null )
			newDetails.add( AlarmDetailEnum.VICTIM_NONE );
		if ( request.getParameter( "shoplifting" ) != null )
			newDetails.add( AlarmDetailEnum.INCIDENT_SHOPLIFTING );
		if ( request.getParameter( "loitering" ) != null )
			newDetails.add( AlarmDetailEnum.INCIDENT_LOITERING );
		if ( request.getParameter( "permanency" ) != null )
			newDetails.add( AlarmDetailEnum.INCIDENT_PERMANENCY );
		if ( request.getParameter( "panic" ) != null )
			newDetails.add( AlarmDetailEnum.INCIDENT_PANIC );
		if ( request.getParameter( "fall" ) != null )
			newDetails.add( AlarmDetailEnum.INCIDENT_FALL );
		if ( request.getParameter( "suspicious" ) != null )
			newDetails.add( AlarmDetailEnum.INCIDENT_SUSPICIOUS );
		if ( request.getParameter( "vandalism" ) != null )
		{
			newDetails.add( AlarmDetailEnum.INCIDENT_VANDALISM );
		}
		if ( request.getParameter( "alarmDetailsNote" ) != null )
		{
			note = request.getParameter( "alarmDetailsNote" );
		}
		try
		{
			alarmService.updateAlarmEntryDetails( "a", alarmEntryId, newDetails, note );
		}
		catch ( AlarmException e )
		{
			LOG.error( "Could not update alarm details for Alarm Entry " + alarmEntryId );
		}
	}

	public void createPageContent( String path, PrintWriter out, String status )
	{
		List<AlarmSourceView> alarmSources = alarmTestService.getAlarmSources();
		List<AlarmEntryView> alarmEntries = alarmTestService.getAlarmEntries();
		List<CompositeDeviceMBean> devices = deviceService.getAllCompositeDevices();

		out.println( "<html><head>" );

		out.println( "<title>Alarm Test</title></head><body>" );
		out.println( "<h2>Alarm Test</h2>" );

		out.println( "<form method='post' action ='" + path + "/AlarmTest' >" );

		out.println( "<table border='0'>" );
		out.println( "<tr>" );
		out.println( "<td> Type: </td> " );
		out.println( "<td> " );
		out.println( "<select name='alarmType'>" );
		out.println( "<option value='user'>user</option>" );
		out.println( "<option value='physical'>physical</option>" );
		out.println( "<option value='analytic'>analytic</option>" );
		out.println( "<option value='other'>other</option>" );
		out.println( "</select>" );
		out.println( "</td>" );
		out.println( "<td> Subtype: </td> " );
		out.println( "<td> " );
		out.println( "<select name='alarmSubtype'>" );
		out.println( "<option value='motion'>motion</option>" );
		out.println( "<option value='tripwire'>tripwire</option>" );
		out.println( "<option value='obstruction'>obstruction</option>" );
		out.println( "<option value='loiter'>loiter</option>" );
		out.println( "<option value='fall'>fall</option>" );
		out.println( "<option value='panic'>panic</option>" );
		out.println( "<option value='none'>none</option>" );
		out.println( "</select>" );
		out.println( "</td>" );
		out.println( "<td> Name: </td> " );
		out.println( "<td> <input type='text' name='alarmSourceName' value='AlarmSource1' size='20'> </td>" );
		out.println( "<td> Device: </td> " );

		out.println( "<td>" );
		out.println( "<select name='deviceId'>" );
		for ( DeviceMBean device : devices )
		{
			out.println( "<option value='" + device.getDeviceId() + "'>" + device.getName() + "</option>" );
		}
		out.println( "</select>" );
		out.println( "</td>" );
		out.println( "<td> <input type='checkbox' name='associateDevices' value='associateDevices' />Associate cameras </td>" );

		out.println( "<td> <input type='submit' name='createAlarmSource' value='Create Alarm Source'> </td>" );
		out.println( "</tr>" );
		out.println( "</table>" );
		out.println( "<br>" );

		out.println( "<table border='0'>" );
		out.println( "<tr>" );
		out.println( "<td> Alarm Source: </td> " );
		out.println( "<td> ID: </td> " );

		out.println( "<td>" );
		out.println( "<select name='alarmSourceId'>" );

		for ( AlarmSourceView alarmSource : alarmSources )
		{
			out.println( "<option value='" + alarmSource.getId() + "'>" + alarmSource.getId() + "</option>" );
		}
		out.println( "</select>" );
		out.println( "</td>" );

		out.println( "<td> <input type='submit' name='setON' value='Set ON'> </td>" );
		out.println( "<td> <input type='submit' name='setOFF' value='Set OFF'> </td>" );
		out.println( "<td> <input type='submit' name='deleteAlarmSource' value='Delete'> </td>" );
		out.println( "<td> <input type='submit' name='disableAlarmSource' value='Disable'> </td>" );
		out.println( "<td> <input type='submit' name='cutAlarmSource' value='Cut'> </td>" );
		out.println( "<td> <input type='submit' name='setONRandom' value='Set ON Random Associations'> </td>" );
		out.println( "</tr>" );

		out.println( "</table>" );
		out.println( "<br>" );

		out.println( "<table border='0'>" );
		out.println( "<tr>" );
		out.println( "<td> Alarm Entry: </td> " );
		out.println( "<td> ID: </td> " );

		out.println( "<td>" );
		out.println( "<select name='alarmEntry'>" );

		for ( AlarmEntryView alarmEntry : alarmEntries )
		{
			out.println( "<option value='" + alarmEntry.getId() + "'>" + alarmEntry.getId() + "</option>" );
		}
		out.println( "</select>" );
		out.println( "</td>" );

		out.println( "<td> <input type='submit' name='closeOnDevice' value='Close On Device'> </td>" );
		out.println( "<td> <input type='submit' name='closeOnServer' value='Close On Server'> </td>" );
		out.println( "<td> <input type='submit' name='handle' value='Handle By'> </td>" );
		out.println( "<td> <input type='text' name='handleByName' value='admin' size='10'> </td>" );
		out.println( "<td> <input type='submit' name='unhandle' value='Unhandle By'> </td>" );
		out.println( "<td> <input type='text' name='unhandleByName' value='admin' size='10'> </td>" );
		out.println( "</tr>" );

		out.println( "</table>" );
		out.println( "<br>" );

		out.println( "<table border='0'>" );
		out.println( "<tr>" );
		out.println( "<td> Device: </td> " );

		out.println( "<td>" );
		out.println( "<select name='customDeviceId'>" );
		for ( DeviceMBean device : devices )
		{
			out.println( "<option value='" + device.getDeviceId() + "'>" + device.getName() + "</option>" );
		}
		out.println( "</select>" );
		out.println( "</td>" );
		out.println( "<td> Device Source ID: </td> " );
		out.println( "<td> <input type='text' name='deviceAlarmSourceId' value='' size='10'> </td>" );
		out.println( "<td> Device Entry ID: </td> " );
		out.println( "<td> <input type='text' name='deviceAlarmEntryId' value='' size='10'> </td>" );
		out.println( "<td> Count: </td> " );
		out.println( "<td> <input type='text' name='customCount' value='1' size='5'> </td>" );

		out.println( "<td> <input type='submit' name='sendCustomEntry' value='Send Custom Alarm Entry'> </td>" );
		out.println( "</tr>" );

		out.println( "</table>" );
		out.println( "<br>" );

		out.println( "<table border='0'>" );
		out.println( "<tr>" );
		out.println( "<td> Device: </td> " );

		out.println( "<td>" );
		out.println( "<select name='customStateDeviceId'>" );
		for ( DeviceMBean device : devices )
		{
			out.println( "<option value='" + device.getDeviceId() + "'>" + device.getName() + "</option>" );
		}
		out.println( "</select>" );
		out.println( "</td>" );

		out.println( "<td> Device Source ID: </td> " );
		out.println( "<td> <input type='text' name='stateDeviceAlarmSourceId' value='' size='10'> </td>" );

		out.println( "<td> State: </td> " );
		out.println( "<td>" );
		out.println( "<select name='customState'>" );
		for ( AlarmState state : AlarmState.values() )
		{
			out.println( "<option value='" + state.getValue() + "'>" + state.getValue() + "</option>" );
		}
		out.println( "</select>" );
		out.println( "</td>" );

		out.println( "<td> Extended State: </td> " );
		out.println( "<td>" );
		out.println( "<select name='customExtendedState'>" );
		out.println( "<option value='none'>none</option>" );
		for ( AlarmExtendedState extendedState : AlarmExtendedState.values() )
		{
			out.println( "<option value='" + extendedState.getValue() + "'>" + extendedState.getValue() + "</option>" );
		}
		out.println( "</select>" );
		out.println( "</td>" );

		out.println( "<td> <input type='submit' name='sendCustomState' value='Send Custom Alarm State'> </td>" );
		out.println( "</tr>" );
		out.println( "</table>" );
		out.println( "<br>" );

		out.println( "<table border='0'>" );
		out.println( "<tr>" );
		out.println( "<td> Server Operations: </td> " );

		out.println( "<td> Max age (days): </td> " );
		out.println( "<td> <input type='text' name='purgeAge' value='365' size='5'> </td>" );
		out.println( "<td> <input type='submit' name='purgeEntries' value='Purge Entries'> </td>" );

		out.println( "</tr>" );

		out.println( "</table>" );
		out.println( "<br>" );

		out.println( "<table border='0'>" );
		out.println( "<tr>" );
		out.println( "<td> Alarm Details: </td> " );
		out.println( "</tr>" );

		out.println( "<tr>" );

		out.println( "<td> <input type='checkbox' name='policeInvolved' value='Police'> Police Involved <br> </td>" );
		out.println( "<td> <input type='checkbox' name='fireInvolved' value='Fire'> Fire Dept Involved <br> </td>" );
		out.println( "<td> <input type='checkbox' name='ambulanceInvolved' value='Ambulance'> AmbulanceInvolved <br> </td>" );
		out.println( "<td> <input type='checkbox' name='otherInvolved' value='OtherInvolved'> Other Involved <br> </td>" );

		out.println( "</tr>" );

		out.println( "<tr>" );

		out.println( "<td> <input type='checkbox' name='unspecifiedSeverity' value='UnspecifiedSeverity'> Unspecified Severity <br> </td>" );
		out.println( "<td> <input type='checkbox' name='criticialSeverity' value='CriticalSeverity'> Critical Severity <br> </td>" );
		out.println( "<td> <input type='checkbox' name='severeSeverity' value='SevereSeverity'> Severe Severity <br> </td>" );
		out.println( "<td> <input type='checkbox' name='minorSeverity' value='MinorSeverity'> Minor Severity <br> </td>" );
		out.println( "<td> <input type='checkbox' name='falseSeverity' value='FalseSeverity'> False Severity <br> </td>" );

		out.println( "</tr>" );

		out.println( "<tr>" );

		out.println( "<td> <input type='checkbox' name='unspecifiedInjury' value='UnspecifiedInjury'> Unspecified Injury <br> </td>" );
		out.println( "<td> <input type='checkbox' name='employeeInjury' value='EmployeeInjury'> Employee Injury<br> </td>" );
		out.println( "<td> <input type='checkbox' name='customerInjury' value='CustomerInjury'> Customer Injury<br> </td>" );
		out.println( "<td> <input type='checkbox' name='otherInjury' value='OtherInjury'> Other Injury <br> </td>" );
		out.println( "<td> <input type='checkbox' name='noInjury' value='NoInjury'> No Injury<br> </td>" );

		out.println( "</tr>" );

		out.println( "<tr>" );

		out.println( "<td> <input type='checkbox' name='shoplifting' value='Shoplifting'> Shoplifting <br> </td>" );
		out.println( "<td> <input type='checkbox' name='loitering' value='Loitering'> Loitering<br> </td>" );
		out.println( "<td> <input type='checkbox' name='permanency' value='Permanency'> Permanency<br> </td>" );
		out.println( "<td> <input type='checkbox' name='panic' value='Panic'> Panic <br> </td>" );
		out.println( "<td> <input type='checkbox' name='fall' value='Fall'> Fall <br> </td>" );
		out.println( "<td> <input type='checkbox' name='suspicious' value='Suspicious'> Suspicious Activity <br> </td>" );
		out.println( "<td> <input type='checkbox' name='vandalism' value='Vandalism'> Vandalism <br> </td>" );

		out.println( "</tr>" );

		out.println( "<tr>" );
		out.println( "<td>  Alarm Details Note: <input type='text' name='alarmDetailsNote'> <br> </td>" );
		out.println( "</tr>" );

		out.println( "<tr><td> <input type='submit' name='alarmDetails' value='Set Alarm Details'> </td></tr>" );

		out.println( "</table>" );
		out.println( "<br>" );

		out.println( "<input type='submit' name='refresh' value=Refresh>" );
		out.println( "</form>" );

		out.println( "<h4>Status: " + status + "</h4>" );
		out.println( "<hr>" );

		out.println( "<h4>Alarm Sources</h4>" );
		out.println( "<table border='1' cellpadding='2'>" );
		out.println( "<tr>" );
		out.println( "<th>ID</th>" );
		out.println( "<th>ID on Device</th>" );
		out.println( "<th>Device ID</th>" );
		out.println( "<th>Type</th>" );
		out.println( "<th>Name</th>" );
		out.println( "<th>State</th>" );
		out.println( "<th>Extended State</th>" );
		out.println( "<th>Associated Channels</th>" );
		out.println( "</tr>" );

		for ( AlarmSourceView alarmSource : alarmSources )
		{
			out.println( "<tr>" );
			out.println( "<td>" + alarmSource.getId() + "</td>" );
			out.println( "<td>" + alarmSource.getDeviceAlarmSourceId() + "</td>" );
			String deviceId = alarmSource.getDeviceId();
			out.println( "<td>" + deviceId + "</td>" );
			out.println( "<td>" + alarmSource.getAlarmType() + "</td>" );
			out.println( "<td>" + alarmSource.getName() + "</td>" );
			out.println( "<td>" + alarmSource.getState() + "</td>" );
			out.println( "<td>" + alarmSource.getExtendedState() + "</td>" );
			String[] channels = alarmSource.getAssociatedChannels();
			String[] channelNames = new String[channels.length];
			for ( int i = 0; i < channels.length; i++ )
			{
				channelNames[i] = deviceService.findChannelNameFromId( deviceId, channels[i] );
				if ( CommonAppUtils.isNullOrEmptyString( channelNames[i] ) )
				{
					channelNames[i] = channels[i];
				}
			}
			out.println( "<td>" + CommonUtils.concatenateStrings( channelNames ) + "</td>" );
			out.println( "</tr>" );
		}
		out.println( "</table>" );
		out.println( "<hr>" );

		out.println( "<h4>Alarm Entries</h4>" );
		out.println( "<table border='1' cellpadding='2'>" );
		out.println( "<tr>" );
		out.println( "<th>ID</th>" );
		out.println( "<th>Alarm Source ID</th>" );
		out.println( "<th>First Instance</th>" );
		out.println( "<th>Last Instance</th>" );
		out.println( "<th>Count</th>" );
		out.println( "<th>Closed Time</th>" );
		out.println( "<th>Closed By User</th>" );
		out.println( "<th>Handling Users</th>" );
		out.println( "<th>Closed Text</th>" );
		out.println( "<th>Associated Channels</th>" );
		out.println( "</tr>" );

		for ( AlarmEntryView alarmEntry : alarmEntries )
		{
			out.println( "<tr>" );
			out.println( "<td>" + alarmEntry.getId() + "</td>" );
			String alarmSourceId = alarmEntry.getAlarmSourceId();
			String deviceId = null;

			if ( ( alarmEntry instanceof DeletedSourceAlarmEntry ) )
			{
				DeletedSourceAlarmEntry deletedSourceEntry = ( DeletedSourceAlarmEntry ) alarmEntry;
				AlarmSourceView deletedSource = deletedSourceEntry.getDeletedAlarmSource();
				deviceId = deletedSource.getDeviceId();
				alarmSourceId = "deleted";
			}
			else
			{
				AlarmSourceView alarmSource = alarmTestService.getAlarmSourceData( alarmSourceId );
				if ( alarmSource != null )
				{
					deviceId = alarmSource.getDeviceId();
				}
			}

			out.println( "<td>" + alarmSourceId + "</td>" );
			out.println( "<td>" + DateUtils.getDateStringFromMicros( alarmEntry.getFirstInstanceTime() ) + "</td>" );
			out.println( "<td>" + DateUtils.getDateStringFromMicros( alarmEntry.getLastInstanceTime() ) + "</td>" );
			out.println( "<td>" + alarmEntry.getCount() + "</td>" );
			out.println( "<td>" + DateUtils.getDateStringFromMicros( alarmEntry.getClosedTime() ) + "</td>" );
			out.println( "<td>" + alarmEntry.getClosedByUser() + "</td>" );
			out.println( "<td>" + CommonUtils.concatenateStrings( alarmEntry.getHandlingUsers() ) + "</td>" );
			out.println( "<td>" + alarmEntry.getClosedText() + "</td>" );
			String[] channels = alarmEntry.getAssociatedChannels();
			String[] channelNames = new String[channels.length];
			if ( deviceId != null )
			{
				for ( int i = 0; i < channels.length; i++ )
				{
					channelNames[i] = deviceService.findChannelNameFromId( deviceId, channels[i] );
					if ( CommonAppUtils.isNullOrEmptyString( channelNames[i] ) )
					{
						channelNames[i] = channels[i];
					}
				}
			}
			out.println( "<td>" + CommonUtils.concatenateStrings( channelNames ) + "</td>" );
			out.println( "</tr>" );
		}
		out.println( "</table>" );

		out.println( "</body></html>" );
	}

	private boolean handleAlarmSourceOn( String alarmSourceId )
	{
		AlarmSourceView alarmSource = alarmTestService.getAlarmSourceData( alarmSourceId );
		String deviceId = alarmSource.getDeviceId();
		String deviceAlarmSourceId = alarmSource.getDeviceAlarmSourceId();
		AlarmEntryView alarmEntry = alarmTestService.findUnclosedAlarmEntry( alarmSourceId );

		long currentTime = System.currentTimeMillis() * 1000L;
		String deviceAlarmEntryId;
		int count;
		long firstInstance;

		if ( alarmEntry != null )
		{
			count = alarmEntry.getCount() + 1;
			firstInstance = alarmEntry.getFirstInstanceTime();
			deviceAlarmEntryId = alarmEntry.getDeviceAlarmEntryId();
		}
		else
		{
			count = 1;
			firstInstance = currentTime;
			int nextAlarmEntryId = getNextDeviceAlarmEntryId( deviceId );
			deviceAlarmEntryId = String.valueOf( nextAlarmEntryId );
		}

		List<AbstractDeviceEvent> eventList = new ArrayList();

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

		deviceEventHandlerScheduler.scheduleDeviceEventHandling( deviceId, eventList );

		return true;
	}

	private boolean handleAlarmSourceOff( String alarmSourceId )
	{
		AlarmSourceView alarmSource = alarmTestService.getAlarmSourceData( alarmSourceId );
		String deviceId = alarmSource.getDeviceId();
		String deviceAlarmSourceId = alarmSource.getDeviceAlarmSourceId();
		long currentTime = System.currentTimeMillis() * 1000L;

		List<AbstractDeviceEvent> eventList = new ArrayList();

		DeviceAlarmEvent alarmEvent = new DeviceAlarmEvent( DeviceAlarmEventType.ALARM_STATE, deviceId, currentTime, deviceAlarmSourceId, AlarmState.OFF.getValue(), null );
		eventList.add( alarmEvent );

		deviceEventHandlerScheduler.scheduleDeviceEventHandling( deviceId, eventList );
		return true;
	}

	private boolean handleAlarmSourceDisabled( String alarmSourceId )
	{
		AlarmSourceView alarmSource = alarmTestService.getAlarmSourceData( alarmSourceId );
		String deviceId = alarmSource.getDeviceId();
		String deviceAlarmSourceId = alarmSource.getDeviceAlarmSourceId();
		long currentTime = System.currentTimeMillis() * 1000L;

		List<AbstractDeviceEvent> eventList = new ArrayList();

		DeviceAlarmEvent alarmEvent = new DeviceAlarmEvent( DeviceAlarmEventType.ALARM_STATE, deviceId, currentTime, deviceAlarmSourceId, AlarmState.DISABLED.getValue(), null );
		eventList.add( alarmEvent );

		deviceEventHandlerScheduler.scheduleDeviceEventHandling( deviceId, eventList );
		return true;
	}

	private boolean handleAlarmSourceCut( String alarmSourceId )
	{
		AlarmSourceView alarmSource = alarmTestService.getAlarmSourceData( alarmSourceId );
		String deviceId = alarmSource.getDeviceId();
		String deviceAlarmSourceId = alarmSource.getDeviceAlarmSourceId();
		long currentTime = System.currentTimeMillis() * 1000L;
		Pair[] details = new Pair[1];
		Pair pair = new Pair();
		pair.setName( "extState" );
		pair.setValue( AlarmExtendedState.CUT.getValue() );
		details[0] = pair;

		List<AbstractDeviceEvent> eventList = new ArrayList();

		DeviceAlarmEvent alarmEvent = new DeviceAlarmEvent( DeviceAlarmEventType.ALARM_STATE, deviceId, currentTime, deviceAlarmSourceId, AlarmState.NOTREADY.getValue(), details );
		eventList.add( alarmEvent );

		deviceEventHandlerScheduler.scheduleDeviceEventHandling( deviceId, eventList );
		return true;
	}

	private boolean handleAlarmSourceOnRandomAssociations( String alarmSourceId )
	{
		AlarmSourceView alarmSource = alarmTestService.getAlarmSourceData( alarmSourceId );
		String deviceId = alarmSource.getDeviceId();
		String deviceAlarmSourceId = alarmSource.getDeviceAlarmSourceId();
		AlarmEntryView alarmEntry = alarmTestService.findUnclosedAlarmEntry( alarmSourceId );

		long currentTime = System.currentTimeMillis() * 1000L;
		String deviceAlarmEntryId;
		int count;
		long firstInstance;

		if ( alarmEntry != null )
		{
			count = alarmEntry.getCount() + 1;
			firstInstance = alarmEntry.getFirstInstanceTime();
			deviceAlarmEntryId = alarmEntry.getDeviceAlarmEntryId();
		}
		else
		{
			count = 1;
			firstInstance = currentTime;
			int nextAlarmEntryId = getNextDeviceAlarmEntryId( deviceId );
			deviceAlarmEntryId = String.valueOf( nextAlarmEntryId );
		}

		List<AbstractDeviceEvent> eventList = new ArrayList();

		Random random = new Random();
		List<String> channels = deviceService.findChannelIdsFromDeviceAndChildren( deviceId );
		Set<String> channelsSet = new LinkedHashSet();

		for ( String channel : channels )
		{
			if ( random.nextBoolean() )
			{
				channelsSet.add( channel );
			}
		}

		int i = 0;
		Pair[] details = new Pair[channelsSet.size()];

		for ( String channel : channelsSet )
		{
			Pair pair = new Pair();
			pair.setName( "assocId" );
			pair.setValue( channel );
			details[( i++ )] = pair;
		}

		DeviceAlarmEvent alarmEvent = new DeviceAlarmEvent( DeviceAlarmEventType.ALARM_STATE, deviceId, currentTime, deviceAlarmSourceId, AlarmState.ON.getValue(), details );
		eventList.add( alarmEvent );

		i = 0;
		details = new Pair[channelsSet.size() + 2];
		Pair pair = new Pair();
		pair.setName( "count" );
		pair.setValue( String.valueOf( count ) );
		details[( i++ )] = pair;
		pair = new Pair();
		pair.setName( "first" );
		pair.setValue( String.valueOf( firstInstance ) );
		details[( i++ )] = pair;

		for ( String channel : channelsSet )
		{
			pair = new Pair();
			pair.setName( "assocId" );
			pair.setValue( channel );
			details[( i++ )] = pair;
		}

		alarmEvent = new DeviceAlarmEvent( DeviceAlarmEventType.ALARM_ENTRY, deviceId, currentTime, deviceAlarmSourceId, deviceAlarmEntryId, details );
		eventList.add( alarmEvent );

		deviceEventHandlerScheduler.scheduleDeviceEventHandling( deviceId, eventList );

		return true;
	}

	private boolean handleAlarmEntryClosedOnDevice( String alarmEntryId )
	{
		AlarmEntryView alarmEntry = alarmTestService.getAlarmEntry( alarmEntryId );
		AlarmSourceView alarmSource = alarmTestService.getAlarmSourceData( alarmEntry.getAlarmSourceId() );
		if ( alarmSource == null )
		{
			return false;
		}
		String deviceAlarmEntryId = alarmEntry.getDeviceAlarmEntryId();
		String deviceId = alarmSource.getDeviceId();
		String deviceAlarmSourceId = alarmSource.getDeviceAlarmSourceId();
		long currentTime = System.currentTimeMillis() * 1000L;

		List<AbstractDeviceEvent> eventList = new ArrayList();

		int count = alarmEntry.getCount();
		long firstInstance = alarmEntry.getFirstInstanceTime();
		long lastInstance = alarmEntry.getLastInstanceTime();

		Pair[] details = new Pair[4];
		Pair pair = new Pair();
		pair.setName( "closedByUser" );
		pair.setValue( "localTestUser" );
		details[0] = pair;
		pair = new Pair();
		pair.setName( "count" );
		pair.setValue( String.valueOf( count ) );
		details[1] = pair;
		pair = new Pair();
		pair.setName( "first" );
		pair.setValue( String.valueOf( firstInstance ) );
		details[2] = pair;
		pair = new Pair();
		pair.setName( "last" );
		pair.setValue( String.valueOf( lastInstance ) );
		details[3] = pair;

		DeviceAlarmEvent alarmEvent = new DeviceAlarmEvent( DeviceAlarmEventType.ALARM_CLOSED, deviceId, currentTime, deviceAlarmSourceId, deviceAlarmEntryId, details );
		eventList.add( alarmEvent );

		deviceEventHandlerScheduler.scheduleDeviceEventHandling( deviceId, eventList );
		return true;
	}

	private boolean handleAlarmEntryClosedOnServer( String alarmEntryId, String userName )
	{
		AlarmEntryCloseRecord closeRecord = new AlarmEntryCloseRecord( alarmEntryId, "Closing alarmEntry " + alarmEntryId );
		try
		{
			alarmService.closeAlarmEntries( userName, new AlarmEntryCloseRecord[] {closeRecord} );
			return true;
		}
		catch ( AlarmException e )
		{
		}
		return false;
	}

	private void setAlarmHandling( String alarmEntryId, boolean handling, String name )
	{
		String[] entries = new String[1];
		entries[0] = alarmEntryId;
		try
		{
			alarmService.setAlarmHandling( name, entries, handling );
		}
		catch ( AlarmException e )
		{
			LOG.error( "AlarmTest servlet could not handle alarm " + e );
		}
	}

	private boolean handleSendCustomEntry( String deviceAlarmSourceId, String deviceAlarmEntryId, String deviceId, String countString )
	{
		int count = Integer.parseInt( countString );
		long currentTime = System.currentTimeMillis() * 1000L;
		long firstInstance = currentTime;

		List<AbstractDeviceEvent> eventList = new ArrayList();

		Pair[] details = new Pair[2];
		Pair pair = new Pair();
		pair.setName( "count" );
		pair.setValue( String.valueOf( count ) );
		details[0] = pair;
		pair = new Pair();
		pair.setName( "first" );
		pair.setValue( String.valueOf( firstInstance ) );
		details[1] = pair;

		DeviceAlarmEvent alarmEvent = new DeviceAlarmEvent( DeviceAlarmEventType.ALARM_ENTRY, deviceId, currentTime, deviceAlarmSourceId, deviceAlarmEntryId, details );
		eventList.add( alarmEvent );

		deviceEventHandlerScheduler.scheduleDeviceEventHandling( deviceId, eventList );

		return true;
	}

	private boolean handleSendCustomState( String deviceAlarmSourceId, String deviceId, String state, String extendedState )
	{
		long currentTime = System.currentTimeMillis() * 1000L;
		List<AbstractDeviceEvent> eventList = new ArrayList();

		Pair[] details = null;
		if ( !extendedState.equals( "none" ) )
		{
			details = new Pair[1];
			Pair pair = new Pair();
			pair.setName( "extState" );
			pair.setValue( extendedState );
			details[0] = pair;
		}

		DeviceAlarmEvent alarmEvent = new DeviceAlarmEvent( DeviceAlarmEventType.ALARM_STATE, deviceId, currentTime, deviceAlarmSourceId, state, details );
		eventList.add( alarmEvent );

		deviceEventHandlerScheduler.scheduleDeviceEventHandling( deviceId, eventList );

		return true;
	}

	private int getNextDeviceAlarmSourceId( String deviceId )
	{
		List<AlarmSourceView> alarmSources = alarmTestService.getAlarmSourcesIncludeDeleted();
		int maxId = 3000;
		for ( AlarmSourceView alarmSource : alarmSources )
		{
			String deviceAlarmSourceId = alarmSource.getDeviceAlarmSourceId();
			try
			{
				int id = Integer.parseInt( deviceAlarmSourceId );
				if ( id > maxId )
				{
					maxId = id;
				}
			}
			catch ( NumberFormatException e )
			{
			}
		}

		return maxId + 1;
	}

	private int getNextDeviceAlarmEntryId( String deviceId )
	{
		List<AlarmEntryView> alarmEntries = alarmTestService.getAlarmEntries();
		int maxId = 10000;
		for ( AlarmEntryView alarmEntry : alarmEntries )
		{
			String deviceAlarmEntryId = alarmEntry.getDeviceAlarmEntryId();
			try
			{
				int id = Integer.parseInt( deviceAlarmEntryId );
				if ( id > maxId )
				{
					maxId = id;
				}
			}
			catch ( NumberFormatException e )
			{
			}
		}

		return maxId + 1;
	}
}
