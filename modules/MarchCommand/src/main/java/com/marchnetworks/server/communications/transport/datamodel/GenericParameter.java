package com.marchnetworks.server.communications.transport.datamodel;

import com.marchnetworks.command.common.transport.data.GenericValue;
import com.marchnetworks.command.common.transport.data.Pair;
import com.marchnetworks.command.common.transport.data.Timestamp;

public class GenericParameter
{
	protected Timestamp timestamp;
	protected String name;
	protected GenericValue value;
	protected String source;
	protected Pair[] info;

	public static GenericParameter newGenericParameter( String name, String value )
	{
		GenericParameter param = newGenericParameter( name );
		param.getValue().setValue( value );
		return param;
	}

	public static GenericParameter newGenericParameter( String name, int value )
	{
		GenericParameter param = newGenericParameter( name );
		param.getValue().setValue( value );
		return param;
	}

	private static GenericParameter newGenericParameter( String name )
	{
		GenericParameter param = new GenericParameter();
		param.setName( name );
		GenericValue genericValue = new GenericValue();
		param.setValue( genericValue );
		return param;
	}

	public Timestamp getTimestamp()
	{
		return timestamp;
	}

	public void setTimestamp( Timestamp value )
	{
		timestamp = value;
	}

	public String getName()
	{
		return name;
	}

	public void setName( String value )
	{
		name = value;
	}

	public GenericValue getValue()
	{
		return value;
	}

	public void setValue( GenericValue value )
	{
		this.value = value;
	}

	public String getSource()
	{
		return source;
	}

	public void setSource( String value )
	{
		source = value;
	}

	public Pair[] getInfo()
	{
		return info;
	}

	public void setInfo( Pair[] value )
	{
		info = value;
	}
}

