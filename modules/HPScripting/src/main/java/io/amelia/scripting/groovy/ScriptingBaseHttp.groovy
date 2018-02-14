/*
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.scripting.groovy

import com.chiorichan.account.Account
import com.chiorichan.account.AccountManager
import com.chiorichan.account.AccountMeta
import com.chiorichan.factory.ScriptingContext
import com.chiorichan.factory.ScriptingFactory
import com.chiorichan.factory.api.Builtin
import com.chiorichan.factory.models.SQLModelBuilder
import com.chiorichan.net.http.HttpRequestWrapper
import com.chiorichan.net.http.HttpResponseWrapper
import com.chiorichan.net.http.Nonce
import com.chiorichan.net.http.Route
import com.chiorichan.permission.PermissibleEntity
import com.chiorichan.permission.Permission
import com.chiorichan.permission.PermissionResult
import com.chiorichan.permission.lang.PermissionDeniedException
import com.chiorichan.session.Session
import com.chiorichan.site.DomainMapping
import com.chiorichan.site.Site
import com.chiorichan.site.SiteModule
import com.chiorichan.utils.*
import groovy.transform.CompileStatic
import io.amelia.lang.MultipleException
import io.amelia.lang.ScriptingException
import io.amelia.lang.SiteConfigurationException
import io.amelia.logging.LogBuilder
import io.amelia.logging.Logger
import io.amelia.storage.old.DatabaseEngineLegacy
import io.amelia.storage.old.bases.SQLDatastore

import java.util.concurrent.atomic.AtomicInteger
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.stream.Collectors

/**
 * Used as the Groovy Scripting Base and provides scripts with custom builtin methods
 * */
@CompileStatic
abstract class ScriptingBaseHttp extends Builtin
{
	/**
	 * Returns the current HttpRequestWrapper instance
	 * XXX This is set inside the {@link HttpRequestWrapper#sessionStarted} and {@link com.chiorichan.session.SessionWrapper#startSession}, this needs looking over for other types
	 *
	 * @return current instance
	 */
	HttpRequestWrapper getRequest()
	{
		return getBindingProperty( "request" ) as HttpRequestWrapper
	}

	/**
	 * Returns the current HttpResponseWrapper instance
	 * XXX This is set inside the {@link HttpRequestWrapper#sessionStarted} and {@link com.chiorichan.session.SessionWrapper#startSession}, this needs looking over for other types
	 *
	 * @return current instance
	 */
	HttpResponseWrapper getResponse()
	{
		return getBindingProperty( "response" ) as HttpResponseWrapper
	}

	void header( String header )
	{
		if ( header.startsWith( "HTTP" ) )
		{
			Matcher m = Pattern.compile( "HTTP[^ ]* (\\d*) (.*)" ).matcher( header );
			if ( m.find() )
				getResponse().setStatus( Integer.parseInt( m.group( 1 ) ) )
		}
		else if ( header.startsWith( "Location:" ) )
		{
			getResponse().sendRedirect( header.substring( header.indexOf( ':' ) + 1 ).trim() )
		}
		else if ( header.contains( ":" ) )
		{
			getResponse().setHeader( header.substring( 0, header.indexOf( ':' ) ), header.substring( header.indexOf( ':' ) + 1 ).trim() )
		}
		else
		{
			throw new IllegalArgumentException( "The header argument is malformed!" )
		}
	}

	void header( String key, String val )
	{
		getResponse().setHeader( key, val )
	}

	/**
	 * Return the current session for this request
	 *
	 * @return current session
	 */
	Session getSession()
	{
		return getRequest().getSession()
	}

	PermissibleEntity getPermissibleEntity()
	{
		getAccount().getPermissibleEntity()
	}

	/**
	 * Get the account matching specified uid
	 * @param uid The uid you wish to use
	 * @return The found account, will return null if none found
	 */
	Account getAccount( String uid )
	{
		Account result = AccountManager.i().getAccount( hasLogin() ? getAccount().getLocation().getId() : getSite().getId(), uid )

		// if ( result == null )
			// result = AccountManager.instance().getAccountPartial( hasLogin() ? getAccount().getLocation().getId() : getSite().getId(), uid )

		return result
	}

	List<AccountMeta> getAccounts( String query )
	{
		return AccountManager.i().getAccounts( hasLogin() ? getAccount().getLocation().getId() : getSite().getId(), query )
	}

	List<AccountMeta> getAccounts( String query, int limit )
	{
		return AccountManager.i().getAccounts( hasLogin() ? getAccount().getLocation().getId() : getSite().getId(), query ).stream().limit( limit ).collect( Collectors.toList() )
	}

	/**
	 * Returns the current logged in account
	 * @return The current account, will return null if no one is logged in
	 */
	Account getAccount()
	{
		return getSession().getAccount()
	}

	Account getAccountOrNull()
	{
		return hasLogin() ? getSession().getAccount() : null
	}

