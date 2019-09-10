package com.marchnetworks.management.logdownload;

import com.marchnetworks.common.spring.ApplicationContextSupport;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogDownloadServlet extends HttpServlet
{
	private static final long serialVersionUID = -2766430794586770326L;
	private static Logger LOG = LoggerFactory.getLogger( LogDownloadServlet.class );

	private LogDownloadBean logService = ( LogDownloadBean ) ApplicationContextSupport.getBean( "logDownloadBean" );

	public void init( ServletConfig servletConfig ) throws ServletException
	{
		super.init( servletConfig );
		LOG.info( "init" );
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
		ServletOutputStream op = logService.getDownloadFile( resp );

		LOG.info( "************************************fetching the LOG file....................." );

		op.flush();
		op.close();
	}
}
