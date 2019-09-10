package com.marchnetworks.management.instrumentation.monitor;

import com.marchnetworks.command.common.device.data.ChannelState;
import com.marchnetworks.common.transport.datamodel.DeviceException;
import com.marchnetworks.management.instrumentation.DeviceAdaptorFactory;
import com.marchnetworks.management.instrumentation.DeviceRegistry;
import com.marchnetworks.management.instrumentation.RemoteDeviceOperations;
import com.marchnetworks.management.instrumentation.events.AbstractDeviceEvent;
import com.marchnetworks.management.instrumentation.events.ChannelConnectionStateEvent;
import com.marchnetworks.management.instrumentation.events.DeviceChannelChangedEvent;
import com.marchnetworks.management.instrumentation.model.CompositeDevice;
import com.marchnetworks.management.instrumentation.model.Device;
import com.marchnetworks.management.topology.ResourceTopologyServiceIF;
import com.marchnetworks.server.communications.transport.datamodel.ChannelDetails;
import com.marchnetworks.server.communications.transport.datamodel.DeviceDetails;
import com.marchnetworks.server.event.EventRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceSoftwareVersionMonitor extends AbstractDeviceMonitor
{
	private static final Logger LOG = LoggerFactory.getLogger( DeviceSoftwareVersionMonitor.class );
	private ResourceTopologyServiceIF resourceTopologyService;

	public boolean doInterceptDeviceEvent( AbstractDeviceEvent event )
	{
		if ( ( event instanceof ChannelConnectionStateEvent ) )
		{
			ChannelConnectionStateEvent connectionStateEvent = ( ChannelConnectionStateEvent ) event;

			if ( connectionStateEvent.getConnectionState().equals( ChannelState.ONLINE ) )
			{
				boolean send = false;
				LOG.debug( "Handling {} event for device {}.", new Object[] {connectionStateEvent, connectionStateEvent.getDeviceId()} );

				Device device = ( Device ) getDeviceRegistry().getDevice( connectionStateEvent.getDeviceId() );
				if ( ( device != null ) && ( device.getParentDevice() != null ) )
				{
					try
					{
						RemoteDeviceOperations adaptor = getDeviceAdaptorFactory().getDeviceAdaptor( resourceTopologyService.getDeviceResourceByDeviceId( device.getParentDevice().getDeviceId() ) );
						ChannelDetails channelDetails = adaptor.retrieveChannelDetails( connectionStateEvent.getChannelId() );
						if ( channelDetails.getIpDevice() != null )
						{
							DeviceDetails desc = channelDetails.getIpDevice();
							if ( desc.getModelId() > -1 )
							{
								String version = desc.getSwVersion();

								if ( ( version != null ) && ( !version.equals( device.getSoftwareVersion() ) ) )
								{
									LOG.debug( "Device {} software version changed from {} to {}.", new String[] {device.getDeviceId(), device.getSoftwareVersion(), version} );

									device.setSoftwareVersion( version );
									send = true;
								}

								Integer model = Integer.valueOf( desc.getModelId() );
								if ( ( model != null ) && ( !model.toString().equals( device.getModel() ) ) && ( !"-1".equals( model.toString() ) ) )
								{
									LOG.debug( "Device {} model changed from {} to {}.", new String[] {device.getDeviceId(), device.getModel(), String.valueOf( desc.getModelId() )} );
									device.setModel( String.valueOf( desc.getModelId() ) );
									if ( desc.getModelName() != null )
										device.setModelName( desc.getModelName() );
									send = true;
								}

								Integer family = Integer.valueOf( desc.getFamilyId() );
								if ( ( family != null ) && ( !family.toString().equals( device.getFamily() ) ) && ( !"-1".equals( family.toString() ) ) )
								{
									device.setFamily( String.valueOf( family ) );
									device.setFamilyName( desc.getFamilyName() );
									send = true;
								}

								String serial = desc.getSerial();
								if ( ( serial != null ) && ( !serial.equals( device.getSerial() ) ) )
								{
									LOG.debug( "Device {} serial changed from {} to {}.", new String[] {device.getDeviceId(), device.getSerial(), serial} );
									device.setSerial( serial );
									send = true;
								}
							}

							if ( send )
							{
								getEventRegistry().sendEventAfterTransactionCommits( new DeviceChannelChangedEvent( connectionStateEvent.getDeviceId(), connectionStateEvent.getChannelId() ) );
							}
						}
					}
					catch ( DeviceException ex )
					{
						LOG.warn( "Error getting device info.", ex );
					}
				}
			}
		}
		return true;
	}

	public void setResourceTopologyService( ResourceTopologyServiceIF resourceTopologyService )
	{
		this.resourceTopologyService = resourceTopologyService;
	}
}

