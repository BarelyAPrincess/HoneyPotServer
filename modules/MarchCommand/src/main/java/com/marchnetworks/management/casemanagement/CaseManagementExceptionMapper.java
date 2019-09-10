package com.marchnetworks.management.casemanagement;

import com.marchnetworks.casemanagementservice.common.CaseManagementException;
import com.marchnetworks.casemanagementservice.common.CaseManagementExceptionTypeEnum;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class CaseManagementExceptionMapper implements ExceptionMapper<CaseManagementException>
{
	public Response toResponse( CaseManagementException ex )
	{
		int status = 500;
		if ( ex.getError() == CaseManagementExceptionTypeEnum.NOT_AUTHORIZED )
		{
			status = 403;
		}
		else if ( ( ex.getError() == CaseManagementExceptionTypeEnum.CASE_NOT_FOUND ) || ( ex.getError() == CaseManagementExceptionTypeEnum.CASENODE_NOT_FOUND ) )
		{
			status = 404;
		}
		else if ( ex.getError() == CaseManagementExceptionTypeEnum.CASE_DATA_CORRUPTED )
		{
			status = 204;
		}
		return Response.status( status ).entity( ex.getMessage() ).header( "x-reason", ex.getError().name() ).type( "text/plain" ).build();
	}
}
