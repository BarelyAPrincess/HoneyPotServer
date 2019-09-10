package com.marchnetworks.server.communications.transport.datamodel;

public class DeviceSession
{
	private String sessionId;

	private int timeout;

	public DeviceSession( String sessionId, int timeout )
	{
		this.sessionId = sessionId;
		this.timeout = timeout;
	}

	public String getSessionId()
	{
		return sessionId;
	}

	public int getTimeout()
	{
		return timeout;
	}
}

