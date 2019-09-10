package com.marchnetworks.command.common.device.data;

public class AudioEncoderView extends EncoderView
{
	public static final AudioEncoderView[] NO_ENCODERS = new AudioEncoderView[0];
	private String codec;
	private String codecPrvData;
	private double estBPS;
	private int channels;
	private int sampBits;
	private int sampRate;

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

	public double getEstBPS()
	{
		return estBPS;
	}

	public void setEstBPS( double estBPS )
	{
		this.estBPS = estBPS;
	}

	public int getChannels()
	{
		return channels;
	}

	public void setChannels( int channels )
	{
		this.channels = channels;
	}

	public int getSampBits()
	{
		return sampBits;
	}

	public void setSampBits( int sampBits )
	{
		this.sampBits = sampBits;
	}

	public int getSampRate()
	{
		return sampRate;
	}

	public void setSampRate( int sampRate )
	{
		this.sampRate = sampRate;
	}
}
