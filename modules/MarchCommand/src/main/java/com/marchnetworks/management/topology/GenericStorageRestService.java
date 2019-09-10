package com.marchnetworks.management.topology;

import com.marchnetworks.command.api.topology.GenericStorageCoreService;
import com.marchnetworks.command.common.topology.GenericStorageException;
import com.marchnetworks.command.common.topology.data.GenericObjectInfo;
import com.marchnetworks.command.common.topology.data.Store;
import com.marchnetworks.common.serialization.CoreJsonSerializer;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.sun.jersey.spi.resource.Singleton;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

@Singleton
@Path( "storage/objects" )
public class GenericStorageRestService
{
	private GenericStorageCoreService genericStorageService = ( GenericStorageCoreService ) ApplicationContextSupport.getBean( "genericStorageServiceProxy" );

	@PUT
	@Path( "{store}/{objectId}" )
	@Consumes( {"application/octet-stream"} )
	public void setObject( @PathParam( "store" ) Store store, @PathParam( "objectId" ) String objectId, @QueryParam( "appId" ) String appId, byte[] objectData ) throws GenericStorageException
	{
		genericStorageService.setObject( store, objectId, objectData, appId );
	}

	@GET
	@Path( "{store}/{objectId}" )
	@Produces( {"application/octet-stream"} )
	public Response getObject( @Context Request request, @PathParam( "store" ) Store store, @PathParam( "objectId" ) String objectId, @QueryParam( "appId" ) String appId, @QueryParam( "userId" ) String userId ) throws GenericStorageException
	{
		String tag = genericStorageService.getObjectTag( store, objectId, appId, userId, true );
		EntityTag entityTag = new EntityTag( tag );

		ResponseBuilder builder = request.evaluatePreconditions( entityTag );
		if ( builder != null )
		{
			return builder.build();
		}

		byte[] result = genericStorageService.getObject( store, objectId, appId, userId, true );
		builder = Response.ok( result ).tag( entityTag );
		CacheControl cc = new CacheControl();
		cc.setMaxAge( 0 );
		builder.cacheControl( cc );
		return builder.build();
	}

	@DELETE
	@Path( "{store}/{objectId}" )
	public void deleteObject( @PathParam( "store" ) Store store, @PathParam( "objectId" ) String objectId, @QueryParam( "appId" ) String appId, @QueryParam( "userId" ) String userId ) throws GenericStorageException
	{
		genericStorageService.deleteObject( store, objectId, appId, userId, true );
	}

	@GET
	@Path( "{store}" )
	@Produces( {"application/json"} )
	public String listObjects( @PathParam( "store" ) Store store, @QueryParam( "appId" ) String appId, @QueryParam( "userId" ) String userId ) throws GenericStorageException
	{
		GenericObjectInfo[] result = genericStorageService.listObjects( store, appId, userId, true );

		String json = CoreJsonSerializer.toJson( result );
		return json;
	}
}
