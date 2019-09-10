package com.marchnetworks.device_ws;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "ArrayOfCertificateChallenge", propOrder = {"challenge"} )
public class ArrayOfCertificateChallenge
{
	protected List<CertificateChallenge> challenge;

	public List<CertificateChallenge> getChallenge()
	{
		if ( challenge == null )
		{
			challenge = new ArrayList();
		}
		return challenge;
	}
}
