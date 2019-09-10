package com.marchnetworks.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class WarningTracker
{
	private static final Logger LOG = LoggerFactory.getLogger( WarningTracker.class );

	protected Date m_dEnd;

	protected int[] m_iWarningDays;

	protected int m_LastWarningDay;

	public WarningTracker( int[] warningDays, Date end ) throws Exception
	{
		m_dEnd = end;

		if ( warningDays.length < 1 )
		{
			throw new Exception( "Invalid warningDays setup" );
		}

		for ( int i = 0; i < warningDays.length - 1; i++ )
		{
			if ( warningDays[i] <= warningDays[( i + 1 )] )
			{
				throw new Exception( "Invalid warningDays setup" );
			}
		}
		m_iWarningDays = warningDays;
		m_LastWarningDay = -1;
	}

	public boolean check( Date now )
	{
		int days = daysBetweenDates( now, m_dEnd );
		if ( ( days <= 0 ) && ( m_LastWarningDay != 0 ) )
		{
			m_LastWarningDay = 0;
			return true;
		}

		int nextWarning = nextWarningDays( m_LastWarningDay - 1 );
		if ( days <= nextWarning )
		{
			m_LastWarningDay = days;
			return true;
		}
		return false;
	}

	public int getDaysLeft()
	{
		return m_LastWarningDay;
	}

	protected int nextWarningDays( int a )
	{
		if ( a < 0 )
		{
			return m_iWarningDays[0];
		}

		int result = 0;
		for ( int i = 0; i < m_iWarningDays.length; i++ )
		{
			if ( m_iWarningDays[i] <= a )
			{
				result = m_iWarningDays[i];
				break;
			}
		}

		LOG.debug( "nextWarningDays: daysLeft={}, array={}: ==> nextWarningDay={}", new Object[] {Integer.valueOf( a ), warningDaysString(), Integer.valueOf( result )} );

		return result;
	}

	protected int daysBetweenDates( Date d1, Date d2 )
	{
		Calendar c1 = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();
		c1.setTime( d1 );
		c2.setTime( d2 );

		boolean negative = false;
		long dMS;
		if ( d2.after( d1 ) )
		{
			dMS = c2.getTimeInMillis() - c1.getTimeInMillis();
		}
		else if ( d1.after( d2 ) )
		{
			dMS = c1.getTimeInMillis() - c2.getTimeInMillis();
			negative = true;
		}
		else
		{
			dMS = 0L;
		}

		long dDays = TimeUnit.DAYS.convert( dMS, TimeUnit.MILLISECONDS );
		int result;
		if ( negative )
		{
			result = ( int ) -dDays;
		}
		else
		{
			result = ( int ) dDays;
		}

		if ( LOG.isDebugEnabled() )
		{
			DateFormat df = DateFormat.getDateTimeInstance();
			LOG.debug( "daysBetweenDates: [" + df.format( d1 ) + "],[" + df.format( d2 ) + "] = " + result );
		}
		return result;
	}

	protected String warningDaysString()
	{
		StringBuffer Result = new StringBuffer( "[" );

		for ( int i = 0; i < m_iWarningDays.length; i++ )
		{
			Result.append( m_iWarningDays[i] );
		}
		Result.append( "]" );
		return Result.toString();
	}
}

