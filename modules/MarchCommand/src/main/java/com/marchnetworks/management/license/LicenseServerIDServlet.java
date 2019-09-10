package com.marchnetworks.management.license;

import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.license.LicenseService;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LicenseServerIDServlet extends HttpServlet
{
	private static final long serialVersionUID = 2300516390908712664L;
	private static Logger LOG = LoggerFactory.getLogger( LicenseServerIDServlet.class );
	private static String m_sHostname;

	public void init( ServletConfig servletConfig ) throws ServletException
	{
		super.init( servletConfig );
		try
		{
			m_sHostname = InetAddress.getLocalHost().getHostName() + ".";
		}
		catch ( UnknownHostException e )
		{
			LOG.error( "Couldn't grab hostname of system, using blank id.txt for filename" );
			m_sHostname = "";
		}
	}

	public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		doDownload( request, response );
	}

	protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		doPost( request, response );
	}

	private void doDownload( HttpServletRequest req, HttpServletResponse resp ) throws IOException
	{
		LicenseService ls = ( LicenseService ) ApplicationContextSupport.getBean( "licenseService" );
		resp.setContentType( "application/text" );
		try
		{
			String serverId = ls.getServerId();
			resp.setHeader( "Content-Disposition", "attachment; filename=\"" + m_sHostname + "id.txt\"" );
			ServletOutputStream op = resp.getOutputStream();
			op.write( serverId.getBytes() );
		}
		catch ( Exception e )
		{
			String error = "Critical Error getting ServerId: " + e.getMessage();
			LOG.error( error );
			resp.setStatus( 500 );
			PrintWriter out = resp.getWriter();
			out.print( error );
		}
	}
}
