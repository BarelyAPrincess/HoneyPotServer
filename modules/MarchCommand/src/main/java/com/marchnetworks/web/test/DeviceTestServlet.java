package com.marchnetworks.web.test;

import com.google.gson.reflect.TypeToken;
import com.marchnetworks.command.api.rest.DeviceRestClient;
import com.marchnetworks.command.api.rest.DeviceRestException;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.device.data.MassRegistrationInfo;
import com.marchnetworks.command.common.topology.TopologyConstants;
import com.marchnetworks.command.common.topology.TopologyException;
import com.marchnetworks.command.common.topology.data.Group;
import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.common.diagnostics.metrics.MetricsHelper;
import com.marchnetworks.common.serialization.CoreJsonSerializer;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.utils.ServletUtils;
import com.marchnetworks.management.topology.ResourceTopologyServiceIF;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

@WebServlet( name = "TestDevice", urlPatterns = {"/TestDevice"} )
public class DeviceTestServlet extends HttpServlet
{
	private ResourceTopologyServiceIF topologyService = ( ResourceTopologyServiceIF ) ApplicationContextSupport.getBean( "resourceTopologyServiceProxy_internal" );

	public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		createResponse( request, response, "Refresh Complete" );
	}

	public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		response.setContentType( "text/html" );

		String status = "";

		if ( request.getParameter( "massRegister" ) != null )
		{
			String deviceIpsString = ServletUtils.getStringParameterValue( request.getParameter( "deviceIps" ) );
			String[] deviceIps = deviceIpsString.split( "," );
			String username = ServletUtils.getStringParameterValue( request.getParameter( "username" ) );
			String password = ServletUtils.getStringParameterValue( request.getParameter( "password" ) );
			Long rootFolder = ServletUtils.getLongParameterValue( request.getParameter( "folderIdRegister" ) );
			if ( password == null )
			{
				password = "";
			}

			List<MassRegistrationInfo> registrationInformation = new ArrayList( deviceIps.length );

			for ( String deviceIp : deviceIps )
			{
				MassRegistrationInfo info = new MassRegistrationInfo( deviceIp, username, password, rootFolder );
				registrationInformation.add( info );
			}

			status = massRegister( registrationInformation, null, rootFolder );

		}
		else if ( request.getParameter( "uploadJson" ) != null )
		{
			Part filePart = request.getPart( "jsonFile" );
			InputStream filecontent = null;
			try
			{
				filecontent = filePart.getInputStream();
				String json = CommonAppUtils.readInputStream( filecontent, "UTF-8" );
				List<MassRegistrationInput> massRegistrationInputs = ( List ) CoreJsonSerializer.collectionFromJson( json, new TypeToken()
				{
				} );
				Long rootFolder = ServletUtils.getLongParameterValue( request.getParameter( "folderIdRegister" ) );

				List<MassRegistrationInfo> registrationInformation = new ArrayList( massRegistrationInputs.size() );
				for ( MassRegistrationInput input : massRegistrationInputs )
				{
					MassRegistrationInfo info = new MassRegistrationInfo( input.getIpAddress(), input.getUsername(), input.getPassword() );
					if ( input.getPath() != null )
					{
						info.setFolderPath( Arrays.asList( input.getPath() ) );
					}
					else
					{
						info.setParentId( TopologyConstants.SYSTEM_ROOT_ID );
					}
					registrationInformation.add( info );
				}

				status = massRegister( registrationInformation, null, rootFolder );
			}
			finally
			{
				if ( filecontent != null )
				{
					filecontent.close();
				}
			}
		}
		else if ( request.getParameter( "massRegisterFromEsm" ) != null )
		{

			String deviceIpsString = ServletUtils.getStringParameterValue( request.getParameter( "deviceIps" ) );
			String[] deviceIps = deviceIpsString.split( "," );
			String esmIp = ServletUtils.getStringParameterValue( request.getParameter( "esmIp" ) );
			String usernameEsm = ServletUtils.getStringParameterValue( request.getParameter( "usernameEsm" ) );
			String passwordEsm = ServletUtils.getStringParameterValue( request.getParameter( "passwordEsm" ) );
			Long rootFolder = ServletUtils.getLongParameterValue( request.getParameter( "folderIdRegister" ) );
			if ( passwordEsm == null )
			{
				passwordEsm = "";
			}
			String securityToken = null;

			DeviceRestClient esmRestClient = new DeviceRestClient( esmIp, MetricsHelper.metrics );
			esmRestClient.setAuthenticationCredentials( usernameEsm, passwordEsm );
			try
			{
				securityToken = esmRestClient.httpRequest( "/debug/login", "GET", null ).getResponseAsString();
			}
			catch ( DeviceRestException e )
			{
				status = "Error getting security token from ESM, Exception: " + e.getMessage();
				return;
			}

			List<MassRegistrationInfo> registrationInformation = new ArrayList( deviceIps.length );

			for ( String deviceIp : deviceIps )
			{
				MassRegistrationInfo info = new MassRegistrationInfo( deviceIp, TopologyConstants.SYSTEM_ROOT_ID );
				registrationInformation.add( info );
			}

			status = massRegister( registrationInformation, securityToken, rootFolder );
		}
		else if ( request.getParameter( "stopMassRegister" ) != null )
		{
			Long rootFolder = ServletUtils.getLongParameterValue( request.getParameter( "folderIdStop" ) );
			try
			{
				topologyService.stopMassRegistration( rootFolder );
				status = "Stopped mass registration for root " + topologyService.getResourcePathString( rootFolder );
			}
			catch ( TopologyException e )
			{
				status = "Problem stopping mass registration, Exception: " + e.getMessage();
			}
		}
		else
		{
			status = "Refresh complete";
		}

		createResponse( request, response, status );
	}

	private void createResponse( HttpServletRequest request, HttpServletResponse response, String status ) throws ServletException, IOException
	{
		response.setContentType( "text/html" );
		request.setAttribute( "status", status );

		List<Resource> folders = null;
		try
		{
			Resource root = topologyService.getResource( TopologyConstants.SYSTEM_ROOT_ID );
			folders = root.createFilteredResourceList( new Class[] {Group.class} );
			request.setAttribute( "folders", folders );
		}
		catch ( TopologyException e )
		{
			request.setAttribute( "status", "Failed to lookup resources in Topology" );
		}
		getServletContext().getRequestDispatcher( "/WEB-INF/pages/DeviceTest.jsp" ).forward( request, response );
	}

	private String massRegister( List<MassRegistrationInfo> registrationInformation, String securityToken, Long rootFolder )
	{
		List<MassRegistrationInfo> result = topologyService.massRegister( registrationInformation, securityToken, rootFolder );

		int failedCount = 0;
		for ( MassRegistrationInfo info : result )
		{
			if ( info.getException() != null )
			{
				failedCount++;
			}
		}
		String status = result.size() + " Devices Mass Registered";
		if ( failedCount > 0 )
		{
			status = status + ", " + failedCount + " failed";
		}

		return status;
	}
}
