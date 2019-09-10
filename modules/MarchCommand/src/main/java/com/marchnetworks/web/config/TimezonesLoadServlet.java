package com.marchnetworks.web.config;

import com.marchnetworks.command.api.user.UserCoreService;
import com.marchnetworks.command.common.timezones.TimezonesDictionary;
import com.marchnetworks.command.common.user.UserException;
import com.marchnetworks.command.common.user.data.ProfileView;
import com.marchnetworks.common.spring.ApplicationContextSupport;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet( name = "LoadTimezones", urlPatterns = {"/LoadTimezones"} )
public class TimezonesLoadServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		UserCoreService userCoreService = ( UserCoreService ) ApplicationContextSupport.getBean( "userCoreServiceProxy" );

		// SecurityContextHolderAwareRequestWrapper wrapper = new SecurityContextHolderAwareRequestWrapper( request, "" );

		String username = request.getRemoteUser();
		ProfileView profile;

		try
		{
			profile = userCoreService.getProfileForUser( username );
		}
		catch ( UserException e )
		{
			throw new ServletException( "Profile could not be found for username: " + username );
		}

		if ( profile.isSuperAdmin() )
		{
			getServletContext().getRequestDispatcher( "/WEB-INF/pages/LoadTimezones.jsp" ).forward( request, response );
		}
		else
		{
			response.sendError( 403, "Only super admin can access this page." );
		}
	}

	protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		try
		{
			request.setAttribute( "result", TimezonesDictionary.addNewData( request.getPart( "xmlFile" ).getInputStream() ) );
			doGet( request, response );
		}
		catch ( Exception e )
		{
			request.setAttribute( "error", e );
			doGet( request, response );
		}
	}
}
