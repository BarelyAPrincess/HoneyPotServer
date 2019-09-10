package com.marchnetworks.command.common.data;

public final class GenericInt32Number extends GenericValue
{
	private Integer value;

	public GenericInt32Number()
	{
	}

	public GenericInt32Number( Integer value )
	{
		this.value = value;
	}

	public Integer getValue()
	{
		return value;
	}

	public void setValue( Integer value )
	{
		this.value = value;
	}

	public int hashCode()
	{
		int prime = 31;
		int result = 1;
		result = 31 * result + ( value == null ? 0 : value.hashCode() );
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
		GenericInt32Number other = ( GenericInt32Number ) obj;
		if ( value == null )
		{
			if ( value != null )
				return false;
		}
		else if ( !value.equals( value ) )
			return false;
		return true;
	}
}
