package com.marchnetworks.command.common.data;

public class GenericString extends GenericValue
{
	private String value;

	public GenericString()
	{
	}

	public GenericString( String value )
	{
		this.value = value;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue( String value )
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
		GenericString other = ( GenericString ) obj;
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
