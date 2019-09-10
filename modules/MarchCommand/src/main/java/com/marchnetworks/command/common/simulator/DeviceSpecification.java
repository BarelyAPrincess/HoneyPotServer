package com.marchnetworks.command.common.simulator;

import java.util.Set;

public class DeviceSpecification
{
	private int numDevices;
	private Set<String> serials;
	private int numCameras = 5;
	private int numAlarms = 5;
	private boolean hasChildDevices = false;

	private String family = "257";
	private String familyName = "R5";
	private String model = "8";
	private String manufacturer = "1";
	private String manufacturerName = "March Networks";
	private String hardwareVersion = "8704v1";
	private String softwareVersion = "5.7.8.0099";

	public DeviceSpecification( int numDevices )
	{
		this.numDevices = numDevices;
	}

	public DeviceSpecification( Set<String> serials )
	{
		this.serials = serials;
		numDevices = serials.size();
	}

	public int getNumDevices()
	{
		return numDevices;
	}

	public void setNumDevices( int numDevices )
	{
		this.numDevices = numDevices;
	}

	public Set<String> getSerials()
	{
		return serials;
	}

	public void setSerials( Set<String> serials )
	{
		this.serials = serials;
	}

	public int getNumCameras()
	{
		return numCameras;
	}

	public void setNumCameras( int numCameras )
	{
		this.numCameras = numCameras;
	}

	public int getNumAlarms()
	{
		return numAlarms;
	}

	public void setNumAlarms( int numAlarms )
	{
		this.numAlarms = numAlarms;
	}

	public boolean hasChildDevices()
	{
		return hasChildDevices;
	}

	public void setHasChildDevices( boolean hasChildDevices )
	{
		this.hasChildDevices = hasChildDevices;
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

	public String getModel()
	{
		return model;
	}

	public void setModel( String model )
	{
		this.model = model;
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

	public String getHardwareVersion()
	{
		return hardwareVersion;
	}

	public void setHardwareVersion( String hardwareVersion )
	{
		this.hardwareVersion = hardwareVersion;
	}

	public String getSoftwareVersion()
	{
		return softwareVersion;
	}

	public void setSoftwareVersion( String softwareVersion )
	{
		this.softwareVersion = softwareVersion;
	}
}
