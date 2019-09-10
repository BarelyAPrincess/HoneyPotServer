package com.marchnetworks.management.communications;

public class NetworkConfiguration
{
	private String iPv4Address;

	private String iPv4NetMask;

	public NetworkConfiguration()
	{
	}

	public NetworkConfiguration( String iPv4Address, String iPv4Netmask )
	{
		this.iPv4Address = iPv4Address;
		iPv4NetMask = iPv4Netmask;
	}

	public String getiPv4Address()
	{
		return iPv4Address;
	}

	public String getiPv4NetMask()
	{
		return iPv4NetMask;
	}

	public void setiPv4Address( String iPv4Address )
	{
		this.iPv4Address = iPv4Address;
	}

	public void setiPv4NetMask( String iPv4NetMask )
	{
		this.iPv4NetMask = iPv4NetMask;
	}

	public String toString()
	{
		String network = new String();
		network = network + iPv4Address + ", " + iPv4NetMask;

		return network;
	}
}
