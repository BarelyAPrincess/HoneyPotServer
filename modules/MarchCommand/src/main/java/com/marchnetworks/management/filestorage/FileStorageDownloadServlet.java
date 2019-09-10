package com.marchnetworks.management.filestorage;

import com.marchnetworks.management.data.FileStorageView;
import com.marchnetworks.management.file.service.FileStorageException;
import com.marchnetworks.management.file.service.FileStorageService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FileStorageDownloadServlet extends HttpServlet
{
	private static final long serialVersionUID = -4311810900788046817L;
	private static Logger LOG = LoggerFactory.getLogger( FileStorageDownloadServlet.class );

	public void init( ServletConfig servletConfig ) throws ServletException
	{
		super.init( servletConfig );
		LOG.info( "init" );
		getFileStorageService();
	}

	public abstract FileStorageService getFileStorageService();

	protected void processRequest( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		response.setContentType( "text/html;charset=UTF-8" );
		String fileName = request.getParameter( "fileName" );
		String filePath = request.getParameter( "filePath" );
		if ( filePath == null )
		{
			filePath = "/";
		}
		Boolean errorOccured = Boolean.valueOf( false );
		if ( ( fileName != null ) && ( filePath != null ) )
		{
			try
			{
				FileStorageView fileStorage = getFileStorage( fileName );
				if ( fileStorage != null )
				{
					InputStream inputStream = getFirmwareFileObject( fileName, filePath );
					if ( inputStream != null )
					{
						ServletOutputStream servletOutputStream = response.getOutputStream();
						ServletContext context = getServletConfig().getServletContext();
						String mimetype = context.getMimeType( fileName );
						response.setContentType( mimetype != null ? mimetype : "application/octet-stream" );
						response.setHeader( "Content-Disposition", "attachment; filename=\"" + fileName + "\"" );
						response.setContentLength( ( int ) fileStorage.getFileLength() );
						response.setStatus( 200 );
						int bytesRead = 0;
						try
						{
							byte[] byteBuffer = new byte[' '];
							while ( ( bytesRead = inputStream.read( byteBuffer ) ) != -1 )
							{
								servletOutputStream.write( byteBuffer, 0, bytesRead );
							}
							servletOutputStream.flush();
						}
						catch ( Exception e )
						{
							LOG.debug( e.getMessage() );
							errorOccured = Boolean.TRUE;
						}
						finally
						{
							try
							{
								servletOutputStream.close();
								inputStream.close();
							}
							catch ( Exception e )
							{
								LOG.debug( e.getMessage() );
							}
						}
					}

					errorOccured = Boolean.TRUE;
				}
				else
				{
					errorOccured = Boolean.TRUE;
				}
			}
			catch ( FileStorageException e )
			{
				LOG.debug( e.getMessage() );
				errorOccured = Boolean.TRUE;
			}
			catch ( FileNotFoundException e )
			{
				LOG.debug( e.getMessage() );
				errorOccured = Boolean.TRUE;
			}
			finally
			{
				if ( errorOccured.booleanValue() )
					objectNotFound( response );
			}
		}
		else
		{
			objectNotFound( response );
		}
	}

	private FileStorageView getFileStorage( String name ) throws FileStorageException
	{
		FileStorageService service = getFileStorageService();
		FileStorageView fileStorage = null;
		if ( service != null )
		{
			fileStorage = service.getFileStorageByName( name );
		}
		return fileStorage;
	}

	private InputStream getFirmwareFileObject( String name, String path ) throws FileNotFoundException, FileStorageException
	{
		FileStorageService service = getFileStorageService();
		InputStream inputStream = null;
		if ( service != null )
		{
			File file = service.getFile( name );
			inputStream = new FileInputStream( file );
		}
		return inputStream;
	}

	private void objectNotFound( HttpServletResponse response ) throws ServletException, IOException
	{
		response.setStatus( 404 );
		response.setContentType( "text/html;charset=UTF-8" );
		PrintWriter out = response.getWriter();
		try
		{
			out.println( "<html>" );
			out.println( "<head>" );
			out.println( "<title>404</title>" );
			out.println( "</head>" );
			out.println( "<body>" );
			out.println( "File not found." );
			out.println( "</body>" );
			out.println( "</html>" );
		}
		finally
		{
			out.close();
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

	public String getServletInfo()
	{
		return "Short description";
	}
}
