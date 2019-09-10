package com.marchnetworks.security.ldap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.event.NamingExceptionEvent;
import javax.naming.ldap.UnsolicitedNotificationEvent;
import javax.naming.ldap.UnsolicitedNotificationListener;

public class UnsolicitListener implements UnsolicitedNotificationListener
{
	private static Logger LOG = LoggerFactory.getLogger( UnsolicitListener.class );

	public void notificationReceived( UnsolicitedNotificationEvent evt )
	{
	}

	public void namingExceptionThrown( NamingExceptionEvent evt )
	{
		LOG.warn( "Unexpected exception coming from LDAP server: ", evt.getException() );
	}
}

