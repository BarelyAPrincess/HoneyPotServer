package com.marchnetworks.alarm.service;

public enum AlarmExceptionTypeEnum
{
	SECURITY,
	CLOSE_ENTRIES_ERROR,
	HANDLE_ENTRIES_ERROR,
	FEATURE_IS_DISABLED,
	ALARM_NOT_FOUND;

	private AlarmExceptionTypeEnum()
	{
	}
}
