package com.marchnetworks.command.common.device.data;

public class DataEncoderView extends EncoderView
{
	public static final DataEncoderView[] NO_ENCODERS = new DataEncoderView[0];
	private String codec;

	public String getCodec()
	{
		return codec;
	}

	public void setCodec( String codec )
	{
		this.codec = codec;
	}
}
