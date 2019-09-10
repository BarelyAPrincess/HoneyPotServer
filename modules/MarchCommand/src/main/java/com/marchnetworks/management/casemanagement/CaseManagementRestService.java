package com.marchnetworks.management.casemanagement;

import com.marchnetworks.casemanagementservice.common.CaseManagementException;
import com.marchnetworks.casemanagementservice.common.CaseManagementExceptionTypeEnum;
import com.marchnetworks.casemanagementservice.data.Case;
import com.marchnetworks.casemanagementservice.service.CaseManagementService;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.common.serialization.CoreJsonSerializer;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.sun.jersey.spi.resource.Singleton;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
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
@Path( "casemanagement" )
public class CaseManagementRestService
{
	private CaseManagementService caseManagementService = ( CaseManagementService ) ApplicationContextSupport.getBean( "caseManagementServiceProxy" );

	@GET
	@Path( "attachments/{casenodeid}" )
	@Produces( {"application/octet-stream"} )
	public Response getAttachment( @Context Request request, @PathParam( "casenodeid" ) String caseNodeIdString ) throws CaseManagementException
	{
		Long caseNodeId = Long.valueOf( caseNodeIdString );
		String userName = CommonAppUtils.getUsernameFromSecurityContext();

		String tag = caseManagementService.getAttachmentTag( caseNodeId );
		EntityTag entityTag = new EntityTag( tag );

		ResponseBuilder builder = request.evaluatePreconditions( entityTag );
		if ( builder != null )
		{
			return builder.build();
		}

		byte[] result = caseManagementService.getCaseNodeAttachment( caseNodeId, userName );
		builder = Response.ok( result ).tag( entityTag );
		CacheControl cc = new CacheControl();
		cc.setMaxAge( 0 );
		builder.cacheControl( cc );
		return builder.build();
	}

	@GET
	@Path( "existingcase/{caseid}" )
	@Produces( {"application/json"} )
	public String getCase( @PathParam( "caseid" ) String caseIdString ) throws CaseManagementException
	{
		String userName = CommonAppUtils.getUsernameFromSecurityContext();
		Long caseId = Long.valueOf( caseIdString );

		Case result = caseManagementService.getCase( caseId, userName );

		String response = result.toJson();

		return response;
	}

	@GET
	@Path( "existingcases" )
	@Produces( {"application/json"} )
	public String getCases() throws CaseManagementException
	{
		String userName = CommonAppUtils.getUsernameFromSecurityContext();
		List<Case> result = caseManagementService.getAllCases( userName );

		String response = CoreJsonSerializer.toJson( result );

		return response;
	}

	@DELETE
	@Path( "removecase/{caseid}" )
	public void removeCase( @PathParam( "caseid" ) String caseIdString ) throws CaseManagementException
	{
		String userName = CommonAppUtils.getUsernameFromSecurityContext();
		Long caseId = Long.valueOf( caseIdString );

		caseManagementService.removeCase( caseId, userName );
	}

	@PUT
	@Path( "newcase" )
	@Consumes( {"application/json"} )
	@Produces( {"application/json"} )
	public String createCase( String newCaseJson ) throws CaseManagementException
	{
		String userName = CommonAppUtils.getUsernameFromSecurityContext();

		Case newCase = ( Case ) CoreJsonSerializer.fromJson( newCaseJson, Case.class );

		if ( newCase == null )
		{
			throw new CaseManagementException( CaseManagementExceptionTypeEnum.CASE_DATA_CORRUPTED, "Case could not be deserialized." );
		}
		newCase.setMember( userName );
		Case result = caseManagementService.createCase( newCase, userName );

		String response = result.toJson();

		return response;
	}

	@PUT
	@Path( "updatedcase" )
	@Consumes( {"application/json"} )
	@Produces( {"application/json"} )
	public String updateCase( String updatedCaseJson ) throws CaseManagementException
	{
		String userName = CommonAppUtils.getUsernameFromSecurityContext();
		Case updatedCase = ( Case ) CoreJsonSerializer.fromJson( updatedCaseJson, Case.class );
		Case result = caseManagementService.updateCase( updatedCase, userName );

		String response = result.toJson();

		return response;
	}
}
