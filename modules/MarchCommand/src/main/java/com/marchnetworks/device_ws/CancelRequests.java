package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "", propOrder = {"requestIds"} )
@XmlRootElement( name = "CancelRequests" )
public class CancelRequests
{
	@XmlElement( required = true )
	protected ArrayOfString requestIds;

	public ArrayOfString getRequestIds()
	{
		return requestIds;
	}

	public void setRequestIds( ArrayOfString value )
	{
		requestIds = value;
	}
}
