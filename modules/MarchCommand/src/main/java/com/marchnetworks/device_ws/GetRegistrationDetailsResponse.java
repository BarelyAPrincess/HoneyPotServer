package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "", propOrder = {"getRegistrationDetailsResult"} )
@XmlRootElement( name = "GetRegistrationDetailsResponse" )
public class GetRegistrationDetailsResponse
{
	@XmlElement( name = "GetRegistrationDetailsResult", required = true )
	protected RegistrationDetails getRegistrationDetailsResult;

	public RegistrationDetails getGetRegistrationDetailsResult()
	{
		return getRegistrationDetailsResult;
	}

	public void setGetRegistrationDetailsResult( RegistrationDetails value )
	{
		getRegistrationDetailsResult = value;
	}
}
