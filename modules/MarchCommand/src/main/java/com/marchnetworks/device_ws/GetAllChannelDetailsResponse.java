package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "", propOrder = {"getAllChannelDetailsResult"} )
@XmlRootElement( name = "GetAllChannelDetailsResponse" )
public class GetAllChannelDetailsResponse
{
	@XmlElement( name = "GetAllChannelDetailsResult", required = true )
	protected ArrayOfChannelDetails getAllChannelDetailsResult;

	public ArrayOfChannelDetails getGetAllChannelDetailsResult()
	{
		return getAllChannelDetailsResult;
	}

	public void setGetAllChannelDetailsResult( ArrayOfChannelDetails value )
	{
		getAllChannelDetailsResult = value;
	}
}
