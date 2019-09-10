package com.marchnetworks.command.common.transport.data;

public class Event
{
	protected Timestamp timestamp;

	protected long id;

	protected EventType type;

	protected String name;

	protected GenericValue value;

	protected String source;

	protected Pair[] info;

	public Timestamp getTimestamp()
	{
		return timestamp;
	}

	public void setTimestamp( Timestamp value )
	{
		timestamp = value;
	}

	public long getId()
	{
		return id;
	}

	public void setId( long value )
	{
		id = value;
	}

	public EventType getType()
	{
		return type;
	}

	public void setType( EventType value )
	{
		type = value;
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
