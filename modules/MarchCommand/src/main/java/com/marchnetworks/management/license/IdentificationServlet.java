package com.marchnetworks.management.license;

import com.marchnetworks.command.api.security.CommandAuthenticationDetails;
import com.marchnetworks.command.common.CollectionUtils;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.common.crypto.CryptoException;
import com.marchnetworks.common.crypto.CryptoUtils;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.utils.CommonUtils;
import com.marchnetworks.license.service.AppLicenseService;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet( {"/tokens/identity"} )
public class IdentificationServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static final String PARAMETER_APPID = "app_id";
	private static final String PARAMETER_CHALLENGE_RESPONSE = "app_id_response";
	private static final String HEADER_CHALLENGE = "X-app-id-challenge";
	private AppLicenseService appLicenseService = ( AppLicenseService ) ApplicationContextSupport.getBean( "appLicenseServiceProxy" );

	public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		String appId = request.getParameter( "app_id" );
		String appIdResponse = request.getParameter( "app_id_response" );

		if ( appId == null )
		{
			setErrorResponse( response, 400, "Client must include an app_id parameter" );
			return;
		}

		CommandAuthenticationDetails sessionDetails = CommonUtils.getAuthneticationDetails();
		if ( sessionDetails != null )
		{
			if ( sessionDetails.isIdentified() )
			{
				return;
			}

			if ( appIdResponse == null )
			{
				if ( sessionDetails.getChallengeNonce() != null )
				{
					response.setHeader( "X-app-id-challenge", sessionDetails.getChallengeNonce() );
					setErrorResponse( response, 400, "Client challenge X-app-id-challenge was already sent for this session" );
					return;
				}

				if ( appLicenseService.getIdentitySignature( appId ) == null )
				{
					setErrorResponse( response, 404, "App id " + appId + " was not found" );
					return;
				}

				String nonce = CommonAppUtils.byteToBase64( UUID.randomUUID().toString().getBytes( "UTF-8" ) );
				sessionDetails.setChallengeNonce( nonce );

				response.setHeader( "X-app-id-challenge", nonce );
				response.setStatus( 202 );
			}
			else
			{
				String nonce = sessionDetails.getChallengeNonce();
				if ( nonce == null )
				{
					setErrorResponse( response, 400, "Client challenge X-app-id-challenge was never sent for this session" );
					return;
				}

				byte[] nonceBytes = CommonAppUtils.stringBase64ToByte( nonce );

				byte[] signature = appLicenseService.getIdentitySignature( appId );
				if ( signature == null )
				{
					setErrorResponse( response, 404, "App id " + appId + " was not found" );
					return;
				}

				byte[] result = CollectionUtils.concatenate( signature, nonceBytes );

				byte[] hash = null;
				try
				{
					hash = CryptoUtils.sha1( result );
				}
				catch ( CryptoException e )
				{
					setErrorResponse( response, 500, "Server error while computing challenge" );
					return;
				}
				byte[] appIdResponseHash = CommonAppUtils.stringBase64ToByte( appIdResponse );

				if ( !Arrays.equals( hash, appIdResponseHash ) )
				{
					setErrorResponse( response, 403, "Incorrect challenge response" );
				}

				sessionDetails.setAppId( appId );
				sessionDetails.setIdentified( true );
			}
		}
		else
		{
			setErrorResponse( response, 500, "Security context not found" );
			return;
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
