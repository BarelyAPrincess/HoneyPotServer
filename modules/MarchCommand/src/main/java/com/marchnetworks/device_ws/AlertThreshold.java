package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "AlertThreshold", propOrder = {"alertCode", "notificationType", "durationSec", "frequencyCount", "frequencySec"} )
public class AlertThreshold
{
	@XmlElement( required = true )
	protected String alertCode;
	@XmlElement( required = true )
	protected AlertThresholdNotification notificationType;
	protected int durationSec;
	protected int frequencyCount;
	protected int frequencySec;

	public String getAlertCode()
	{
		return alertCode;
	}

	public void setAlertCode( String value )
	{
		alertCode = value;
	}

	public AlertThresholdNotification getNotificationType()
	{
		return notificationType;
	}

	public void setNotificationType( AlertThresholdNotification value )
	{
		notificationType = value;
	}

	public int getDurationSec()
	{
		return durationSec;
	}

	public void setDurationSec( int value )
	{
		durationSec = value;
	}

	public int getFrequencyCount()
	{
		return frequencyCount;
	}

	public void setFrequencyCount( int value )
	{
		frequencyCount = value;
	}

	public int getFrequencySec()
	{
		return frequencySec;
	}

	public void setFrequencySec( int value )
	{
		frequencySec = value;
	}
}
