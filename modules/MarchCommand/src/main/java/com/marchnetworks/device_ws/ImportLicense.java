package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "", propOrder = {"licenseXml"} )
@XmlRootElement( name = "ImportLicense" )
public class ImportLicense
{
	@XmlElement( name = "LicenseXml", required = true )
	protected String licenseXml;

	public String getLicenseXml()
	{
		return licenseXml;
	}

	public void setLicenseXml( String value )
	{
		licenseXml = value;
	}
}
