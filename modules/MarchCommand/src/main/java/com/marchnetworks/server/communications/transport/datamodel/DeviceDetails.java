package com.marchnetworks.server.communications.transport.datamodel;

import com.marchnetworks.command.common.transport.data.ConfigurationURL;
import com.marchnetworks.command.common.transport.data.TimeZoneInfo;

import java.util.ArrayList;
import java.util.List;

public class DeviceDetails
{
	protected String deviceId;
	protected String[] ipAddresses;
	protected String[] macAddresses;
	protected String[] networkNames;
	protected String manufacturerName;
	protected int manufacturerId;
	protected String familyName;
	protected int familyId;
	protected String modelName;
	protected int modelId;
	protected String id;
	protected String serial;
	protected String hwVersion;
	protected String swVersion;
	protected TimeZoneInfo timeZoneInfo;
	protected ConfigurationURL configurationUrl;
	protected AddressZones addressZones;
	protected String registrationId;
	protected long deviceTime;
	protected String subscriptionId;
	protected Integer channelsInUse;
	protected Integer maxChannels;
	protected String path;
	protected String connectState;
	protected String stationId;
	protected String patchList;
	protected List<DeviceDetails> childDevices = new ArrayList();
	protected List<ChannelDetails> deviceChannels = new ArrayList();

	public void addChannelDetailsForDevice( String channelId, String channelName, ChannelState channelState, String ptzDome, VideoDetails[] videoEncodersDetail, AudioDetails[] audioEncodersDetail )
	{
		if ( getDeviceId() != null )
		{
			ChannelDetails channel = new ChannelDetails();
			channel.setId( channelId );
			channel.setName( channelName );
			channel.setChannelState( channelState );
			channel.setPtzDomeId( ptzDome );
			channel.setVideo( videoEncodersDetail );
			channel.setAudio( audioEncodersDetail );

			getDeviceChannels().add( channel );
		}
	}

	public void addChannelDetails( ChannelDetails channelDetails )
	{
		deviceChannels.add( channelDetails );
	}

	public String buildHostAndMacString()
	{
		StringBuilder hostAndMac = new StringBuilder();
		String delimiter = "_";
		if ( getIPAddresses() != null )
		{
			for ( String address : getIPAddresses() )
			{
				hostAndMac.append( address );
				hostAndMac.append( delimiter );
			}
		}

		if ( getMACAddresses() != null )
		{
			for ( String macAddress : getMACAddresses() )
			{
				hostAndMac.append( macAddress );
				hostAndMac.append( delimiter );
			}
		}

		return hostAndMac.toString();
	}

	public boolean hasMacAddresses()
	{
		if ( ( macAddresses != null ) && ( macAddresses.length > 0 ) )
		{
			return true;
		}
		return false;
	}

	private boolean contains( String name )
	{
		for ( String networkName : networkNames )
		{
			if ( networkName.equals( name ) )
			{
				return true;
			}
		}

		return false;
	}

	public void addNetworkName( String name )
	{
		if ( ( name == null ) || ( name.isEmpty() ) )
		{
			return;
		}

		if ( ( networkNames != null ) && ( networkNames.length > 0 ) && ( !contains( name ) ) )
		{
			String[] newNetworkNames = new String[networkNames.length + 1];
			for ( int i = 0; i < networkNames.length; i++ )
			{
				if ( ( networkNames[i] == null ) || ( networkNames[i].isEmpty() ) || ( networkNames[i].equals( "N/A" ) ) )
				{
					networkNames[i] = name;
					return;
				}

				newNetworkNames[i] = networkNames[i];
			}
			newNetworkNames[networkNames.length] = name;
			networkNames = newNetworkNames;
		}

		if ( ( networkNames == null ) || ( networkNames.length == 0 ) )
		{
			networkNames = new String[1];
			networkNames[0] = name;
		}
	}

	public String[] getIPAddresses()
	{
		return ipAddresses;
	}

	public void setIPAddresses( String[] value )
	{
		ipAddresses = value;
	}

	public String[] getMACAddresses()
	{
		return macAddresses;
	}

	public void setMACAddresses( String[] value )
	{
		macAddresses = value;
	}

	public String[] getNetworkNames()
	{
		return networkNames;
	}

