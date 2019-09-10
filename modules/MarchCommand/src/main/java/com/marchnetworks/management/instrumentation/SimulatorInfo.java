package com.marchnetworks.management.instrumentation;

public class SimulatorInfo
{
	private String address;

	private int devices;

	public SimulatorInfo( String address, int devices )
	{
		this.address = address;
		this.devices = devices;
	}

	public String getAddress()
	{
		return address;
	}

	public int getDevices()
	{
		return devices;
	}
}

