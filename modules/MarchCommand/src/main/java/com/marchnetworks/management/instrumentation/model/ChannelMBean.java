package com.marchnetworks.management.instrumentation.model;

import com.marchnetworks.command.common.device.data.AudioEncoderView;
import com.marchnetworks.command.common.device.data.ChannelState;
import com.marchnetworks.command.common.device.data.DataEncoderView;
import com.marchnetworks.command.common.device.data.TextEncoderView;
import com.marchnetworks.command.common.device.data.VideoEncoderView;

public abstract interface ChannelMBean
{
	public abstract String getIdAsString();

	public abstract String getChannelId();

	public abstract String getName();

	public abstract ChannelState getChannelState();

	public abstract String getChannelStateString();

	public abstract String getPtzDomeIdentifier();

	public abstract String[] getAssocIds();

	public abstract VideoEncoderView[] getVideoEncoders();

	public abstract AudioEncoderView[] getAudioEncoders();

	public abstract TextEncoderView[] getTextEncoders();

	public abstract DataEncoderView[] getDataEncoders();
}

