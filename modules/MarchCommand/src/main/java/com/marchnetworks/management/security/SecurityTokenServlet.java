package com.marchnetworks.management.security;

import com.marchnetworks.command.api.security.SamlException;
import com.marchnetworks.command.api.security.SamlException.SamlExceptionTypeEnum;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.utils.ServletUtils;
import com.marchnetworks.security.saml.SecurityTokenService;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurityTokenServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger( SecurityTokenServlet.class );

	protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		SecurityTokenService sts = ( SecurityTokenService ) ApplicationContextSupport.getBean( "securityTokenService" );
		String deviceIdParam = request.getParameter( "deviceid" );
		String tokenFormat = request.getParameter( "tokenversion" );
		String idTypeParam = ServletUtils.getStringParameterValue( request.getParameter( "idtype" ) );
		Long archiverIdParam = ServletUtils.getLongParameterValue( request.getParameter( "archiverid" ) );
		if ( deviceIdParam == null )
		{
			LOG.error( "Error creating security token: Device ID is empty" );
			setErrorResponse( response, 400, "Error creating security token: Device ID is empty" );

			return;
		}

		LOG.debug( "Token requested from remote address {} for device id {}", new Object[] {request.getRemoteAddr(), deviceIdParam} );
		String tokenContents;

		try
		{
			if ( idTypeParam == null )
			{
				tokenContents = sts.getUserSecurityToken( Long.valueOf( Long.parseLong( deviceIdParam ) ), tokenFormat, archiverIdParam );
			}
			else
			{
				tokenContents = sts.getUserSecurityToken( deviceIdParam, tokenFormat );
			}
		}
		catch ( SamlException ex )
		{
			LOG.warn( "Error creating security token.", ex );
			setErrorResponse( response, ex.getError().getStatus(), ex.getMessage() + " cause:" + ex.getCause() );
			return;
		}
		catch ( NumberFormatException nfex )
		{
			LOG.warn( "Id parameter can't be converted to a number. cause: {}", nfex );
			setErrorResponse( response, 400, nfex.getMessage() );
			return;
		}

		response.setContentType( "application/xml; charset=UTF-8" );

		ServletOutputStream out = response.getOutputStream();
		try
		{
			out.print( tokenContents );
			out.flush();
		}
		finally
		{
			out.close();
		}
	}

	private void setErrorResponse( HttpServletResponse response, int status, String message ) throws IOException
	{
		response.setStatus( status );
		PrintWriter out = response.getWriter();
		out.print( message );
		out.close();
	}
}
