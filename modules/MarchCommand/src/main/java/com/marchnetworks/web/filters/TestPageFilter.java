package com.marchnetworks.web.filters;

import com.marchnetworks.common.config.ConfigProperty;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.shared.config.CommonConfiguration;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;

@WebFilter( servletNames = {"AlarmTest", "AlarmLoadTest", "AudioOutputTest", "AuditLoadTest", "DeviceStatsTest", "EventTest", "DeviceLoadTest", "ArchiverAssociationTest", "TestSchedule", "TestNotification", "TestUserRights", "AppTest", "TestAudit", "TestDiagnostic", "TestDevice", "Metrics"} )
public class TestPageFilter implements Filter
{
	private static boolean enabled = false;

	static
	{
		CommonConfiguration commonConfig = ( CommonConfiguration ) ApplicationContextSupport.getBean( "commonConfiguration" );
		enabled = commonConfig.getBooleanProperty( ConfigProperty.TEST_PAGES );
	}

	public void destroy()
	{
	}

	public void init( FilterConfig filterConfig )
	{
	}

	public void doFilter( ServletRequest req, ServletResponse res, FilterChain chain ) throws IOException, ServletException
	{
		if ( !enabled )
		{
			HttpServletResponse response = ( HttpServletResponse ) res;
			response.sendError( 404 );
			return;
		}

		chain.doFilter( req, res );
	}
}
