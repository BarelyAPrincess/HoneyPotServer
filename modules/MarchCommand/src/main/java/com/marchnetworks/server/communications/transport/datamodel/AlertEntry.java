package com.marchnetworks.server.communications.transport.datamodel;

import com.marchnetworks.command.common.transport.data.Timestamp;

public class AlertEntry
{
	protected String id;
	protected String state;
	protected String alertCode;
	protected String source;
	protected com.marchnetworks.command.common.transport.data.GenericValue value;
	protected com.marchnetworks.command.common.transport.data.Pair[] info;

	public com.marchnetworks.command.common.transport.data.GenericValue getValue()
	{
		return value;
	}

	public void setValue( com.marchnetworks.command.common.transport.data.GenericValue value )
	{
		this.value = value;
	}

	protected int count;

	protected int durationCount;
	protected int frequencyCount;
	protected Timestamp first;
	protected Timestamp last;
	protected Timestamp lastResolved;

	public String getId()
	{
		return id;
	}

	public void setId( String id )
	{
		this.id = id;
	}

	public String getState()
	{
		return state;
	}

	public void setState( String state )
	{
		this.state = state;
	}

	public String getAlertCode()
	{
		return alertCode;
	}

	public void setAlertCode( String alertCode )
	{
		this.alertCode = alertCode;
	}

	public String getSource()
	{
		return source;
	}

	public void setSource( String source )
	{
		this.source = source;
	}

	public com.marchnetworks.command.common.transport.data.Pair[] getInfo()
	{
		return info;
	}

	public void setInfo( com.marchnetworks.command.common.transport.data.Pair[] info )
	{
		this.info = info;
	}

	public int getCount()
	{
		return count;
	}

	public void setCount( int count )
	{
		this.count = count;
	}

	public int getDurationCount()
	{
		return durationCount;
	}

	public void setDurationCount( int durationCount )
	{
		this.durationCount = durationCount;
	}

	public int getFrequencyCount()
	{
		return frequencyCount;
	}

	public void setFrequencyCount( int frequencyCount )
	{
		this.frequencyCount = frequencyCount;
	}

	public Timestamp getFirst()
	{
		return first;
	}

	public void setFirst( Timestamp first )
	{
		this.first = first;
	}

	public Timestamp getLast()
	{
		return last;
	}

	public void setLast( Timestamp last )
	{
		this.last = last;
	}

	public Timestamp getLastResolved()
	{
		return lastResolved;
	}

	public void setLastResolved( Timestamp lastResolved )
	{
		this.lastResolved = lastResolved;
	}
}

