/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.net.ssl;

import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.joda.time.Days;
import org.joda.time.LocalDate;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.net.ssl.SSLException;

import io.amelia.net.Networking;
import io.amelia.support.IO;
import io.amelia.support.Objs;
import io.amelia.support.VoluntaryWithCause;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

public class CertificateWrapper
{
	private final X509Certificate cert;
	private final Path sslCertFile;
	private final Path sslKeyFile;
	private final String sslSecret;
	private SslContext context = null;

	public CertificateWrapper( Path sslCertFile, Path sslKeyFile, String sslSecret ) throws IOException, CertificateException
	{
		if ( Files.notExists( sslCertFile ) )
			throw new FileNotFoundException( "The SSL Certificate \"" + IO.relPath( sslCertFile ) + "\" (aka. SSL Cert) file does not exist" );
		if ( Files.notExists( sslKeyFile ) )
			throw new FileNotFoundException( "The SSL Key \"" + IO.relPath( sslKeyFile ) + "\" (aka. SSL Key) file does not exist" );

		this.sslCertFile = sslCertFile;
		this.sslKeyFile = sslKeyFile;
		this.sslSecret = sslSecret;

		CertificateFactory cf;
		try
		{
			cf = CertificateFactory.getInstance( "X.509" );
		}
		catch ( CertificateException e )
		{
			throw new IllegalStateException( "Failed to initialize X.509 certificate factory." );
		}

		InputStream in = null;
		try
		{
			in = Files.newInputStream( sslCertFile );
			cert = ( X509Certificate ) cf.generateCertificate( in );
		}
		finally
		{
			if ( in != null )
				IO.closeQuietly( in );
		}
	}

	public CertificateValidityState checkValidity()
	{
		try
		{
			cert.checkValidity();
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

	public SslContext context() throws SSLException, IOException, CertificateException
	{
		if ( context == null )
		{
			context = SslContextBuilder.forServer( Files.newInputStream( sslCertFile ), Files.newInputStream( sslKeyFile ), sslSecret ).build();
			Networking.L.info( String.format( "Initialized SslContext %s using cert '%s', key '%s', and hasSecret? %s", context.getClass(), IO.relPath( sslCertFile ), IO.relPath( sslKeyFile ), sslSecret != null && !sslSecret.isEmpty() ) );
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
		return Days.daysBetween( LocalDate.fromDateFields( new Date() ), LocalDate.fromDateFields( cert.getNotAfter() ) ).getDays();
	}

	public Path getCertFile()
	{
		return sslCertFile;
	}

	public X509Certificate getCertificate()
	{
		return cert;
	}

	public VoluntaryWithCause<String, CertificateEncodingException> getCommonName()
	{
		try
		{
			X500Name x500name = new JcaX509CertificateHolder( cert ).getSubject();
			RDN cn = x500name.getRDNs( BCStyle.CN )[0];

			return VoluntaryWithCause.ofWithCause( IETFUtils.valueToString( cn.getFirst().getValue() ) );
		}
		catch ( CertificateEncodingException e )
		{
			return VoluntaryWithCause.withException( e );
		}
	}

	public byte[] getEncoded() throws CertificateEncodingException
	{
		return getCertificate().getEncoded();
	}

	public Path getKeyFile()
	{
		return sslKeyFile;
	}

	public String getSslSecret()
	{
		return sslSecret;
	}

	public VoluntaryWithCause<List<String>, CertificateParsingException> getSubjectAltDNSNames()
	{
		return getSubjectAltNames( 2 );
	}

	public VoluntaryWithCause<List<String>, CertificateParsingException> getSubjectAltNames( int type )
	{
		try
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

			List<String> results = new ArrayList<>();

			if ( cert.getSubjectAlternativeNames() != null )
				for ( List<?> l : cert.getSubjectAlternativeNames() )
					try
					{
						int i = Objs.castToIntWithException( l.get( 0 ) );
						String dns = Objs.castToStringWithException( l.get( 1 ) );

						if ( i == type )
							results.add( dns );
					}
					catch ( ClassCastException e )
					{
						Networking.L.severe( e.getMessage() );
					}

			return VoluntaryWithCause.ofWithCause( results );
		}
		catch ( CertificateParsingException e )
		{
			return VoluntaryWithCause.withException( e );
		}
	}

	public boolean isExpired()
	{
		return checkValidity() == CertificateValidityState.Expired;
	}

	public enum CertificateValidityState
	{
		Valid,
		NotYetValid,
		Expired
	}
}
