package com.marchnetworks.management.communications;

import javax.xml.ws.WebFault;

@WebFault( name = "CommunicationsFault" )
public class CommunicationsException extends Exception
{
	private static final long serialVersionUID = -1415772478301464812L;

	public CommunicationsException( String msg )
	{
		super( msg );
	}
}
