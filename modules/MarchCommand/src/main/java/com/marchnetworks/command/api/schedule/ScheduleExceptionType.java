package com.marchnetworks.command.api.schedule;

public enum ScheduleExceptionType
{
	NOT_FOUND,
	SCHEDULE_NAME_EXISTS,
	SCHEDULE_CREATE_ERROR,
	SCHEDULE_IN_USE,
	SCHEDULE_USER_NOT_FOUND,
	SCHEDULE_INSUFFICIENT_EDIT_RIGHTS;

	private ScheduleExceptionType()
	{
	}
}
