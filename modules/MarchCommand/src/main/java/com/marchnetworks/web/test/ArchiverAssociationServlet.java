package com.marchnetworks.web.test;

import com.marchnetworks.command.common.topology.TopologyException;
import com.marchnetworks.command.common.topology.data.ArchiverAssociation;
import com.marchnetworks.command.common.topology.data.DeviceResource;
import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.command.common.topology.data.ResourceMarkForReplacement;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.management.topology.ArchiverAssociationService;
import com.marchnetworks.management.topology.ResourceTopologyServiceIF;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet( name = "ArchiverAssociationTest", urlPatterns = {"/ArchiverAssociationTest"} )
public class ArchiverAssociationServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static final String EXTRACTOR_FAMILY_ID = "1001";
	private static ArchiverAssociationService archiverAssociationService = ( ArchiverAssociationService ) ApplicationContextSupport.getBean( "archiverAssociationServiceProxy" );
	private static ResourceTopologyServiceIF topologyService = ( ResourceTopologyServiceIF ) ApplicationContextSupport.getBean( "resourceTopologyServiceProxy_internal" );

	protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		response.setContentType( "text/html" );
		PrintWriter out = response.getWriter();
		createPageContent( request.getContextPath(), out, "Refresh complete" );
	}

	protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		response.setContentType( "text/html" );

		String status = "";
		Long[] deviceId = new Long[1];
		Long archiverId = null;
		if ( request.getParameter( "Update" ) != null )
		{
			Enumeration<String> parameterNames = request.getParameterNames();
			while ( parameterNames.hasMoreElements() )
			{
				String paramName = ( String ) parameterNames.nextElement();

				if ( paramName.equalsIgnoreCase( "ExtractorId" ) )
				{
					archiverId = Long.valueOf( Long.parseLong( request.getParameter( "ExtractorId" ) ) );
				}

				if ( ( paramName.equalsIgnoreCase( "DeviceId0" ) ) && ( !request.getParameter( "DeviceId0" ).isEmpty() ) )
				{
					deviceId[0] = Long.valueOf( Long.parseLong( request.getParameter( "DeviceId0" ) ) );
				}
			}

			ArchiverAssociation archiverAssociation = new ArchiverAssociation( archiverId, deviceId );
			archiverAssociationService.updateArchiverAssociation( archiverAssociation );
		}
		else if ( request.getParameter( "markForReplacement" ) != null )
		{
			Enumeration<String> parameterNames = request.getParameterNames();
			while ( parameterNames.hasMoreElements() )
			{
				String paramName = ( String ) parameterNames.nextElement();

				if ( ( paramName.equalsIgnoreCase( "DeviceId0" ) ) && ( !request.getParameter( "DeviceId0" ).isEmpty() ) )
				{
					deviceId[0] = Long.valueOf( Long.parseLong( request.getParameter( "DeviceId0" ) ) );
				}
			}

			try
			{
				topologyService.markForReplacement( new ResourceMarkForReplacement( deviceId[0], true ) );
			}
			catch ( TopologyException e )
			{
				e.printStackTrace();
			}
		}

		status = "Mark for replacement for device deviceid " + deviceId[0].toString();

		PrintWriter out = response.getWriter();
		createPageContent( request.getContextPath(), out, status );
	}

	public void createPageContent( String path, PrintWriter out, String status )
	{
		ArchiverAssociation[] archiverAssociations = archiverAssociationService.getArchiverAssociations();
		List<DeviceResource> devices = new ArrayList();

		List<Resource> resources = topologyService.getResources( new Class[] {DeviceResource.class} );
		for ( Resource res : resources )
		{
			DeviceResource deviceResource = ( DeviceResource ) res;
			if ( deviceResource.isRootDevice() )
			{
				devices.add( deviceResource );
			}
		}

		out.println( "<html><head>" );

		out.println( "<title>ArchiverAssociation Service</title></head><body>" );
		out.println( "<h2>UpdateArchiverAssociations</h2>" );

		out.println( "<form method='post' action ='" + path + "/ArchiverAssociationTest' >" );

		out.println( "<table border='0'>" );
		out.println( "<tr>" );
		out.println( "<td> Extractor: </td> " );
		out.println( "<td>" );
		out.println( "<select name='ExtractorId'>" );
		for ( DeviceResource device : devices )
		{
			if ( device.getDeviceView().getFamily().equalsIgnoreCase( "1001" ) )
			{
				out.println( "<option value='" + device.getId() + "'>" + device.getName() + " (" + device.getId() + ")</option>" );
			}
		}

		out.println( "</select>" );
		out.println( "</tr>" );
		out.println( "<br>" );
		out.println( "<td> Device: </td> " );
		out.println( "<td> " );

		if ( !( ( DeviceResource ) devices.get( 0 ) ).getDeviceView().getFamily().equalsIgnoreCase( "1001" ) )
		{
			out.println( "<td> <input type='checkbox' name='DeviceId0' value='" + ( ( DeviceResource ) devices.get( 0 ) ).getId() + "'> (" + ( ( DeviceResource ) devices.get( 0 ) ).getId() + ") </td>" );
			out.println( "</tr>" );
			out.println( "<br>" );
		}

		out.println( "</td>" );
		out.println( "<td> <input type='submit' name='Update' value='Update ArchiverAssociation'> </td>" );
		out.println( "</tr>" );
		out.println( "</table>" );
		out.println( "<br>" );

		out.println( "<table border='0'>" );
		out.println( "<tr>" );
		out.println( "<td> Extractor: </td> " );
		out.println( "<td>" );
		out.println( "<select name='ExtractorId'>" );
		for ( DeviceResource device : devices )
		{
			if ( device.getDeviceView().getFamily().equalsIgnoreCase( "1001" ) )
			{
				out.println( "<option value='" + device.getId() + "'>" + device.getName() + " (" + device.getId() + ")</option>" );
			}
		}
		out.println( "</select>" );
		out.println( "</tr>" );
		out.println( "<td> <input type='submit' name='deletebyArchiverId' value='deleteById'> </td>" );
		out.println( "<td> <input type='submit' name='deleteAll' value='deleteAll'> </td>" );
		out.println( "<td> <input type='submit' name='markForReplacement' value='markForReplacement'> </td>" );
		out.println( "<td> <input type='submit' name='replacement' value='replacement'> </td>" );
		out.println( "</tr>" );
		out.println( "</table>" );
		out.println( "<br>" );

		out.println( "<h4>ArchiverAssociation</h4>" );
		out.println( "<table border='1' cellpadding='2'>" );
		out.println( "<tr>" );
		out.println( "<th>Extractor ID</th>" );
		out.println( "<th>Device ID</th>" );
		out.println( "</tr>" );

		if ( ( archiverAssociations != null ) && ( archiverAssociations.length != 0 ) )
		{
			for ( ArchiverAssociation archiverAssociation : archiverAssociations )
			{
				out.println( "<tr>" );
				out.println( "<td>" + archiverAssociation.getArchiverResourceId() + "</td>" );
				for ( Long s : archiverAssociation.getDeviceResourceIds() )
				{
					out.println( "<td> " + s + " </td>" );
				}
				out.println( "</tr>" );
			}
		}
		out.println( "</table>" );
		out.println( "<br>" );

		out.println( "<input type='submit' name='refresh' value=Refresh>" );

		out.println( "</form>" );
		out.println( "<h4>Status: " + status + "</h4>" );

		out.println( "</body></html>" );
	}
}
