package com.marchnetworks.server.event;

import java.io.IOException;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract interface EventRequestContainer
{
	public abstract void addRequest( Integer paramInteger, long paramLong, HttpServletRequest paramHttpServletRequest, HttpServletResponse paramHttpServletResponse ) throws IOException;

	public abstract void timeoutRequest( Integer paramInteger );

	public abstract void startRespondRequests( Set<Integer> paramSet );

	public abstract void respondRequests( Set<Integer> paramSet );

	public abstract void respondRequest( EventRequest paramEventRequest, String paramString );
}