	Account getAccountOrFail()
	{
		requireLogin()
		return getSession().getAccount()
	}

	boolean hasLogin()
	{
		return getSession().hasLogin()
	}

	/**
	 * Returns an instance of the current site
	 * @return The current site
	 */
	@Override
	Site getSite()
	{
		return getRequest().getSite()
	}

	void requireLogin()
	{
		if ( !getSession().hasLogin() )
			throw new PermissionDeniedException( PermissionDeniedException.PermissionDeniedReason.LOGIN_PAGE );
	}

	/**
	 * Returns the uri to the login page
	 * @return The login uri
	 */
	String url_to_login()
	{
		if ( getRequest().getSite() == null )
			return "/login"
		return getRequest().getSite().getConfig().getString( "scripts.login-form", "/login" )
	}

	/**
	 * Returns the to log current account out
	 * @return The logout uri
	 */
	String url_to_logout()
	{
		return url_to_login() + "?logout"
	}

	void define( String key, Object val )
	{
		getSession().setGlobal( key, val );
	}

	File dirname()
	{
		File file = getRequest().getInterpreter().getFile();
		return file == null ? null : file.getParentFile();
	}

	String domain()
	{
		return domain( null );
	}

	String domain( String subdomain )
	{
		StringBuilder domain = new StringBuilder();

		if ( subdomain != null && subdomain.trim().length() > 0 )
		{
			domain.append( subdomain.trim() ).append( "." )
		};

		domain.append( getRequest().getRootDomain() ).append( "/" );

		return domain.toString();
	}

	/**
	 * Returns an instance of the current site database
	 *
	 * @return The site database engine
	 * @throws IllegalStateException thrown if the requested database is unconfigured
	 */
	DatabaseEngineLegacy getDatabase()
	{
		SQLDatastore engine = getSite().getDatabase();

		if ( engine == null )
		{
			throw new IllegalStateException( "The site database is unconfigured. It will need to be setup in order for you to use the getSql() method." )
		};

		return engine.getLegacy();
	}

	Logger getLogger()
	{
		return LogBuilder.get( getClass().getSimpleName() );
	}

	Nonce nonce()
	{
		return getSession().getNonce();
	}

	String base_url()
	{
		return getRequest().getBaseUrl();
	}

	String route_id( String id ) throws SiteConfigurationException
	{
		return route_id( id, new HashMap<>() );
	}

	String route_id( String id, List<String> params ) throws SiteConfigurationException
	{
		return route_id( id, UtilMaps.indexMap( params ) )
	}

	String route_id( String id, Map<String, String> params ) throws SiteConfigurationException
	{
		params = UtilObjects.castMap( params, String.class, String.class );
		Route route = getSite().getRoutes().routeUrl( id );

		if ( UtilObjects.isNull( route ) )
		{
			throw new SiteConfigurationException( "Failed to find a route for id [" + id + "]" )
		};

		if ( route.hasParam( "pattern" ) )
		{
			String url = route.getParam( "pattern" );
			Pattern p = Pattern.compile( "\\[([a-zA-Z0-9]*)\\=\\]" );
			Matcher m = p.matcher( url );
			AtomicInteger iteration = new AtomicInteger();

			if ( m.find() )
				for ( ; ; )
				{
					String key = m.group( 1 );

					if ( !params.containsKey( key ) )
					{
						if ( params.containsKey( Integer.toString( iteration.get() ) ) )
						{
							key = Integer.toString( iteration.getAndIncrement() )
						}
						else
						{
							throw new SiteConfigurationException( "Route param [" + key + "] went unspecified for id [" + id + "], pattern [" + route.getParam( "pattern" ) + "]" )
						}
					};

					url = m.replaceFirst( params.get( key ) );
					m = p.matcher( url );

					if ( !m.find() )
						break
				}

			url = UtilStrings.trimFront( url, "/".charAt( 0 ) );

			if ( route.hasParam( "domain" ) )
			{
				String domain = UtilHttp.normalize( route.getParam( "domain" ) );
				if ( UtilObjects.isEmpty( domain ) )
					return getRequest().getFullDomain() + url
				if ( domain.startsWith( "http" ) || domain.startsWith( "//" ) )
					return domain + url
				return ( getRequest().isSecure() ? "https://" : "http://" ) + domain + "/" + url;
			}

			/* Validates if the host string could be used as the domain, meaning it's a simple (or not) regex string */
			if ( route.hasParam( "host" ) && !UtilObjects.isEmpty( route.getParam( "host" ) ) )
			{
				String host = UtilHttp.normalize( route.getParam( "host" ) );

				if ( host.startsWith( "^" ) )
					host = host.substring( 1 )
				if ( host.endsWith( "\$" ) )
					host = host.substring( 0, host.length() - 1 )

				if ( host.matches( "[a-z0-9.]+" ) )
				{
					if ( host.startsWith( "http" ) || host.startsWith( "//" ) )
						return host + url
					return ( getRequest().isSecure() ? "https://" : "http://" ) + host + "/" + url;
				}
			}

			return getRequest().getFullDomain() + url;
		}
		else if ( route.hasParam( "url" ) )
		{
			String url = route.getParam( "url" );
			return url.toLowerCase().startsWith( "http" ) || url.toLowerCase().startsWith( "//" ) ? url : getRequest().getFullDomain() + UtilStrings.trimAll( url, "/".charAt( 0 ) );
		}
		else
		{
			throw new SiteConfigurationException( "The route with id [" + id + "] has no 'pattern' nor 'url' directive, we can not produce a route url without either one." )
		};
	}

