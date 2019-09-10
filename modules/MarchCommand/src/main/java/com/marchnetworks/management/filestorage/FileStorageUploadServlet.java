package com.marchnetworks.management.filestorage;

import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.management.data.FileStatusResult;
import com.marchnetworks.management.data.FileUploadStatusEnum;
import com.marchnetworks.management.file.service.FileStorageException;
import com.marchnetworks.management.file.service.FileStorageService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileStorageUploadServlet extends HttpServlet implements ChunkUploadServlet
{
	private static final long serialVersionUID = -7725971244404695993L;
	private static Logger LOG = LoggerFactory.getLogger( FileStorageUploadServlet.class );

	private int m_FactorySizeThreshold = 409600;

	public static final long MAX_FILESIZE = 1073741824L;

	private FileStorageService fileStorageService;

	protected ChunkUploadHelper m_ChunkUploadHelper;

	public void init( ServletConfig servletConfig ) throws ServletException
	{
		super.init( servletConfig );
		LOG.info( "init" );
		m_ChunkUploadHelper = new ChunkUploadHelper( this );
	}

	protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		processRequest( request, response );
	}

	protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		processRequest( request, response );
	}

	protected void doPut( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		processRequest( request, response );
	}

	protected void processRequest( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		if ( !m_ChunkUploadHelper.handleChunkUpload( request, response, null ) )
		{
			processRegularUpload( request, response );
		}
	}

	protected void processRegularUpload( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		String info = "processRequest type = " + request.getContentType() + " enc = " + request.getCharacterEncoding() + " method = " + request.getMethod();

		LOG.info( info );

		response.setContentType( "text/html;charset=UTF-8" );

		try
		{
			String fileName = request.getHeader( "X-fileName" );
			String filePath = request.getHeader( "X-filePath" );

			if ( filePath == null )
			{
				filePath = "";
			}

			if ( ServletFileUpload.isMultipartContent( request ) )
			{
				LOG.info( "isMultipartContent" );
				DiskFileItemFactory factory = new DiskFileItemFactory();

				factory.setSizeThreshold( m_FactorySizeThreshold );
				ServletFileUpload upload = new ServletFileUpload( factory );

				upload.setSizeMax( 1073741824L );
				List<?> fileItems = upload.parseRequest( request );

				Iterator<?> iter = fileItems.iterator();
				while ( iter.hasNext() )
				{
					FileItem item = ( FileItem ) iter.next();

					if ( item.isFormField() )
					{
						processFormField( item );
					}
					else
					{
						processUploadedFile( request, response, item );
						break;
					}
				}
			}
			else
			{
				LOG.info( "NOT  isMultipartContent" );
				processUploadedFile( request, response, fileName );
			}
		}
		catch ( FileUploadException e )
		{
			LOG.error( "Exception uploading file from client", e );
		}
		catch ( Exception e )
		{
			LOG.error( "Unknown Exception: ", e.getMessage() );
		}
	}

	public boolean fileExists( String name )
	{
		FileStorageService fss = getFileStorageService();
		return fss.isFileStorageExist( name );
	}

	private void processFormField( FileItem item )
	{
		String name = item.getFieldName();
		String value = item.getString();

		LOG.info( "processFormField: item=" + item + " name=" + name + " value=" + value );
	}

	private void processUploadedFile( HttpServletRequest request, HttpServletResponse response, String fileName ) throws IOException, ServletException
	{
		InputStream inputStream = request.getInputStream();
		saveFile( response, fileName, inputStream );
	}

	private void processUploadedFile( HttpServletRequest request, HttpServletResponse response, FileItem item ) throws IOException, ServletException
	{
		InputStream inputStream = item.getInputStream();
		String fileName = item.getName();
		fileName = fileName.substring( fileName.lastIndexOf( File.separatorChar ) + 1 );
		saveFile( response, fileName, inputStream );
	}

	public boolean saveFile( HttpServletResponse response, String fileName, File moveFile, Object userObject ) throws IOException
	{
		FileStatusResult result = validFile( fileName, moveFile );

		if ( !result.getStatus().equals( FileUploadStatusEnum.OK ) )
		{
			moveFile.delete();
			if ( result.getStatus() == FileUploadStatusEnum.IMCOMPATIBLE_AGENT )
			{
				LOG.warn( "The target version is not compatible with the server. removing the firmware file." );
				setErrorResponse( response, ChunkUploadError.IMCOMPATIBLE_AGENT, "The target version is not compatible with the server." );
			}
			else
			{
				LOG.warn( "Invalid file, Status " + result.getStatus() );
				setErrorResponse( response, ChunkUploadError.FILE_STORAGE_ERROR, "Invalid file, Status " + result.getStatus() );
			}
			return false;
		}

		FileStorageService fss = getFileStorageService();
		try
		{
			fss.addFileStorage( fileName, moveFile, result.getProperties() );

			return true;
		}
		catch ( FileStorageException e )
		{
			LOG.error( "Could not save file storage uploaded file, Error:" + e.getMessage() );

			setErrorResponse( response, ChunkUploadError.FILE_STORAGE_ERROR, e.getMessage() );
		}
		return false;
	}

	private void setErrorResponse( HttpServletResponse response, ChunkUploadError code, String message ) throws IOException
	{
		response.setHeader( "x-reason", code.toString() );
		response.setStatus( 400 );
		PrintWriter out = response.getWriter();
		out.print( message );
		out.close();
	}

	public void saveFile( HttpServletResponse response, String fileName, InputStream inputStream ) throws IOException, ServletException
	{
		File aFile = writeFile( fileName, ChunkUploadHelper.DEFAULT_DIR, inputStream );
		if ( aFile != null )
		{
			saveFile( response, fileName, aFile, null );
		}
	}

	private File writeFile( String fileName, String tempPath2, InputStream inputStream )
	{
		if ( fileExists( fileName ) )
		{
			return null;
		}

		try
		{
			File resultFile = new File( tempPath2 + File.separator + fileName );
			OutputStream out = new FileOutputStream( resultFile );

			int read = 0;
			byte[] bytes = new byte['Ѐ'];

			while ( ( read = inputStream.read( bytes ) ) != -1 )
			{
				out.write( bytes, 0, read );
			}

			inputStream.close();
			out.flush();
			out.close();

			return resultFile;
		}
		catch ( IOException e )
		{
			System.out.println( e.getMessage() );
		}

		return null;
	}

	private FileStatusResult validFile( String fileName, File aFile )
	{
		return getFileStorageService().validateFile( fileName, null, aFile );
	}

	private FileStorageService getFileStorageService()
	{
		if ( fileStorageService == null )
		{
			fileStorageService = ( ( FileStorageService ) ApplicationContextSupport.getBean( "fileStorageServiceProxy" ) );
		}
		return fileStorageService;
	}
}
