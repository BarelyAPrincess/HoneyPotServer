package com.marchnetworks.management.config.service;

public class DeviceImageDescriptor
{
	private String id;

	private String name;
	private String description;
	private String family;
	private String model;
	private String firmware;
	private ConfigSnapshotDescriptor snapshot;

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

	public void setDescription( String desc )
	{
		description = desc;
	}

	public String getModel()
	{
		return model;
	}

	public void setModel( String model )
	{
		this.model = model;
	}

	public String getFamily()
	{
		return family;
	}

	public void setFamily( String family )
	{
		this.family = family;
	}

	public String getFirmware()
	{
		return firmware;
	}

	public void setFirmware( String firmware )
	{
		this.firmware = firmware;
	}

	public ConfigSnapshotDescriptor getSnapshot()
	{
		return snapshot;
	}

	public void setSnapshot( ConfigSnapshotDescriptor snapshot )
	{
		this.snapshot = snapshot;
	}
}
