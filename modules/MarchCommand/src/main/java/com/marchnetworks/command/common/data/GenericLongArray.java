package com.marchnetworks.command.common.data;

import com.marchnetworks.command.common.CollectionUtils;

import java.util.Arrays;

public class GenericLongArray extends GenericValue
{
	private Long[] value;

	public GenericLongArray()
	{
	}

	public GenericLongArray( Long[] array )
	{
		value = array;
	}

	public Long[] getValue()
	{
		return value;
	}

	public void setValue( Long[] value )
	{
		this.value = value;
	}

	public int hashCode()
	{
		int prime = 31;
		int result = 1;
		result = 31 * result + Arrays.hashCode( value );
		return result;
	}

	public boolean equals( Object obj )
	{
		if ( this == obj )
			return true;
		if ( obj == null )
			return false;
		if ( getClass() != obj.getClass() )
			return false;
		GenericLongArray other = ( GenericLongArray ) obj;
		if ( !Arrays.equals( value, value ) )
			return false;
		return true;
	}

	public String toString()
	{
		return CollectionUtils.arrayToString( value, ",", true );
	}
}
