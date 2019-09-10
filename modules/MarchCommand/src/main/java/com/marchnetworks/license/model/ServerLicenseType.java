package com.marchnetworks.license.model;

import com.marchnetworks.license.exception.LicenseException;
import com.marchnetworks.license.exception.LicenseExceptionType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ServerLicenseType
{
	private static final Logger LOG = LoggerFactory.getLogger( ServerLicenseType.class );
	protected LicenseType m_Type;
	protected Expiry m_Expiry;
	protected Date m_dStart;
	protected Date m_dEnd;
	protected int m_iTotal;
	protected List<String> m_sLicenseIDs;
	protected boolean m_bExpired;

	public ServerLicenseType( LicenseType type )
	{
		m_Type = type;
		m_sLicenseIDs = new ArrayList();
		m_iTotal = 0;
		m_Expiry = null;
		m_dStart = null;
		m_dEnd = null;
		m_bExpired = false;
	}

	public LicenseType getType()
	{
		return m_Type;
	}

	public Expiry getExpiry()
	{
		return m_Expiry;
	}

	public Date getStart()
	{
		return m_dStart;
	}

	public Date getEnd()
	{
		return m_dEnd;
	}

	public int getTotal()
	{
		return m_iTotal;
	}

	public boolean expired()
	{
		return m_bExpired;
	}

	public void setExpired( boolean b )
	{
		m_bExpired = b;
	}

	public boolean isEmpty()
	{
		return m_Expiry == null;
	}

	public ServerLicenseInfo toInfo( int inUse )
	{
		ServerLicenseInfo Result = new ServerLicenseInfo();
		Result.setType( m_Type );
		Result.setTotal( m_iTotal );
		Result.setAllocated( inUse );

		if ( !isEmpty() )
		{
			Result.setExpiry( m_Expiry );
			if ( m_Expiry == Expiry.TRIAL )
			{
				Result.setStart( m_dStart );
				Result.setEnd( m_dEnd );
			}
		}
		return Result;
	}

	public void checkLoad( RecordingLicenseImport slit, int InUseCount, Date today ) throws LicenseException
	{
		if ( slit.getType() != m_Type )
		{
			throw new LicenseException( "Different license type", LicenseExceptionType.LICENSE_TYPE_INCOMPATIBLE );
		}

		if ( m_sLicenseIDs.contains( slit.getLicenseId() ) )
		{
			throw new LicenseException( "License already loaded", LicenseExceptionType.LICENSE_ALREADY_ADDED );
		}

		checkExpiry( slit, today );

		if ( m_Expiry != null )
		{

			if ( m_Expiry == Expiry.TRIAL )
			{

				if ( slit.getCount() < InUseCount )
				{
					LicenseExceptionType exceptionType = m_Type == LicenseType.RECORDER ? LicenseExceptionType.LICENSE_COUNT_RECORDER : LicenseExceptionType.LICENSE_COUNT_CHANNEL;
					throw new LicenseException( "Type=" + m_Type + " Not enough licenses. NewCount(" + slit.getCount() + ") < CurrentInUse(" + InUseCount + ")", exceptionType );
				}
			}
			else if ( m_Expiry == Expiry.PERMANENT )
			{
				if ( slit.getExpiry() == Expiry.TRIAL )
				{
					throw new LicenseException( "Cannot add trial license to existing permanent license", LicenseExceptionType.LICENSE_TRIAL_OVER_PERMANENT );
				}

				if ( slit.getExpiry() != Expiry.PERMANENT )
				{
				}

			}
			else
			{
				LOG.error( "Critical Error: No handler written for license expiry: {}", m_Expiry );
			}
		}
	}

	public void checkExpiry( RecordingLicenseImport slit, Date today ) throws LicenseException
	{
		if ( slit.getExpiry() == Expiry.TRIAL )
		{
			if ( today != null )
			{
				if ( today.after( slit.getEnd() ) )
					throw new LicenseException( "Trial license is expired", LicenseExceptionType.LICENSE_EXPIRED );
				if ( today.before( slit.getStart() ) )
				{
					throw new LicenseException( "Trial license is not valid yet", LicenseExceptionType.LICENSE_NOT_YET_VALID );
				}
			}
		}
		else if ( slit.getExpiry() != Expiry.PERMANENT )
		{

			LOG.error( "Critical Error: No handler written for license expiry: {}", slit.getExpiry() );
		}
	}

	public void load( RecordingLicenseImport slit, int InUseCount, Date today ) throws LicenseException
	{
		checkLoad( slit, InUseCount, today );

		if ( m_Expiry == null )
		{
			m_iTotal = slit.getCount();
			m_Expiry = slit.getExpiry();
			m_dStart = slit.getStart();
			m_dEnd = slit.getEnd();
			m_sLicenseIDs.add( slit.getLicenseId() );

		}
		else if ( m_Expiry == Expiry.TRIAL )
		{
			m_iTotal = slit.getCount();
			m_Expiry = slit.getExpiry();
			m_dStart = slit.getStart();
			m_dEnd = slit.getEnd();

			m_sLicenseIDs.clear();
			m_sLicenseIDs.add( slit.getLicenseId() );
		}
		else if ( m_Expiry == Expiry.PERMANENT )
		{

			if ( slit.getExpiry() == Expiry.PERMANENT )
			{
				m_iTotal += slit.getCount();
				m_sLicenseIDs.add( slit.getLicenseId() );
			}
		}

		m_bExpired = false;
	}
}
