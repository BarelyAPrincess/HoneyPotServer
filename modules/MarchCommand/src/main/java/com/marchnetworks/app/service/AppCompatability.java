package com.marchnetworks.app.service;

import com.marchnetworks.common.utils.CommonUtils;

import java.util.HashMap;
import java.util.Map;

public class AppCompatability
{
	private static Map<String, String> minimumVersions = new HashMap<String, String>();

	static
	{
		minimumVersions.put( "9b944d3e-810f-4487-8d00-1bf583112ba1", "4.6" );
		minimumVersions.put( "71f5b99e-131c-4cc2-9c0f-98cb1b9f0d17", "2.5" );
		minimumVersions.put( "ca795d99-d5a2-4142-83ab-35a67007507d", "2.5" );
		minimumVersions.put( "9eca6672-3ca7-4a05-80cf-8982bc53838f", "2.5" );
	}

	public static boolean isCompatible( String appId, String version )
	{
		String minimum = ( String ) minimumVersions.get( appId );
		if ( minimum == null )
		{
			return true;
		}

		return CommonUtils.compareVersions( version, minimum ) != -1;
	}

	public static String getMinimumVersion( String appId )
	{
		return ( String ) minimumVersions.get( appId );
	}
}
