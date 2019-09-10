package com.marchnetworks.management.localgroup;

import com.marchnetworks.casemanagementservice.data.LocalGroup;
import com.marchnetworks.casemanagementservice.data.LocalGroupException;
import com.marchnetworks.casemanagementservice.data.LocalGroupException.LocalGroupExceptionType;
import com.marchnetworks.casemanagementservice.service.LocalGroupService;
import com.marchnetworks.common.serialization.CoreJsonSerializer;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.sun.jersey.spi.resource.Singleton;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

@Singleton
@Path( "localgroups" )
public class LocalGroupRestService
{
	private LocalGroupService localGroupService = ( LocalGroupService ) ApplicationContextSupport.getBean( "localGroupServiceProxy" );

	@POST
	@Consumes( {"application/json"} )
	@Produces( {"application/json"} )
	public String createLocalGroup( String newLocalGroupJson ) throws LocalGroupException
	{
		LocalGroup newLocalGroup = ( LocalGroup ) CoreJsonSerializer.fromJson( newLocalGroupJson, LocalGroup.class );

		if ( newLocalGroup == null )
		{
			throw new LocalGroupException( "Local group cannot be deserialized", LocalGroupExceptionType.LOCAL_GROUP_DATA_CORRUPTED );
		}

		return CoreJsonSerializer.toJson( localGroupService.create( newLocalGroup ) );
	}

	@PUT
	@Consumes( {"application/json"} )
	@Produces( {"application/json"} )
	public String bulkUpdateLocalGroups( String updatedLocalGroupJson ) throws LocalGroupException
	{
		LocalGroup[] localGroups = ( LocalGroup[] ) CoreJsonSerializer.fromJson( updatedLocalGroupJson, LocalGroup[].class );

		List<LocalGroup> updatedLocalGroups = new ArrayList();

		for ( LocalGroup localGroup : localGroups )
		{
			updatedLocalGroups.add( localGroupService.update( localGroup ) );
		}

		return CoreJsonSerializer.toJson( updatedLocalGroups );
	}

	@PUT
	@Path( "{localgroupid}" )
	@Consumes( {"application/json"} )
	@Produces( {"application/json"} )
	public String updateLocalGroup( String updatedLocalGroupJson ) throws LocalGroupException
	{
		LocalGroup localGroup = ( LocalGroup ) CoreJsonSerializer.fromJson( updatedLocalGroupJson, LocalGroup.class );
		return CoreJsonSerializer.toJson( localGroupService.update( localGroup ) );
	}

	@DELETE
	@Path( "{localgroupid}" )
	public void removeLocalGroup( @PathParam( "localgroupid" ) String localGroupIdString ) throws NumberFormatException, LocalGroupException
	{
		localGroupService.delete( Long.valueOf( localGroupIdString ) );
	}

	@GET
	@Produces( {"application/json"} )
	public String getLocalGroups() throws LocalGroupException
	{
		return CoreJsonSerializer.toJson( localGroupService.getAll() );
	}

	@GET
	@Path( "{username}" )
	@Produces( {"application/json"} )
	public String getUserLocalGroups( @PathParam( "username" ) String username ) throws LocalGroupException
	{
		return CoreJsonSerializer.toJson( localGroupService.getAllByUser( username ) );
	}
}
