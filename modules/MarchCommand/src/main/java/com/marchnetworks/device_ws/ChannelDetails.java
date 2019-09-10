package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "ChannelDetails", propOrder = {"id", "ipDevice", "name", "channelState", "ptzDomeId", "assocIds", "video", "audio", "text", "data"} )
public class ChannelDetails
{
	@XmlElement( required = true )
	protected String id;
	protected DeviceDetails ipDevice;
	@XmlElement( required = true )
	protected String name;
	@XmlElement( required = true )
	protected ChannelState channelState;
	@XmlElement( required = true )
	protected String ptzDomeId;
	@XmlElement( required = true )
	protected ArrayOfString assocIds;
	@XmlElement( required = true )
	protected ArrayOfVideoDetails video;
	@XmlElement( required = true )
	protected ArrayOfAudioDetails audio;
	protected ArrayOfTextDetails text;
	protected ArrayOfDataDetails data;

	public String getId()
	{
		return id;
	}

	public void setId( String value )
	{
		id = value;
	}

	public DeviceDetails getIpDevice()
	{
		return ipDevice;
	}

	public void setIpDevice( DeviceDetails value )
	{
		ipDevice = value;
	}

	public String getName()
	{
		return name;
	}

	public void setName( String value )
	{
		name = value;
	}

	public ChannelState getChannelState()
	{
		return channelState;
	}

	public void setChannelState( ChannelState value )
	{
		channelState = value;
	}

	public String getPtzDomeId()
	{
		return ptzDomeId;
	}

	public void setPtzDomeId( String value )
	{
		ptzDomeId = value;
	}

	public ArrayOfString getAssocIds()
	{
		return assocIds;
	}

	public void setAssocIds( ArrayOfString value )
	{
		assocIds = value;
	}

	public ArrayOfVideoDetails getVideo()
	{
		return video;
	}

	public void setVideo( ArrayOfVideoDetails value )
	{
		video = value;
	}

	public ArrayOfAudioDetails getAudio()
	{
		return audio;
	}

	public void setAudio( ArrayOfAudioDetails value )
	{
		audio = value;
	}

	public ArrayOfTextDetails getText()
	{
		return text;
	}

	public void setText( ArrayOfTextDetails value )
	{
		text = value;
	}

	public ArrayOfDataDetails getData()
	{
		return data;
	}

	public void setData( ArrayOfDataDetails value )
	{
		data = value;
	}
}
