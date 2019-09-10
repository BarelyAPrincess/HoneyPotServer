package com.marchnetworks.common.device;

import com.marchnetworks.common.serialization.CoreJsonSerializer;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table( name = "DELETED_DEVICE" )
public class DeletedDevice implements Serializable
{
	private static final long serialVersionUID = 381513656750319891L;
	@Id
	@GeneratedValue
	@Column( name = "ID" )
	private Long id;
	@Column( name = "PATH", length = 1000 )
	private String path;
	@Column( name = "PATH_STRING", length = 4000 )
	private String pathString;
	@Column( name = "ADDRESS" )
	private String address;
	@Column( name = "MANUFACTURER" )
	private String manufacturer;
	@Column( name = "MANUFACTURER_NAME" )
	private String manufacturerName;
	@Column( name = "MODEL" )
	private String model;
	@Column( name = "MODEL_NAME" )
	private String modelName;
	@Column( name = "MAC_ADDRESS" )
	private String macAddress;
	@Column( name = "SERIAL" )
	private String serial;
	@Column( name = "NAME" )
	private String name;
	@Column( name = "SOFTWARE_VERSION" )
	private String softwareVersion;
	@Column( name = "HARDWARE_VERSION" )
	protected String hardwareVersion;
	@Column( name = "FAMILY" )
	private String family;
	@Column( name = "FAMILY_NAME" )
	private String familyName;

	public DeletedDevice()
	{
	}

	public DeletedDevice( List<String> path, String pathString, String address, String manufacturer, String manufacturerName, String model, String modelName, String macAddress, String serial, String name, String softwareVersion, String hardwareVersion, String family, String familyName )
	{
		this.path = CoreJsonSerializer.toJson( path );
		this.pathString = pathString;
		this.address = address;
		this.manufacturer = manufacturer;
		this.manufacturerName = manufacturerName;
		this.model = model;
		this.modelName = modelName;
		this.macAddress = macAddress;
		this.serial = serial;
		this.name = name;
		this.softwareVersion = softwareVersion;
		this.hardwareVersion = hardwareVersion;
		this.family = family;
		this.familyName = familyName;
	}

	public DeletedDeviceData toDataObject()
	{
		return new DeletedDeviceData( this );
	}

	public Long getId()
	{
		return id;
	}

	protected String getPath()
	{
		return path;
	}

	protected void setPath( String path )
	{
		this.path = path;
	}

	public String getAddress()
	{
		return address;
	}

	public String getManufacturer()
	{
		return manufacturer;
	}

	public String getManufacturerName()
	{
		return manufacturerName;
	}

	public String getModel()
	{
		return model;
	}

	public String getModelName()
	{
		return modelName;
	}

	public String getMacAddress()
	{
		return macAddress;
	}

	public String getSerial()
	{
		return serial;
	}

	public String getName()
	{
		return name;
	}

	public String getSoftwareVersion()
	{
		return softwareVersion;
	}

	public String getHardwareVersion()
	{
		return hardwareVersion;
	}

	public String getFamily()
	{
		return family;
	}

	public String getFamilyName()
	{
		return familyName;
	}

	public String getPathString()
	{
		return pathString;
	}
}
