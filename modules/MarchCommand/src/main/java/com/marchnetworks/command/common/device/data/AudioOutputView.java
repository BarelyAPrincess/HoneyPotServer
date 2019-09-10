package com.marchnetworks.command.common.device.data;

import com.marchnetworks.command.common.transport.data.Pair;

import javax.xml.bind.annotation.XmlElement;

public class AudioOutputView implements DeviceOutputView
{
	private String audioOutputId;
	private String name;
	private AudioOutputType type;
	private String audioOutputDeviceId;
	private String audioOutputDeviceAddress;
	private String codec;
	private int channels;
	private int samplesPerSecond;
	private int bitsPerSample;
	private AudioOutputState state;
	private Pair[] info;

	public AudioOutputView( String audioOutputId, String name, AudioOutputType type, String audioOutputDeviceId, String audioOutputDeviceAddress, String codec, int channels, int samplesPerSecond, int bitsPerSample, AudioOutputState state, Pair[] info )
	{
		this.audioOutputId = audioOutputId;
		this.name = name;
		this.type = type;
		this.audioOutputDeviceId = audioOutputDeviceId;
		this.audioOutputDeviceAddress = audioOutputDeviceAddress;
		this.codec = codec;
		this.channels = channels;
		this.samplesPerSecond = samplesPerSecond;
		this.bitsPerSample = bitsPerSample;
		this.state = state;
		this.info = info;
	}

	public String getAudioOutputId()
	{
		return audioOutputId;
	}

	public void setAudioOutputId( String audioOutputId )
	{
		this.audioOutputId = audioOutputId;
	}

	public String getName()
	{
		return name;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	@XmlElement( required = true )
	public AudioOutputType getType()
	{
		return type;
	}

	public void setType( AudioOutputType type )
	{
		this.type = type;
	}

	public String getAudioOutputDeviceId()
	{
		return audioOutputDeviceId;
	}

	public void setAudioOutputDeviceId( String audioOutputDeviceId )
	{
		this.audioOutputDeviceId = audioOutputDeviceId;
	}

	public String getAudioOutputDeviceAddress()
	{
		return audioOutputDeviceAddress;
	}

	public void setAudioOutputDeviceAddress( String audioOutputDeviceAddress )
	{
		this.audioOutputDeviceAddress = audioOutputDeviceAddress;
	}

	public String getCodec()
	{
		return codec;
	}

	public void setCodec( String codec )
	{
		this.codec = codec;
	}

	public int getChannels()
	{
		return channels;
	}

	public void setChannels( int channels )
	{
		this.channels = channels;
	}

	public int getSamplesPerSecond()
	{
		return samplesPerSecond;
	}

	public void setSamplesPerSecond( int samplesPerSecond )
	{
		this.samplesPerSecond = samplesPerSecond;
	}

	public int getBitsPerSample()
	{
		return bitsPerSample;
	}

	public void setBitsPerSample( int bitsPerSample )
	{
		this.bitsPerSample = bitsPerSample;
	}

	@XmlElement( required = true )
	public AudioOutputState getState()
	{
		return state;
	}

	public void setState( AudioOutputState state )
	{
		this.state = state;
	}

	public Pair[] getInfo()
	{
		return info;
	}

	public void setInfo( Pair[] info )
	{
		this.info = info;
	}
}
