package com.marchnetworks.command.common.topology.data;

import javax.xml.bind.annotation.XmlElement;

public class AudioOutputLinkResource extends LinkResource
{
	private String audioOutputId;
	private Long deviceResourceId;

	public String getAudioOutputId()
	{
		return audioOutputId;
	}

	public void setAudioOutputId( String audioOutputId )
	{
		this.audioOutputId = audioOutputId;
	}

	@XmlElement( required = true )
	public Long getDeviceResourceId()
	{
		return deviceResourceId;
	}

	public void setDeviceResourceId( Long deviceResourceId )
	{
		this.deviceResourceId = deviceResourceId;
	}
}
