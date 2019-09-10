package com.marchnetworks.server.communications.transport.datamodel;

public class GetParametersResult
{
	protected long lastEventId;

	protected GenericParameter[] parameters;

	public long getLastEventId()
	{
		return lastEventId;
	}

	public void setLastEventId( long value )
	{
		lastEventId = value;
	}

	public GenericParameter[] getParameters()
	{
		return parameters;
	}

	public void setParameters( GenericParameter[] value )
	{
		parameters = value;
	}
}

