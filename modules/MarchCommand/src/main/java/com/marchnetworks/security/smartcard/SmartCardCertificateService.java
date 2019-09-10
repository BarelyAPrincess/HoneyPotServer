package com.marchnetworks.security.smartcard;

import java.security.cert.X509Certificate;

public abstract interface SmartCardCertificateService
{
	public abstract X509Certificate loadCertificate( byte[] paramArrayOfByte );

	public abstract byte[] convertCertId( byte[] paramArrayOfByte );
}

