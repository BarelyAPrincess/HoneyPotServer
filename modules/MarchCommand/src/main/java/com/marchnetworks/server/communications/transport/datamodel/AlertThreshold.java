package com.marchnetworks.server.communications.transport.datamodel;

public class AlertThreshold
{
	protected String alertCode;

	protected AlertThresholdNotification notificationType;

	protected int durationSec;

	protected int frequencyCount;
	protected int frequencySec;

	public AlertThreshold()
	{
	}

	public AlertThreshold( String alertCode, AlertThresholdNotification notificationType, int durationSec, int frequencySec, int frequencyCount )
	{
		this.alertCode = alertCode;
		this.notificationType = notificationType;
		this.durationSec = durationSec;
		this.frequencySec = frequencySec;
		this.frequencyCount = frequencyCount;
	}

	public String getAlertCode()
	{
		return alertCode;
	}

	public void setAlertCode( String alertCode )
	{
		this.alertCode = alertCode;
	}

	public AlertThresholdNotification getNotificationType()
	{
		return notificationType;
	}

	public void setNotificationType( AlertThresholdNotification notificationType )
	{
		this.notificationType = notificationType;
	}

	public int getDurationSec()
	{
		return durationSec;
	}

	public void setDurationSec( int durationSec )
	{
		this.durationSec = durationSec;
	}

	public int getFrequencyCount()
	{
		return frequencyCount;
	}

	public void setFrequencyCount( int frequencyCount )
	{
		this.frequencyCount = frequencyCount;
	}

	public int getFrequencySec()
	{
		return frequencySec;
	}

	public void setFrequencySec( int frequencySec )
	{
		this.frequencySec = frequencySec;
	}
}

