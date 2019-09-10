package com.marchnetworks.management.instrumentation.model;

import com.marchnetworks.command.common.device.data.AudioOutputState;
import com.marchnetworks.command.common.device.data.AudioOutputType;
import com.marchnetworks.command.common.device.data.AudioOutputView;
import com.marchnetworks.server.communications.transport.datamodel.AudioOutput;
import com.marchnetworks.server.communications.transport.datamodel.DeviceOutput;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Entity
@DiscriminatorValue( "AudioOutput" )
public class AudioOutputEntity extends DeviceOutputEntity
{
	@Column( name = "AUDIO_OUTPUT_TYPE" )
	@Enumerated( EnumType.STRING )
	private AudioOutputType type;
	@Column( name = "AUDIO_OUTPUT_CODEC" )
	private String codec;
	@Column( name = "AUDIO_OUTPUT_CHANNELS" )
	private int channels;
	@Column( name = "AUDIO_OUTPUT_SAMPLES_PER_SECOND" )
	private int samplesPerSecond;
	@Column( name = "AUDIO_OUTPUT_BITS_PER_SAMPLE" )
	private int bitsPerSample;
	@Column( name = "AUDIO_OUTPUT_STATE" )
	@Enumerated( EnumType.STRING )
	private AudioOutputState state;

	public AudioOutputView toDataObject()
	{
		AudioOutputView result = new AudioOutputView( outputId, name, type, outputDeviceId, outputDeviceAddress, codec, channels, samplesPerSecond, bitsPerSample, state, getInfoAsPairs() );

		return result;
	}

	public boolean readFromTransportObject( DeviceOutput deviceOutput )
	{
		boolean updated = false;
		if ( ( deviceOutput != null ) && ( ( deviceOutput instanceof AudioOutput ) ) )
		{
			AudioOutput audioOutput = ( AudioOutput ) deviceOutput;

			AudioOutputType type = AudioOutputType.fromValue( audioOutput.getType() );
			String audioOutputDeviceId = audioOutput.getAudioOutputDeviceId();
			String audioOutputDeviceAddress = audioOutput.getAudioOutputDeviceAddress();
			String name = audioOutput.getName();
			String codec = audioOutput.getCodec();
			int channels = audioOutput.getChannels();
			int samplesPerSecond = audioOutput.getSamplesPerSecond();
			int bitsPerSample = audioOutput.getBitsPerSample();

			if ( ( type != this.type ) || ( !audioOutputDeviceId.equals( outputDeviceId ) ) || ( !audioOutputDeviceAddress.equals( outputDeviceAddress ) ) || ( !name.equals( this.name ) ) || ( !codec.equals( this.codec ) ) || ( channels != this.channels ) || ( samplesPerSecond != this.samplesPerSecond ) || ( bitsPerSample != this.bitsPerSample ) )
			{

				this.type = type;
				outputDeviceId = audioOutputDeviceId;
				outputDeviceAddress = audioOutputDeviceAddress;
				this.name = name;
				this.codec = codec;
				this.channels = channels;
				this.samplesPerSecond = samplesPerSecond;
				this.bitsPerSample = bitsPerSample;
				updated = true;
			}
		}
		return updated;
	}

	public Class<AudioOutputView> getDataObjectClass()
	{
		return AudioOutputView.class;
	}

	public AudioOutputType getType()
	{
		return type;
	}

	public void setType( AudioOutputType type )
	{
		this.type = type;
	}

	public String getCodec()
	{
		return codec;
	}

	public void setCodec( String codec )
	{
		this.codec = codec;
	}

	public int getChannels()
	{
		return channels;
	}

	public void setChannels( int channels )
	{
		this.channels = channels;
	}

	public int getSamplesPerSecond()
	{
		return samplesPerSecond;
	}

	public void setSamplesPerSecond( int samplesPerSecond )
	{
		this.samplesPerSecond = samplesPerSecond;
	}

	public int getBitsPerSample()
	{
		return bitsPerSample;
	}

	public void setBitsPerSample( int bitsPerSample )
	{
		this.bitsPerSample = bitsPerSample;
	}

	public AudioOutputState getState()
	{
		return state;
	}

	public void setState( AudioOutputState state )
	{
		this.state = state;
	}
}

