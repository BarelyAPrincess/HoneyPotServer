package com.marchnetworks.common.types;

public enum AlertSeverityEnum
{
	CRITICAL,
	WARNING,
	INFO,
	IGNORE;

	private AlertSeverityEnum()
	{
	}

	public boolean isMoreSeverThan( AlertSeverityEnum a_Other )
	{
		return compareTo( a_Other ) < 0;
	}

	public boolean isLessSeverThan( AlertSeverityEnum a_Other )
	{
		return compareTo( a_Other ) > 0;
	}
}