	public void setNetworkNames( String[] value )
	{
		networkNames = value;
	}

	public String getManufacturerName()
	{
		return manufacturerName;
	}

	public void setManufacturerName( String value )
	{
		manufacturerName = value;
	}

	public int getManufacturerId()
	{
		return manufacturerId;
	}

	public void setManufacturerId( int value )
	{
		manufacturerId = value;
	}

	public String getFamilyName()
	{
		return familyName;
	}

	public void setFamilyName( String value )
	{
		familyName = value;
	}

	public int getFamilyId()
	{
		return familyId;
	}

	public void setFamilyId( int value )
	{
		familyId = value;
	}

	public String getModelName()
	{
		return modelName;
	}

	public void setModelName( String value )
	{
		modelName = value;
	}

	public int getModelId()
	{
		return modelId;
	}

	public void setModelId( int value )
	{
		modelId = value;
	}

	public String getId()
	{
		return id;
	}

	public void setId( String value )
	{
		id = value;
	}

	public String getSerial()
	{
		return serial;
	}

	public void setSerial( String value )
	{
		serial = value;
	}

	public String getHwVersion()
	{
		return hwVersion;
	}

	public void setHwVersion( String value )
	{
		hwVersion = value;
	}

	public String getSwVersion()
	{
		return swVersion;
	}

	public void setSwVersion( String value )
	{
		swVersion = value;
	}

	public TimeZoneInfo getTimeZoneInfo()
	{
		return timeZoneInfo;
	}

	public void setTimeZoneInfo( TimeZoneInfo value )
	{
		timeZoneInfo = value;
	}

	public ConfigurationURL getConfigurationURL()
	{
		return configurationUrl;
	}

	public void setConfigurationURL( ConfigurationURL value )
	{
		configurationUrl = value;
	}

	public AddressZones getAddressZones()
	{
		return addressZones;
	}

	public void setAddressZones( AddressZones addressZones )
	{
		this.addressZones = addressZones;
	}

	public String getDeviceId()
	{
		return deviceId;
	}

	public void setDeviceId( String deviceId )
	{
		this.deviceId = deviceId;
	}

	public ConfigurationURL getConfigurationUrl()
	{
		return configurationUrl;
	}

	public void setConfigurationUrl( ConfigurationURL configurationUrl )
	{
		this.configurationUrl = configurationUrl;
	}

	public String getRegistrationId()
	{
		return registrationId;
	}

	public void setRegistrationId( String registrationId )
	{
		this.registrationId = registrationId;
	}

	public long getDeviceTime()
	{
		return deviceTime;
	}

	public void setDeviceTime( long deviceTime )
	{
		this.deviceTime = deviceTime;
	}

	public String getSubscriptionId()
	{
		return subscriptionId;
	}

	public void setSubscriptionId( String subscriptionId )
	{
		this.subscriptionId = subscriptionId;
	}

	public Integer getChannelsInUse()
	{
		return channelsInUse;
	}

	public void setChannelsInUse( Integer channelsInUse )
	{
		this.channelsInUse = channelsInUse;
	}

	public Integer getMaxChannels()
	{
		return maxChannels;
	}

	public void setMaxChannels( Integer maxChannels )
	{
		this.maxChannels = maxChannels;
	}

	public List<DeviceDetails> getChildDevices()
	{
		return childDevices;
	}

	public void setChildDevices( List<DeviceDetails> childDevices )
	{
		this.childDevices = childDevices;
	}

	public List<ChannelDetails> getDeviceChannels()
	{
		return deviceChannels;
	}

	public void setDeviceChannels( List<ChannelDetails> deviceChannels )
	{
		this.deviceChannels = deviceChannels;
	}

	public String getPath()
	{
		return path;
	}

	public void setPath( String path )
	{
		this.path = path;
	}

	public String getConnectState()
	{
		return connectState;
	}

	public void setConnectState( String connectState )
	{
		this.connectState = connectState;
	}

	public String getStationId()
	{
		return stationId;
	}

	public void setStationId( String stationId )
	{
		this.stationId = stationId;
	}

	public String getPatchList()
	{
		return patchList;
	}

	public void setPatchList( String patchList )
	{
		this.patchList = patchList;
	}
}

