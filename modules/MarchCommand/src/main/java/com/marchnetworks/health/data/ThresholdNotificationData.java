package com.marchnetworks.health.data;

import com.marchnetworks.common.types.AlertThresholdNotificationEnum;

public enum ThresholdNotificationData
{
	ALWAYS,
	NEVER,
	FREQUENCY,
	DURATION,
	FREQUENCYORDURATION;

	private ThresholdNotificationData()
	{
	}

	public static AlertThresholdNotificationEnum Convert( ThresholdNotificationData data )
	{
		if ( data == ALWAYS )
		{
			return AlertThresholdNotificationEnum.ALWAYS;
		}
		if ( data == DURATION )
		{
			return AlertThresholdNotificationEnum.DURATION;
		}
		if ( data == FREQUENCY )
		{
			return AlertThresholdNotificationEnum.FREQUENCY;
		}
		if ( data == FREQUENCYORDURATION )
		{
			return AlertThresholdNotificationEnum.FREQUENCYORDURATION;
		}
		return AlertThresholdNotificationEnum.NEVER;
	}

	public static ThresholdNotificationData Convert( AlertThresholdNotificationEnum data )
	{
		if ( data == AlertThresholdNotificationEnum.ALWAYS )
		{
			return ALWAYS;
		}
		if ( data == AlertThresholdNotificationEnum.DURATION )
		{
			return DURATION;
		}
		if ( data == AlertThresholdNotificationEnum.FREQUENCY )
		{
			return FREQUENCY;
		}
		if ( data == AlertThresholdNotificationEnum.FREQUENCYORDURATION )
		{
			return FREQUENCYORDURATION;
		}
		return NEVER;
	}
}
