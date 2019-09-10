package com.marchnetworks.command.common.device.data;

public class TextEncoderView extends EncoderView
{
	public static final TextEncoderView[] NO_ENCODERS = new TextEncoderView[0];
	private String protocolName;

	public String getProtocolName()
	{
		return protocolName;
	}

	public void setProtocolName( String protocolName )
	{
		this.protocolName = protocolName;
	}
}
