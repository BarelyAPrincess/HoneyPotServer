package com.marchnetworks.web.test;

import com.marchnetworks.command.common.scheduling.TaskScheduler;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.utils.JavaUtils;
import com.marchnetworks.common.utils.ServletUtils;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet( name = "TestDignostic", urlPatterns = {"/TestDiagnostic"} )
public class DiagnosticTestServlet extends HttpServlet
{
	private static final TaskScheduler taskScheduler = ( TaskScheduler ) ApplicationContextSupport.getBean( "taskScheduler" );

	public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		createResponse( request, response, "Refresh Complete" );
	}

	public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		response.setContentType( "text/html" );

		String status = "";

		if ( request.getParameter( "blockConnections" ) != null )
		{
			int connections = ServletUtils.getIntegerParameterValue( request.getParameter( "connections" ) );
			int time = ServletUtils.getIntegerParameterValue( request.getParameter( "time" ) );

			for ( int i = 0; i < connections; i++ )
			{
				DatabaseTestThread test = new DatabaseTestThread( time );
				taskScheduler.executeNow( test );
			}

			status = "Blocked " + connections + " database connections";
		}
		else if ( request.getParameter( "createThreadDump" ) != null )
		{
			JavaUtils.generateThreadDump();
			status = "Created thread dump";
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
		getServletContext().getRequestDispatcher( "/WEB-INF/pages/DiagnosticTest.jsp" ).forward( request, response );
	}
}
