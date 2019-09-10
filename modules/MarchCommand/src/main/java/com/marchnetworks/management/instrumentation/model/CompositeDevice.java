package com.marchnetworks.management.instrumentation.model;

import com.marchnetworks.command.common.transport.data.LocalZone;
import com.marchnetworks.common.serialization.CoreJsonSerializer;
import com.marchnetworks.management.instrumentation.data.DeviceNetworkInfoType;
import com.marchnetworks.server.communications.transport.datamodel.AddressZones;
import com.marchnetworks.server.communications.transport.datamodel.DeviceDetails;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;

@javax.persistence.Entity
@javax.persistence.DiscriminatorValue( "CompositeDevice" )
public class CompositeDevice extends Device implements CompositeDeviceMBean
{
	private static final long serialVersionUID = -5564699437641704513L;
	@Column( name = "DEVICE_EVENT_SUBSCRIPTION_ID" )
	private String deviceEventSubscriptionId;
	@Column( name = "DEVICE_EVENT_SEQUENCE_ID" )
	private Long deviceEventSequenceId;
	@Column( name = "DEVICE_EVENT_SUBSCRIPTION_PREFIXES", length = 4000 )
	private String deviceEventSubscriptionPrefixes;
	@Column( name = "TIMEZONE_INFO", length = 450 )
	private String timeZoneInfo;
	@Column( name = "TIME_DELTA" )
	private Long timeDelta;

	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append( "CompositeDevice [deviceEventSubscriptionId=" ).append( deviceEventSubscriptionId ).append( ", deviceEventSequenceId=" ).append( deviceEventSequenceId ).append( ", deviceEventSubscriptionPrefixes=" ).append( deviceEventSubscriptionPrefixes ).append( ", timeZoneInfo=" ).append( timeZoneInfo ).append( ", timeDelta=" ).append( timeDelta ).append( ", stationId=" ).append( stationId ).append( "]" );

