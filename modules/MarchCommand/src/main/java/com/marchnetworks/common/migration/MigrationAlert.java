package com.marchnetworks.common.migration;

public class MigrationAlert
{
	long id;

	String deviceId;

	public MigrationAlert( long id, String deviceId )
	{
		this.id = id;
		this.deviceId = deviceId;
	}
}
