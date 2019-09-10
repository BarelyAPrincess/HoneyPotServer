package com.marchnetworks.management.topology;

import com.marchnetworks.command.api.topology.GenericStorageCoreService;
import com.marchnetworks.command.common.topology.GenericStorageException;
import com.marchnetworks.command.common.topology.data.GenericObjectInfo;
import com.marchnetworks.command.common.topology.data.Store;
import com.marchnetworks.common.spring.ApplicationContextSupport;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

@WebService( serviceName = "GenericStorageService", name = "GenericStorageService", portName = "GenericStoragePort" )
public class GenericStorageWebService
{
	private GenericStorageCoreService genericStorageService = ( GenericStorageCoreService ) ApplicationContextSupport.getBean( "genericStorageServiceProxy" );

	@WebMethod( operationName = "setObject" )
	public void setObject( @WebParam( name = "store" ) Store store, @WebParam( name = "objectId" ) String objectId, @WebParam( name = "objectData" ) byte[] objectData, @WebParam( name = "appId" ) String appId ) throws GenericStorageException
	{
		genericStorageService.setObject( store, objectId, objectData, appId );
	}

	@WebMethod( operationName = "getObject" )
	public byte[] getObject( @WebParam( name = "store" ) Store store, @WebParam( name = "objectId" ) String objectId, @WebParam( name = "appId" ) String appId, @WebParam( name = "userId" ) String userId ) throws GenericStorageException
	{
		return genericStorageService.getObject( store, objectId, appId, userId, true );
	}

	@WebMethod( operationName = "deleteObject" )
	public void deleteObject( @WebParam( name = "store" ) Store store, @WebParam( name = "objectId" ) String objectId, @WebParam( name = "appId" ) String appId, @WebParam( name = "userId" ) String userId ) throws GenericStorageException
	{
		genericStorageService.deleteObject( store, objectId, appId, userId, true );
	}

	@WebMethod( operationName = "listObjects" )
	public GenericObjectInfo[] listObjects( @WebParam( name = "store" ) Store store, @WebParam( name = "appId" ) String appId, @WebParam( name = "userId" ) String userId ) throws GenericStorageException
	{
		return genericStorageService.listObjects( store, appId, userId, true );
	}
}
