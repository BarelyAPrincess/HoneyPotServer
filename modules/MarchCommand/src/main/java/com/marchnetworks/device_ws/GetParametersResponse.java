package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "", propOrder = {"getParametersResult"} )
@XmlRootElement( name = "GetParametersResponse" )
public class GetParametersResponse
{
	@XmlElement( name = "GetParametersResult", required = true )
	protected GetParametersResult getParametersResult;

	public GetParametersResult getGetParametersResult()
	{
		return getParametersResult;
	}

	public void setGetParametersResult( GetParametersResult value )
	{
		getParametersResult = value;
	}
}
