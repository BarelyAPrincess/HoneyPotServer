package com.marchnetworks.management.filestorage;

import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.management.data.FileStorageView;
import com.marchnetworks.management.file.model.FileStorageType;
import com.marchnetworks.management.file.service.FileStorageException;
import com.marchnetworks.management.file.service.FileStorageExceptionType;
import com.marchnetworks.management.file.service.FileStorageService;

import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

import org.springframework.security.access.AccessDeniedException;

@WebService( serviceName = "FileStorageService", name = "FileStorageService" )
public class FileStorageWebService
{
	private FileStorageService fileService = ( FileStorageService ) ApplicationContextSupport.getBean( "fileStorageServiceProxy" );
	private String accessDenied = "not_authorized";

	@Resource
	WebServiceContext wsContext;

	@WebMethod( operationName = "getFileStorage" )
	public FileStorageView getFileStorageObject( @WebParam( name = "fileStorageId" ) String fileStorageId ) throws FileStorageException
	{
		try
		{
			return fileService.getFileStorage( fileStorageId );
		}
		catch ( AccessDeniedException e )
		{
			throw new FileStorageException( accessDenied );
		}
	}

	@WebMethod( operationName = "getFileStorageList" )
	public FileStorageView[] getFileStorageList( FileStorageType aType ) throws FileStorageException
	{
		try
		{
			return fileService.getFileStorageListAsArray( aType );
		}
		catch ( AccessDeniedException e )
		{
			throw new FileStorageException( accessDenied );
		}
	}

	@WebMethod( operationName = "deleteFileStorage" )
	public void deleteFileStorage( @WebParam( name = "fileStorageId" ) String fileStorageId ) throws FileStorageException
	{
		try
		{
			fileService.deleteFileStorage( fileStorageId );
		}
		catch ( AccessDeniedException e )
		{
			throw new FileStorageException( accessDenied );
		}
		catch ( IllegalStateException ise )
		{
			throw new FileStorageException( FileStorageExceptionType.FILE_IN_USE, ise.getMessage() );
		}
	}

	@WebMethod( operationName = "fileInUseCheck" )
	public boolean fileInUseCheck( @WebParam( name = "fileStorageId" ) String fileStorageId ) throws FileStorageException
	{
		try
		{
			return fileService.fileInUseCheck( fileStorageId );
		}
		catch ( AccessDeniedException e )
		{
			throw new FileStorageException( accessDenied );
		}
		catch ( IllegalStateException ise )
		{
			throw new FileStorageException( FileStorageExceptionType.FILE_IN_USE, ise.getMessage() );
		}
	}
}
