package com.marchnetworks.management.config.service;

import javax.xml.bind.annotation.XmlTransient;

public class ConfigSnapshotDescriptor
{
	private String id;
	private String name;
	private String description;
	private String family;
	private String model;
	private String firmwareVersion;
	private String configData;
	private String hash;
	private String timestamp;

	public String getId()
	{
		return id;
	}

	public void setId( String id )
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription( String description )
	{
		this.description = description;
	}

	public String getModel()
	{
		return model;
	}

	public void setModel( String model )
	{
		this.model = model;
	}

	public String getFirmwareVersion()
	{
		return firmwareVersion;
	}

	public void setFirmwareVersion( String version )
	{
		firmwareVersion = version;
	}

	@XmlTransient
	public String getConfigData()
	{
		return configData;
	}

	@XmlTransient
	public String getHash()
	{
		return hash;
	}

	public String getTimestamp()
	{
		return timestamp;
	}

	public void setTimestamp( String timestamp )
	{
		this.timestamp = timestamp;
	}

	public String getFamily()
	{
		return family;
	}

	public void setFamily( String family )
	{
		this.family = family;
	}
}
