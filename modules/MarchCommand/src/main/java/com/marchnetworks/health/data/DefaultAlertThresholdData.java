package com.marchnetworks.health.data;

import com.marchnetworks.common.types.AlertThresholdDefinitionEnum;

public class DefaultAlertThresholdData extends AlertThresholdData
{
	private boolean hasDurationSupport = false;
	private boolean hasFrequencySupport = false;

	public DefaultAlertThresholdData()
	{
	}

	public boolean getHasFrequencySupport()
	{
		return hasFrequencySupport;
	}

	public boolean getHasDurationSupport()
	{
		return hasDurationSupport;
	}

	public void setHasFrequencySupport( boolean hasFrequencySupport )
	{
		this.hasFrequencySupport = hasFrequencySupport;
	}

	public void setHasDurationSupport( boolean hasDurationSupport )
	{
		this.hasDurationSupport = hasDurationSupport;
	}

	public DefaultAlertThresholdData( AlertThresholdDefinitionEnum alertThresholdDefinitionEnum )
	{
		super( alertThresholdDefinitionEnum );
		hasDurationSupport = alertThresholdDefinitionEnum.HasDurationSupport();
		hasFrequencySupport = alertThresholdDefinitionEnum.HasFrequencySupport();
	}

	public DefaultAlertThresholdData( String alertCode, ThresholdNotificationData notificationType, int duration, int frequencyDuration, int frequencyCount, boolean hasDurationSupport, boolean hasFrequencySupport )
	{
		super( alertCode, notificationType, duration, frequencyDuration, frequencyCount );

		this.hasDurationSupport = hasDurationSupport;
		this.hasFrequencySupport = hasFrequencySupport;
	}
}
