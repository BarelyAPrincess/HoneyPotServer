package com.marchnetworks.web.test;

import com.marchnetworks.command.common.transport.data.Pair;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.management.instrumentation.AudioOutputService;
import com.marchnetworks.management.instrumentation.DeviceService;
import com.marchnetworks.management.instrumentation.adaptation.DeviceEventHandlerScheduler;
import com.marchnetworks.management.instrumentation.events.AbstractDeviceEvent;
import com.marchnetworks.management.instrumentation.events.DeviceAudioOutputEvent;
import com.marchnetworks.management.instrumentation.events.DeviceOutputEventType;
import com.marchnetworks.management.instrumentation.model.AudioOutputEntity;
import com.marchnetworks.management.instrumentation.model.CompositeDeviceMBean;
import com.marchnetworks.management.instrumentation.model.DeviceMBean;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet( name = "AudioOutputTest", urlPatterns = {"/AudioOutputTest"} )
public class AudioOutputServlet extends HttpServlet
{
	private static final long serialVersionUID = 6814357323970544714L;
	private static final int STARTING_AUDIO_OUTPUT_ID = 9;
	private DeviceService deviceService = ( DeviceService ) ApplicationContextSupport.getBean( "deviceServiceProxy" );
	private AudioOutputService audioOutputService = ( AudioOutputService ) ApplicationContextSupport.getBean( "audioOutputServiceProxy" );
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
		if ( request.getParameter( "createAudioOutput" ) != null )
		{
			Long deviceId = Long.valueOf( request.getParameter( "deviceId" ) );
			String audioOutputId = request.getParameter( "audioOutputId" );
			String type = request.getParameter( "createType" );
			String name = request.getParameter( "createName" );
			String state = request.getParameter( "createState" );
			String user = request.getParameter( "createUser" );
			audioOutputService.createDeviceOutput( deviceId, audioOutputId, type, name, state, user );

			status = "Created Audio Output";
		}
		else if ( request.getParameter( "updateAudioOutput" ) != null )
		{
			Long id = Long.valueOf( request.getParameter( "id" ) );
			AudioOutputEntity audioOutput = audioOutputService.getById( id );

			String audioOutputId = audioOutput.getOutputId();
			String deviceId = String.valueOf( audioOutput.getDeviceId() );
			String state = request.getParameter( "updateState" );
			Pair[] info = audioOutput.getInfoAsPairs();

			List<AbstractDeviceEvent> eventList = new ArrayList();
			DeviceAudioOutputEvent deviceEvent = new DeviceAudioOutputEvent( deviceId, DeviceOutputEventType.OUTPUT_STATE, audioOutputId, state, info );
			eventList.add( deviceEvent );

			deviceEventHandlerScheduler.scheduleDeviceEventHandling( deviceId, eventList );

			status = "Updated Audio Output " + id + ", refresh to see changes";
		}
		else if ( request.getParameter( "deleteAudioOutput" ) != null )
		{
			Long id = Long.valueOf( request.getParameter( "id" ) );
			audioOutputService.delete( id );
		}
		else if ( request.getParameter( "deleteAll" ) != null )
		{
			audioOutputService.deleteAll();
			status = "Deleted all AudioOutputs";
		}
		else
		{
			status = "Refresh complete";
		}

