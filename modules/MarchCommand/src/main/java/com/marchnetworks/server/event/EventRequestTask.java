package com.marchnetworks.server.event;

import com.marchnetworks.common.spring.ApplicationContextSupport;

import java.util.Set;

public class EventRequestTask implements Runnable
{
	private static EventRequestContainer eventRequestContainer = ( EventRequestContainer ) ApplicationContextSupport.getBean( "eventRequestContainer" );
	private Set<Integer> ids;

	public EventRequestTask( Set<Integer> ids )
	{
		this.ids = ids;
	}

	public void addIds( Set<Integer> ids )
	{
		this.ids.addAll( ids );
	}

	public void run()
	{
		eventRequestContainer.respondRequests( ids );
	}

	public Set<Integer> getIds()
	{
		return ids;
	}
}

