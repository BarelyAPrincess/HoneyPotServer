package com.marchnetworks.device_ws;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "", propOrder = {"eventPrefixes", "timeoutSeconds", "offlineSeqNo"} )
@XmlRootElement( name = "Subscribe" )
public class Subscribe
{
	@XmlElement( required = true )
	protected ArrayOfString eventPrefixes;
	protected double timeoutSeconds;
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

	public JAXBElement<Long> getOfflineSeqNo()
	{
		return offlineSeqNo;
	}

	public void setOfflineSeqNo( JAXBElement<Long> value )
	{
		offlineSeqNo = value;
	}
}
