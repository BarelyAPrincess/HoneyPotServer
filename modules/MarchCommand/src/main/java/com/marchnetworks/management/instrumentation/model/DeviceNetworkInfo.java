package com.marchnetworks.management.instrumentation.model;

import com.marchnetworks.management.instrumentation.data.DeviceNetworkInfoType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table( name = "DEVICE_NETWORK_INFO" )
public class DeviceNetworkInfo implements DeviceNetworkInfoMBean, Comparable<DeviceNetworkInfo>
{
	@Id
	@GeneratedValue
	@Column( name = "ID" )
	private Long id;
	@ManyToOne
	@JoinColumn( name = "DEVICE" )
	private Device device;
	@Enumerated( EnumType.STRING )
	@Column( name = "NETWORK_INFO_TYPE", nullable = false )
	private DeviceNetworkInfoType networkInfoType;
	@Column( name = "VALUE", length = 1000 )
	private String value;
	@Version
	@Column( name = "VERSION" )
	protected Long version;

	public String getValue()
	{
		return value;
	}

	public Long getId()
	{
		return id;
	}

	public void setId( Long id )
	{
		this.id = id;
	}

	public Device getDevice()
	{
		return device;
	}

	public void setDevice( Device device )
	{
		this.device = device;
	}

	public Long getVersion()
	{
		return version;
	}

	public void setVersion( Long version )
	{
		this.version = version;
	}

	public void setValue( String value )
	{
		this.value = value;
	}

	public DeviceNetworkInfoType getNetworkInfoType()
	{
		return networkInfoType;
	}

	public void setNetworkInfoType( DeviceNetworkInfoType networkInfoType )
	{
		this.networkInfoType = networkInfoType;
	}

	public int compareTo( DeviceNetworkInfo anotherInfo )
	{
		if ( anotherInfo == null )
		{
			return -1;
		}
		if ( anotherInfo.getNetworkInfoType().equals( networkInfoType ) )
		{
			return anotherInfo.getValue().compareTo( value );
		}
		return 1;
	}
}

