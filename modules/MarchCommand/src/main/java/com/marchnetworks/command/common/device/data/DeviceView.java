package com.marchnetworks.command.common.device.data;

import com.marchnetworks.command.api.rest.DeviceManagementConstants;
import com.marchnetworks.command.common.transport.data.AddressZone;
import com.marchnetworks.command.common.transport.data.ConfigurationURL;
import com.marchnetworks.command.common.transport.data.LocalZone;
import com.marchnetworks.command.common.transport.data.TimeZoneInfo;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlType( propOrder = {"channelsInUse", "channelsMax", "configurationUrl", "connectState", "deviceId", "family", "familyName", "ipAddresses", "lastCommunicationTime", "localAddressZone", "macAddresses", "manufacturer", "manufacturerName", "model", "modelName", "networkNames", "parentDeviceId", "registrationAddress", "registrationStatus", "serial", "softwareVersion", "stationId", "subModel", "timezoneInfo", "userAddressZones", "patchList"} )
public class DeviceView
{
	private String deviceId;
	private transient String assignedDeviceId;
	private String registrationAddress;
	private String manufacturer;
	private String manufacturerName;
	private String model;
	private String modelName;
	private String subModel;
	private String[] macAddresses;
	private String serial;
	private String[] networkNames;
	private String softwareVersion;
	private RegistrationStatus registrationStatus;
	private ConnectState connectState;
	private String parentDeviceId;
	private String family;
	private String familyName;
	private TimeZoneInfo timezoneInfo;
	private Integer channelsMax;
	private Integer channelsInUse;
	private String[] ipAddresses;
	private ConfigurationURL configurationUrl;
	private transient Long timeDelta;
	private LocalZone localAddressZone;
	private AddressZone[] userAddressZones;
	private transient String eventSubscriptionId;
	private transient Map<String, Object> additionalDeviceRegistrationInfo;
	private transient String[] deviceEventSubscriptionPrefixes;
	private long lastCommunicationTime;
	private String stationId;
	private transient String registrationError;
	private transient Long deviceEventSequenceId;
	private transient Map<String, String> globalSettings;
	private List<String> capabilities;
	private String patchList;
	private long deviceCreationTime;
	private Integer notifyInterval;

	public boolean isR5()
	{
		String manuID = getManufacturer();
		String famID = getFamily();
		return DeviceManagementConstants.isR5Device( manuID, famID );
	}

	public String getDeviceId()
	{
		return deviceId;
	}

	public void setDeviceId( String deviceId )
	{
		this.deviceId = deviceId;
	}

	public String getRegistrationAddress()
	{
		return registrationAddress;
	}

	public void setRegistrationAddress( String address )
	{
		registrationAddress = address;
	}

	public String getManufacturer()
	{
		return manufacturer;
	}

	public void setManufacturer( String manufacturer )
	{
		this.manufacturer = manufacturer;
	}

	public String getManufacturerName()
	{
		return manufacturerName;
	}

	public void setManufacturerName( String manufacturerName )
	{
		this.manufacturerName = manufacturerName;
	}

	public String getModel()
	{
		return model;
	}

	public void setModel( String model )
	{
		this.model = model;
	}

	public String getSubModel()
	{
		return subModel;
	}

	public void setSubModel( String subModel )
	{
		this.subModel = subModel;
	}

	public String[] getMacAddresses()
	{
		return macAddresses;
	}

	public void setMacAddresses( String[] macAddresses )
	{
		this.macAddresses = macAddresses;
	}

	public String getSerial()
	{
		return serial;
	}

	public void setSerial( String serial )
	{
		this.serial = serial;
	}

	public String[] getNetworkNames()
	{
		return networkNames;
	}

	public void setNetworkNames( String[] names )
	{
		networkNames = names;
	}

	public String getSoftwareVersion()
	{
		return softwareVersion;
	}

	public void setSoftwareVersion( String softwareVersion )
	{
		this.softwareVersion = softwareVersion;
	}

