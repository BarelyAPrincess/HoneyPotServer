package com.marchnetworks.device_ws;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "ArrayOfVideoDetails", propOrder = {"videoDetails"} )
public class ArrayOfVideoDetails
{
	@XmlElement( name = "VideoDetails" )
	protected List<VideoDetails> videoDetails;

	public List<VideoDetails> getVideoDetails()
	{
		if ( videoDetails == null )
		{
			videoDetails = new ArrayList();
		}
		return videoDetails;
	}
}
