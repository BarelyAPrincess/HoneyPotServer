package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "", propOrder = {"getLicenseInfoResponse"} )
@XmlRootElement( name = "GetLicenseInfoResponse" )
public class GetLicenseInfoResponse
{
	@XmlElement( name = "GetLicenseInfoResponse", required = true )
	protected LicenseInfo getLicenseInfoResponse;

	public LicenseInfo getGetLicenseInfoResponse()
	{
		return getLicenseInfoResponse;
	}

	public void setGetLicenseInfoResponse( LicenseInfo value )
	{
		getLicenseInfoResponse = value;
	}
}
