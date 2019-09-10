package com.marchnetworks.device_ws;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "", propOrder = {"eventPrefixes", "timeoutSeconds", "notifyContextPath", "maxNotifyTime", "offlineSeqNo"} )
@XmlRootElement( name = "SubscribeEventsNotify" )
public class SubscribeEventsNotify
{
	@XmlElement( required = true )
	protected ArrayOfString eventPrefixes;
	protected double timeoutSeconds;
	@XmlElement( required = true )
	protected String notifyContextPath;
	protected double maxNotifyTime;
	@XmlElementRef( name = "offlineSeqNo", namespace = "http://marchnetworks.com/device_ws/", type = JAXBElement.class, required = false )
	protected JAXBElement<Long> offlineSeqNo;

	public ArrayOfString getEventPrefixes()
	{
		return eventPrefixes;
	}

	public void setEventPrefixes( ArrayOfString value )
	{
		eventPrefixes = value;
	}

	public double getTimeoutSeconds()
	{
		return timeoutSeconds;
	}

	public void setTimeoutSeconds( double value )
	{
		timeoutSeconds = value;
	}

	public String getNotifyContextPath()
	{
		return notifyContextPath;
	}

	public void setNotifyContextPath( String value )
	{
		notifyContextPath = value;
	}

	public double getMaxNotifyTime()
	{
		return maxNotifyTime;
	}

	public void setMaxNotifyTime( double value )
	{
		maxNotifyTime = value;
	}

	public JAXBElement<Long> getOfflineSeqNo()
	{
		return offlineSeqNo;
	}

	public void setOfflineSeqNo( JAXBElement<Long> value )
	{
		offlineSeqNo = value;
	}
}
