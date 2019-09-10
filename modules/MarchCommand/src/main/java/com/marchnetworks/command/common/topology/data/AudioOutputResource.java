package com.marchnetworks.command.common.topology.data;

import com.marchnetworks.command.common.device.data.AudioOutputView;

import javax.xml.bind.annotation.XmlTransient;

public class AudioOutputResource extends Resource
{
	private transient Long audioOutputId;
	private AudioOutputView audioOutputView;

	public void update( Resource updatedResource )
	{
		if ( ( updatedResource instanceof AudioOutputResource ) )
		{
			super.update( updatedResource );
			AudioOutputResource updatedAudioOutputResource = ( AudioOutputResource ) updatedResource;
			audioOutputId = updatedAudioOutputResource.getAudioOutputId();
			audioOutputView = updatedAudioOutputResource.getAudioOutputView();
		}
	}

	public LinkType getLinkType()
	{
		return LinkType.AUDIO_OUTPUT;
	}

	@XmlTransient
	public Long getAudioOutputId()
	{
		return audioOutputId;
	}

	public void setAudioOutputId( Long audioOutputId )
	{
		this.audioOutputId = audioOutputId;
	}

	public AudioOutputView getAudioOutputView()
	{
		return audioOutputView;
	}

	public void setAudioOutputView( AudioOutputView audioOutputView )
	{
		this.audioOutputView = audioOutputView;
	}
}
