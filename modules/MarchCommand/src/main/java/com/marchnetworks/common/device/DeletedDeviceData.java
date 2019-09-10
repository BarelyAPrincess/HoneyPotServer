package com.marchnetworks.common.device;

public class DeletedDeviceData
{
	private String pathString;

	private String address;

	private String manufacturer;

	private String manufacturerName;

	private String model;

	private String modelName;

	private String macAddress;

	private String serial;

	private String name;
	private String softwareVersion;
	protected String hardwareVersion;
	private String family;
	private String familyName;

	public DeletedDeviceData()
	{
	}

	public DeletedDeviceData( DeletedDevice deletedDevice )
	{
		pathString = deletedDevice.getPathString();
		address = deletedDevice.getAddress();
		manufacturer = deletedDevice.getManufacturer();
		manufacturerName = deletedDevice.getManufacturerName();
		model = deletedDevice.getModel();
		modelName = deletedDevice.getModelName();
		macAddress = deletedDevice.getMacAddress();
		serial = deletedDevice.getSerial();
		name = deletedDevice.getName();
		softwareVersion = deletedDevice.getSoftwareVersion();
		hardwareVersion = deletedDevice.getHardwareVersion();
		family = deletedDevice.getFamily();
		familyName = deletedDevice.getFamilyName();
	}

	public String getPathString()
	{
		return pathString;
	}

	public void setPathString( String pathString )
	{
		this.pathString = pathString;
	}

	public String getAddress()
	{
		return address;
	}

	public void setAddress( String address )
	{
		this.address = address;
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

	public String getModelName()
	{
		return modelName;
	}

	public void setModelName( String modelName )
	{
		this.modelName = modelName;
	}

	public String getMacAddress()
	{
		return macAddress;
	}

	public void setMacAddress( String macAddress )
	{
		this.macAddress = macAddress;
	}

	public String getSerial()
	{
		return serial;
	}

	public void setSerial( String serial )
	{
		this.serial = serial;
	}

	public String getName()
	{
		return name;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	public String getSoftwareVersion()
	{
		return softwareVersion;
	}

	public void setSoftwareVersion( String softwareVersion )
	{
		this.softwareVersion = softwareVersion;
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
}
