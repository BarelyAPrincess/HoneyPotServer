package com.marchnetworks.command.common.data;

import java.math.BigDecimal;

public class GenericDecimalNumber extends GenericValue
{
	private BigDecimal value;

	public GenericDecimalNumber()
	{
	}

	public GenericDecimalNumber( BigDecimal value )
	{
		this.value = value;
	}

	public GenericDecimalNumber( Double value )
	{
		this.value = new BigDecimal( value.doubleValue() );
	}

	public BigDecimal getValue()
	{
		return value;
	}

	public void setValue( BigDecimal value )
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
		GenericDecimalNumber other = ( GenericDecimalNumber ) obj;
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
