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

import com.google.common.base.Joiner;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.net.ssl.SSLException;

import io.amelia.events.Events;
import io.amelia.foundation.ConfigRegistry;
import io.amelia.foundation.Kernel;
import io.amelia.http.events.SslCertificateDefaultEvent;
import io.amelia.http.events.SslCertificateMapEvent;
import io.amelia.http.mappings.DomainMapping;
import io.amelia.http.mappings.DomainTree;
import io.amelia.lang.ConfigException;
import io.amelia.lang.StartupException;
import io.amelia.net.Networking;
import io.amelia.net.web.WebService;
import io.amelia.support.DateAndTime;
import io.amelia.support.EnumColor;
import io.amelia.support.Http;
import io.amelia.support.IO;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.util.Mapping;

public class SslRegistry implements Mapping<String, SslContext>
{
	public static Kernel.Logger L = Kernel.getLogger( SslRegistry.class );

	private Path lastSslCert;
	private Path lastSslKey;

	private String lastSslSecret;

	private SslContext serverContext;

	private boolean usingSelfSignedCert = false;

	public SslRegistry() throws StartupException
	{
		final Path sslCert = getServerCertificateFile();
		final Path sslKey = getServerKeyFile();
		final String sslSecret = getServerCertificateSecret();

		try
		{
			if ( sslCert == null || sslKey == null || Files.notExists( sslCert ) || Files.notExists( sslKey ) )
				selfSignCertificate();
			else
				try
				{
					updateDefaultCertificateWithException( sslCert, sslKey, sslSecret, true );
				}
				catch ( CertificateExpiredException e )
				{
					Networking.L.severe( "The SSL Certificate specified in server configuration was expired. (" + e.getMessage() + ") Loading a self signed certificate." );
					selfSignCertificate();
				}
				catch ( IOException e )
				{
					Networking.L.severe( "SSL Certificate specified in server configuration was not found. Loading a self signed certificate." );
					selfSignCertificate();
				}
		}
		catch ( CertificateException e )
		{
			throw new StartupException( "Certificate Exception Thrown", e );
		}
		catch ( SSLException e )
		{
			throw new StartupException( "SSL Exception Thrown", e );
		}
	}

	public Path getLastCertificateFile()
	{
		return lastSslCert;
	}

	public Path getLastKeyFile()
	{
		return lastSslKey;
	}

	public Path getServerCertificateFile()
	{
		Path path = ConfigRegistry.config.getValue( WebService.ConfigKeys.SSL_SHARED_CERT );
		return path.isAbsolute() ? path : Kernel.getPath( Kernel.PATH_STORAGE ).resolve( path );
	}

	public String getServerCertificateSecret()
	{
		return ConfigRegistry.config.getValue( WebService.ConfigKeys.SSL_SHARED_SECRET );
	}

	public Path getServerKeyFile()
	{
		Path path = ConfigRegistry.config.getValue( WebService.ConfigKeys.SSL_SHARED_KEY );
		return path.isAbsolute() ? path : Kernel.getPath( Kernel.PATH_STORAGE ).resolve( path );
	}

	public boolean isUsingSelfSignedCert()
	{
		return usingSelfSignedCert;
	}

	@Override
	public SslContext map( String host )
	{
		final String hostname = Http.hostnameNormalize( host );

		if ( hostname != null )
		{
			SslCertificateMapEvent event = Events.getInstance().callEvent( new SslCertificateMapEvent( hostname ) );

			if ( event.getSslContext() != null )
				return event.getSslContext();

			//try
			//{
			DomainMapping mapping = DomainTree.parseDomain( hostname ).getDomainMapping();
			if ( mapping != null )
			{
				SslContext context = mapping.getSslContext( true );
				if ( context != null )
					return context;
			}
			/*}
			catch ( Exception e )
			{
				Networking.L.info( "The NetworkRegistry is missing, defaulting to server SSL certificate." );
			}*/
		}

		SslCertificateDefaultEvent event = Events.getInstance().callEvent( new SslCertificateDefaultEvent( hostname ) );

		if ( event.getSslContext() != null )
			return event.getSslContext();

		return serverContext;
	}

	public void reloadCertificate() throws IOException, SSLException, CertificateException
	{
		updateDefaultCertificate( lastSslCert, lastSslKey, lastSslSecret, false );
	}

