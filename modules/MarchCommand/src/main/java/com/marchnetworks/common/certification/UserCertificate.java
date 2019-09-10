package com.marchnetworks.common.certification;

import java.security.cert.Certificate;

public class UserCertificate
{
	private Certificate m_Certificate;
	private String m_Alias;

	public UserCertificate( Certificate c, String alias )
	{
		m_Certificate = c;
		m_Alias = alias;
	}

	public Certificate getCertificate()
	{
		return m_Certificate;
	}

	public String getAlias()
	{
		return m_Alias;
	}
}
