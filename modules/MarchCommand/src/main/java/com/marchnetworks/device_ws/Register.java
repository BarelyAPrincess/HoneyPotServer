package com.marchnetworks.device_ws;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "", propOrder = {"serverAddress", "serverPath", "deviceId", "serverHostname", "serverAddresses"} )
@XmlRootElement( name = "Register" )
public class Register
{
	@XmlElementRef( name = "serverAddress", namespace = "http://marchnetworks.com/device_ws/", type = JAXBElement.class, required = false )
	protected JAXBElement<ArrayOfString> serverAddress;
	@XmlElement( required = true )
	protected String serverPath;
	@XmlElement( required = true )
	protected String deviceId;
	@XmlElementRef( name = "serverHostname", namespace = "http://marchnetworks.com/device_ws/", type = JAXBElement.class, required = false )
	protected JAXBElement<ArrayOfString> serverHostname;
	@XmlElementRef( name = "serverAddresses", namespace = "http://marchnetworks.com/device_ws/", type = JAXBElement.class, required = false )
	protected JAXBElement<ArrayOfString> serverAddresses;

	public JAXBElement<ArrayOfString> getServerAddress()
	{
		return serverAddress;
	}

	public void setServerAddress( JAXBElement<ArrayOfString> value )
	{
		serverAddress = value;
	}

	public String getServerPath()
	{
		return serverPath;
	}

	public void setServerPath( String value )
	{
		serverPath = value;
	}

	public String getDeviceId()
	{
		return deviceId;
	}

	public void setDeviceId( String value )
	{
		deviceId = value;
	}

	public JAXBElement<ArrayOfString> getServerHostname()
	{
		return serverHostname;
	}

	public void setServerHostname( JAXBElement<ArrayOfString> value )
	{
		serverHostname = value;
	}

	public JAXBElement<ArrayOfString> getServerAddresses()
	{
		return serverAddresses;
	}

	public void setServerAddresses( JAXBElement<ArrayOfString> value )
	{
		serverAddresses = value;
	}
}
