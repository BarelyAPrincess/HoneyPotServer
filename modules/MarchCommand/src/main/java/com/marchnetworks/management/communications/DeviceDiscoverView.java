package com.marchnetworks.management.communications;

import java.util.ArrayList;
import java.util.List;

public final class DeviceDiscoverView
{
	private String deviceModel;
	private String deviceModelId;
	private String deviceFamily;
	private String deviceFamilyId;
	private String deviceName;
	private String deviceIpAddress;
	private String deviceMacAddress;
	private String deviceSoftwareVersion;
	private String deviceManufacturer;
	private List<NetworkConfiguration> extendedNetworkConfiguration = new ArrayList();
	private boolean isManaged;

	public String getDeviceModel()
	{
		return deviceModel;
	}

	public void setDeviceModel( String deviceModel )
	{
		this.deviceModel = deviceModel;
	}

	public String getDeviceFamily()
	{
		return deviceFamily;
	}

	public void setDeviceFamily( String deviceFamily )
	{
		this.deviceFamily = deviceFamily;
	}

	public String getDeviceName()
	{
		return deviceName;
	}

	public void setDeviceName( String deviceName )
	{
		this.deviceName = deviceName;
	}

	public String getDeviceIpAddress()
	{
		return deviceIpAddress;
	}

	public void setDeviceIpAddress( String deviceIpAddress )
	{
		this.deviceIpAddress = deviceIpAddress;
	}

	public String getDeviceMacAddress()
	{
		return deviceMacAddress;
	}

	public boolean getIsManaged()
	{
		return isManaged;
	}

	public void setDeviceMacAddress( String deviceMacAddress )
	{
		this.deviceMacAddress = deviceMacAddress;
	}

	public String getDeviceSoftwareVersion()
	{
		return deviceSoftwareVersion;
	}

	public void setDeviceSoftwareVersion( String deviceSoftwareVersion )
	{
		this.deviceSoftwareVersion = deviceSoftwareVersion;
	}

	public String getDeviceModelId()
	{
		return deviceModelId;
	}

	public void setDeviceModelId( String deviceModelId )
	{
		this.deviceModelId = deviceModelId;
	}

	public String getDeviceFamilyId()
	{
		return deviceFamilyId;
	}

	public void setDeviceFamilyId( String deviceFamilyId )
	{
		this.deviceFamilyId = deviceFamilyId;
	}

	public String getDeviceManufacturer()
	{
		return deviceManufacturer;
	}

	public void setDeviceManufacturer( String deviceManufacturer )
	{
		this.deviceManufacturer = deviceManufacturer;
	}

	public List<NetworkConfiguration> getExtendedNetworkConfiguration()
	{
		return extendedNetworkConfiguration;
	}

	public void setExtendedNetworkConfiguration( List<NetworkConfiguration> extendedNetworkConfiguration )
	{
		this.extendedNetworkConfiguration = extendedNetworkConfiguration;
	}

	public void setIsManaged( boolean isManaged )
	{
		this.isManaged = isManaged;
	}

	public String toString()
	{
		StringBuilder device = new StringBuilder();
		device.append( deviceModel ).append( ", " );
		device.append( deviceModelId ).append( ", " );
		device.append( deviceFamily ).append( ", " );
		device.append( deviceFamilyId ).append( ", " );
		device.append( deviceName ).append( ", " );
		device.append( deviceIpAddress ).append( ", " );
		device.append( deviceMacAddress ).append( ", " );
		device.append( deviceSoftwareVersion ).append( ", " );
		device.append( deviceManufacturer ).append( ", " );
		device.append( extendedNetworkConfiguration );
		device.append( ", " ).append( isManaged );
		return device.toString();
	}
}
