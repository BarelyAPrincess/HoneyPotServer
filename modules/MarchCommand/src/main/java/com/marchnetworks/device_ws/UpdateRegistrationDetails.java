package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "", propOrder = {"serverAddresses"} )
@XmlRootElement( name = "UpdateRegistrationDetails" )
public class UpdateRegistrationDetails
{
	@XmlElement( required = true )
	protected ArrayOfString serverAddresses;

	public ArrayOfString getServerAddresses()
	{
		return serverAddresses;
	}

	public void setServerAddresses( ArrayOfString value )
	{
		serverAddresses = value;
	}
}
