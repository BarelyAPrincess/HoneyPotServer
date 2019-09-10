package com.marchnetworks.management.instrumentation.model;

import com.marchnetworks.command.common.device.data.DeviceOutputView;
import com.marchnetworks.command.common.transport.data.Pair;
import com.marchnetworks.common.serialization.CoreJsonSerializer;
import com.marchnetworks.server.communications.transport.datamodel.DeviceOutput;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table( name = "DEVICE_OUTPUT" )
@Inheritance( strategy = InheritanceType.SINGLE_TABLE )
@DiscriminatorColumn( name = "DISCRIMINATOR", discriminatorType = DiscriminatorType.STRING, length = 20 )
@DiscriminatorValue( "DeviceOutput" )
public abstract class DeviceOutputEntity implements DeviceOutputMBean
{
	@Id
	@GeneratedValue
	@Column( name = "ID" )
	protected Long id;
	@Version
	@Column( name = "VERSION" )
	private Long version;
	@Column( name = "DEVICE_ID" )
	protected Long deviceId;
	@Column( name = "RESOURCE_ID" )
	protected Long resourceId;
	@Column( name = "OUTPUT_ID" )
	protected String outputId;
	@Column( name = "NAME" )
	protected String name;
	@Column( name = "OUTPUT_DEVICE_ID" )
	protected String outputDeviceId;
	@Column( name = "OUTPUT_DEVICE_ADDRESS" )
	protected String outputDeviceAddress;
	@Column( name = "INFO", length = 500 )
	protected String info;

	public abstract DeviceOutputView toDataObject();

	public abstract boolean readFromTransportObject( DeviceOutput paramDeviceOutput );

	public abstract Class<? extends DeviceOutputView> getDataObjectClass();

	public Long getId()
	{
		return id;
	}

	public void setId( Long id )
	{
		this.id = id;
	}

	public Long getVersion()
	{
		return version;
	}

	public void setVersion( Long version )
	{
		this.version = version;
	}

	public Long getDeviceId()
	{
		return deviceId;
	}

	public void setDeviceId( Long deviceId )
	{
		this.deviceId = deviceId;
	}

	public Long getResourceId()
	{
		return resourceId;
	}

	public void setResourceId( Long resourceId )
	{
		this.resourceId = resourceId;
	}

	public String getOutputId()
	{
		return outputId;
	}

	public void setOutputId( String outputId )
	{
		this.outputId = outputId;
	}

	public String getName()
	{
		return name;
	}

	public void setName( String name )
	{
		this.name = name;
	}

	public String getOutputDeviceId()
	{
		return outputDeviceId;
	}

	public void setOutputDeviceId( String outputDeviceId )
	{
		this.outputDeviceId = outputDeviceId;
	}

	public String getOutputDeviceAddress()
	{
		return outputDeviceAddress;
	}

	public void setOutputDeviceAddress( String outputDeviceAddress )
	{
		this.outputDeviceAddress = outputDeviceAddress;
	}

	public Pair[] getInfoAsPairs()
	{
		return ( Pair[] ) CoreJsonSerializer.fromJson( info, Pair[].class );
	}

	protected String getInfo()
	{
		return info;
	}

	public void setInfo( Pair[] info )
	{
		if ( ( info != null ) && ( info.length > 0 ) )
		{
			this.info = CoreJsonSerializer.toJson( info );
		}
		else
		{
			this.info = null;
		}
	}

	protected void setInfo( String info )
	{
		this.info = info;
	}
}

