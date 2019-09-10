package com.marchnetworks.management.instrumentation;

import com.marchnetworks.command.common.device.data.AudioOutputState;
import com.marchnetworks.command.common.device.data.AudioOutputType;
import com.marchnetworks.command.common.topology.ResourceAssociationType;
import com.marchnetworks.command.common.topology.data.AudioOutputResource;
import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.command.common.transport.data.Pair;
import com.marchnetworks.common.transport.datamodel.DeviceException;
import com.marchnetworks.management.instrumentation.dao.DeviceOutputDAO;
import com.marchnetworks.management.instrumentation.model.AudioOutputEntity;
import com.marchnetworks.server.communications.transport.datamodel.AudioOutput;
import com.marchnetworks.server.communications.transport.datamodel.DeviceOutput;

import java.util.List;

public class AudioOutputServiceImpl extends DeviceOutputServiceImpl<AudioOutputEntity> implements AudioOutputService
{
	protected List<? extends DeviceOutput> getOutputs( Long deviceId ) throws DeviceException
	{
		RemoteCompositeDeviceOperations adaptor = getAdaptor( deviceId );
		return adaptor.getAudioOutputs();
	}

	protected Resource createResource( AudioOutputEntity outputEntity )
	{
		AudioOutputResource result = new AudioOutputResource();
		result.setAudioOutputId( outputEntity.getId() );
		return result;
	}

	protected AudioOutputEntity createEntity( Long deviceId, DeviceOutput deviceOutput )
	{
		if ( ( deviceOutput instanceof AudioOutput ) )
		{
			AudioOutput audioOutput = ( AudioOutput ) deviceOutput;
			AudioOutputEntity audioOutputEntity = new AudioOutputEntity();
			audioOutputEntity.setOutputId( audioOutput.getId() );
			audioOutputEntity.setDeviceId( deviceId );
			audioOutputEntity.setType( AudioOutputType.fromValue( audioOutput.getType() ) );
			audioOutputEntity.setOutputDeviceId( audioOutput.getAudioOutputDeviceId() );
			audioOutputEntity.setOutputDeviceAddress( audioOutput.getAudioOutputDeviceAddress() );
			audioOutputEntity.setName( audioOutput.getName() );
			audioOutputEntity.setState( AudioOutputState.fromValue( audioOutput.getState() ) );
			audioOutputEntity.setCodec( audioOutput.getCodec() );
			audioOutputEntity.setChannels( audioOutput.getChannels() );
			audioOutputEntity.setSamplesPerSecond( audioOutput.getSamplesPerSecond() );
			audioOutputEntity.setBitsPerSample( audioOutput.getBitsPerSample() );
			audioOutputEntity.setInfo( audioOutput.getInfo() );
			return audioOutputEntity;
		}
		return null;
	}

	protected String getResourceAssociationType()
	{
		return ResourceAssociationType.AUDIO_OUTPUT.name();
	}

	protected void updateState( AudioOutputEntity outputEntity, String state )
	{
		outputEntity.setState( AudioOutputState.fromValue( state ) );
	}

	protected boolean isDisabled( String state )
	{
		return AudioOutputState.DISABLED.getValue().equals( state );
	}

	public List<AudioOutputEntity> getAll()
	{
		List<AudioOutputEntity> deviceOutputs = outputDAO.findAll();
		return deviceOutputs;
	}

	public AudioOutputEntity getById( Long id )
	{
		AudioOutputEntity result = ( AudioOutputEntity ) outputDAO.findById( id );
		return result;
	}

	public void delete( Long id )
	{
		AudioOutputEntity audioOutput = ( AudioOutputEntity ) outputDAO.findById( id );
		removeDeviceOutput( audioOutput );
	}

	public void deleteAll()
	{
		List<AudioOutputEntity> deviceOutputs = outputDAO.findAll();
		for ( AudioOutputEntity audioOutput : deviceOutputs )
		{
			removeDeviceOutput( audioOutput );
		}
	}

	public void createDeviceOutput( Long deviceId, String deviceOutputId, String type, String name, String state, String user )
	{
		AudioOutput audioOutput = new AudioOutput();
		audioOutput.setId( deviceOutputId );
		audioOutput.setType( type );
		audioOutput.setAudioOutputDeviceId( "1" );
		audioOutput.setAudioOutputDeviceAddress( "USB" );
		audioOutput.setName( name );
		audioOutput.setState( state );
		audioOutput.setCodec( "gsm06.10" );
		audioOutput.setChannels( 3 );
		audioOutput.setSamplesPerSecond( 3 );
		audioOutput.setBitsPerSample( 3000 );
		Pair[] info = new Pair[1];
		Pair p = new Pair();
		p.setName( "user" );
		p.setValue( user );
		info[0] = p;
		audioOutput.setInfo( info );

		createDeviceOutput( deviceId, audioOutput );
	}
}

