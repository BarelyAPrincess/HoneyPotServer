package com.marchnetworks.management.config.model;

import com.marchnetworks.management.config.service.DeviceImageDescriptor;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table( name = "DEVICEIMAGE" )
public class DeviceImage implements Serializable
{
	private static final long serialVersionUID = -73741847434016499L;
	@Id
	@GeneratedValue
	private Long id;
	@Column( name = "NAME" )
	private String name;
	@Column( name = "DESCRIPTION", length = 4000, nullable = true )
	private String description;
	@Column( name = "MODEL" )
	private String model;
	@Column( name = "FAMILY" )
	private String family;
	@Column( name = "FIRMWARE_VERSION" )
	private String firmwareVersion;
	@OneToOne( targetEntity = ConfigSnapshot.class )
	@JoinColumn( name = "FK_CONFIGSNAPSHOT_ID" )
	private ConfigSnapshot snapshot;

	public ConfigSnapshot getSnapshot()
	{
		return snapshot;
	}

	public void setSnapshot( ConfigSnapshot val )
	{
		snapshot = val;
	}

	public Long getId()
	{
		return id;
	}

	public void setId( Long id )
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

	public String getFamily()
	{
		return family;
	}

	public void setFamily( String family )
	{
		this.family = family;
	}

	public String getFirmwareVersion()
	{
		return firmwareVersion;
	}

	public void setFirmwareVersion( String firmwareVersion )
	{
		this.firmwareVersion = firmwareVersion;
	}

	public boolean isModelMatch( String otherModel )
	{
		return model.equals( otherModel );
	}

	public boolean isFamilyMatch( String otherFamily )
	{
		return family.equals( otherFamily );
	}

	public boolean isFirmwareVersionMatch( String otherFirmwareVer )
	{
		return firmwareVersion.equals( otherFirmwareVer );
	}

	public boolean isFirmwareUpgradable()
	{
		return !family.equals( "4" );
	}

	public void readFromDataObject( DeviceImageDescriptor imageDescriptor )
	{
		if ( imageDescriptor.getId() != null )
		{
			setId( Long.valueOf( imageDescriptor.getId() ) );
		}
		setDescription( imageDescriptor.getDescription() );
		setFamily( imageDescriptor.getFamily() );
		setFirmwareVersion( imageDescriptor.getFirmware() );
		setModel( imageDescriptor.getModel() );
		setName( imageDescriptor.getName() );
	}

	public DeviceImageDescriptor toDataObject()
	{
		DeviceImageDescriptor imageDescriptor = new DeviceImageDescriptor();
		if ( getId() != null )
		{
			imageDescriptor.setId( getId().toString() );
		}
		imageDescriptor.setDescription( getDescription() );
		imageDescriptor.setFamily( getFamily() );
		imageDescriptor.setFirmware( getFirmwareVersion() );
		imageDescriptor.setModel( getModel() );
		imageDescriptor.setName( getName() );

		return imageDescriptor;
	}
}
