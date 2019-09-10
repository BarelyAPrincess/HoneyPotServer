package com.marchnetworks.license.model;

import java.util.Date;

import javax.xml.bind.annotation.XmlElement;

public class ServerLicenseInfo
{
	protected LicenseType m_Type;
	protected int m_iTotal;
	protected int m_iAllocated;
	protected Expiry m_Expiry;
	protected Date m_dStart;
	protected Date m_dEnd;
	protected boolean m_bIsExpired;

	public ServerLicenseInfo()
	{
		m_Type = null;
		m_iTotal = -1;
		m_iAllocated = -1;
		m_Expiry = null;
		m_dStart = null;
		m_dEnd = null;
	}

	@XmlElement( required = true )
	public LicenseType getType()
	{
		return m_Type;
	}

	public void setType( LicenseType t )
	{
		m_Type = t;
	}

	public int getTotal()
	{
		return m_iTotal;
	}

	public void setTotal( int total )
	{
		m_iTotal = total;
	}

	public int getAllocated()
	{
		return m_iAllocated;
	}

	public void setAllocated( int allocated )
	{
		m_iAllocated = allocated;
	}

	@XmlElement( required = true, nillable = true )
	public Expiry getExpiry()
	{
		return m_Expiry;
	}

	public void setExpiry( Expiry expiry )
	{
		m_Expiry = expiry;
	}

	@XmlElement( required = true, nillable = true )
	public Date getStart()
	{
		return m_dStart;
	}

	public void setStart( Date start )
	{
		m_dStart = start;
	}

	@XmlElement( required = true, nillable = true )
	public Date getEnd()
	{
		return m_dEnd;
	}

	public void setEnd( Date end )
	{
		m_dEnd = end;
	}

	public boolean getIsExpired()
	{
		return m_bIsExpired;
	}

	public void setIsExpired( boolean expired )
	{
		m_bIsExpired = expired;
	}
}
