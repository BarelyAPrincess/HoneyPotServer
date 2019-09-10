package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "AlertEntry", propOrder = {"id", "state", "alertCode", "source", "value", "info", "count", "durationCount", "frequencyCount", "first", "last", "lastResolved"} )
public class AlertEntry
{
	@XmlElement( required = true )
	protected String id;
	@XmlElement( required = true )
	protected String state;
	@XmlElement( required = true )
	protected String alertCode;
	@XmlElement( required = true )
	protected String source;
	@XmlElement( required = true )
	protected GenericValue value;
	@XmlElement( required = true )
	protected ArrayOfPair info;
	protected int count;
	protected int durationCount;
	protected int frequencyCount;
	@XmlElement( required = true )
	protected Timestamp first;
	@XmlElement( required = true )
	protected Timestamp last;
	@XmlElement( required = true )
	protected Timestamp lastResolved;

	public String getId()
	{
		return id;
	}

	public void setId( String value )
	{
		id = value;
	}

	public String getState()
	{
		return state;
	}

	public void setState( String value )
	{
		state = value;
	}

	public String getAlertCode()
	{
		return alertCode;
	}

	public void setAlertCode( String value )
	{
		alertCode = value;
	}

	public String getSource()
	{
		return source;
	}

	public void setSource( String value )
	{
		source = value;
	}

	public GenericValue getValue()
	{
		return value;
	}

	public void setValue( GenericValue value )
	{
		this.value = value;
	}

	public ArrayOfPair getInfo()
	{
		return info;
	}

	public void setInfo( ArrayOfPair value )
	{
		info = value;
	}

	public int getCount()
	{
		return count;
	}

	public void setCount( int value )
	{
		count = value;
	}

	public int getDurationCount()
	{
		return durationCount;
	}

	public void setDurationCount( int value )
	{
		durationCount = value;
	}

	public int getFrequencyCount()
	{
		return frequencyCount;
	}

	public void setFrequencyCount( int value )
	{
		frequencyCount = value;
	}

	public Timestamp getFirst()
	{
		return first;
	}

	public void setFirst( Timestamp value )
	{
		first = value;
	}

	public Timestamp getLast()
	{
		return last;
	}

	public void setLast( Timestamp value )
	{
		last = value;
	}

	public Timestamp getLastResolved()
	{
		return lastResolved;
	}

	public void setLastResolved( Timestamp value )
	{
		lastResolved = value;
	}
}