	@XmlElement( required = true )
	public RegistrationStatus getRegistrationStatus()
	{
		return registrationStatus;
	}

	public void setRegistrationStatus( RegistrationStatus registrationStatus )
	{
		this.registrationStatus = registrationStatus;
	}

	public String getParentDeviceId()
	{
		return parentDeviceId;
	}

	public void setParentDeviceId( String parentDeviceId )
	{
		this.parentDeviceId = parentDeviceId;
	}

	public String getFamily()
	{
		return family;
	}

	public void setFamily( String family )
	{
		this.family = family;
	}

	public String getFamilyName()
	{
		return familyName;
	}

	public void setFamilyName( String familyName )
	{
		this.familyName = familyName;
	}

	public String getModelName()
	{
		return modelName;
	}

	public void setModelName( String modelName )
	{
		this.modelName = modelName;
	}

	public void setConnectState( ConnectState connectState )
	{
		this.connectState = connectState;
	}

	@XmlElement( required = true )
	public ConnectState getConnectState()
	{
		return connectState;
	}

	@XmlElement( required = true, nillable = true )
	public Integer getChannelsMax()
	{
		return channelsMax;
	}

	public void setChannelsMax( Integer m_ChannelsMax )
	{
		channelsMax = m_ChannelsMax;
	}

	@XmlElement( required = true, nillable = true )
	public Integer getChannelsInUse()
	{
		return channelsInUse;
	}

	public void setChannelsInUse( Integer m_ChannelsInUse )
	{
		channelsInUse = m_ChannelsInUse;
	}

	@XmlTransient
	public String getEventSubscriptionId()
	{
		return eventSubscriptionId;
	}

	public void setEventSubscriptionId( String eventSubscriptionId )
	{
		this.eventSubscriptionId = eventSubscriptionId;
	}

	@XmlTransient
	public Map<String, Object> getAdditionalDeviceRegistrationInfo()
	{
		return additionalDeviceRegistrationInfo;
	}

	public void setAdditionalDeviceRegistrationInfo( Map<String, Object> additionalDeviceRegistrationInfo )
	{
		this.additionalDeviceRegistrationInfo = additionalDeviceRegistrationInfo;
	}

	public List<String> createAuditInfo()
	{
		List<String> attributesAndValues = new LinkedList();

		attributesAndValues.add( "Object Class Name: " + getClass().getName() );
		attributesAndValues.add( "Device Id: " + deviceId );
		attributesAndValues.add( "Address: " + registrationAddress );
		attributesAndValues.add( "Manufacturer: " + manufacturer );
		attributesAndValues.add( "Manufacturer Name: " + manufacturerName );
		attributesAndValues.add( "Model: " + model );
		attributesAndValues.add( "Model Name: " + modelName );
		attributesAndValues.add( "Submodel: " + subModel );
		if ( networkNames != null )
		{
			attributesAndValues.add( "Network names: " );
			for ( int i = 0; i < networkNames.length; i++ )
			{
				attributesAndValues.add( networkNames[i] + " " );
			}
		}
		if ( ipAddresses != null )
		{
			attributesAndValues.add( "Network names: " );
			for ( int i = 0; i < ipAddresses.length; i++ )
			{
				attributesAndValues.add( ipAddresses[i] + " " );
			}
		}
		attributesAndValues.add( "Serial: " + serial );
		attributesAndValues.add( "Software Version: " + softwareVersion );
		if ( registrationStatus != null )
		{
			attributesAndValues.add( "Registration Status: " + registrationStatus );
		}
		attributesAndValues.add( "Connection State: " + connectState );
		attributesAndValues.add( "Parent Device Id: " + parentDeviceId );
		attributesAndValues.add( "Family: " + family );
		attributesAndValues.add( "Family Name: " + familyName );

		return attributesAndValues;
	}

	public TimeZoneInfo getTimezoneInfo()
	{
		return timezoneInfo;
	}

