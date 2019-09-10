package com.marchnetworks.management.map;

import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.map.service.MapException;
import com.marchnetworks.map.service.MapService;
import com.sun.jersey.spi.resource.Singleton;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

@Singleton
@Path( "maps" )
public class MapRestService
{
	private MapService mapService = ( MapService ) ApplicationContextSupport.getBean( "mapServiceProxy" );

	@GET
	@Path( "{mapDataId}" )
	@Produces( {"application/octet-stream"} )
	public Response getObject( @Context Request request, @PathParam( "mapDataId" ) Long mapDataId ) throws MapException
	{
		String tag = mapService.getMapTag( mapDataId );
		EntityTag entityTag = new EntityTag( tag );

		ResponseBuilder builder = request.evaluatePreconditions( entityTag );
		if ( builder != null )
		{
			return builder.build();
		}

		byte[] result = mapService.getMapData( mapDataId );
		builder = Response.ok( result ).tag( entityTag );
		CacheControl cc = new CacheControl();
		cc.setMaxAge( 0 );
		builder.cacheControl( cc );
		return builder.build();
	}
}
