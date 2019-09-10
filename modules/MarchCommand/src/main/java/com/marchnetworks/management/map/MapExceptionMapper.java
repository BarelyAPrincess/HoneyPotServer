package com.marchnetworks.management.map;

import com.marchnetworks.map.service.MapException;
import com.marchnetworks.map.service.MapExceptionTypeEnum;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class MapExceptionMapper implements ExceptionMapper<MapException>
{
	public Response toResponse( MapException ex )
	{
		int status = 500;
		if ( ex.getError() == MapExceptionTypeEnum.MAP_NOT_FOUND )
		{
			status = 404;
		}
		return Response.status( status ).entity( ex.getMessage() ).header( "x-reason", ex.getError().name() ).type( "text/plain" ).build();
	}
}
