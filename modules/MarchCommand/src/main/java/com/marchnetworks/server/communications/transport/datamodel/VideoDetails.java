package com.marchnetworks.server.communications.transport.datamodel;

public class VideoDetails
{
	protected String encoderId;

	protected String codec;

	protected String codecPrvData;

	protected String profile;

	protected int resolutionHint;

	protected double estBPS;

	protected int width;

	protected int height;

	protected double fps;

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

	public String getProfile()
	{
		return profile;
	}

	public void setProfile( String value )
	{
		profile = value;
	}

	public int getResolutionHint()
	{
		return resolutionHint;
	}

	public void setResolutionHint( int value )
	{
		resolutionHint = value;
	}

	public double getEstBPS()
	{
		return estBPS;
	}

	public void setEstBPS( double value )
	{
		estBPS = value;
	}

	public int getWidth()
	{
		return width;
	}

	public void setWidth( int value )
	{
		width = value;
	}

	public int getHeight()
	{
		return height;
	}

	public void setHeight( int value )
	{
		height = value;
	}

	public double getFps()
	{
		return fps;
	}

	public void setFps( double value )
	{
		fps = value;
	}
}

