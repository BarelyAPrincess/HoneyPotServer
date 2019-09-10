package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "AlarmSource", propOrder = {"id", "type", "name", "assocIds", "state", "extState"} )
public class AlarmSource
{
	@XmlElement( required = true )
	protected String id;
	@XmlElement( required = true )
	protected String type;
	@XmlElement( required = true )
	protected String name;
	@XmlElement( required = true )
	protected ArrayOfString assocIds;
	@XmlElement( required = true )
	protected String state;
	@XmlElement( required = true )
	protected String extState;

	public String getId()
	{
		return id;
	}

	public void setId( String value )
	{
		id = value;
	}

	public String getType()
	{
		return type;
	}

	public void setType( String value )
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

	public ArrayOfString getAssocIds()
	{
		return assocIds;
	}

	public void setAssocIds( ArrayOfString value )
	{
		assocIds = value;
	}

	public String getState()
	{
		return state;
	}

	public void setState( String value )
	{
		state = value;
	}

	public String getExtState()
	{
		return extState;
	}

	public void setExtState( String value )
	{
		extState = value;
	}
}

