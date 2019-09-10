package com.marchnetworks.security.smartcard;

import org.bouncycastle.util.Arrays;
import org.springframework.security.core.Authentication;

class CertificateValidationSession
{
	private byte[] sessionId;
	private byte[] certId;
	private byte[] validationString;
	private Authentication auth;

	public CertificateValidationSession()
	{
	}

	public CertificateValidationSession( byte[] aSessionId, byte[] aCertId, byte[] aValidationString )
	{
		sessionId = aSessionId;
		certId = aCertId;
		validationString = aValidationString;
	}

	public byte[] getSessionId()
	{
		return sessionId;
	}

	public void setSessionId( byte[] sessionId )
	{
		this.sessionId = sessionId;
	}

	public byte[] getCertId()
	{
		return certId;
	}

	public void setCertId( byte[] certId )
	{
		this.certId = certId;
	}

	public byte[] getValidationString()
	{
		return validationString;
	}

	public void setValidationString( byte[] validationString )
	{
		this.validationString = validationString;
	}

	public boolean equals( Object aSession )
	{
		if ( aSession == null )
			return false;
		if ( aSession == this )
			return true;
		if ( !( aSession instanceof CertificateValidationSession ) )
		{
			return false;
		}
		CertificateValidationSession aSessionCasted = ( CertificateValidationSession ) aSession;

		if ( ( Arrays.areEqual( certId, certId ) ) && ( Arrays.areEqual( validationString, validationString ) ) && ( Arrays.areEqual( sessionId, sessionId ) ) )
		{
			return true;
		}
		return false;
	}

	public Authentication getAuthentication()
	{
		return auth;
	}

	public void setAuthentication( Authentication auth )
	{
		this.auth = auth;
	}
}

