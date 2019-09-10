package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "", propOrder = {"subscriptionId", "eventPrefixes"} )
@XmlRootElement( name = "ModifySubscription" )
public class ModifySubscription
{
	@XmlElement( required = true )
	protected String subscriptionId;
	@XmlElement( required = true )
	protected ArrayOfString eventPrefixes;

	public String getSubscriptionId()
	{
		return subscriptionId;
	}

	public void setSubscriptionId( String value )
	{
		subscriptionId = value;
	}

	public ArrayOfString getEventPrefixes()
	{
		return eventPrefixes;
	}

	public void setEventPrefixes( ArrayOfString value )
	{
		eventPrefixes = value;
	}
}
