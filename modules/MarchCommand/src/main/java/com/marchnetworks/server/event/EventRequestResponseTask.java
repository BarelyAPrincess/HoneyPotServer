package com.marchnetworks.server.event;

import com.marchnetworks.common.spring.ApplicationContextSupport;

public class EventRequestResponseTask implements Runnable
{
	private static EventRequestContainer eventRequestContainer = ( EventRequestContainer ) ApplicationContextSupport.getBean( "eventRequestContainer" );
	private EventRequest request;
	private String response;

	public EventRequestResponseTask( EventRequest request, String response )
	{
		this.request = request;
		this.response = response;
	}

	public void run()
	{
		eventRequestContainer.respondRequest( request, response );
	}
}

