package com.marchnetworks.server.communications.transport.datamodel;

import com.marchnetworks.command.common.transport.data.Pair;

public class AudioOutput implements DeviceOutput
{
	private String id;
	private String type;
	private String audioOutputDeviceId;
	private String audioOutputDeviceAddress;
	private String name;
	private String state;
	private String codec;
	private int channels;
	private int samplesPerSecond;
	private int bitsPerSample;
	private Pair[] info;

	public String getId()
	{
		return id;
	}

	public void setId( String id )
	{
		this.id = id;
	}

	public String getType()
	{
		return type;
	}

	public void setType( String type )
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

	public String getName()
	{
		return name;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	public String getState()
	{
		return state;
	}

	public void setState( String state )
	{
		this.state = state;
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

	public Pair[] getInfo()
	{
		return info;
	}

	public void setInfo( Pair[] info )
	{
		this.info = info;
	}
}

