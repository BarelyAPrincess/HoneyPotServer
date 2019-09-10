package com.marchnetworks.device_ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "", propOrder = {"certificateIds"} )
@XmlRootElement( name = "GetCertificateChallenges" )
public class GetCertificateChallenges
{
	@XmlElement( required = true )
	protected ArrayOfString certificateIds;

	public ArrayOfString getCertificateIds()
	{
		return certificateIds;
	}

	public void setCertificateIds( ArrayOfString value )
	{
		certificateIds = value;
	}
}
