package com.marchnetworks.app.service;

import java.io.File;

public class AppConstants
{
	public static final String APP_DIRECTORY = "apps" + File.separator;
	public static final String APP_TEST_DIRECTORY = APP_DIRECTORY + "test" + File.separator;
	public static final String BUILT_IN_APP_DIRECTORY = "apps" + File.separator + "builtIn";
	public static final String APP_XML_FILE = "app.xml";
	public static final String DETAILS_START_TIME = "startTime";
	public static final int APP_RESTART_DELAY = 5000;
	public static final int APP_MAX_WAIT = 240000;
}
