package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "", propOrder = {"getTimeResult"} )
@XmlRootElement( name = "GetTimeResponse" )
public class GetTimeResponse
{
	@XmlElement( name = "GetTimeResult", required = true )
	protected Timestamp getTimeResult;

	public Timestamp getGetTimeResult()
	{
		return getTimeResult;
	}

	public void setGetTimeResult( Timestamp value )
	{
		getTimeResult = value;
	}
}
