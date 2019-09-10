package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "DeviceDetails", propOrder = {"ipAddresses", "macAddresses", "networkNames", "manufacturerName", "manufacturerId", "familyName", "familyId", "modelName", "modelId", "id", "serial", "hwVersion", "swVersion", "timeZoneInfo", "configurationUrl", "addressZones", "path", "connectState", "stationId", "patchList"} )
public class DeviceDetails
{
	@XmlElement( name = "IPAddresses", required = true )
	protected ArrayOfString ipAddresses;
	@XmlElement( name = "MACAddresses", required = true )
	protected ArrayOfString macAddresses;
	@XmlElement( required = true )
	protected ArrayOfString networkNames;
	@XmlElement( required = true )
	protected String manufacturerName;
	protected int manufacturerId;
	@XmlElement( required = true )
	protected String familyName;
	protected int familyId;
	@XmlElement( required = true )
	protected String modelName;
	protected int modelId;
	@XmlElement( required = true )
	protected String id;
	@XmlElement( required = true )
	protected String serial;
	@XmlElement( required = true )
	protected String hwVersion;
	@XmlElement( required = true )
	protected String swVersion;
	@XmlElement( required = true )
	protected TimeZoneInfo timeZoneInfo;
	protected ConfigurationURL configurationUrl;
	protected AddressZones addressZones;
	protected String path;
	protected String connectState;
	protected String stationId;
	protected String patchList;

	public ArrayOfString getIPAddresses()
	{
		return ipAddresses;
	}

	public void setIPAddresses( ArrayOfString value )
	{
		ipAddresses = value;
	}

	public ArrayOfString getMACAddresses()
	{
		return macAddresses;
	}

	public void setMACAddresses( ArrayOfString value )
	{
		macAddresses = value;
	}

	public ArrayOfString getNetworkNames()
	{
		return networkNames;
	}

	public void setNetworkNames( ArrayOfString value )
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

	public ConfigurationURL getConfigurationUrl()
	{
		return configurationUrl;
	}

	public void setConfigurationUrl( ConfigurationURL value )
	{
		configurationUrl = value;
	}

	public AddressZones getAddressZones()
	{
		return addressZones;
	}

	public void setAddressZones( AddressZones value )
	{
		addressZones = value;
	}

	public String getPath()
	{
		return path;
	}

	public void setPath( String value )
	{
		path = value;
	}

	public String getConnectState()
	{
		return connectState;
	}

	public void setConnectState( String value )
	{
		connectState = value;
	}

	public String getStationId()
	{
		return stationId;
	}

	public void setStationId( String value )
	{
		stationId = value;
	}

	public String getPatchList()
	{
		return patchList;
	}

	public void setPatchList( String value )
	{
		patchList = value;
	}
}
