package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "", propOrder = {"registerResult"} )
@XmlRootElement( name = "RegisterResponse" )
public class RegisterResponse
{
	@XmlElement( name = "RegisterResult", required = true )
	protected String registerResult;

	public String getRegisterResult()
	{
		return registerResult;
	}

	public void setRegisterResult( String value )
	{
		registerResult = value;
	}
}
