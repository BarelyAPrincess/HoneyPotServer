package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "", propOrder = {"getConfigHashResult"} )
@XmlRootElement( name = "GetConfigHashResponse" )
public class GetConfigHashResponse
{
	@XmlElement( name = "GetConfigHashResult", required = true )
	protected String getConfigHashResult;

	public String getGetConfigHashResult()
	{
		return getConfigHashResult;
	}

	public void setGetConfigHashResult( String value )
	{
		getConfigHashResult = value;
	}
}
