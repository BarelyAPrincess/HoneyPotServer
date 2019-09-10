package com.marchnetworks.server.communications.transport.datamodel;

public class ConfigurationEnvelope
{
	private String deviceId;
	private String hash;
	private byte[] document;

	public String getDeviceId()
	{
		return deviceId;
	}

	public void setDeviceId( String deviceId )
	{
		this.deviceId = deviceId;
	}

	public String getHash()
	{
		return hash;
	}

	public void setHash( String hash )
	{
		this.hash = hash;
	}

	public byte[] getDocument()
	{
		return document;
	}

	public void setDocument( byte[] document )
	{
		this.document = document;
	}
}

