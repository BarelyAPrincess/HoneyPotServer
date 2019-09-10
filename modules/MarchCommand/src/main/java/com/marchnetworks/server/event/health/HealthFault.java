package com.marchnetworks.server.event.health;

import javax.xml.ws.WebFault;

@WebFault( name = "HealthFault" )
public class HealthFault extends Exception
{
	private static final long serialVersionUID = -3101546640835313312L;
	HealthFaultTypeEnum error;

	public HealthFault( HealthFaultTypeEnum err )
	{
		error = err;
	}

	public HealthFault( HealthFaultTypeEnum err, String message )
	{
		super( message );
		error = err;
	}

	public HealthFaultTypeEnum getError()
	{
		return error;
	}
}

