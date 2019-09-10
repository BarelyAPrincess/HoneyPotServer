package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "GenericParameter", propOrder = {"timestamp", "name", "value", "source", "info"} )
public class GenericParameter
{
	@XmlElement( required = true )
	protected Timestamp timestamp;
	@XmlElement( required = true )
	protected String name;
	@XmlElement( required = true )
	protected GenericValue value;
	@XmlElement( required = true )
	protected String source;
	@XmlElement( required = true )
	protected ArrayOfPair info;

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

	public ArrayOfPair getInfo()
	{
		return info;
	}

	public void setInfo( ArrayOfPair value )
	{
		info = value;
	}
}
