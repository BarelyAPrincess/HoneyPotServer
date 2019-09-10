package com.marchnetworks.com.util;

import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.topology.data.AlarmSourceResource;
import com.marchnetworks.command.common.topology.data.AudioOutputResource;
import com.marchnetworks.command.common.topology.data.ChannelResource;
import com.marchnetworks.command.common.topology.data.DeviceResource;
import com.marchnetworks.command.common.topology.data.Resource;
import com.marchnetworks.command.common.topology.data.SwitchResource;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.management.topology.ResourceTopologyServiceIF;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class AuditLogDetailsHelper
{
	private static Map<String, Long> deviceIdsMap = new HashMap<>();

	private static final Logger LOG = LoggerFactory.getLogger( AuditLogDetailsHelper.class );

	public static Long findDeviceResourceId( String deviceId )
	{
		if ( CommonAppUtils.isNullOrEmptyString( deviceId ) )
		{
			return null;
		}

		Long id = null;
		DeviceResource deviceResource = getDeviceResource( deviceId );
		if ( deviceResource != null )
		{
			id = deviceResource.getId();
		}
		else
		{
			LOG.warn( "DeviceResource {} not found.", deviceId );
		}
		return id;
	}

	public static Long findChannelResourceId( String deviceId, String channelId )
	{
		if ( ( CommonAppUtils.isNullOrEmptyString( deviceId ) ) || ( CommonAppUtils.isNullOrEmptyString( channelId ) ) )
		{
			return null;
		}

		DeviceResource deviceResource = getDeviceResource( deviceId );
		if ( deviceResource != null )
		{
			for ( Resource childResource : deviceResource.createResourceList() )
			{
				if ( ( childResource instanceof ChannelResource ) )
				{
					ChannelResource channel = ( ChannelResource ) childResource;
					if ( channel.getChannelId().equals( channelId ) )
					{
						return channel.getId();
					}
				}
			}
		}
		else
		{
			LOG.warn( "DeviceResource {} not found.", deviceId );
		}
		return null;
	}

	public static Long findAlarmSourceResourceId( String deviceId, String deviceAlarmSourceId )
	{
		if ( ( CommonAppUtils.isNullOrEmptyString( deviceId ) ) || ( CommonAppUtils.isNullOrEmptyString( deviceAlarmSourceId ) ) )
		{
			return null;
		}

		DeviceResource deviceResource = getDeviceResource( deviceId );
		if ( deviceResource != null )
		{
			for ( Resource childResource : deviceResource.createResourceList() )
			{
				if ( ( childResource instanceof AlarmSourceResource ) )
				{
					AlarmSourceResource alarmSource = ( AlarmSourceResource ) childResource;
					if ( alarmSource.getAlarmSource().getDeviceAlarmSourceId().equals( deviceAlarmSourceId ) )
					{
						return alarmSource.getId();
					}
				}
			}
		}
		else
		{
			LOG.warn( "DeviceResource {} not found.", deviceId );
		}
		return null;
	}

	public static Long findOutputResourceId( String deviceId, String deviceOutputId )
	{
		if ( ( CommonAppUtils.isNullOrEmptyString( deviceId ) ) || ( CommonAppUtils.isNullOrEmptyString( deviceOutputId ) ) )
		{
			return null;
		}

		DeviceResource deviceResource = getDeviceResource( deviceId );
		if ( deviceResource != null )
		{

			for ( Resource childResource : deviceResource.createResourceList() )
			{
				if ( ( childResource instanceof SwitchResource ) )
				{
					SwitchResource switchRes = ( SwitchResource ) childResource;
					if ( switchRes.getSwitchView().getSwitchId().equals( deviceOutputId ) )
					{
						return switchRes.getId();
					}
				}
				if ( ( childResource instanceof AudioOutputResource ) )
				{
					AudioOutputResource audioRes = ( AudioOutputResource ) childResource;
					if ( audioRes.getAudioOutputView().getAudioOutputId().equals( deviceOutputId ) )
					{
						return audioRes.getId();
					}
				}
			}
		}
		else
		{
			LOG.warn( "DeviceResource {} not found.", deviceId );
		}
		return null;
	}

	public static String findResourcePath( Long resourceId )
	{
		return getTopologyService().getResourcePathString( resourceId );
	}

	private static DeviceResource getDeviceResource( String deviceId )
	{
		Long deviceResourceId = ( Long ) deviceIdsMap.get( deviceId );
		if ( deviceResourceId == null )
		{
			DeviceResource tempDeviceResource = getTopologyService().getDeviceResourceByDeviceId( deviceId );
			if ( tempDeviceResource != null )
			{
				deviceResourceId = tempDeviceResource.getId();
				deviceIdsMap.put( deviceId, deviceResourceId );
			}
		}

		if ( deviceResourceId == null )
		{
			LOG.warn( "Cannot find deviceResourceId for device {}.", deviceId );
			return null;
		}
		return getTopologyService().getDeviceResource( deviceResourceId );
	}

	private static ResourceTopologyServiceIF getTopologyService()
	{
		return ( ResourceTopologyServiceIF ) ApplicationContextSupport.getBean( "resourceTopologyServiceProxy_internal" );
	}
}
