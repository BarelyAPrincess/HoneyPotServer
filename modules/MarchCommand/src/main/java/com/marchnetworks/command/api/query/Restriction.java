package com.marchnetworks.command.api.query;

import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Restriction
{
	private List<String> propertyPath = new ArrayList( 1 );
	private Object value;
	private RestrictionType type;
	private Criteria criteria;
	private String[] subPaths;

	public Restriction( String name, Object value, RestrictionType type )
	{
		if ( CommonAppUtils.isNullOrEmptyString( name ) )
		{
			throw new IllegalArgumentException( "Parameter name must not be null or empty" );
		}
		if ( ( ( type == RestrictionType.IN ) || ( type == RestrictionType.CONTAINS ) || ( type == RestrictionType.NOT_IN ) ) && ( value == null ) )
		{
			throw new IllegalArgumentException( "Parameter value must not be null for " + type );
		}
		String[] properties = name.split( "\\." );

		subPaths = new String[properties.length];

		for ( int i = 0; i < properties.length; i++ )
		{
			propertyPath.add( properties[i] );
			String path = "";
			for ( int j = 0; j <= i; j++ )
			{
				path = path + properties[j];
				if ( j < i )
				{
					path = path + ".";
				}
			}
			subPaths[i] = path;
		}
		this.value = value;
		this.type = type;
	}

	public boolean match( Object obj )
	{
		Object valueToMatch = obj;
		for ( int i = 0; i < propertyPath.size(); i++ )
		{
			String path = subPaths[i];
			Field field = criteria.getField( path, ( String ) propertyPath.get( i ), valueToMatch.getClass() );
			valueToMatch = ReflectionUtils.getSpecifiedFieldValue( field, valueToMatch );

			if ( ( i < propertyPath.size() - 1 ) && ( valueToMatch == null ) )
			{
				return false;
			}
		}

		if ( type == RestrictionType.EQUAL )
			return isEqual( value, valueToMatch );
		if ( type == RestrictionType.GREATER_THAN )
			return greaterThan( value, valueToMatch );
		if ( type == RestrictionType.LESS_THAN )
			return lessThan( value, valueToMatch );
		if ( type == RestrictionType.NOT_EQUAL )
			return !isEqual( value, valueToMatch );
		if ( type == RestrictionType.IN )
		{

			if ( ( value instanceof Collection ) )
			{
				return ( ( Collection ) value ).contains( valueToMatch );
			}
			for ( Object element : ( Object[] ) value )
			{
				if ( isEqual( element, valueToMatch ) )
				{
					return true;
				}
			}

			return false;
		}
		if ( type == RestrictionType.NOT_IN )
		{
			return !( ( Collection ) value ).contains( valueToMatch );
		}
		if ( type == RestrictionType.IS_NOT_EMPTY )
		{
			if ( valueToMatch != null )
			{
				if ( ( valueToMatch instanceof Collection ) )
					return !( ( Collection ) valueToMatch ).isEmpty();
				if ( valueToMatch.getClass().isArray() )
				{
					return ( ( Object[] ) valueToMatch ).length != 0;
				}
			}
		}
		else if ( type == RestrictionType.CONTAINS )
		{

			if ( valueToMatch != null )
			{
				if ( ( valueToMatch instanceof Collection ) )
					return ( ( Collection ) valueToMatch ).contains( value );
				if ( valueToMatch.getClass().isArray() )
				{
					for ( Object element : ( Object[] ) valueToMatch )
					{
						if ( isEqual( element, value ) )
						{
							return true;
						}
					}
				}
				return false;
			}
		}
		return false;
	}

	private boolean isEqual( Object value1, Object value2 )
	{
		if ( ( value1 == null ) && ( value2 == null ) )
		{
			return true;
		}
		if ( ( value1 == null ) && ( value2 != null ) )
		{
			return false;
		}
		return value1.equals( value2 );
	}

	private boolean greaterThan( Object value1, Object value2 )
	{
		if ( ( value1 == null ) || ( value2 == null ) )
		{
			return false;
		}
		return compareObjects( value2, value1 ) > 0;
	}

	private boolean lessThan( Object value1, Object value2 )
	{
		if ( ( value1 == null ) || ( value2 == null ) )
		{
			return false;
		}
		return compareObjects( value2, value1 ) < 0;
	}

	private int compareObjects( Object value1, Object value2 )
	{
		return ( ( Comparable ) value1 ).compareTo( ( Comparable ) value2 );
	}

	public void setCriteria( Criteria criteria )
	{
		this.criteria = criteria;
	}
}
