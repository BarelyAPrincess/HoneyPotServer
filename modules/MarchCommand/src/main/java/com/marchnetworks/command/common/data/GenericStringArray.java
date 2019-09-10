package com.marchnetworks.command.common.data;

import com.marchnetworks.command.common.CollectionUtils;

import java.util.Arrays;

public class GenericStringArray extends GenericValue
{
	private String[] value;

	public GenericStringArray()
	{
	}

	public GenericStringArray( String[] value )
	{
		this.value = value;
	}

	public String[] getValue()
	{
		return value;
	}

	public void setValue( String[] value )
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
		GenericStringArray other = ( GenericStringArray ) obj;
		if ( !Arrays.equals( value, value ) )
			return false;
		return true;
	}

	public String toString()
	{
		return CollectionUtils.arrayToString( value, ",", true );
	}
}
