package com.marchnetworks.server.communications.transport.datamodel;

public class Capabilities
{
	protected int protocolVersion;

	protected String[] strCapabilities;

	public int getProtocolVersion()
	{
		return protocolVersion;
	}

	public void setProtocolVersion( int protocolVersion )
	{
		this.protocolVersion = protocolVersion;
	}

	public String[] getStrCapabilities()
	{
		return strCapabilities;
	}

	public void setStrCapabilities( String[] strCapabilities )
	{
		this.strCapabilities = strCapabilities;
	}
}

