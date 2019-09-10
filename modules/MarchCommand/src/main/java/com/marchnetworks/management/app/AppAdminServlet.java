package com.marchnetworks.management.app;

import com.marchnetworks.app.service.AppManager;
import com.marchnetworks.command.common.app.AppException;
import com.marchnetworks.command.common.app.AppExceptionTypeEnum;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.utils.CommonUtils;
import com.marchnetworks.management.filestorage.ChunkUploadError;
import com.marchnetworks.management.filestorage.ChunkUploadHelper;
import com.marchnetworks.management.filestorage.ChunkUploadServlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

@WebServlet( {"/App"} )
public class AppAdminServlet extends HttpServlet implements ChunkUploadServlet
{
	private static final long serialVersionUID = 1L;
	private AppManager appManager = ( AppManager ) ApplicationContextSupport.getBean( "appManagerProxy" );
	protected ChunkUploadHelper chunkUploadHelper;

	public void init( ServletConfig servletConfig ) throws ServletException
	{
		super.init( servletConfig );
		chunkUploadHelper = new ChunkUploadHelper( this );
	}

	public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		String appId = request.getParameter( "id" );
		String fileString = null;
		try
		{
			fileString = appManager.getClientFile( appId );
		}
		catch ( AppException e )
		{
			setErrorResponse( response, e.getError().toString(), e.getMessage() );
			return;
		}

		File xapFile = new File( fileString );
		String etag = CommonUtils.getFileEtag( xapFile );

		String requestEtagValue = request.getHeader( "If-None-Match" );
		if ( ( requestEtagValue != null ) && ( etag.equals( requestEtagValue ) ) )
		{
			response.setStatus( 304 );
			response.addHeader( "ETag", etag );
			return;
		}

		String mimeType = getServletContext().getMimeType( xapFile.getName() );
		response.setContentType( mimeType );
		response.setContentLength( ( int ) xapFile.length() );

		response.addHeader( "ETag", etag );
		response.addHeader( "Cache-Control", "max-age=0" );

		FileInputStream in = null;
		try
		{
			in = new FileInputStream( xapFile );
			ServletOutputStream out = response.getOutputStream();

			IOUtils.copy( in, out );
		}
		finally
		{
			if ( in != null )
			{
				in.close();
			}
		}
	}

	private void setErrorResponse( HttpServletResponse response, String code, String message ) throws IOException
	{
		int status = 400;
		if ( code.equals( AppExceptionTypeEnum.APP_LICENSE_ERROR.toString() ) )
		{
			status = 403;
		}
		response.setHeader( "x-reason", code );
		response.setStatus( status );
		PrintWriter out = response.getWriter();
		out.print( message );
		out.close();
	}

	protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		Map<String, String> parameters = new HashMap();
		parameters.put( "operation", request.getParameter( "operation" ) );
		parameters.put( "id", request.getParameter( "id" ) );

		if ( !chunkUploadHelper.handleChunkUpload( request, response, parameters ) )
		{

			setErrorResponse( response, ChunkUploadError.WRONG_PROTOCOL.toString(), "Only chunk upload protocol is accepted" );
		}
	}

	public boolean fileExists( String file )
	{
		return false;
	}

	public boolean saveFile( HttpServletResponse response, String fileName, File moveFile, Object userObject ) throws IOException
	{
		try
		{
			Map<String, String> parameters = ( Map ) userObject;
			String operation = ( String ) parameters.get( "operation" );
			if ( ( operation != null ) && ( operation.equals( "upgrade" ) ) )
			{
				appManager.upgrade( moveFile.getPath(), ( String ) parameters.get( "id" ) );
			}
			else
			{
				appManager.install( moveFile.getPath() );
			}

			return true;
		}
		catch ( AppException e )
		{
			setErrorResponse( response, e.getError().toString(), e.getMessage() );
		}
		return false;
	}
}