	String url_id( String id ) throws SiteConfigurationException
	{
		return url_id( id, getRequest().isSecure() );
	}

	String url_id( String id, boolean ssl ) throws SiteConfigurationException
	{
		return url_id( id, ssl ? "https://" : "http://" );
	}

	String url_id( String id, String prefix ) throws SiteConfigurationException
	{
		Optional<DomainMapping> result = SiteModule.i().getDomainMappingsById( id ).findFirst();
		if ( !result.isPresent() )
		{
			throw new SiteConfigurationException( "Can't find a domain mapping with id [" + id + "] in site [" + getSite().getId() + "]" )
		};
		return prefix + result.get().getFullDomain() + "/";
	}

	/**
	 * Same as @link url_to( null )	*/
	String url_to()
	{
		return url_to( null );
	}

	String url_to( String subdomain )
	{
		return getRequest().getFullDomain( subdomain );
	}

	/**
	 * Returns a fresh built URL based on the current domain Used to produce absolute uri's within scripts, e.g., url_to( "css" ) + "stylesheet.css"
	 *
	 * @param subdomain The subdomain
	 * @return A valid formatted URI
	 */
	String url_to( String subdomain, boolean secure )
	{
		return getRequest().getFullDomain( subdomain, secure );
	}

	/**
	 * Returns GET params as a String to appended after the full uri, e.g., {@code uri_to ( ) + get_map()}
	 *
	 * @return
	 */
	String get_map()
	{
		Map<String, String> getMap = getRequest().getGetMapRaw();
		return getMap.size() == 0 ? "" : "?" + UtilLists.joinQuery( getMap )
	}

	/**
	 * Returns GET params as a String to be appended after the full uri, e.g., {@code uri_to ( ) + get_append( "key", "value" )}
	 *
	 * @param map The additional get values
	 * @return The GET params as a string
	 */
	String get_append( Map<String, Object> map )
	{
		UtilObjects.notNull( map, "Map can not be null" )

		Map<String, String> getMap = new HashMap<>( getRequest().getGetMapRaw() )

		for ( Map.Entry<String, Object> e : map.entrySet() )
			getMap.put( e.getKey(), UtilObjects.castToString( e.getValue() ) )

		return getMap.size() == 0 ? "" : "?" + UtilLists.joinQuery( getMap )
	}

	String get_append( String key, Object val )
	{
		UtilObjects.notNull( key );
		UtilObjects.notNull( val );

		return get_append( UtilMaps.newHashMap( key, val ) );
	}

	String uri_to()
	{
		return getRequest().getFullUrl();
	}

	String uri_to( boolean secure )
	{
		return getRequest().getFullUrl( secure );
	}

	String uri_to( String subdomain )
	{
		return getRequest().getFullUrl( subdomain );
	}

	String uri_to( String subdomain, boolean ssl )
	{
		return getRequest().getFullUrl( subdomain, ssl );
	}

	String uri_to( String subdomain, String prefix )
	{
		return getRequest().getFullUrl( subdomain, prefix );
	}

	ScriptingFactory getScriptingFactory()
	{
		return getRequest().getScriptingFactory();
	}

	boolean isAdmin()
	{
		getSession().isAdmin()
	}

	boolean isOp()
	{
		getSession().isOp()
	}

	PermissionResult checkPermission( String perm )
	{
		getSession().checkPermission( perm )
	}

	PermissionResult checkPermission( Permission perm )
	{
		getSession().checkPermission( perm )
	}

	PermissionResult requirePermission( String perm )
	{
		getSession().requirePermission( perm )
	}

	PermissionResult requirePermission( Permission perm )
	{
		getSession().requirePermission( perm )
	}

	ScriptingFactory getEvalFactory()
	{
		return getRequest().getScriptingFactory()
	}

	Object include( String pack ) throws MultipleException, ScriptingException
	{
		return ScriptingContext.fromPackage( getSite(), pack ).request( getRequest() ).eval()
	}

	Object require( String pack ) throws IOException, MultipleException, ScriptingException
	{
		return ScriptingContext.fromPackageWithException( getSite(), pack ).request( getRequest() ).eval()
	}

	SQLModelBuilder model( String pack ) throws IOException, MultipleException, ScriptingException
	{
		return ScriptingContext.fromPackageWithException( getSite(), pack ).request( getRequest() ).model()
	}
}
