package com.marchnetworks.server.event;

import java.util.concurrent.ScheduledFuture;

import javax.servlet.AsyncContext;

public class EventRequest
{
	private Integer id;
	private AsyncContext asyncContext;
	private ScheduledFuture<?> timeoutTask;

	public EventRequest( Integer id, AsyncContext request, ScheduledFuture<?> timeout )
	{
		this.id = id;
		asyncContext = request;
		timeoutTask = timeout;
	}

	public Integer getId()
	{
		return id;
	}

	public void setId( Integer id )
	{
		this.id = id;
	}

	public AsyncContext getAsyncContext()
	{
		return asyncContext;
	}

	public void setAsyncContext( AsyncContext request )
	{
		asyncContext = request;
	}

	public ScheduledFuture<?> getTimeoutTask()
	{
		return timeoutTask;
	}

	public void setTimeoutTask( ScheduledFuture<?> timeout )
	{
		timeoutTask = timeout;
	}
}

