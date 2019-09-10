package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "", propOrder = {"subscriptionId", "timeoutSeconds"} )
@XmlRootElement( name = "GetWaitingEvents" )
public class GetWaitingEvents
{
	@XmlElement( required = true )
	protected String subscriptionId;
	protected double timeoutSeconds;

	public String getSubscriptionId()
	{
		return subscriptionId;
	}

	public void setSubscriptionId( String value )
	{
		subscriptionId = value;
	}

	public double getTimeoutSeconds()
	{
		return timeoutSeconds;
	}

	public void setTimeoutSeconds( double value )
	{
		timeoutSeconds = value;
	}
}
