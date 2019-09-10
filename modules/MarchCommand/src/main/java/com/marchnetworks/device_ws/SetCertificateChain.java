package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "", propOrder = {"serverCertificatesForDevice"} )
@XmlRootElement( name = "SetCertificateChain" )
public class SetCertificateChain
{
	@XmlElement( required = true )
	protected ArrayOfString serverCertificatesForDevice;

	public ArrayOfString getServerCertificatesForDevice()
	{
		return serverCertificatesForDevice;
	}

	public void setServerCertificatesForDevice( ArrayOfString value )
	{
		serverCertificatesForDevice = value;
	}
}
