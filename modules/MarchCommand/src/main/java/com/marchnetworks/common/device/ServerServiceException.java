package com.marchnetworks.common.device;

import com.marchnetworks.command.common.topology.ExpectedException;

import javax.xml.ws.WebFault;

@WebFault( name = "ServerServiceFault" )
public class ServerServiceException extends Exception implements ExpectedException
{
	public ServerServiceException( String message )
	{
		super( message );
	}
}
