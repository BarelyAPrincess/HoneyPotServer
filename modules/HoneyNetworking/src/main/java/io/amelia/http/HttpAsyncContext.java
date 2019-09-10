package io.amelia.http;

import java.util.Set;

import javax.annotation.Nonnull;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncListener;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import io.amelia.foundation.Kernel;

public class HttpAsyncContext implements AsyncContext
{
	private boolean hasOriginalRequestAndResponse;
	private boolean isCompleted = false;
	private Set<AsyncListener> listeners;
	private ServletRequest request;
	private ServletResponse response;
	private long timeout;

	public HttpAsyncContext( @Nonnull ServletRequest request, @Nonnull ServletResponse response, boolean hasOriginalRequestAndResponse )
	{
		this.request = request;
		this.response = response;
		this.hasOriginalRequestAndResponse = hasOriginalRequestAndResponse;
	}

	@Override
	public void addListener( AsyncListener listener )
	{
		listeners.add( listener );
	}

	@Override
	public void addListener( AsyncListener listener, ServletRequest servletRequest, ServletResponse servletResponse )
	{

	}

	@Override
	public void complete()
	{
		isCompleted = true;
	}

	public boolean isCompleted()
	{
		return isCompleted;
	}

	@Override
	public <T extends AsyncListener> T createListener( Class<T> clazz ) throws ServletException
	{

	}

	@Override
	public void dispatch()
	{

	}

	@Override
	public void dispatch( String path )
	{

	}

	@Override
	public void dispatch( ServletContext context, String path )
	{

	}

	@Override
	public ServletRequest getRequest()
	{
		return request;
	}

	@Override
	public ServletResponse getResponse()
	{
		return response;
	}

	@Override
	public long getTimeout()
	{
		return timeout;
	}

	@Override
	public boolean hasOriginalRequestAndResponse()
	{
		return hasOriginalRequestAndResponse;
	}

	public void reset()
	{
		listeners.clear();
		isCompleted = false;
	}

	@Override
	public void setTimeout( long timeout )
	{
		this.timeout = timeout;
	}

	@Override
	public void start( Runnable task )
	{
		Kernel.getExecutorSerial().execute( task );
	}
}
