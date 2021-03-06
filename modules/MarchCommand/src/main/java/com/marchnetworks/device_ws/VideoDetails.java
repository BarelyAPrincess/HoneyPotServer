package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "VideoDetails", propOrder = {"encoderId", "codec", "codecPrvData", "profile", "resolutionHint", "estBPS", "width", "height", "fps"} )
public class VideoDetails
{
	@XmlElement( required = true )
	protected String encoderId;
	@XmlElement( required = true )
	protected String codec;
	@XmlElement( required = true )
	protected String codecPrvData;
	@XmlElement( required = true )
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
