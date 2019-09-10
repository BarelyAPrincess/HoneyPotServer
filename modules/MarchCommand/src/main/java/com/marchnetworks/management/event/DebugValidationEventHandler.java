package com.marchnetworks.management.event;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DebugValidationEventHandler implements ValidationEventHandler
{
	private static final Logger LOG = LoggerFactory.getLogger( DebugValidationEventHandler.class );

	public DebugValidationEventHandler()
	{
		if ( LOG.isTraceEnabled() )
		{
			LOG.trace( "new instance created=" + this );
		}
	}

	public boolean handleEvent( ValidationEvent a_Event )
	{
		if ( LOG.isDebugEnabled() )
		{
			LOG.debug( "a_Event=" + a_Event + " message=" + a_Event.getMessage() );
		}

		return false;
	}
}
