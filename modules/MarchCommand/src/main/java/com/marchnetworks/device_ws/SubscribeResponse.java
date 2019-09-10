package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "", propOrder = {"subscribeResult"} )
@XmlRootElement( name = "SubscribeResponse" )
public class SubscribeResponse
{
	@XmlElement( name = "SubscribeResult", required = true )
	protected String subscribeResult;

	public String getSubscribeResult()
	{
		return subscribeResult;
	}

	public void setSubscribeResult( String value )
	{
		subscribeResult = value;
	}
}
