package com.marchnetworks.management.topology;

import com.marchnetworks.command.common.topology.GenericStorageException;
import com.marchnetworks.command.common.topology.GenericStorageExceptionType;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class GenericStorageExceptionMapper implements ExceptionMapper<GenericStorageException>
{
	public Response toResponse( GenericStorageException ex )
	{
		int status = 500;
		if ( ex.getFaultCode() == GenericStorageExceptionType.NOT_FOUND )
		{
			status = 404;
		}
		else if ( ex.getFaultCode() == GenericStorageExceptionType.USER_RIGHT )
		{
			status = 401;
		}
		return Response.status( status ).entity( ex.getMessage() ).header( "x-reason", ex.getFaultCode().name() ).type( "text/plain" ).build();
	}
}
