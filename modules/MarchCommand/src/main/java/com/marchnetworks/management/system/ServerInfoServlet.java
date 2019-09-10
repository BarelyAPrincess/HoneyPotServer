package com.marchnetworks.management.system;

import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.utils.ServerUtils;
import com.marchnetworks.monitoring.diagnostics.SystemTimeDiagnosticsService;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ServerInfoServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		SystemTimeDiagnosticsService systemTimeService = ( SystemTimeDiagnosticsService ) ApplicationContextSupport.getBean( "systemTimeDiagnosticsService" );

		response.setHeader( "x-sync-time", Long.toString( systemTimeService.getSystemTime() ) );
		response.setContentType( "text/plain" );
		ServletOutputStream out = response.getOutputStream();
		try
		{
			out.print( ServerUtils.getServerInfo() );
			out.flush();
		}
		finally
		{
			out.close();
		}
	}
}
