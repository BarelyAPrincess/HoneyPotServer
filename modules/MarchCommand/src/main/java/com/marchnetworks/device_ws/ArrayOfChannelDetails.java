package com.marchnetworks.device_ws;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "ArrayOfChannelDetails", propOrder = {"channelDetails"} )
public class ArrayOfChannelDetails
{
	@XmlElement( name = "ChannelDetails" )
	protected List<ChannelDetails> channelDetails;

	public List<ChannelDetails> getChannelDetails()
	{
		if ( channelDetails == null )
		{
			channelDetails = new ArrayList();
		}
		return channelDetails;
	}
}
