package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "", propOrder = {"challenges"} )
@XmlRootElement( name = "GetCertificateChallengesResponse" )
public class GetCertificateChallengesResponse
{
	@XmlElement( required = true )
	protected ArrayOfCertificateChallenge challenges;

	public ArrayOfCertificateChallenge getChallenges()
	{
		return challenges;
	}

	public void setChallenges( ArrayOfCertificateChallenge value )
	{
		challenges = value;
	}
}
