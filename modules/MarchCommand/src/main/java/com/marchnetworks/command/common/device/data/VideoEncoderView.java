package com.marchnetworks.command.common.device.data;

public class VideoEncoderView extends EncoderView
{
	public static final VideoEncoderView[] NO_ENCODERS = new VideoEncoderView[0];
	private String codec;
	private String codecPrvData;
	private String profile;
	private int resolutionHint;
	private double estBPS;
	private int width;
	private int height;
	private double fps;

	public String getCodec()
	{
		return codec;
	}

	public void setCodec( String codec )
	{
		this.codec = codec;
	}

	public String getCodecPrvData()
	{
		return codecPrvData;
	}

	public void setCodecPrvData( String codecPrvData )
	{
		this.codecPrvData = codecPrvData;
	}

	public String getProfile()
	{
		return profile;
	}

	public void setProfile( String profile )
	{
		this.profile = profile;
	}

	public int getResolutionHint()
	{
		return resolutionHint;
	}

	public void setResolutionHint( int resolutionHint )
	{
		this.resolutionHint = resolutionHint;
	}

	public double getEstBPS()
	{
		return estBPS;
	}

	public void setEstBPS( double estBPS )
	{
		this.estBPS = estBPS;
	}

	public int getWidth()
	{
		return width;
	}

	public void setWidth( int width )
	{
		this.width = width;
	}

	public int getHeight()
	{
		return height;
	}

	public void setHeight( int height )
	{
		this.height = height;
	}

	public double getFps()
	{
		return fps;
	}

	public void setFps( double fps )
	{
		this.fps = fps;
	}
}
