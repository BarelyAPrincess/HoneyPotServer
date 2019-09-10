package com.marchnetworks.common.service;

import com.marchnetworks.common.certification.CertificationCreator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.x500.X500PrivateCredential;

public class CertificationServiceImpl implements CertificationService
{
	private static final Logger LOG = LoggerFactory.getLogger( CertificationService.class );

	private CertificationCreator m_certCreator;

	public CertificationServiceImpl()
	{
		m_certCreator = null;
	}

	public void init()
	{
		m_certCreator = new CertificationCreator();
		try
		{
			m_certCreator.LoadDefaults();
		}
		catch ( Exception e )
		{
			LOG.error( "Error initializing: ", e );
		}
	}

	public byte[] getRootCertCache()
	{
		return m_certCreator.getRootCertCache();
	}

	public String[] signAgentCSRArray( String CSRPem ) throws Exception
	{
		return m_certCreator.signAgentCSRArray( CSRPem );
	}

	public String[] getCommandCertChain() throws Exception
	{
		return m_certCreator.getCommandCertChain();
	}

	public X500PrivateCredential getLicenseSigningAuthority() throws Exception
	{
		return m_certCreator.getCommandCredential();
	}

	public X500PrivateCredential getCommandCredential()
	{
		return m_certCreator.getCommandCredential();
	}
}
