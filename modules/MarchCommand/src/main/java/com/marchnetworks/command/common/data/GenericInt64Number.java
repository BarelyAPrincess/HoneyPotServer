package com.marchnetworks.command.common.data;

public final class GenericInt64Number extends GenericValue
{
	private Long value;

	public GenericInt64Number()
	{
	}

	public GenericInt64Number( Long value )
	{
		this.value = value;
	}

	public Long getValue()
	{
		return value;
	}

	public void setValue( Long value )
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
		GenericInt64Number other = ( GenericInt64Number ) obj;
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
