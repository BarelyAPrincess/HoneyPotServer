package com.marchnetworks.management.instrumentation.adaptation;

import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.monitoring.diagnostics.SystemTimeDiagnosticsService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceEventServlet extends HttpServlet
{
	private static final long serialVersionUID = 4199442154833543018L;
	private static final Logger LOG = LoggerFactory.getLogger( DeviceEventServlet.class );

	protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException
	{
		DeviceEventFetcher deviceEventFetcher = ( DeviceEventFetcher ) ApplicationContextSupport.getBean( "deviceEventFetcher" );

		Map<String, String> requestParams = new HashMap();
		requestParams.put( "type", req.getParameter( "type" ) );
		requestParams.put( "id", req.getParameter( "id" ) );
		requestParams.put( "deviceId", req.getParameter( "deviceId" ) );
		requestParams.put( "noCB", req.getParameter( "noCB" ) );

		String xForwardedForHeader = req.getHeader( "X-Forwarded-For" );
		String ip;

		if ( ( CommonAppUtils.isNullOrEmptyString( xForwardedForHeader ) ) || ( "unknown".equalsIgnoreCase( xForwardedForHeader ) ) )
		{
			ip = req.getRemoteHost();
		}
		else
		{
			ip = xForwardedForHeader.split( "," )[0];
			LOG.warn( "HTTP DeviceEventServlet request X-Forwarded-For IP:{}. ", ip );
		}
		requestParams.put( "deviceRemoteAddress", ip );

		boolean processResult = deviceEventFetcher.processFetchNotification( requestParams );
		if ( processResult )
		{
			resp.setStatus( 200 );
		}
		else
		{
			resp.setStatus( 500 );
		}

		SystemTimeDiagnosticsService systemTimeService = ( SystemTimeDiagnosticsService ) ApplicationContextSupport.getBean( "systemTimeDiagnosticsService" );
		resp.setHeader( "x-sync-time", Long.toString( systemTimeService.getSystemTime() ) );
	}
}

