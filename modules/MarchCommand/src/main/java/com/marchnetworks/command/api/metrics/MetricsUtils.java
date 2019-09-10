package com.marchnetworks.command.api.metrics;

public class MetricsUtils
{
	public static final String SUCCESS = "success";

	public static final String FAILURE = "failure";

	public static final int METRICS_PATH_LENGTH = 36;

	public static String getUrlPathShort( String urlPath )
	{
		return urlPath.substring( urlPath.length() <= 36 ? 0 : urlPath.length() - 36, urlPath.length() );
	}
}
