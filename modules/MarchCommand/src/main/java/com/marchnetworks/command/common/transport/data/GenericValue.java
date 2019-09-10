package com.marchnetworks.command.common.transport.data;

public class GenericValue
{
	protected Object value;

	private short type;

	public static final short GENERIC_INT = 0;

	public static final short GENERIC_DOUBLE = 1;

	public static final short GENERIC_LONG = 2;

	public static final short GENERIC_BOOLEAN = 3;

	public static final short GENERIC_STRING = 4;

	public static final short GENERIC_NULL = 5;

	public GenericValue()
	{
		type = 5;
	}

	public short getType()
	{
		return type;
	}

	public void setValue( int intValue )
	{
		value = Integer.valueOf( intValue );
		type = 0;
	}

	public void setValue( double doubleValue )
	{
		value = Double.valueOf( doubleValue );
		type = 1;
	}

	public void setValue( long longValue )
	{
		value = Long.valueOf( longValue );
		type = 2;
	}

	public void setValue( boolean boolValue )
	{
		value = Boolean.valueOf( boolValue );
		type = 3;
	}

	public void setValue( String stringValue )
	{
		value = stringValue;
		type = 4;
	}

	public int getIntValue()
	{
		return ( ( Integer ) value ).intValue();
	}

	public double getDoubleValue()
	{
		return ( ( Double ) value ).doubleValue();
	}

	public long getLongValue()
	{
		return ( ( Long ) value ).longValue();
	}

	public boolean getBooleanValue()
	{
		return ( ( Boolean ) value ).booleanValue();
	}

	public String getStringValue()
	{
		return ( String ) value;
	}

	public String convertToString()
	{
		return String.valueOf( value );
	}

	public Object getValue()
	{
		return value;
	}
}
