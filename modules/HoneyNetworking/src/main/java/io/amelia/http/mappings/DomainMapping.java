/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.http.mappings;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.amelia.http.ssl.CertificateWrapper;
import io.amelia.lang.WebrootException;
import io.amelia.net.Networking;
import io.amelia.foundation.ConfigRegistry;
import io.amelia.http.webroot.Webroot;
import io.amelia.support.Http;
import io.amelia.support.IO;
import io.amelia.support.Namespace;
import io.amelia.support.Objs;
import io.netty.handler.ssl.SslContext;

public class DomainMapping
{
	protected final Map<String, String> config = new TreeMap<>();
	protected final DomainParser domain;
	protected final Webroot webroot;

	public DomainMapping( Webroot webroot, String fullDomain )
	{
		this.webroot = webroot;
		this.domain = new DomainParser( fullDomain );
	}

	public void clearConfig()
	{
		config.clear();
	}

	public Path directory()
	{
		try
		{
			return directory0( false );
		}
		catch ( WebrootException.Configuration e )
		{
			// IGNORED - NEVER THROWN
		}
		return null;
	}

	protected Path directory0( boolean throwException ) throws WebrootException.Configuration
	{
		try
		{
			if ( hasConfig( "directory" ) )
			{
				String directory = getConfig( "directory" );
				if ( IO.isAbsolute( directory ) )
				{
					if ( !ConfigRegistry.config.getBoolean( "webroots.allowPublicOutsideWebroot" ).orElse( false ) && !directory.startsWith( webroot.getPublicDirectory().toString() ) )
						throw new WebrootException.Configuration( String.format( "The public directory [%s] is not allowed outside the webroot.", IO.relPath( new File( directory ) ) ) );

					return Paths.get( directory );
				}

				return webroot.getPublicDirectory().resolve( directory );
			}
		}
		catch ( WebrootException.Configuration e )
		{
			/* Should an exception be thrown instead of returning the default directory */
			if ( throwException )
				throw e;
		}

		return webroot.getPublicDirectory().resolve( getNamespace().replace( "_", "-" ).setGlue( "_" ).getString() );
	}

	public Path directoryWithException() throws WebrootException.Configuration
	{
		return directory0( true );
	}

	public String getChildDomain()
	{
		return domain.getChildDomain().getString();
	}

	public DomainMapping getChildMapping( String child )
	{
		return webroot.getMappings( getDomainNamespace().prepend( child ).getString() ).findFirst().orElse( null );
	}

	public String getConfig( String key )
	{
		return config.get( key.toLowerCase() );
	}

	public Namespace getDomainNamespace()
	{
		return domain.getFullDomain().clone();
	}

	public DomainNode getDomainNode()
	{
		return DomainTree.parseDomain( getFullDomain() );
	}

	public String getFullDomain()
	{
		return domain.getFullDomain().getString();
	}

	public Namespace getNamespace()
	{
		return domain.getFullDomain().reverseOrder();
	}

	public String getNamespaceString()
	{
		return getNamespace().setGlue( "_" ).getString();
	}

	public DomainMapping getParentMapping()
	{
		Namespace ns = getDomainNamespace();
		if ( ns.getNodeCount() <= 1 )
			return null;
		return webroot.getMappings( ns.getSubNodes( 1 ).getString() ).findFirst().orElse( null );
	}

	public Stream<DomainMapping> getParentMappings()
	{
		return Stream.of( getParentMapping() ).flatMap( DomainMapping::getParentMappings0 );
	}

	public Stream<DomainMapping> getParentMappings0()
	{
		DomainMapping mapping = getParentMapping();
		if ( mapping == null )
			return Stream.of( this );
		else
			return Stream.concat( Stream.of( this ), mapping.getParentMappings0() );
	}

	public String getRootDomain()
	{
		return domain.getRootDomain().getString();
	}