	private void selfSignCertificate() throws SSLException
	{
		Networking.L.warning( "No proper server-wide SSL certificate was provided, we will generate an extremely insecure temporary self signed one for now but please obtain an official one or self sign one of your own ASAP." );

		try
		{
			SelfSignedCertificate ssc = new SelfSignedCertificate( "chiorichan.com" );
			updateDefaultCertificate( ssc.certificate().toPath(), ssc.privateKey().toPath(), null, false );
			usingSelfSignedCert = true;
		}
		catch ( IOException | CertificateException e )
		{
			// Ignore
		}
	}

	public boolean updateDefaultCertificate( final CertificateWrapper wrapper, boolean updateConfig ) throws IOException
	{
		try
		{
			updateDefaultCertificateWithException( wrapper, updateConfig );
			return true;
		}
		catch ( SSLException | CertificateException e )
		{
			Networking.L.severe( "Unexpected Exception thrown while updating default SSL certificate", e );
			return false;
		}
	}

	public boolean updateDefaultCertificate( final Path sslCert, final Path sslKey, final String sslSecret, boolean updateConfig ) throws IOException
	{
		try
		{
			updateDefaultCertificateWithException( sslCert, sslKey, sslSecret, updateConfig );
			return true;
		}
		catch ( SSLException | CertificateException e )
		{
			Networking.L.severe( "Unexpected Exception thrown while updating default SSL certificate", e );
			return false;
		}
	}

	public void updateDefaultCertificateWithException( final CertificateWrapper wrapper, boolean updateConfig ) throws IOException, CertificateException
	{
		Path sslCert = wrapper.getCertFile();
		Path sslKey = wrapper.getKeyFile();
		String sslSecret = wrapper.getSslSecret();

		if ( updateConfig )
			try
			{
				ConfigRegistry.config.setValue( WebService.ConfigKeys.SSL_SHARED_CERT, IO.relPath( sslCert ) );
				ConfigRegistry.config.setValue( WebService.ConfigKeys.SSL_SHARED_KEY, IO.relPath( sslKey ) );
				ConfigRegistry.config.setValue( WebService.ConfigKeys.SSL_SHARED_SECRET, lastSslSecret );
			}
			catch ( ConfigException.Error e )
			{
				Networking.L.severe( "Failed to update certificate configuration.", e );
			}

		X509Certificate cert = wrapper.getCertificate();

		try
		{
			cert.checkValidity();
		}
		catch ( CertificateExpiredException e )
		{
			Networking.L.severe( "The server SSL certificate is expired, please obtain a renewed certificate ASAP." );
		}

		List<String> names = wrapper.getSubjectAltDNSNames().orElseGet( ( Supplier ) ArrayList::new );
		wrapper.getCommonName().ifPresent( names::add );

		Networking.L.info( String.format( "Updating default SSL cert with '%s', key '%s', and hasSecret? %s", IO.relPath( sslCert ), IO.relPath( sslKey ), sslSecret != null && !sslSecret.isEmpty() ) );
		Networking.L.info( EnumColor.AQUA + "The SSL Certificate has the following DNS names: " + EnumColor.GOLD + Joiner.on( EnumColor.AQUA + ", " + EnumColor.GOLD ).join( names ) );
		Networking.L.info( EnumColor.AQUA + "The SSL Certificate will expire after: " + EnumColor.GOLD + DateAndTime.now( cert.getNotAfter() ) );

		serverContext = wrapper.context();
		lastSslCert = sslCert;
		lastSslKey = sslKey;
		lastSslSecret = sslSecret;
		usingSelfSignedCert = false; // TODO Check for Self Signed
	}

	/**
	 * Used to set/update the server wide global SSL certificate.
	 *
	 * @param sslCertFile The updated SSL Certificate
	 * @param sslKeyFile  The updated SSL Key
	 * @param sslSecret   The SSL Shared Secret
	 */
	public void updateDefaultCertificateWithException( final Path sslCertFile, final Path sslKeyFile, final String sslSecret, boolean updateConfig ) throws IOException, SSLException, CertificateException
	{
		if ( Files.notExists( sslCertFile ) )
			throw new FileNotFoundException( "We could not set the server SSL Certificate because the \"" + IO.relPath( sslCertFile ) + "\" (aka. SSL Cert) file does not exist" );
		if ( Files.notExists( sslKeyFile ) )
			throw new FileNotFoundException( "We could not set the server SSL Certificate because the \"" + IO.relPath( sslKeyFile ) + "\" (aka. SSL Key) file does not exist" );

		CertificateWrapper wrapper = new CertificateWrapper( sslCertFile, sslKeyFile, sslSecret );

		updateDefaultCertificateWithException( wrapper, updateConfig );
	}
}
