package com.marchnetworks.web.test;

import com.marchnetworks.command.api.metrics.MetricSnapshot;
import com.marchnetworks.command.api.metrics.MetricsCoreService;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.utils.DateUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

@WebServlet( name = "Metrics", urlPatterns = {"/Metrics"} )
public class MetricsServlet extends HttpServlet
{
	private MetricsCoreService metricsService = ( MetricsCoreService ) ApplicationContextSupport.getBean( "metricsService" );

	public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		createResponse( request, response, "Refresh Complete", null );
	}

	public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		response.setContentType( "text/html" );

		String status = "";

		List<MetricSnapshot> snapshots = null;

		if ( request.getParameter( "getCurrentMetrics" ) != null )
		{
			MetricSnapshot snapshot = metricsService.getCurrentMetrics();
			snapshots = Collections.singletonList( snapshot );

			status = "Retrieved current metrics at " + DateUtils.getDateStringFromMillis( System.currentTimeMillis() );
		}
		else if ( request.getParameter( "getAllMetrics" ) != null )
		{
			snapshots = metricsService.getAllMetrics();

			status = "Retrieved all metrics";
		}
		else if ( request.getParameter( "clearCurrentMetrics" ) != null )
		{
			metricsService.clearCurrentMetrics();

			status = "Cleared current metrics";
		}
		else if ( request.getParameter( "saveCurrentMetrics" ) != null )
		{
			metricsService.saveCurrentMetrics();

			status = "Saved current metrics at " + DateUtils.getDateStringFromMillis( System.currentTimeMillis() );
		}
		else if ( request.getParameter( "uploadJson" ) != null )
		{
			Part filePart = request.getPart( "jsonFile" );
			InputStream filecontent = null;
			try
			{
				filecontent = filePart.getInputStream();
				String list = CommonAppUtils.readInputStream( filecontent, "UTF-8" );

				snapshots = metricsService.readMetricsFromLogString( list );
			}
			finally
			{
				if ( filecontent != null )
				{
					filecontent.close();
				}
			}
		}
		else
		{
			status = "Refresh complete";
		}

		createResponse( request, response, status, snapshots );
	}

	private void createResponse( HttpServletRequest request, HttpServletResponse response, String status, List<MetricSnapshot> snapshots ) throws ServletException, IOException
	{
		response.setContentType( "text/html" );
		request.setAttribute( "status", status );
		request.setAttribute( "snapshots", snapshots );

		getServletContext().getRequestDispatcher( "/WEB-INF/pages/Metrics.jsp" ).forward( request, response );
	}
}