		PrintWriter out = response.getWriter();
		createPageContent( request.getContextPath(), out, status );
	}

	public void createPageContent( String path, PrintWriter out, String status )
	{
		List<AudioOutputEntity> audioOutputs = audioOutputService.getAll();
		List<CompositeDeviceMBean> devices = deviceService.getAllCompositeDevices();

		int maxId = 9;
		for ( AudioOutputEntity audioOutput : audioOutputs )
		{
			String deviceAudioOutputId = audioOutput.getOutputId();
			try
			{
				int id = Integer.parseInt( deviceAudioOutputId );
				if ( id > maxId )
				{
					maxId = id;
				}
			}
			catch ( NumberFormatException e )
			{
			}
		}

		maxId++;

		out.println( "<html><head>" );

		out.println( "<title>Audio Output Test</title></head><body>" );
		out.println( "<h2>Audio Output Test</h2>" );

		out.println( "<form method='post' action ='" + path + "/AudioOutputTest' >" );

		out.println( "<table border='0'>" );
		out.println( "<tr>" );
		out.println( "<td> Type: </td> " );
		out.println( "<td> " );
		out.println( "<select name='createType'>" );
		out.println( "<option value='built_in'>built_in</option>" );
		out.println( "<option value='local'>local</option>" );
		out.println( "<option value='ip'>ip</option>" );
		out.println( "<option value='ip_camera'>ip_camera</option>" );
		out.println( "</select>" );
		out.println( "</td>" );
		out.println( "<td> Name: </td> " );
		out.println( "<td> <input type='text' name='createName' value='TestAudioOutput1' size='17'> </td>" );
		out.println( "<td> ID on Device: </td> " );
		out.println( "<td> <input type='text' name='audioOutputId' value='" + maxId + "' size='7'> </td>" );
		out.println( "<td> State: </td> " );
		out.println( "<td>" );
		out.println( "<select name='createState'>" );
		out.println( "<option value='free'>free</option>" );
		out.println( "<option value='inuse'>inuse</option>" );
		out.println( "<option value='offline'>offline</option>" );
		out.println( "</select>" );
		out.println( "</td>" );
		out.println( "<td> User: </td> " );
		out.println( "<td> <input type='text' name='createUser' value='testUser@10.53.101.102' size='21'> </td>" );
		out.println( "<td> Device: </td> " );
		out.println( "<td>" );
		out.println( "<select name='deviceId'>" );
		for ( DeviceMBean device : devices )
		{
			out.println( "<option value='" + device.getDeviceId() + "'>" + device.getName() + "</option>" );
		}
		out.println( "</select>" );
		out.println( "</td>" );
		out.println( "<td> <input type='submit' name='createAudioOutput' value='Create Audio Output'> </td>" );
		out.println( "</tr>" );

		out.println( "</table>" );
		out.println( "<br>" );

		out.println( "<table border='0'>" );
		out.println( "<tr>" );
		out.println( "<td> ID: </td> " );
		out.println( "<td>" );
		out.println( "<select name='id'>" );
		for ( AudioOutputEntity audioOutput : audioOutputs )
		{
			out.println( "<option value='" + audioOutput.getId() + "'>" + audioOutput.getId() + "</option>" );
		}
		out.println( "</select>" );
		out.println( "</td>" );
		out.println( "<td> State: </td> " );
		out.println( "<td>" );
		out.println( "<select name='updateState'>" );
		out.println( "<option value='free'>free</option>" );
		out.println( "<option value='inuse'>inuse</option>" );
		out.println( "<option value='offline'>offline</option>" );
		out.println( "</select>" );
		out.println( "</td>" );
		out.println( "<td> <input type='submit' name='updateAudioOutput' value='Update'> </td>" );
		out.println( "<td> <input type='submit' name='deleteAudioOutput' value='Delete'> </td>" );
		out.println( "</tr>" );

		out.println( "</table>" );
		out.println( "<br>" );

		out.println( "<table border='0'>" );
		out.println( "</td>" );
		out.println( "<td> <input type='submit' name='deleteAll' value='Delete All'> </td>" );
		out.println( "</tr>" );

		out.println( "</table>" );
		out.println( "<br>" );

		out.println( "<h3>Audio Outputs</h3>" );
		out.println( "<table border='1' cellpadding='2'>" );
		out.println( "<tr>" );
		out.println( "<th>ID</th>" );
		out.println( "<th>ID on Device</th>" );
		out.println( "<th>Root Device Id</th>" );
		out.println( "<th>Name</th>" );
		out.println( "<th>Type</th>" );
		out.println( "<th>AudioOut DevId</th>" );
		out.println( "<th>AudioOut Dev Address</th>" );
		out.println( "<th>Codec</th>" );
		out.println( "<th>Channels</th>" );
		out.println( "<th>Samples Per Second</th>" );
		out.println( "<th>Bits Per Sample</th>" );
		out.println( "<th>State</th>" );
		out.println( "<th>Info</th>" );
		out.println( "</tr>" );

		for ( AudioOutputEntity audioOutput : audioOutputs )
		{
			out.println( "<tr>" );
			out.println( "<td>" + audioOutput.getId() + "</td>" );
			out.println( "<td>" + audioOutput.getOutputId() + "</td>" );
			out.println( "<td>" + audioOutput.getDeviceId() + "</td>" );
			out.println( "<td>" + audioOutput.getName() + "</td>" );
			out.println( "<td>" + audioOutput.getType() + "</td>" );
			out.println( "<td>" + audioOutput.getOutputDeviceId() + "</td>" );
			out.println( "<td>" + audioOutput.getOutputDeviceAddress() + "</td>" );
			out.println( "<td>" + audioOutput.getCodec() + "</td>" );
			out.println( "<td>" + audioOutput.getChannels() + "</td>" );
			out.println( "<td>" + audioOutput.getSamplesPerSecond() + "</td>" );
			out.println( "<td>" + audioOutput.getBitsPerSample() + "</td>" );
			out.println( "<td>" + audioOutput.getState() + "</td>" );
			out.println( "<td>" + concatenatePairs( audioOutput.getInfoAsPairs() ) + "</td>" );
			out.println( "</tr>" );
		}
		out.println( "</table>" );
		out.println( "<br>" );

		out.println( "<input type='submit' name='refresh' value=Refresh>" );
		out.println( "</form>" );

		out.println( "<h4>Status: " + status + "</h4>" );

		out.println( "</body></html>" );
	}

	private String concatenatePairs( Pair[] pairs )
	{
		String result = "";
		if ( pairs != null )
		{
			for ( int i = 0; i < pairs.length; i++ )
			{
				result = result + pairs[i].getName() + ":" + pairs[i].getValue();
				if ( i < pairs.length - 1 )
				{
					result = result + ", ";
				}
			}
		}
		return result;
	}
}
