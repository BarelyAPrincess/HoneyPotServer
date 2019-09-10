package com.marchnetworks.management.instrumentation;

public class InstrumentationSettings
{
	public static final int REGISTRATION_RETRYPERIOD = 5;

	public static final int REGISTRATION_EXPIRATIONPERIOD = 1800000;

	public static final int SUBSCRIPTION_RETRYPERIOD = 10;

	public static final int SUBSCRIPTION_EXPIRATIONPERIOD = 3600;

	public static final long CDA_SUBSCRIPTIONTIMEOUT = 172800L;

	public static final int CDA_MAXRETRY = 0;

	public static final int COMMANDCONFIGURATION_TIMEOUT = 900;

	public static final int COMMANDCONFIGURATION_MOBILE_TIMEOUT = 172800;

	public static final long COMMANDCONFIGURATIONAPPLIED_TIMEOUT = 15L;

	public static final long COMMANDCONFIGURATIONAPPLIED_TIMEOUT_R5 = 2L;

	public static final int CONCURRENT_UPGRADES_PER_DEVICE = 1;

	public static final int COMMANDUPGRADE_TIMEOUT = 900;

	public static final int COMMANDUPGRADE_MOBILE_TIMEOUT = 172800;

	public static final int COMMANDUPGRADE_LONG_TIMEOUT = 2700;

	public static final int BUNDLED_UPGRADE_TIMEOUT = 300;

	public static final long OFFLINE_UPGRADECOMMAND_TIMEOUT = 172800L;

	public static int DEVICE_TIME_TO_OFFLINE = 240;

	public static int DEVICE_NOTIFY_INTERVAL = 180;
	public static int DEVICE_OFFLINE_CHECK_INTERVAL = 60;
	public static final int DEVICE_TIME_TO_OFFLINE_MIN = 100;
	public static final int DEVICE_NOTIFY_INTERVAL_MIN = 80;
	public static final int DEVICE_OFFLINE_CHECK_INTERVAL_MIN = 30;
	public static final int DEVICE_OFFLINE_TO_ALERT = 2880;
}

