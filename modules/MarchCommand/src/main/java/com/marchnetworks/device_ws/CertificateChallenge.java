package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "CertificateChallenge", propOrder = {"certificateId", "challenge"} )
public class CertificateChallenge
{
	@XmlElement( required = true )
	protected String certificateId;
	@XmlElement( required = true )
	protected String challenge;

	public String getCertificateId()
	{
		return certificateId;
	}

	public void setCertificateId( String value )
	{
		certificateId = value;
	}

	public String getChallenge()
	{
		return challenge;
	}

	public void setChallenge( String value )
	{
		challenge = value;
	}
}
