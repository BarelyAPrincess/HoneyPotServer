package com.marchnetworks.management.user.exception;

import com.marchnetworks.command.common.user.UserException;

import javax.xml.ws.WebFault;

@WebFault( name = "UserFault" )
public class UPNRequiredException extends UserException
{
	private static final long serialVersionUID = 7519207529407296223L;

	public UPNRequiredException( String message )
	{
		super( message );
	}
}