	public void setTimezoneInfo( TimeZoneInfo timezoneInfo )
	{
		this.timezoneInfo = timezoneInfo;
	}

	public String[] getIpAddresses()
	{
		return ipAddresses;
	}

	public void setIpAddresses( String[] ipAddresses )
	{
		this.ipAddresses = ipAddresses;
	}

	public ConfigurationURL getConfigurationUrl()
	{
		return configurationUrl;
	}

	public void setConfigurationUrl( ConfigurationURL configurationUrl )
	{
		this.configurationUrl = configurationUrl;
	}

	@XmlTransient
	public Long getTimeDelta()
	{
		return Long.valueOf( timeDelta != null ? timeDelta.longValue() : 0L );
	}

	public void setTimeDelta( Long timeDelta )
	{
		this.timeDelta = timeDelta;
	}

	public void calculateTimeDelta( Long deviceTime )
	{
		if ( deviceTime.longValue() > 0L )
		{
			setTimeDelta( Long.valueOf( deviceTime.longValue() - System.currentTimeMillis() ) );
		}
	}

	public LocalZone getLocalAddressZone()
	{
		return localAddressZone;
	}

	public void setLocalAddressZone( LocalZone localAddressZone )
	{
		this.localAddressZone = localAddressZone;
	}

	public AddressZone[] getUserAddressZones()
	{
		return userAddressZones;
	}

	public void setUserAddressZones( AddressZone[] userAddressZones )
	{
		this.userAddressZones = userAddressZones;
	}

	@XmlTransient
	public String getAssignedDeviceId()
	{
		return assignedDeviceId;
	}

	public void setAssignedDeviceId( String assignedDeviceId )
	{
		this.assignedDeviceId = assignedDeviceId;
	}

	@XmlTransient
	public String[] getDeviceEventSubscriptionPrefixes()
	{
		return deviceEventSubscriptionPrefixes;
	}

	public void setDeviceEventSubscriptionPrefixes( String[] deviceEventSubscriptionPrefixes )
	{
		this.deviceEventSubscriptionPrefixes = deviceEventSubscriptionPrefixes;
	}

	public long getLastCommunicationTime()
	{
		return lastCommunicationTime;
	}

	public void setLastCommunicationTime( long lastCommunicationTime )
	{
		this.lastCommunicationTime = lastCommunicationTime;
	}

	public String getStationId()
	{
		return stationId;
	}

	public void setStationId( String stationId )
	{
		this.stationId = stationId;
	}

	@XmlTransient
	public String getRegistrationError()
	{
		return registrationError;
	}

	public void setRegistrationError( String registrationError )
	{
		this.registrationError = registrationError;
	}

	@XmlTransient
	public Long getDeviceEventSequenceId()
	{
		return deviceEventSequenceId;
	}

	public void setDeviceEventSequenceId( Long deviceEventSequenceId )
	{
		this.deviceEventSequenceId = deviceEventSequenceId;
	}

	@XmlTransient
	public Map<String, String> getGlobalSettings()
	{
		return globalSettings;
	}

	public void setGlobalSettings( Map<String, String> globalSettings )
	{
		this.globalSettings = globalSettings;
	}

	@XmlTransient
	public List<String> getCapabilities()
	{
		return capabilities;
	}

	public void setCapabilities( List<String> capabilities )
	{
		this.capabilities = capabilities;
	}

	public String getPatchList()
	{
		return patchList;
	}

	public void setPatchList( String patchList )
	{
		this.patchList = patchList;
	}

	@XmlTransient
	public long getDeviceCreationTime()
	{
		return deviceCreationTime;
	}

	public void setDeviceCreationTime( long deviceCreationTime )
	{
		this.deviceCreationTime = deviceCreationTime;
	}

	@XmlTransient
	public Integer getNotifyInterval()
	{
		return notifyInterval;
	}

	public void setNotifyInterval( Integer notifyInterval )
	{
		this.notifyInterval = notifyInterval;
	}
}
