package com.marchnetworks.common.service;

import javax.security.auth.x500.X500PrivateCredential;

public abstract interface CertificationService
{
	public abstract void init();

	public abstract byte[] getRootCertCache();

	public abstract String[] signAgentCSRArray( String paramString ) throws Exception;

	public abstract String[] getCommandCertChain() throws Exception;

	public abstract X500PrivateCredential getLicenseSigningAuthority() throws Exception;

	public abstract X500PrivateCredential getCommandCredential();
}
