package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "", propOrder = {"channelIds"} )
@XmlRootElement( name = "GetChannelConfigHashes" )
public class GetChannelConfigHashes
{
	protected ArrayOfString channelIds;

	public ArrayOfString getChannelIds()
	{
		return channelIds;
	}

	public void setChannelIds( ArrayOfString value )
	{
		channelIds = value;
	}
}
