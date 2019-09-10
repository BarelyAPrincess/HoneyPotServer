package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "", propOrder = {"command", "params"} )
@XmlRootElement( name = "ExecuteFailsafeCommand" )
public class ExecuteFailsafeCommand
{
	@XmlElement( required = true )
	protected String command;
	@XmlElement( required = true )
	protected String params;

	public String getCommand()
	{
		return command;
	}

	public void setCommand( String value )
	{
		command = value;
	}

	public String getParams()
	{
		return params;
	}

	public void setParams( String value )
	{
		params = value;
	}
}
