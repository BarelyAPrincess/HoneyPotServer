package com.marchnetworks.license.service;

import java.util.Collections;
import java.util.List;

public class BlacklistedApps
{
	private static final List<String> apps = Collections.singletonList( AppIds.SEARCHLIGHT_4_0.getId() );

	public static boolean isBlacklisted( String appId )
	{
		return apps.contains( appId );
	}
}
