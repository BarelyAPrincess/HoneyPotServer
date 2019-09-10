package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "AudioOutput", propOrder = {"id", "type", "audioOutputDeviceId", "audioOutputDeviceAddress", "name", "state", "info", "codec", "channels", "samplesPerSecond", "bitsPerSample"} )
public class AudioOutput
{
	@XmlElement( required = true )
	protected String id;
	@XmlElement( required = true )
	protected String type;
	@XmlElement( required = true )
	protected String audioOutputDeviceId;
	@XmlElement( required = true )
	protected String audioOutputDeviceAddress;
	@XmlElement( required = true )
	protected String name;
	@XmlElement( required = true )
	protected String state;
	protected ArrayOfPair info;
	@XmlElement( required = true )
	protected String codec;
	protected int channels;
	protected int samplesPerSecond;
	protected int bitsPerSample;

	public String getId()
	{
		return id;
	}

	public void setId( String value )
	{
		id = value;
	}

	public String getType()
	{
		return type;
	}

	public void setType( String value )
	{
		type = value;
	}

	public String getAudioOutputDeviceId()
	{
		return audioOutputDeviceId;
	}

	public void setAudioOutputDeviceId( String value )
	{
		audioOutputDeviceId = value;
	}

	public String getAudioOutputDeviceAddress()
	{
		return audioOutputDeviceAddress;
	}

	public void setAudioOutputDeviceAddress( String value )
	{
		audioOutputDeviceAddress = value;
	}

	public String getName()
	{
		return name;
	}

	public void setName( String value )
	{
		name = value;
	}

	public String getState()
	{
		return state;
	}

	public void setState( String value )
	{
		state = value;
	}

	public ArrayOfPair getInfo()
	{
		return info;
	}

	public void setInfo( ArrayOfPair value )
	{
		info = value;
	}

	public String getCodec()
	{
		return codec;
	}

	public void setCodec( String value )
	{
		codec = value;
	}

	public int getChannels()
	{
		return channels;
	}

	public void setChannels( int value )
	{
		channels = value;
	}

	public int getSamplesPerSecond()
	{
		return samplesPerSecond;
	}

	public void setSamplesPerSecond( int value )
	{
		samplesPerSecond = value;
	}

	public int getBitsPerSample()
	{
		return bitsPerSample;
	}

	public void setBitsPerSample( int value )
	{
		bitsPerSample = value;
	}
}
