package com.marchnetworks.command.common.device.data;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

public class ChannelView
{
	private transient Long id;
	private String channelId;
	private String channelName;
	private ChannelState channelState;
	private String ptzDomeIdentifier;
	private String[] assocIds;
	private VideoEncoderView[] video;
	private AudioEncoderView[] audio;
	private TextEncoderView[] text;
	private DataEncoderView[] data;
	private transient String deviceId;

	public String getChannelId()
	{
		return channelId;
	}

	public void setChannelId( String channelId )
	{
		this.channelId = channelId;
	}

	public String getChannelName()
	{
		return channelName;
	}

	public void setChannelName( String channelName )
	{
		this.channelName = channelName;
	}

	@XmlElement( required = true )
	public ChannelState getChannelState()
	{
		return channelState;
	}

	public void setChannelState( ChannelState channelState )
	{
		this.channelState = channelState;
	}

	public String getPtzDomeIdentifier()
	{
		return ptzDomeIdentifier;
	}

	public void setPtzDomeIdentifier( String ptzDomeIdentifier )
	{
		this.ptzDomeIdentifier = ptzDomeIdentifier;
	}

	public String[] getAssocIds()
	{
		return assocIds;
	}

	public void setAssocIds( String[] assocIds )
	{
		this.assocIds = assocIds;
	}

	public VideoEncoderView[] getVideo()
	{
		return video;
	}

	public void setVideo( VideoEncoderView[] video )
	{
		this.video = video;
	}

	public AudioEncoderView[] getAudio()
	{
		return audio;
	}

	public void setAudio( AudioEncoderView[] audio )
	{
		this.audio = audio;
	}

	public TextEncoderView[] getText()
	{
		return text;
	}

	public void setText( TextEncoderView[] text )
	{
		this.text = text;
	}

	public DataEncoderView[] getData()
	{
		return data;
	}

	public void setData( DataEncoderView[] data )
	{
		this.data = data;
	}

	@XmlTransient
	public Long getId()
	{
		return id;
	}

	public void setId( Long id )
	{
		this.id = id;
	}

	@XmlTransient
	public String getDeviceId()
	{
		return deviceId;
	}

	public void setDeviceId( String deviceId )
	{
		this.deviceId = deviceId;
	}
}
