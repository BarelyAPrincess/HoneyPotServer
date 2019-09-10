package com.marchnetworks.management.user;

import com.marchnetworks.common.spring.ApplicationContextSupport;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet( description = "Downloads User Rights", urlPatterns = {"/ExportUserRights"} )
public class UserExportServlet extends HttpServlet
{
	private static final Logger LOG = LoggerFactory.getLogger( UserExportServlet.class );

	private static final long serialVersionUID = 1L;

	private static UserService userService = ( UserService ) ApplicationContextSupport.getBean( "userServiceProxy" );

	protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		doDownload( request, response );
	}

	private void doDownload( HttpServletRequest request, HttpServletResponse response ) throws IOException
	{
		response.setContentType( "application/text" );
		try
		{
			byte[] userData = userService.exportUserRights();
			response.setHeader( "Content-Disposition", "attachment; filename=\"Command_User_Rights.xls\"" );
			ServletOutputStream op = response.getOutputStream();
			op.write( userData );
		}
		catch ( Exception e )
		{
			String error = "Critical Error getting User Data: " + e.getMessage();
			LOG.error( error );
			response.setStatus( 500 );
			PrintWriter out = response.getWriter();
			out.print( error );
		}
	}

	protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		doGet( request, response );
	}
}
