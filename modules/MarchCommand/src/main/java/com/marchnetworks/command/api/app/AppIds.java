package com.marchnetworks.command.api.app;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum AppIds
{
	COMMAND_CLIENT( "52b27086-b48d-4a19-a33d-7f598f1e96cb" ),
	R5( "e7ee1593-4021-41df-90c1-8cbcdc34954f" ),
	IMAGE_RETENTION( "85fca90b-22ac-4a04-b37e-ef56b8c3713c" ),
	COMMAND_CLIENT_20( "5e21c4cd-4cd5-4f0b-bfe6-71af27762261" ),
	MOBILE_CLIENT( "0412083b-a854-4a7d-aded-529f4a03c816" );

	public static final Set<String> COMMAND_IDS = new HashSet( Arrays.asList( new String[] {COMMAND_CLIENT.getAppId(), COMMAND_CLIENT_20.getAppId(), MOBILE_CLIENT.getAppId()} ) );
	private String appId;

	private AppIds( String appid )
	{
		appId = appid;
	}

	public String getAppId()
	{
		return appId;
	}

	public static boolean isCommandClient( String appId )
	{
		return COMMAND_IDS.contains( appId );
	}

	public static boolean isR5AppId( String appId )
	{
		return R5.getAppId().equals( appId );
	}
}
