package com.marchnetworks.command.api.alarm;

public class AlarmConstants
{
	public static final String DETAILS_COUNT = "count";
	public static final String DETAILS_FIRST_INSTANCE = "first";
	public static final String DETAILS_LAST_INSTANCE = "last";
	public static final String DETAILS_CLOSED_BY_USER = "closedByUser";
	public static final String DETAILS_TEXT = "text";
	public static final String DETAILS_HANDLER = "handler";
	public static final String DETAILS_EXTENDED_STATE = "extState";
	public static final String DETAILS_ASSOCIATIONS = "assocId";
	public static final String DETAILS_LAST_STATE_CHANGE = "lastStateChange";
	public static final String DETAILS_ALARM_DETAILS = "alarmDetails";
	public static final String ALARM_SOURCE_TRIGGER_PREFIX = "ALARM_SOURCE_";
	public static final String CLOSED_TEXT_DEVICE_DELETED = "@StringCode_DeviceRemoved";
	public static final String CLOSED_TEXT_ALARM_SOURCE_DELETED = "@StringCode_AlarmRemoved";
	public static final String CES_CLOSED_BY_USER = "CES";
	public static final String ALARMSOURCE_CAMERA_OBSTRUCTION = "alarm.obstruction";
	public static final long ALARM_CLOSURE_DISPATCH_EVENT_EVICTION_AGE = 86400000L;
}
