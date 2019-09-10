package com.marchnetworks.management.security;

import com.marchnetworks.command.common.CommonAppUtils;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet( {"/authenticateredirect"} )
public class AuthenticationRedirectionServlet extends HttpServlet
{
	protected void processRequest( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		HttpSession session = request.getSession( false );
		if ( session != null )
		{
			String sessionId = session.getId();
			String redirectURL = request.getParameter( "redirectURL" );
			if ( !CommonAppUtils.isNullOrEmptyString( redirectURL ) )
			{
				StringBuilder sb = new StringBuilder( redirectURL );
				sb.append( "?sessionId=" );
				sb.append( sessionId );
				response.setContentType( "text/html" );
				response.setStatus( 302 );
				response.setHeader( "Location", sb.toString() );
			}
		}
	}

	protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		processRequest( request, response );
	}

	protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		processRequest( request, response );
	}
}
