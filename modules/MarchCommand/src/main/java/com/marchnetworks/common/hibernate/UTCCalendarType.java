package com.marchnetworks.common.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.TimeZone;

public class UTCCalendarType implements UserType
{
	private static final int[] SQL_TYPES = {93};

	public int[] sqlTypes()
	{
		return SQL_TYPES;
	}

	public Class returnedClass()
	{
		return Calendar.class;
	}

	public boolean equals( Object x, Object y ) throws HibernateException
	{
		if ( x == y )
			return true;
		if ( ( x == null ) || ( y == null ) )
		{
			return false;
		}
		return x.equals( y );
	}

	public Object nullSafeGet( ResultSet resultSet, String[] names, Object owner ) throws HibernateException, SQLException
	{
		Calendar result = null;

		Timestamp timestamp = resultSet.getTimestamp( names[0], Calendar.getInstance( TimeZone.getTimeZone( "UTC" ) ) );

		if ( ( !resultSet.wasNull() ) && ( timestamp != null ) )
		{
			result = Calendar.getInstance();
			result.setTimeInMillis( timestamp.getTime() );
		}

		return result;
	}

	public void nullSafeSet( PreparedStatement statement, Object value, int index ) throws HibernateException, SQLException
	{
		if ( value == null )
		{
			statement.setTimestamp( index, null );
		}
		else
		{
			Calendar cal = ( Calendar ) value;
			Timestamp timestamp = new Timestamp( cal.getTimeInMillis() );
			statement.setTimestamp( index, timestamp, Calendar.getInstance( TimeZone.getTimeZone( "UTC" ) ) );
		}
	}

	public Object deepCopy( Object value ) throws HibernateException
	{
		return value;
	}

	public boolean isMutable()
	{
		return false;
	}

	public Object assemble( Serializable cached, Object owner ) throws HibernateException
	{
		return cached;
	}

	public Serializable disassemble( Object value ) throws HibernateException
	{
		return ( Serializable ) value;
	}

	public Object replace( Object original, Object target, Object owner ) throws HibernateException
	{
		return original;
	}

	public int hashCode( Object x ) throws HibernateException
	{
		return x.hashCode();
	}
}
