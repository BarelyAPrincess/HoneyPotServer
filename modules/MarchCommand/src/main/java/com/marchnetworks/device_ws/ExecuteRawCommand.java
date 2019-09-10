package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "", propOrder = {"command", "params", "data"} )
@XmlRootElement( name = "ExecuteRawCommand" )
public class ExecuteRawCommand
{
	@XmlElement( required = true )
	protected String command;
	@XmlElement( required = true )
	protected ArrayOfPair params;
	@XmlElement( required = true )
	protected String data;

	public String getCommand()
	{
		return command;
	}

	public void setCommand( String value )
	{
		command = value;
	}

	public ArrayOfPair getParams()
	{
		return params;
	}

	public void setParams( ArrayOfPair value )
	{
		params = value;
	}

	public String getData()
	{
		return data;
	}

	public void setData( String value )
	{
		data = value;
	}
}
