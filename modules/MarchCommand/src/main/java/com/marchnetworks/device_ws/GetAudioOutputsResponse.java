package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "", propOrder = {"getAudioOutputsResult"} )
@XmlRootElement( name = "GetAudioOutputsResponse" )
public class GetAudioOutputsResponse
{
	@XmlElement( name = "GetAudioOutputsResult", required = true )
	protected ArrayOfAudioOutput getAudioOutputsResult;

	public ArrayOfAudioOutput getGetAudioOutputsResult()
	{
		return getAudioOutputsResult;
	}

	public void setGetAudioOutputsResult( ArrayOfAudioOutput value )
	{
		getAudioOutputsResult = value;
	}
}
