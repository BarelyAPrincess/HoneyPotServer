/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.http.ssl;

import com.google.common.base.Charsets;

import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.util.io.pem.PemReader;
import org.joda.time.Days;
import org.joda.time.LocalDate;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.amelia.support.Voluntary;
import io.amelia.lang.NetworkException;
import io.amelia.networking.Networking;
import io.amelia.support.Encrypt;
import io.amelia.support.Exceptions;
import io.amelia.support.IO;
import io.amelia.support.Objs;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

public class CertificateWrapper
{
	public enum CertificateValidityState
	{
		Valid,
		NotYetValid,
		Expired
	}

	private final Path sslCertFile;
	private final Path sslKeyFile;
	private final String sslSecret;

	private final X509Certificate certificate;
	private final PrivateKey privateKey;
	private SslContext context = null;

	public CertificateWrapper( Path sslCertFile, Path sslKeyFile, String sslSecret ) throws IOException, CertificateException, InvalidKeySpecException
	{
		if ( !Files.isRegularFile( sslCertFile ) )
			throw new FileNotFoundException( "The SSL Certificate '" + IO.relPath( sslCertFile ) + "' (aka. SSL Cert) does not exist or is a directory." );

		if ( !Files.isRegularFile( sslKeyFile ) )
			throw new FileNotFoundException( "The SSL Key '" + IO.relPath( sslKeyFile ) + "' (aka. SSL Key) does not exist or is a directory." );

		this.sslCertFile = sslCertFile;
		this.sslKeyFile = sslKeyFile;
		this.sslSecret = sslSecret;

		CertificateFactory certificateFactory = Exceptions.tryCatch( () -> CertificateFactory.getInstance( "X.509" ), exp -> new NetworkException.Runtime( "Failed to initialize X.509 certificate factory.", exp ) );
		KeyFactory keyFactory = Exceptions.tryCatch( () -> KeyFactory.getInstance( "RSA", "BC" ), exp -> new NetworkException.Runtime( "Failed to initialize RSA key factory.", exp ) );

		InputStream certInputStream = null;
		try
		{
			certInputStream = Files.newInputStream( sslCertFile );
			certificate = ( X509Certificate ) certificateFactory.generateCertificate( certInputStream );
		}
		finally
		{
			IO.closeQuietly( certInputStream );
		}

		PemReader pemReader = null;
		try
		{
			pemReader = new PemReader( Files.newBufferedReader( sslKeyFile ) );
			PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec( pemReader.readPemObject().getContent() );
			privateKey = keyFactory.generatePrivate( privateKeySpec );
		}
		finally
		{
			IO.closeQuietly( pemReader );
		}
	}

	public CertificateValidityState checkValidity()
	{
		try
		{
			certificate.checkValidity();
			return CertificateValidityState.Valid;
		}
		catch ( CertificateExpiredException e )
		{
			return CertificateValidityState.Expired;
		}
		catch ( CertificateNotYetValidException e )
		{
			return CertificateValidityState.NotYetValid;
		}
	}

	public SslContext context() throws IOException, CertificateException
	{
		if ( context == null )
		{
			if ( sslSecret == null || sslSecret.isEmpty() )
				context = SslContextBuilder.forServer( privateKey, certificate ).build();
			else
				context = SslContextBuilder.forServer( privateKey, sslSecret, certificate ).build();

			Networking.L.info( String.format( "Initialized SslContext %s using certificate '%s', key '%s', and hasSecret? %s", context.getClass(), IO.relPath( sslCertFile ), IO.relPath( sslKeyFile ), sslSecret != null && !sslSecret.isEmpty() ) );
		}

		return context;
	}

	/**
	 * Returns the number of days left on this certificate
	 * Will return -1 if already expired
	 */
	public int daysRemaining()
	{
		if ( isExpired() )
			return -1;
		return Days.daysBetween( LocalDate.fromDateFields( new Date() ), LocalDate.fromDateFields( certificate.getNotAfter() ) ).getDays();
	}

	public Path getCertFile()
	{
		return sslCertFile;
	}

	public Path getKeyFile()
	{
		return sslKeyFile;
	}

	public String getSslSecret()
	{
		return sslSecret;
	}

	public X509Certificate getCertificate()
	{
		return certificate;
	}

	public PrivateKey getPrivateKey()
	{
		return privateKey;
	}

	public Voluntary<String, CertificateEncodingException> getCommonName()
	{
		try
		{
			X500Name x500name = new JcaX509CertificateHolder( certificate ).getSubject();
			RDN cn = x500name.getRDNs( BCStyle.CN )[0];

			return Voluntary.ofNullable( IETFUtils.valueToString( cn.getFirst().getValue() ) );
		}
		catch ( CertificateEncodingException e )
		{
			return Voluntary.withException( e );
		}
	}

	public byte[] getEncoded() throws CertificateEncodingException
	{
		return certificate.getEncoded();
	}

	public Voluntary<List<String>, CertificateParsingException> getSubjectAltDNSNames()
	{
		return getSubjectAltNames( 2 );
	}

	public Voluntary<List<String>, CertificateParsingException> getSubjectAltNames( int type )
	{
		/*
		 * otherName [0] OtherName,
		 * rfc822Name [1] IA5String,
		 * dNSName [2] IA5String,
		 * x400Address [3] ORAddress,
		 * directoryName [4] Name,
		 * ediPartyName [5] EDIPartyName,
		 * uniformResourceIdentifier [6] IA5String,
		 * iPAddress [7] OCTET STRING,
		 * registeredID [8] OBJECT IDENTIFIER}
		 */

		if ( type < 0 || type > 8 )
			throw new IllegalArgumentException( "Type range out of bounds!" );

		try
		{
			List<String> result = new ArrayList<>();
			if ( certificate.getSubjectAlternativeNames() != null )
				for ( List<?> l : certificate.getSubjectAlternativeNames() )
					try
					{
						int i = Objs.castToIntWithException( l.get( 0 ) );
						String dns = Objs.castToStringWithException( l.get( 1 ) );

						if ( i == type )
							result.add( dns );
					}
					catch ( ClassCastException e )
					{
						Networking.L.severe( e.getMessage() );
					}
			return Voluntary.of( result );
		}
		catch ( CertificateParsingException e )
		{
			return Voluntary.withException( e );
		}
	}

	public boolean isExpired()
	{
		return checkValidity() == CertificateValidityState.Expired;
	}

	public String sha1() throws IOException
	{
		return new String( Encrypt.sha1( IO.readFileToBytes( sslCertFile ) ), Charsets.UTF_8 );
	}

	public String md5() throws IOException
	{
		return new String( Encrypt.md5( IO.readFileToBytes( sslCertFile ) ), Charsets.UTF_8 );
	}
}
