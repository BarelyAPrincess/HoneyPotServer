package com.marchnetworks.web.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.amelia.march.Helper;
import io.amelia.support.IO;

@WebServlet( name = "CommandClientServlet", urlPatterns = {"/CommandClientServlet"} )
public class CommandClientServlet extends HttpServlet
{
	private static final String FILE_NAME_x64 = "CommandClient_x64.exe";
	private static final String FILE_NAME_x86 = "CommandClient_x86.exe";
	private static final String WOW_64 = "WOW64";
	private static final String WIN_64 = "Win64";
	private static final String X_64 = "x64";

	protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		String userAgentStr = request.getHeader( "User-Agent" );
		String xPlatform = request.getHeader( "x-platform" );

		Path matchWebDirectory = Helper.getWebDirectory();

		Path clientFile = matchWebDirectory.resolve( "CommandClient_x64.exe" );

		if ( ( xPlatform != null ) && ( !xPlatform.contains( "x64" ) ) )
		{
			clientFile = matchWebDirectory.resolve( "CommandClient_x86.exe" );
		}
		else if ( ( userAgentStr != null ) && ( !userAgentStr.contains( "WOW64" ) ) && ( !userAgentStr.contains( "Win64" ) ) )
		{
			clientFile = matchWebDirectory.resolve( "CommandClient_x86.exe" );
		}

		response.setHeader( "Content-Disposition", String.format( "attachment; filename=\"%s\"", clientFile.getFileName() ) );
		response.setContentType( "application/octet-stream" );

		InputStream in = Files.newInputStream( clientFile );
		try
		{
			byte[] bytes = IO.readStreamToBytes( in );
			response.setContentLength( bytes.length );
			ServletOutputStream out = response.getOutputStream();
			out.write( bytes );
		}
		finally
		{
			IO.closeQuietly( in );
		}
	}
}
