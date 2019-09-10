package com.marchnetworks.license.model;

import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

public class DeviceLicenseInfo
{
	protected Long m_iDeviceId;
	protected LicenseType m_Type;
	protected int m_iCount;
	protected Expiry m_Expiry;
	protected Date m_dStart;
	protected Date m_dEnd;
	protected boolean m_bRevoked;

	@XmlElement( required = true )
	public Long getDeviceId()
	{
		return m_iDeviceId;
	}

	public void setDeviceId( Long deviceId )
	{
		m_iDeviceId = deviceId;
	}

	@XmlTransient
	public LicenseType getType()
	{
		return m_Type;
	}

	public void setType( LicenseType t )
	{
		m_Type = t;
	}

	public int getCount()
	{
		return m_iCount;
	}

	public void setCount( int count )
	{
		m_iCount = count;
	}

	public Expiry getExipry()
	{
		return m_Expiry;
	}

	public void setExpiry( Expiry e )
	{
		m_Expiry = e;
	}

	@XmlTransient
	public Date getStart()
	{
		return m_dStart;
	}

	public void setStart( Date start )
	{
		m_dStart = start;
	}

	@XmlTransient
	public Date getEnd()
	{
		return m_dEnd;
	}

	public void setEnd( Date end )
	{
		m_dEnd = end;
	}

	public boolean getRevoked()
	{
		return m_bRevoked;
	}

	public void setRevoked( boolean revoked )
	{
		m_bRevoked = revoked;
	}
}
