package com.marchnetworks.server.event;

import javax.xml.ws.WebFault;

@WebFault
public class EventNotificationException extends Exception
{
	private static final long serialVersionUID = -1271768431874460489L;

	public EventNotificationException()
	{
	}

	public EventNotificationException( String msg )
	{
		super( msg );
	}
}

