package com.marchnetworks.license.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table( name = "DEVICE_LICENSE" )
public class DeviceLicenseEntity
{
	@Id
	@GeneratedValue( strategy = GenerationType.AUTO )
	@Column( name = "ID" )
	private Long m_Id;
	@Column( name = "DEVICE_ID" )
	private Long m_DeviceId;
	@Column( name = "LICENSE_TYPE" )
	@Enumerated( EnumType.STRING )
	private LicenseType m_Type;
	@Column( name = "ASSIGNED" )
	private Integer m_Assigned;

	public Long getId()
	{
		return m_Id;
	}

	public void setId( Long id )
	{
		m_Id = id;
	}

	public Long getDeviceId()
	{
		return m_DeviceId;
	}

	public void setDeviceId( Long m_DeviceId )
	{
		this.m_DeviceId = m_DeviceId;
	}

	public LicenseType getType()
	{
		return m_Type;
	}

	public void setType( LicenseType m_Type )
	{
		this.m_Type = m_Type;
	}

	public Integer getAssigned()
	{
		return m_Assigned;
	}

	public void setAssigned( Integer m_Assigned )
	{
		this.m_Assigned = m_Assigned;
	}
}
