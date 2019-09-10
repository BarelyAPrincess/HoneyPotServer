package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "", propOrder = {"getChannelDetailsResult"} )
@XmlRootElement( name = "GetChannelDetailsResponse" )
public class GetChannelDetailsResponse
{
	@XmlElement( name = "GetChannelDetailsResult" )
	protected ChannelDetails getChannelDetailsResult;

	public ChannelDetails getGetChannelDetailsResult()
	{
		return getChannelDetailsResult;
	}

	public void setGetChannelDetailsResult( ChannelDetails value )
	{
		getChannelDetailsResult = value;
	}
}
