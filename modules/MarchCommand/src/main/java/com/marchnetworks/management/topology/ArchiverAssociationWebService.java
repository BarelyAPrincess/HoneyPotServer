package com.marchnetworks.management.topology;

import com.marchnetworks.command.common.topology.data.ArchiverAssociation;
import com.marchnetworks.common.spring.ApplicationContextSupport;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

@WebService( serviceName = "ArchiverAssociationService", name = "ArchiverAssociationService", portName = "ArchiverAssociationPort" )
public class ArchiverAssociationWebService
{
	private ArchiverAssociationService archiverAssociationService = ( ArchiverAssociationService ) ApplicationContextSupport.getBean( "archiverAssociationServiceProxy" );

	@WebMethod( operationName = "updateArchiverAssociation" )
	public void updateArchiverAssociation( @WebParam( name = "archiverAssociation" ) ArchiverAssociation archiverAssociation )
	{
		archiverAssociationService.updateArchiverAssociation( archiverAssociation );
	}

	@WebMethod( operationName = "getArchiverAssociations" )
	public ArchiverAssociation[] getArchiverAssociations()
	{
		return archiverAssociationService.getArchiverAssociations();
	}
}
