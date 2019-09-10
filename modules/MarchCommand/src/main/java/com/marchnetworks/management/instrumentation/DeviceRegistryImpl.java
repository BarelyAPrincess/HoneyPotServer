package com.marchnetworks.management.instrumentation;

import com.marchnetworks.command.api.metrics.MetricsCoreService;
import com.marchnetworks.command.common.device.data.ConnectState;
import com.marchnetworks.command.common.device.data.DeviceView;
import com.marchnetworks.command.common.topology.data.DeviceResource;
import com.marchnetworks.common.diagnostics.metrics.MetricsHelper;
import com.marchnetworks.common.diagnostics.metrics.MetricsTypes;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.utils.DateUtils;
import com.marchnetworks.management.instrumentation.dao.ChannelDAO;
import com.marchnetworks.management.instrumentation.dao.DeviceDAO;
import com.marchnetworks.management.instrumentation.events.DeviceConnectionStateChangeEvent;
import com.marchnetworks.management.instrumentation.model.Channel;
import com.marchnetworks.management.instrumentation.model.ChannelMBean;
import com.marchnetworks.management.instrumentation.model.CompositeDevice;
import com.marchnetworks.management.instrumentation.model.CompositeDeviceMBean;
import com.marchnetworks.management.instrumentation.model.Device;
import com.marchnetworks.management.instrumentation.model.DeviceMBean;
import com.marchnetworks.management.topology.ResourceTopologyServiceIF;
import com.marchnetworks.server.event.EventRegistry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceRegistryImpl implements DeviceRegistry
{
	private static final Logger LOG = LoggerFactory.getLogger( DeviceRegistryImpl.class );

	protected DeviceDAO deviceDAO;
	protected ChannelDAO channelDAO;
	protected EventRegistry eventRegistry;
	protected Map<String, ConnectState> m_DeviceConnectState = new ConcurrentHashMap();

	public DeviceMBean getDevice( String deviceId )
	{
		Device device = deviceDAO.findById( deviceId );

		return device;
	}

	public Collection<DeviceMBean> getAllRootDevices()
	{
		List<CompositeDevice> rds = deviceDAO.findAllRegisteredAndReplacingDevices();
		Collection<DeviceMBean> Result = new ArrayList( rds.size() );
		for ( CompositeDevice cd : rds )
		{
			Result.add( cd );
		}
		return Result;
	}

	public DeviceMBean getDeviceEagerDetached( String deviceId )
	{
		Device device = deviceDAO.findByIdEagerDetached( deviceId );

		return device;
	}

	public Set<String> findAllStationIds()
	{
		Set<String> stationIdSet = deviceDAO.findAllStationIds();
		return stationIdSet;
	}

	public <T extends CompositeDeviceMBean> T getDeviceByAddress( String deviceAddress )
	{
		return ( T ) deviceDAO.findByAddress( deviceAddress );
	}

	public <T extends CompositeDeviceMBean> T getDeviceByStationId( String stationId )
	{
		return ( T ) deviceDAO.findByStationId( stationId );
	}

	public DeviceMBean getDeviceByTime( long timeCreated )
	{
		return deviceDAO.findByTimeCreated( timeCreated );
	}

	public void removeDevice( String deviceId )
	{
		Device device = deviceDAO.findById( deviceId );

		if ( device != null )
		{
			deviceDAO.delete( device );
		}
		else
		{
			LOG.info( "Device {} not found in DB store. Already removed by other task or wrong deviceId.", deviceId );
		}
	}

	public void removeDeviceChannels( String deviceId )
	{
		Device device = deviceDAO.findById( deviceId );
		if ( device != null )
		{
			device.getChannels().clear();
			LOG.info( "Removing Channels from Device {}.", deviceId );
		}
	}

	public ChannelMBean getChannel( Long id )
	{
		Channel channel = ( Channel ) channelDAO.findById( id );

		return channel;
	}

	public DeviceMBean getDeviceByChannel( String rootDeviceId, String channelId )
	{
		DeviceMBean channelOwner = null;

		if ( ( channelId == null ) || ( rootDeviceId == null ) )
		{
			return null;
		}

		List<Channel> channelList = channelDAO.findByChannelId( channelId );
		for ( Channel channel : channelList )
		{
			if ( ( channel.getDevice() instanceof CompositeDevice ) )
			{
				if ( channel.getDevice().getDeviceId().equals( rootDeviceId ) )
				{
					channelOwner = channel.getDevice();
				}
			}
			else
			{
				Device childDevice = channel.getDevice();
				if ( childDevice.getParentDevice() == null )
				{
					LOG.warn( "Channel {} is associated to an orphan child device {}.", channelId, childDevice.getDeviceId() );
					return null;
				}

				if ( childDevice.getParentDevice().getDeviceId().equals( rootDeviceId ) )
				{
					channelOwner = childDevice;
				}
			}

			if ( channelOwner != null )
			{
				break;
			}
		}

		return channelOwner;
	}

	public ConnectState getConnectState( String deviceId )
	{
		ConnectState cs = ( ConnectState ) m_DeviceConnectState.get( deviceId );
		if ( cs == null )
		{
			return ConnectState.OFFLINE;
		}
		return cs;
	}

	public void putConnectState( String deviceId, ConnectState connectState )
	{
		m_DeviceConnectState.put( deviceId, connectState );
	}

	public void updateConnectState( String deviceId, ConnectState connectState )
	{
		if ( m_DeviceConnectState.get( deviceId ) != connectState )
		{
			LOG.info( "DeviceId({}) ConnectState= {}", deviceId, connectState );
			m_DeviceConnectState.put( deviceId, connectState );

			DeviceResource device = getTopologyService().getDeviceResourceByDeviceId( deviceId );
			device.getDeviceView().setConnectState( connectState );

			MetricsHelper.metrics.addBucketCounter( MetricsTypes.DEVICE_CONNECTION.getName(), connectState.toString() );

			DeviceConnectionStateChangeEvent dcs = new DeviceConnectionStateChangeEvent( deviceId, connectState );
			eventRegistry.send( dcs );
		}
	}

	public void updateLastConnectionTime( String deviceId, Calendar time )
	{
		if ( time != null )
		{

			DeviceResource device = getTopologyService().getDeviceResourceByDeviceId( deviceId );
			if ( device == null )
			{
				LOG.warn( "Update: Couldn't find device for deviceId=" + deviceId + ". Aborting" );
				return;
			}

			if ( !device.isRootDevice() )
			{
				LOG.warn( "Only update ConnectState of Root Devices" );
				return;
			}

			Long lct = Long.valueOf( device.getDeviceView().getLastCommunicationTime() );
			boolean differentTime = Math.abs( time.getTimeInMillis() - lct.longValue() ) >= 15000L;
			if ( differentTime )
			{
				if ( LOG.isDebugEnabled() )
				{
					LOG.debug( " --DeviceId=" + device.getDeviceId() + " LAST_COMM_TIME: " + DateUtils.calendar2String( time ) );
				}

				deviceDAO.updateLastConnectionTime( deviceId, time );

				device.getDeviceView().setLastCommunicationTime( time.getTimeInMillis() );
			}
		}
	}

	public void updateDeviceCapabilities( String deviceId, List<String> capabilities )
	{
		DeviceResource device = getTopologyService().getDeviceResourceByDeviceId( deviceId );

		if ( device == null )
		{
			LOG.warn( "Unable to update device capabilities: Couldn't find device for deviceId=" + deviceId + "" );
			return;
		}

		device.getDeviceView().setCapabilities( capabilities );
		deviceDAO.updateDeviceCapabilities( deviceId, capabilities );
	}

	public void setDeviceDAO( DeviceDAO deviceDAO )
	{
		this.deviceDAO = deviceDAO;
	}

	public void setEventRegistry( EventRegistry eventRegistry )
	{
		this.eventRegistry = eventRegistry;
	}

	public void setChannelDAO( ChannelDAO channelDAO )
	{
		this.channelDAO = channelDAO;
	}

	private ResourceTopologyServiceIF getTopologyService()
	{
		return ( ResourceTopologyServiceIF ) ApplicationContextSupport.getBean( "resourceTopologyService_internal" );
	}
}

