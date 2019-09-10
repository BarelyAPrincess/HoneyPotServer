package com.marchnetworks.server.communications.transport.datamodel;

public class AudioDetails
{
	protected String encoderId;

	protected String codec;

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

