package com.marchnetworks.app.data;

public enum AppStatus
{
	INSTALLED,
	RUNNING,
	UNLICENSED,
	NOTSUPPORTED,
	UPGRADE_REQUIRED;

	private AppStatus()
	{
	}

	public String toString()
	{
		if ( this == INSTALLED )
			return "installed";
		if ( this == RUNNING )
			return "running";
		if ( this == UNLICENSED )
			return "unlicensed";
		if ( this == NOTSUPPORTED )
			return "notSupported";
		if ( this == UPGRADE_REQUIRED )
			return "upgradeRequired";
		return "";
	}
}
