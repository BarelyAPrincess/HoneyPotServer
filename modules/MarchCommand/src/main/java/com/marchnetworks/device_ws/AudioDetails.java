package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "AudioDetails", propOrder = {"encoderId", "codec", "codecPrvData", "estBPS", "channels", "sampBits", "sampRate"} )
public class AudioDetails
{
	@XmlElement( required = true )
	protected String encoderId;
	@XmlElement( required = true )
	protected String codec;
	@XmlElement( required = true )
	protected String codecPrvData;
	protected double estBPS;
	protected int channels;
	protected int sampBits;
	protected int sampRate;

	public String getEncoderId()
	{
		return encoderId;
	}

	public void setEncoderId( String value )
	{
		encoderId = value;
	}

	public String getCodec()
	{
		return codec;
	}

	public void setCodec( String value )
	{
		codec = value;
	}

	public String getCodecPrvData()
	{
		return codecPrvData;
	}

	public void setCodecPrvData( String value )
	{
		codecPrvData = value;
	}

	public double getEstBPS()
	{
		return estBPS;
	}

	public void setEstBPS( double value )
	{
		estBPS = value;
	}

	public int getChannels()
	{
		return channels;
	}

	public void setChannels( int value )
	{
		channels = value;
	}

	public int getSampBits()
	{
		return sampBits;
	}

	public void setSampBits( int value )
	{
		sampBits = value;
	}

	public int getSampRate()
	{
		return sampRate;
	}

	public void setSampRate( int value )
	{
		sampRate = value;
	}
}
