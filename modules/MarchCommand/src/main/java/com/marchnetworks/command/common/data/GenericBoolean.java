package com.marchnetworks.command.common.data;

public final class GenericBoolean extends GenericValue
{
	private Boolean value;

	public GenericBoolean()
	{
	}

	public GenericBoolean( Boolean value )
	{
		this.value = value;
	}

	public Boolean getValue()
	{
		return value;
	}

	public void setValue( Boolean value )
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
		GenericBoolean other = ( GenericBoolean ) obj;
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
