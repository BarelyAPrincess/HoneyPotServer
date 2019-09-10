package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "", propOrder = {"certificateId", "challengeValidation"} )
@XmlRootElement( name = "ValidateCertificate" )
public class ValidateCertificate
{
	@XmlElement( required = true )
	protected String certificateId;
	@XmlElement( required = true )
	protected String challengeValidation;

	public String getCertificateId()
	{
		return certificateId;
	}

	public void setCertificateId( String value )
	{
		certificateId = value;
	}

	public String getChallengeValidation()
	{
		return challengeValidation;
	}

	public void setChallengeValidation( String value )
	{
		challengeValidation = value;
	}
}
