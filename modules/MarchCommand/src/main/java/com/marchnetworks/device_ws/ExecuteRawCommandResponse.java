package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "", propOrder = {"executeRawCommandResult"} )
@XmlRootElement( name = "ExecuteRawCommandResponse" )
public class ExecuteRawCommandResponse
{
	@XmlElement( name = "ExecuteRawCommandResult" )
	protected String executeRawCommandResult;

	public String getExecuteRawCommandResult()
	{
		return executeRawCommandResult;
	}

	public void setExecuteRawCommandResult( String value )
	{
		executeRawCommandResult = value;
	}
}
