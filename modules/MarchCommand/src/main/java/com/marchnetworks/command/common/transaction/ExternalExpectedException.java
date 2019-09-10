package com.marchnetworks.command.common.transaction;

import com.marchnetworks.command.common.topology.ExpectedException;

public class ExternalExpectedException extends Exception implements ExpectedException
{
	public ExternalExpectedException( Throwable cause )
	{
		super( cause );
	}
}