		return builder.toString();
	}

	@javax.persistence.OneToMany( cascade = {javax.persistence.CascadeType.REMOVE, javax.persistence.CascadeType.DETACH}, orphanRemoval = true )
	@javax.persistence.JoinColumn( name = "PARENT_DEVICE", nullable = true )
	private Map<String, Device> childDevices = new HashMap();

	@Column( name = "STATION_ID" )
	private String stationId;

	@Column( name = "GLOBAL_SETTINGS", length = 4000 )
	private String globalSettingsString;

	@Column( name = "CAPABILITIES", length = 1000, nullable = true )
	private String capabilities;

	@Column( name = "NOTIFY_INTERVAL" )
	private Integer notifyInterval;

	public CompositeDevice()
	{
		super( CompositeDeviceMBean.class, false );
	}

	public void consolidateDevices( DeviceDetails deviceDetails )
	{
		Map<String, DeviceDetails> hostAndMacMap = new HashMap();

		List<DeviceDetails> consolidatedChildDevices = new java.util.ArrayList();
		for ( DeviceDetails childDevice : deviceDetails.getChildDevices() )
		{
			String hostAndMac = childDevice.buildHostAndMacString();
			DeviceDetails existing = ( DeviceDetails ) hostAndMacMap.get( hostAndMac );

			if ( existing == null )
			{
				consolidatedChildDevices.add( childDevice );
				hostAndMacMap.put( hostAndMac, childDevice );
			}
			else
			{
				for ( String name : childDevice.getNetworkNames() )
				{
					existing.addNetworkName( name );
				}

				if ( ( childDevice.getDeviceChannels() != null ) && ( !childDevice.getDeviceChannels().isEmpty() ) )
				{
					existing.getDeviceChannels().addAll( childDevice.getDeviceChannels() );
				}
			}
		}
		deviceDetails.setChildDevices( consolidatedChildDevices );
	}

	public Map<String, DeviceMBean> getChildDeviceMBeans()
	{
		Map<String, DeviceMBean> ret = new HashMap();
		ret.putAll( childDevices );
		return ret;
	}

	public String getAddress()
	{
		return address;
	}

	public String convertToDeviceIdFromChannelId( String channelId )
	{
		String deviceId = super.convertToDeviceIdFromChannelId( channelId );
		Device childDevice;
		Iterator i$;

		if ( ( deviceId == null ) && ( channelId != null ) )
		{
			for ( i$ = getChildDevices().values().iterator(); i$.hasNext(); )
			{
				childDevice = ( Device ) i$.next();
				if ( childDevice.getChannels() != null )
				{
					for ( ChannelMBean channel : childDevice.getChannels().values() )
					{
						if ( channel.getChannelId().equals( channelId ) )
						{
							deviceId = childDevice.getDeviceId();
							break;
						}
					}
				}
			}
		}

		return deviceId;
	}

	public Channel getChannelFromDevice( String channelId )
	{
		Channel deviceChannel = super.getChannelFromDevice( channelId );

		if ( deviceChannel == null )
		{
			for ( Device childDevice : getChildDevices().values() )
			{
				deviceChannel = childDevice.getChannelFromDevice( channelId );
				if ( deviceChannel != null )
				{
					break;
				}
			}
		}
		return deviceChannel;
	}

	public void setDeviceInfoFromTransport( DeviceDetails deviceDetails )
	{
		super.setDeviceInfoFromTransport( deviceDetails );
		if ( isValidTransportObject( deviceDetails ) )
		{
			setTimeZoneInfo( deviceDetails.getTimeZoneInfo() );
			calculateTimeDelta( deviceDetails.getDeviceTime() );
		}
	}

	protected void setDeviceNetworkInfoFromTransport( DeviceDetails deviceDetails )
	{
		super.setDeviceNetworkInfoFromTransport( deviceDetails );
		if ( ( deviceDetails.getAddressZones() != null ) && ( deviceDetails.getAddressZones().getLocalZone() != null ) )
		{
			DeviceNetworkInfo deviceNetInfo = new DeviceNetworkInfo();
			deviceNetInfo.setNetworkInfoType( DeviceNetworkInfoType.LOCAL_ADDRESS_ZONE );
			deviceNetInfo.setValue( CoreJsonSerializer.toJson( deviceDetails.getAddressZones().getLocalZone() ) );
			deviceNetInfo.setDevice( this );
			getDeviceNetworkInfos().add( deviceNetInfo );
		}
		if ( ( deviceDetails.getAddressZones() != null ) && ( deviceDetails.getAddressZones().getUserAddressZones() != null ) )
		{
			DeviceNetworkInfo deviceNetInfo = new DeviceNetworkInfo();
			deviceNetInfo.setNetworkInfoType( DeviceNetworkInfoType.USER_ADDRESS_ZONE );
			deviceNetInfo.setValue( CoreJsonSerializer.toJson( deviceDetails.getAddressZones().getUserAddressZones() ) );
			deviceNetInfo.setDevice( this );
			getDeviceNetworkInfos().add( deviceNetInfo );
		}
	}

	public Map<String, Device> getChildDevices()
	{
		return childDevices;
	}

	public void setChildDevices( Map<String, Device> childDevices )
	{
		this.childDevices = childDevices;
	}

	public Long getDeviceEventSequenceId()
	{
		if ( deviceEventSequenceId == null )
		{
			initializeDeviceEventSequenceId();
		}
		return deviceEventSequenceId;
	}

	public void setDeviceEventSequenceId( Long deviceEventSequenceId )
	{
		this.deviceEventSequenceId = deviceEventSequenceId;
	}

	public void initializeDeviceEventSequenceId()
	{
		if ( com.marchnetworks.command.api.rest.DeviceManagementConstants.isR5OrR4Device( manufacturer, family ) )
		{
			deviceEventSequenceId = Long.valueOf( ( System.currentTimeMillis() + timeDelta.longValue() ) * 1000L );
		}
		else
		{
			deviceEventSequenceId = com.marchnetworks.command.api.rest.DeviceManagementConstants.DEVICE_EVENT_START_SEQUENCE_ID;
		}
	}

	public String getDeviceEventSubscriptionId()
	{
		return deviceEventSubscriptionId;
	}

	public void setDeviceEventSubscriptionId( String deviceEventSubscriptionId )
	{
		this.deviceEventSubscriptionId = deviceEventSubscriptionId;
	}

	public String getDeviceEventSubscriptionPrefixes()
	{
		return deviceEventSubscriptionPrefixes;
	}

	@javax.persistence.Transient
	public String[] getEventSubscriptionPrefixes()
	{
		return ( String[] ) CoreJsonSerializer.fromJson( deviceEventSubscriptionPrefixes, String[].class );
	}

	protected void setDeviceEventSubscriptionPrefixes( String deviceEventSubscriptionPrefixes )
	{
		this.deviceEventSubscriptionPrefixes = deviceEventSubscriptionPrefixes;
	}

	public void setEventSubscriptionPrefixes( String[] eventPrefixes )
	{
		deviceEventSubscriptionPrefixes = CoreJsonSerializer.toJson( eventPrefixes );
	}

	public String getTimeZoneInfo()
	{
		return timeZoneInfo;
	}

	public void setTimeZoneInfo( String timeZoneInfo )
	{
		this.timeZoneInfo = timeZoneInfo;
	}

	public void setTimeZoneInfo( com.marchnetworks.command.common.transport.data.TimeZoneInfo timeZoneInfo )
	{
		setTimeZoneInfo( CoreJsonSerializer.toJson( timeZoneInfo ) );
	}

	public Long getTimeDelta()
	{
		return timeDelta;
	}

	public void setTimeDelta( Long deviceTimeDelta )
	{
		timeDelta = deviceTimeDelta;
	}

	public void calculateTimeDelta( long deviceTime )
	{
		if ( deviceTime > 0L )
		{
			setTimeDelta( Long.valueOf( deviceTime - com.marchnetworks.common.utils.DateUtils.getCurrentUTCTimeInMillis() ) );
		}
	}

	public LocalZone getLocalZone()
	{
		DeviceNetworkInfo localZoneInfo = getDeviceNetworkInfoSetByType( DeviceNetworkInfoType.LOCAL_ADDRESS_ZONE );
		if ( localZoneInfo != null )
		{
			LocalZone localZone = ( LocalZone ) CoreJsonSerializer.fromJson( localZoneInfo.getValue(), LocalZone.class );
			return localZone;
		}
		return null;
	}

	public com.marchnetworks.command.common.transport.data.AddressZone[] getUserAddressZones()
	{
		DeviceNetworkInfo userZonesInfo = getDeviceNetworkInfoSetByType( DeviceNetworkInfoType.USER_ADDRESS_ZONE );
		if ( userZonesInfo != null )
		{
			return ( com.marchnetworks.command.common.transport.data.AddressZone[] ) CoreJsonSerializer.fromJson( userZonesInfo.getValue(), com.marchnetworks.command.common.transport.data.AddressZone[].class );
		}
		return null;
	}

	public String getAssignedDeviceId()
	{
		return getDeviceId() + "__" + getTimeCreated().getTimeInMillis();
	}

	public String getStationId()
	{
		return stationId;
	}

	public void setStationId( String stationId )
	{
		this.stationId = stationId;
	}

	protected String getGlobalSettingsString()
	{
		return globalSettingsString;
	}

	protected void setGlobalSettingsString( String globalSettingsString )
	{
		this.globalSettingsString = globalSettingsString;
	}

	public Map<String, String> getGlobalSettings()
	{
		String settingsAsJson = getGlobalSettingsString();
		if ( settingsAsJson == null )
			return null;

		return CoreJsonSerializer.collectionFromJson( settingsAsJson, new com.google.gson.reflect.TypeToken<HashMap<String, String>>()
		{
		} );
	}

	public void setGlobalSettings( Map<String, String> globalSettings )
	{
		if ( globalSettings == null )
		{
			setGlobalSettingsString( null );
		}

		setGlobalSettingsString( CoreJsonSerializer.toJson( globalSettings ) );
	}

	public List<String> getCapabilities()
	{
		if ( capabilities == null )
		{
			return null;
		}
		List<String> capabilityList = ( List ) CoreJsonSerializer.collectionFromJson( capabilities, new com.google.gson.reflect.TypeToken()
		{
		} );
		return capabilityList;
	}

	public void setCapabilities( List<String> capabilities )
	{
		if ( capabilities == null )
		{
			this.capabilities = null;
		}
		this.capabilities = CoreJsonSerializer.toJson( capabilities );
	}

	public Integer getNotifyInterval()
	{
		return notifyInterval;
	}

	public void setNotifyInterval( Integer notifyInterval )
	{
		this.notifyInterval = notifyInterval;
	}
}

