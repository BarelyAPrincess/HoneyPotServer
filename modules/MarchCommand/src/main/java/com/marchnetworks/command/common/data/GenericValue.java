package com.marchnetworks.command.common.data;

import java.math.BigDecimal;

public abstract class GenericValue
{
	public abstract Object getValue();

	public static GenericValue newGenericValue( Object value )
	{
		if ( value == null )
		{
			return null;
		}
		if ( ( value instanceof Boolean ) )
		{
			return new GenericBoolean( ( Boolean ) value );
		}
		if ( ( value instanceof BigDecimal ) )
		{
			return new GenericDecimalNumber( ( BigDecimal ) value );
		}
		if ( ( value instanceof Double ) )
		{
			return new GenericDecimalNumber( ( Double ) value );
		}
		if ( ( value instanceof Integer ) )
		{
			return new GenericInt32Number( ( Integer ) value );
		}
		if ( ( value instanceof Long ) )
		{
			return new GenericInt64Number( ( Long ) value );
		}
		if ( ( value instanceof String ) )
		{
			return new GenericString( ( String ) value );
		}
		if ( ( value instanceof Long[] ) )
		{
			return new GenericLongArray( ( Long[] ) value );
		}
		if ( ( value instanceof String[] ) )
		{
			return new GenericStringArray( ( String[] ) value );
		}

		throw new IllegalArgumentException( "Object is not of a supported type" );
	}

	public String toString()
	{
		return getValue().toString();
	}
}
