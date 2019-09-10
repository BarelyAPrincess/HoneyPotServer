package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "", propOrder = {"getWaitingEventsResult"} )
@XmlRootElement( name = "GetWaitingEventsResponse" )
public class GetWaitingEventsResponse
{
	@XmlElement( name = "GetWaitingEventsResult", required = true )
	protected ArrayOfEvent getWaitingEventsResult;

	public ArrayOfEvent getGetWaitingEventsResult()
	{
		return getWaitingEventsResult;
	}

	public void setGetWaitingEventsResult( ArrayOfEvent value )
	{
		getWaitingEventsResult = value;
	}
}
