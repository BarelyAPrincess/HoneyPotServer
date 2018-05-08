/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.http.mappings;

import com.chiorichan.net.http.ssl.CertificateWrapper;
import com.chiorichan.utils.UtilHttp;
import com.chiorichan.utils.UtilIO;
import com.chiorichan.utils.UtilObjects;

import java.io.File;
import java.io.FileNotFoundException;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.net.ssl.SSLException;

import io.amelia.foundation.ConfigRegistry;
import io.amelia.http.webroot.Webroot;
import io.amelia.lang.SiteConfigurationException;
import io.amelia.support.IO;
import io.amelia.support.Namespace;
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

	public File directory()
	{
		try
		{
			return directory0( false );
		}
		catch ( SiteConfigurationException e )
		{
			// IGNORED - NEVER THROWN
		}
		return null;
	}

	public File directory( String subdir ) throws SiteConfigurationException
	{
		return new File( directory(), subdir );
	}

	protected File directory0( boolean throwException ) throws SiteConfigurationException
	{
		try
		{
			if ( hasConfig( "directory" ) )
			{
				String directory = getConfig( "directory" );
				if ( IO.isAbsolute( directory ) )
				{
					if ( !ConfigRegistry.config.getBoolean( "sites.allowPublicOutsideWebroot" ) && !directory.startsWith( webroot.getPublicDirectory().getAbsolutePath() ) )
						throw new SiteConfigurationException( String.format( "The public directory [%s] is not allowed outside the webroot.", UtilIO.relPath( new File( directory ) ) ) );

					return new File( directory );
				}

				return new File( webroot.getPublicDirectory(), directory );
			}
		}
		catch ( SiteConfigurationException e )
		{
			/* Should an exception be thrown instead of returning the default directory */
			if ( throwException )
				throw e;
		}

		return new File( webroot.getPublicDirectory(), getNamespace().replace( "_", "-" ).getString( "_" ) );
	}

	public File directoryWithException() throws SiteConfigurationException
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
		return domain.getFullDomain().reverseOrderNew();
	}

	public String getNamespaceString()
	{
		return getNamespace().getString( "_", true );
	}

	public DomainMapping getParentMapping()
	{
		Namespace ns = getDomainNamespace();
		if ( ns.getNodeCount() <= 1 )
			return null;
		return webroot.getMappings( ns.subNamespace( 1 ).getString() ).findFirst().orElse( null );
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
		nodes.putAll( getParentMappings().filter( p -> p.hasSslContext() ).collect( Collectors.toMap( DomainMapping::getFullDomain, p -> p ) ) );

		for ( Map.Entry<String, DomainMapping> entry : nodes.entrySet() )
		{
			CertificateWrapper wrapper = entry.getValue().initSsl();
			if ( wrapper != null && ( entry.getValue() == this || UtilHttp.normalize( wrapper.getCommonName() ).equals( getFullDomain() ) || wrapper.getSubjectAltDNSNames().contains( getFullDomain() ) ) )
				try
				{
					return wrapper.context();
				}
				catch ( SSLException | FileNotFoundException | CertificateException e )
				{
					e.printStackTrace();
					// Ignore
				}
		}

		return webroot.getDefaultSslContext();
	}

	public Site getWebroot()
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
		File ssl = webroot.getDirectory( "ssl" );
		UtilIO.setDirectoryAccessWithException( ssl );

		try
		{
			if ( hasConfig( "sslCert" ) && hasConfig( "sslKey" ) )
			{
				String sslCertPath = getConfig( "sslCert" );
				String sslKeyPath = getConfig( "sslKey" );

				File sslCert = UtilIO.isAbsolute( sslCertPath ) ? new File( sslCertPath ) : new File( ssl, sslCertPath );
				File sslKey = UtilIO.isAbsolute( sslKeyPath ) ? new File( sslKeyPath ) : new File( ssl, sslKeyPath );

				return new CertificateWrapper( sslCert, sslKey, getConfig( "sslSecret" ) );
			}
		}
		catch ( FileNotFoundException | CertificateException e )
		{
			SiteModule.getLogger().severe( String.format( "Failed to load SslContext for webroot '%s' using cert '%s', key '%s', and hasSecret? %s", webroot.getId(), getConfig( "sslCert" ), getConfig( "sslKey" ), hasConfig( "sslSecret" ) ), e );
		}

		return null;
	}

	public boolean isDefault()
	{
		return hasConfig( "default" ) && UtilObjects.castToBool( getConfig( "default" ) );
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
		webroot.mappings.add( this );
		return node;
	}

	public Boolean matches( String domain )
	{
		if ( UtilHttp.needsNormalization( domain ) )
			domain = UtilHttp.normalize( domain );
		return UtilHttp.matches( domain, this.domain.getFullDomain().getString() );
	}

	public void putConfig( String key, String value )
	{
		UtilObjects.notEmpty( key );
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
		return String.format( "DomainMapping{webroot=%s,rootDomain=%s,childDomain=%s,config=[%s]}", webroot.getId(), domain.getTld(), domain.getSub(), config.entrySet().stream().map( e -> e.getKey() + "=\"" + e.getValue() + "\"" ).collect( Collectors.joining( "," ) ) );
	}

	public void unmap()
	{
		DomainNode node = getDomainNode();
		if ( node != null )
			node.setWebroot( null );
	}
}