	/**
	 * Try to initialize the SslContext
	 *
	 * @param recursive Should we look backwards for a valid SSL Context?, e.g., look at our parents.
	 *
	 * @return SslContext
	 */
	public SslContext getSslContext( boolean recursive )
	{
		Map<String, DomainMapping> nodes = new TreeMap<>( Collections.reverseOrder() );
		nodes.putAll( getParentMappings().filter( DomainMapping::hasSslContext ).collect( Collectors.toMap( DomainMapping::getFullDomain, p -> p ) ) );

		for ( Map.Entry<String, DomainMapping> entry : nodes.entrySet() )
		{
			CertificateWrapper wrapper = entry.getValue().initSsl();
			if ( wrapper != null && ( entry.getValue() == this || Http.hostnameNormalize( wrapper.getCommonName().orElse( null ) ).equals( getFullDomain() ) || wrapper.getSubjectAltDNSNames().orElseGet( ( Supplier ) ArrayList::new ).contains( getFullDomain() ) ) )
				try
				{
					return wrapper.context();
				}
				catch ( IOException | CertificateException e )
				{
					e.printStackTrace();
					// Ignore
				}
		}

		return webroot.getDefaultSslContext();
	}

	public Webroot getWebroot()
	{
		return webroot;
	}

	public boolean hasConfig( String key )
	{
		return config.containsKey( key.toLowerCase() );
	}

	public boolean hasSslContext()
	{
		return hasConfig( "sslCert" ) && hasConfig( "sslKey" );
	}

	private CertificateWrapper initSsl()
	{
		try
		{
			Path ssl = webroot.getDirectory( "ssl" );
			IO.forceCreateDirectory( ssl );

			if ( hasConfig( "sslCert" ) && hasConfig( "sslKey" ) )
			{
				String sslCertPath = getConfig( "sslCert" );
				String sslKeyPath = getConfig( "sslKey" );

				Path sslCert = IO.isAbsolute( sslCertPath ) ? Paths.get( sslCertPath ) : ssl.resolve( sslCertPath );
				Path sslKey = IO.isAbsolute( sslKeyPath ) ? Paths.get( sslKeyPath ) : ssl.resolve( sslKeyPath );

				return new CertificateWrapper( sslCert, sslKey, getConfig( "sslSecret" ) );
			}
		}
		catch ( IOException | CertificateException | InvalidKeySpecException e )
		{
			Networking.L.severe( String.format( "Failed to load SslContext for webroot '%s' using cert '%s', key '%s', and hasSecret? %s", webroot.getWebrootId(), getConfig( "sslCert" ), getConfig( "sslKey" ), hasConfig( "sslSecret" ) ), e );
		}

		return null;
	}

	public boolean isDefault()
	{
		return hasConfig( "default" ) && Objs.castToBoolean( getConfig( "default" ) );
	}

	public boolean isMapped()
	{
		DomainNode node = getDomainNode();
		return node != null && node.getWebroot() == webroot;
	}

	public DomainNode map()
	{
		DomainNode node = getDomainNode();
		if ( node != null )
			node.setWebroot( webroot );
		webroot.addDomainMapping( this );
		return node;
	}

	public Boolean matches( String domain )
	{
		if ( Http.hostnameNeedsNormalization( domain ) )
			domain = Http.hostnameNormalize( domain );
		return Http.matches( domain, this.domain.getFullDomain().getString() );
	}

	public void putConfig( String key, String value )
	{
		Objs.notEmpty( key );
		key = key.trim().toLowerCase();
		if ( key.startsWith( "__" ) )
			key = key.substring( 2 );
		if ( value == null )
			config.remove( key );
		else
			config.put( key, value );
	}

	public void save()
	{
		throw new IllegalStateException( "Not Implemented!" );
		// isCommitted = true;

		// EventBus.instance().callEvent( new SiteDomainChangeEvent( SiteDomainChangeEventType.ADD, webroot, domain, this ) );
	}

	@Override
	public String toString()
	{
		return String.format( "DomainMapping{webroot=%s,rootDomain=%s,childDomain=%s,config=[%s]}", webroot.getWebrootId(), domain.getTld(), domain.getSub(), config.entrySet().stream().map( e -> e.getKey() + "=\"" + e.getValue() + "\"" ).collect( Collectors.joining( "," ) ) );
	}

	public void unmap()
	{
		DomainNode node = getDomainNode();
		if ( node != null )
			node.setWebroot( null );
	}
}
