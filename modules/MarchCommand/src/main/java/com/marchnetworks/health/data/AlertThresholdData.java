package com.marchnetworks.health.data;

import com.marchnetworks.common.types.AlertThresholdDefinitionEnum;

import javax.xml.bind.annotation.XmlElement;

public class AlertThresholdData
{
	private String alertCode;
	private ThresholdNotificationData notificationType;
	private int duration;
	private int frequencyDuration;
	private int frequencyCount;

	public AlertThresholdData()
	{
	}

	public AlertThresholdData( AlertThresholdDefinitionEnum alertThresholdDefinitionEnum )
	{
		alertCode = alertThresholdDefinitionEnum.getAlertCode();
		duration = alertThresholdDefinitionEnum.getDuration();
		frequencyCount = alertThresholdDefinitionEnum.getFrequencyCount();
		frequencyDuration = alertThresholdDefinitionEnum.getFrequencyDuration();
		notificationType = ThresholdNotificationData.Convert( alertThresholdDefinitionEnum.getNotificationType() );
	}

	public AlertThresholdData( String alertCode, ThresholdNotificationData notificationType, int duration, int frequencyDuration, int frequencyCount )
	{
		this.alertCode = alertCode;
		this.notificationType = notificationType;
		this.duration = duration;
		this.frequencyCount = frequencyCount;
		this.frequencyDuration = frequencyDuration;
	}

	public String getAlertCode()
	{
		return alertCode;
	}

	@XmlElement( required = true )
	public ThresholdNotificationData getNotificationType()
	{
		return notificationType;
	}

	public int getDuration()
	{
		return duration;
	}

	public int getFrequencyDuration()
	{
		return frequencyDuration;
	}

	public int getFrequencyCount()
	{
		return frequencyCount;
	}

	public void setAlertCode( String alertCode )
	{
		this.alertCode = alertCode;
	}

	public void setNotificationType( ThresholdNotificationData notificationType )
	{
		this.notificationType = notificationType;
	}

	public void setDuration( int duration )
	{
		this.duration = duration;
	}

	public void setFrequencyDuration( int frequencyTimeSpan )
	{
		frequencyDuration = frequencyTimeSpan;
	}

	public void setFrequencyCount( int frequencyCount )
	{
		this.frequencyCount = frequencyCount;
	}
}
