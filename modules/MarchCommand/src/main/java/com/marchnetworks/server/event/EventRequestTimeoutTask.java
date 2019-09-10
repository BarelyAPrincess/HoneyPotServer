package com.marchnetworks.server.event;

import com.marchnetworks.common.spring.ApplicationContextSupport;

public class EventRequestTimeoutTask implements Runnable
{
	private static EventRequestContainer eventRequestContainer = ( EventRequestContainer ) ApplicationContextSupport.getBean( "eventRequestContainer" );
	private int id;

	public EventRequestTimeoutTask( int id )
	{
		this.id = id;
	}

	public void run()
	{
		eventRequestContainer.timeoutRequest( Integer.valueOf( id ) );
	}
}

