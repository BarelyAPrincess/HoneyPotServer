/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.http;

import com.chiorichan.account.Account;
import com.chiorichan.account.AccountManager;
import com.chiorichan.account.auth.AccountAuthenticator;
import com.chiorichan.account.lang.AccountDescriptiveReason;
import com.chiorichan.account.lang.AccountException;
import com.chiorichan.account.lang.AccountResult;
import com.chiorichan.net.NetworkManager;
import com.chiorichan.session.Session;
import com.chiorichan.session.SessionContext;
import com.chiorichan.session.SessionModule;
import com.chiorichan.session.SessionWrapper;
import com.chiorichan.site.DomainMapping;
import com.chiorichan.site.Site;
import com.chiorichan.site.SiteModule;
import com.chiorichan.tasks.Timings;
import com.chiorichan.utils.UtilHttp;
import com.chiorichan.utils.UtilMaps;
import com.chiorichan.utils.UtilNet;
import com.chiorichan.utils.UtilObjects;
import com.chiorichan.utils.UtilStrings;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Stream;

import io.amelia.ServerLoader;
import io.amelia.foundation.ConfigRegistry;
import io.amelia.lang.EnumColor;
import io.amelia.lang.HttpError;
import io.amelia.logging.experimental.LogEvent;
import io.amelia.messaging.MessageSender;
import io.amelia.support.Versioning;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.ssl.SslHandler;

/**
 * Wraps the Netty HttpRequest and provides shortcut methods
 */
public class HttpRequestWrapper extends SessionWrapper implements SessionContext
{
	private static final Map<Thread, WeakReference<HttpRequestWrapper>> references = new ConcurrentHashMap<>();

	public static HttpRequestWrapper getRequest()
	{
		if ( !references.containsKey( Thread.currentThread() ) || references.get( Thread.currentThread() ).get() == null )
			throw new IllegalStateException( "Thread '" + Thread.currentThread().getName() + "' does not seem to currently link to any existing HTTP requests, please try again or notify an administrator." );
		return references.get( Thread.currentThread() ).get();
	}

	private static void putRequest( HttpRequestWrapper request )
	{
		references.put( Thread.currentThread(), new WeakReference<>( request ) );
	}

	/**
	 * The original Netty Channel
	 */
	private final Channel channel;

	/**
	 * The size of the posted content
	 */
	int contentSize = 0;

	/**
	 * Cookie Cache
	 */
	final Set<HttpCookie> cookies = new HashSet<>();

	/**
	 * The Get Map
	 */
	final Map<String, String> getMap = new TreeMap<>();

	/**
	 * The {@link HttpHandler} for this request
	 */
	final HttpHandler handler;

	/**
	 * The original Netty Http Request
	 */
	private final HttpRequest http;

	/**
	 * Instance of LogEvent used by this request
	 */
	final LogEvent log;

	/**
	 * The Post Map
	 */
	final Map<String, String> postMap = new TreeMap<>();

	/**
	 * The time of this request
	 */
	final long requestTime;

	/**
	 * The paired HttpResponseWrapper
	 */
	final HttpResponseWrapper response;

	/**
	 * The URI Rewrite Map
	 */
	final Map<String, String> rewriteMap = new TreeMap<>();

	/**
	 * Server Cookie Cache
	 */
	final Set<HttpCookie> serverCookies = new HashSet<>();

	/**
	 * Server Variables
	 */
	HttpVariableMapper vars = new HttpVariableMapper();

	/**
	 * Is this a SSL request
	 */
	final boolean ssl;

	/**
	 * Files uploaded with this request
	 */
	final Map<String, UploadedFile> uploadedFiles = new HashMap<>();

	/**
	 * The requested URI
	 */
	private String uri = null;

	private boolean nonceProcessed = false;

	private HttpAuthenticator auth = null;

	private DomainMapping domainMapping;

	HttpRequestWrapper( Channel channel, HttpRequest http, HttpHandler handler, boolean ssl, LogEvent log ) throws IOException, HttpError
	{
		this.channel = channel;
		this.http = http;
		this.handler = handler;
		this.ssl = ssl;
		this.log = log;

		putRequest( this );

		// Set Time of this Request
		requestTime = Timings.epoch();

		// Create a matching HttpResponseWrapper
		response = new HttpResponseWrapper( this, log );

		String host = UtilHttp.normalize( getHostDomain() );

		if ( UtilObjects.isEmpty( host ) )
			this.domainMapping = SiteModule.i().getDefaultSite().getDefaultMapping();
		else if ( UtilNet.isValidIPv4( host ) || UtilNet.isValidIPv6( host ) )
		{
			Stream<DomainMapping> domains = SiteModule.i().getDomainMappingsByIp( host );
			domainMapping = domains.count() == 0 ? null : domains.findFirst().orElse( null );
		}
		else
		{
			if ( "localhost".equals( host ) && !getIpAddress().startsWith( "127" ) && !getIpAddress().equals( getLocalIpAddress() ) )
				throw new HttpError( 418 );

			this.domainMapping = SiteModule.i().getDomainMapping( host );
		}

		if ( domainMapping == null )
			domainMapping = SiteModule.i().getDefaultSite().getMappings( "" ).findFirst().orElse( null );

		if ( getUri().startsWith( "/~" ) && domainMapping.getSite() == SiteModule.i().getDefaultSite() )
		{
			String uri = getUri();
			int inx = uri.indexOf( "/", 2 );
			String siteId = uri.substring( 2, inx == -1 ? uri.length() - 2 : inx );
			String newUri = inx == -1 ? "/" : uri.substring( inx );

			Site siteTmp = SiteModule.i().getSiteById( siteId );
			if ( !siteId.equals( "wisp" ) && siteTmp != null )
			{
				/* Get the declared default domain mapping, the first if otherwise */
				domainMapping = siteTmp.getDefaultMapping();
				setUri( newUri );
			}
		}

		// log.log( Level.INFO, "SiteId: " + site.getSiteId() + ", ParentDomain: " + rootDomain + ", ChildDomain: " + childDomain );

		try
		{
			QueryStringDecoder queryStringDecoder = new QueryStringDecoder( http.uri() );
			Map<String, List<String>> params = queryStringDecoder.parameters();
			if ( !params.isEmpty() )
				for ( Entry<String, List<String>> p : params.entrySet() )
				{
					// XXX This is overriding the key, why would their there be multiple values???
					String key = p.getKey();
					for ( String val : p.getValue() )
						getMap.put( key, val );
				}
		}
		catch ( IllegalStateException e )
		{
			log.log( Level.SEVERE, "Failed to decode the GET map because " + e.getMessage() );
		}

		// Decode Cookies
		// String var1 = URLDecoder.decode( http.headers().getAndConvert( "Cookie" ), Charsets.UTF_8.displayName() );
		String var1 = http.headers().getAndConvert( "Cookie" );

		// TODO Find a way to fix missing invalid stuff

		if ( var1 != null )
			try
			{
				Set<Cookie> var2 = CookieDecoder.decode( var1 );
				for ( Cookie cookie : var2 )
					if ( cookie.name().startsWith( "_ws" ) )
						serverCookies.add( new HttpCookie( cookie ) );
					else
						cookies.add( new HttpCookie( cookie ) );
			}
			catch ( IllegalArgumentException | NullPointerException e )
			{
				//NetworkManager.getLogger().debug( var1 );

				NetworkManager.getLogger().severe( "Failed to parse cookie for reason: " + e.getMessage() );
				// NetworkManager.getLogger().warning( "There was a problem decoding the request cookie.", e );
				// NetworkManager.getLogger().debug( "Cookie: " + var1 );
				// NetworkManager.getLogger().debug( "Headers: " + Joiner.on( "," ).withKeyValueSeparator( "=" ).join( http.headers() ) );
			}

		initServerVars();
	}

	@Override
	protected void finish0()
	{
		// Do Nothing
	}

	public String getArgument( String key )
	{
		String val = getMap.get( key );

		if ( val == null && postMap != null )
			val = postMap.get( key );

		if ( val == null && rewriteMap != null )
			val = rewriteMap.get( key );

		return val;
	}

	public String getArgument( String key, String def )
	{
		String val = getArgument( key );
		return val == null ? def : val;
	}

	public boolean getArgumentBoolean( String key )
	{
		String rtn = getArgument( key, "0" ).toLowerCase();
		return UtilObjects.isTrue( rtn );
	}

	public double getArgumentDouble( String key )
	{
		Object obj = getArgument( key, "-1.0" );
		return UtilObjects.castToDouble( obj );
	}

	public int getArgumentInt( String key )
	{
		Object obj = getArgument( key, "-1" );
		return UtilObjects.castToInt( obj );
	}

	public Set<String> getArgumentKeys()
	{
		Set<String> keys = Sets.newHashSet();
		keys.addAll( getMap.keySet() );
		keys.addAll( postMap.keySet() );
		keys.addAll( rewriteMap.keySet() );
		return keys;
	}

	public long getArgumentLong( String key )
	{
		Object obj = getArgument( key, "-1" );
		return UtilObjects.castToLong( obj );
	}

	public HttpAuthenticator getAuth()
	{
		if ( auth == null )
			initAuthorization();
		return auth;
	}

	public String getBaseUrl()
	{
		String url = getRootDomain();

		if ( getChildDomain() != null && !getChildDomain().isEmpty() )
			url = getChildDomain() + "." + url;

		return ( isSecure() ? "https://" : "http://" ) + url;
	}

	public Channel getChannel()
	{
		return channel;
	}

	public int getContentLength()
	{
		return contentSize;
	}

	@Override
	public HttpCookie getCookie( String key )
	{
		for ( HttpCookie cookie : cookies )
			if ( cookie.getKey().equals( key ) )
				return cookie;
		return null;
	}

	@Override
	public Set<HttpCookie> getCookies()
	{
		return Collections.unmodifiableSet( cookies );
	}

	public Set<HttpCookie> getServerCookies()
	{
		return Collections.unmodifiableSet( serverCookies );
	}

	public DomainMapping getDomainMapping()
	{
		return domainMapping;
	}

	public String getRootDomain()
	{
		return domainMapping.getRootDomain();
	}

	public String getFullDomain()
	{
		return getFullDomain( null, ssl );
	}

	public String getFullDomain( boolean ssl )
	{
		return getFullDomain( null, ssl );
	}

	public String getFullDomain( String subdomain )
	{
		return getFullDomain( subdomain, ssl );
	}

	public String getFullDomain( String subdomain, boolean ssl )
	{
		return getFullDomain( subdomain, ssl ? "https://" : "http://" );
	}

	public String getFullDomain( String subdomain, String prefix )
	{
		return prefix + ( UtilObjects.isEmpty( subdomain ) ? getHostDomain() : subdomain + "." + getRootDomain() ) + "/";
	}

	public String getTopDomain()
	{
		return getFullDomain( null, ssl );
	}

	public String getTopDomain( boolean ssl )
	{
		return getFullDomain( null, ssl );
	}

	public String getTopDomain( String subdomain )
	{
		return getFullDomain( subdomain, ssl );
	}

	public String getTopDomain( String subdomain, boolean ssl )
	{
		return getTopDomain( subdomain, ssl ? "https://" : "http://" );
	}

	public String getTopDomain( String subdomain, String prefix )
	{
		return prefix + ( UtilObjects.isEmpty( subdomain ) ? "" : subdomain + "." ) + getRootDomain() + "/";
	}

	public String getFullUrl()
	{
		return getFullUrl( null, ssl );
	}

	public String getFullUrl( boolean ssl )
	{
		return getFullUrl( null, ssl );
	}

	public String getFullUrl( String subdomain )
	{
		return getFullUrl( subdomain, ssl );
	}

	public String getFullUrl( String subdomain, boolean ssl )
	{
		return getFullDomain( subdomain, ssl ) + getUri();
	}

	public String getFullUrl( String subdomain, String prefix )
	{
		return getFullDomain( subdomain, prefix ) + getUri().substring( 1 );
	}

	public Map<String, Object> getGetMap()
	{
		return parseMapArrays( getGetMapRaw() );
	}

	public Map<String, String> getGetMapRaw()
	{
		return getMap;
	}

	public String getHeader( CharSequence key )
	{
		try
		{
			return http.headers().getAndConvert( key );
		}
		catch ( NullPointerException | IndexOutOfBoundsException e )
		{
			return null;
		}
	}

	public HttpHeaders getHeaders()
	{
		return http.headers();
	}

	public String getHost()
	{
		return UtilHttp.normalize( http.headers().getAndConvert( "Host" ) );
	}

	public String getHostDomain()
	{
		if ( http.headers().contains( "Host" ) )
			return http.headers().getAndConvert( "Host" ).split( "\\:" )[0];
		return null;
	}

	public HttpVersion getHttpVersion()
	{
		return http.protocolVersion();
	}

	/**
	 * Similar to {@link #getInetAddr()}
	 *
	 * @return the remote connections IP address
	 */
	public InetAddress getInetAddr()
	{
		return getInetAddr( true );
	}

	/**
	 * Similar to {@link #getInetAddr(boolean)}
	 *
	 * @param detectCDN Try to detect the use of CDNs, e.g., CloudFlare, IP headers when set to false.
	 * @return the remote connections IP address
	 */
	public InetAddress getInetAddr( boolean detectCDN )
	{
		if ( detectCDN && http.headers().contains( "CF-Connecting-IP" ) )
			try
			{
				return InetAddress.getByName( http.headers().getAndConvert( "CF-Connecting-IP" ) );
			}
			catch ( UnknownHostException e )
			{
				e.printStackTrace();
				return null;
			}

		return ( ( InetSocketAddress ) channel.remoteAddress() ).getAddress();
	}

	public WebInterpreter getInterpreter()
	{
		return handler.getInterpreter();
	}

	/**
	 * Similar to {@link #getIpAddress(boolean)} except defaults to true
	 *
	 * @return the remote connections IP address as a string
	 */
	@Override
	public String getIpAddress()
	{
		return getIpAddress( true );
	}

	/**
	 * This method uses a checker that makes it possible for our server to get the correct remote IP even if using it with CloudFlare.
	 * I believe there are other CDN services like CloudFlare. I'd love it if people could inform me, so I can implement similar methods.
	 * https://support.cloudflare.com/hc/en-us/articles/200170786-Why-do-my-server-logs-show-CloudFlare-s-IPs-using-CloudFlare-
	 *
	 * @param detectCDN Try to detect the use of CDNs, e.g., CloudFlare, IP headers when set to false.
	 * @return the remote connections IP address as a string
	 */
	public String getIpAddress( boolean detectCDN )
	{
		// TODO Implement other CDNs
		if ( detectCDN && http.headers().contains( "CF-Connecting-IP" ) )
			return http.headers().getAndConvert( "CF-Connecting-IP" );

		return ( ( InetSocketAddress ) channel.remoteAddress() ).getAddress().getHostAddress();
	}

	public String getLocalHostName()
	{
		return ( ( InetSocketAddress ) channel.localAddress() ).getHostName();
	}

	public String getLocalIpAddress()
	{
		return ( ( InetSocketAddress ) channel.localAddress() ).getAddress().getHostAddress();
	}

	public int getLocalPort()
	{
		return ( ( InetSocketAddress ) channel.localAddress() ).getPort();
	}

	@Override
	public Site getLocation()
	{
		return domainMapping.getSite();
	}

	public HttpRequest getOriginal()
	{
		return http;
	}

	public String getParameter( String key )
	{
		return null;
	}

	public Map<String, Object> getPostMap()
	{
		return parseMapArrays( getPostMapRaw() );
	}

	public Map<String, String> getPostMapRaw()
	{
		return postMap;
	}

	public String getQuery()
	{
		if ( getMap.isEmpty() )
			return "";
		return "?" + Joiner.on( "&" ).withKeyValueSeparator( "=" ).join( getMap );
	}

	public String getRemoteHostname()
	{
		return ( ( InetSocketAddress ) channel.remoteAddress() ).getHostName();
	}

	public int getRemotePort()
	{
		return ( ( InetSocketAddress ) channel.remoteAddress() ).getPort();
	}

	public String getRequestHost()
	{
		return getHeader( "Host" );
	}

	public Map<String, Object> getRequestMap() throws Exception
	{
		return parseMapArrays( getRequestMapRaw() );
	}

	public Map<String, String> getRequestMapRaw() throws Exception
	{
		Map<String, String> requestMap = new TreeMap<>();

		if ( getMap != null )
			UtilMaps.mergeMaps( requestMap, getMap );

		if ( postMap != null )
			UtilMaps.mergeMaps( requestMap, postMap );

		if ( rewriteMap != null )
			UtilMaps.mergeMaps( requestMap, rewriteMap );

		return requestMap;
	}

	public long getRequestTime()
	{
		return requestTime;
	}

	public HttpResponseWrapper getResponse()
	{
		return response;
	}

	public Map<String, String> getRewriteMap()
	{
		return rewriteMap;
	}

	public HttpVariableMapper getServer()
	{
		return vars;
	}

	@Override
	protected HttpCookie getServerCookie( String key )
	{
		return serverCookies.stream().filter( c -> c.getKey().equals( key ) ).findFirst().orElse( null );
	}

	public Site getSite()
	{
		return getLocation();
	}

	public String getChildDomain()
	{
		return domainMapping.getChildDomain();
	}

	public Map<String, UploadedFile> getUploadedFiles()
	{
		return Collections.unmodifiableMap( uploadedFiles );
	}

	Map<String, UploadedFile> getUploadedFilesRaw()
	{
		return uploadedFiles;
	}

	public String getUri()
	{
		if ( uri == null )
		{
			uri = http.uri();

			try
			{
				uri = URLDecoder.decode( uri, Charsets.UTF_8.name() );
			}
			catch ( UnsupportedEncodingException e )
			{
				try
				{
					uri = URLDecoder.decode( uri, Charsets.ISO_8859_1.name() );
				}
				catch ( UnsupportedEncodingException e1 )
				{
					throw new Error();
				}
			}
			catch ( IllegalArgumentException e1 )
			{
				// [ni..up-3-1] 02-05 00:17:10.273 [WARNING] [HttpHdl] WARNING THIS IS AN UNCAUGHT EXCEPTION! CAN YOU KINDLY REPORT THIS STACKTRACE TO THE DEVELOPER?
				// java.lang.IllegalArgumentException: URLDecoder: Illegal hex characters in escape (%) pattern - For input string: "im"
			}

			// if ( uri.contains( File.separator + '.' ) || uri.contains( '.' + File.separator ) || uri.startsWith( "." ) || uri.endsWith( "." ) || INSECURE_URI.matcher( uri ).matches() )
			// {
			// return "/";
			// }

			if ( uri.contains( "?" ) )
				uri = uri.substring( 0, uri.indexOf( "?" ) );

			uri = UtilStrings.trimFront( uri, '/' );
		}

		return uri;
	}

	public String getUserAgent()
	{
		return getHeader( "User-Agent" );
	}

	public String getWebSocketLocation( HttpObject req )
	{
		return ( isSecure() ? "wss://" : "ws://" ) + getHost() + "/wisp/websocket";
	}

	public boolean hasArgument( String key )
	{
		return getMap.containsKey( key ) || postMap.containsKey( key ) || rewriteMap.containsKey( key );
	}

	private void initAuthorization()
	{
		if ( auth == null && getHeader( HttpHeaderNames.AUTHORIZATION ) != null )
			auth = new HttpAuthenticator( this );
	}

	public boolean forceNoTrailingSlash()
	{
		if ( uri.endsWith( "/" ) )
		{
			NetworkManager.getLogger().fine( "Forcing URL redirect to have NO trailing slash." );
			getResponse().sendRedirect( getFullDomain() + uri.substring( uri.length() - 1 ), HttpCode.HTTP_MOVED_PERM );
			return true;
		}
		return false;
	}

	public boolean forceTrailingSlash()
	{
		if ( !UtilObjects.isEmpty( uri ) && !uri.endsWith( "/" ) )
		{
			NetworkManager.getLogger().fine( "Forcing URL redirect to have trailing slash." );
			getResponse().sendRedirect( getFullDomain() + uri + "/", HttpCode.HTTP_MOVED_PERM );
			return true;
		}
		return false;
	}

	/**
	 * Initializes the serverVars with initial information from this request
	 */
	private void initServerVars()
	{
		vars.put( ServerVars.SERVER_SOFTWARE, Versioning.getProduct() );
		vars.put( ServerVars.SERVER_VERSION, Versioning.getVersion() );
		vars.put( ServerVars.SERVER_ADMIN, ConfigRegistry.i().getString( "server.admin", "me@chiorichan.com" ) );
		vars.put( ServerVars.SERVER_SIGNATURE, Versioning.getProduct() + " Version " + Versioning.getVersion() );
		vars.put( ServerVars.HTTP_VERSION, http.protocolVersion() );
		vars.put( ServerVars.HTTP_ACCEPT, getHeader( "Accept" ) );
		vars.put( ServerVars.HTTP_USER_AGENT, getUserAgent() );
		vars.put( ServerVars.HTTP_CONNECTION, getHeader( "Connection" ) );
		vars.put( ServerVars.HTTP_HOST, getLocalHostName() );
		vars.put( ServerVars.HTTP_ACCEPT_ENCODING, getHeader( "Accept-Encoding" ) );
		vars.put( ServerVars.HTTP_ACCEPT_LANGUAGE, getHeader( "Accept-Language" ) );
		vars.put( ServerVars.HTTP_X_REQUESTED_WITH, getHeader( "X-requested-with" ) );
		vars.put( ServerVars.REMOTE_HOST, getRemoteHostname() );
		vars.put( ServerVars.REMOTE_ADDR, getIpAddress() );
		vars.put( ServerVars.REMOTE_PORT, getRemotePort() );
		vars.put( ServerVars.REQUEST_TIME, getRequestTime() );
		vars.put( ServerVars.REQUEST_URI, getUri() );
		vars.put( ServerVars.CONTENT_LENGTH, getContentLength() );
		vars.put( ServerVars.SERVER_IP, getLocalIpAddress() );
		vars.put( ServerVars.SERVER_NAME, Versioning.getProductSimple() );
		vars.put( ServerVars.SERVER_PORT, getLocalPort() );
		vars.put( ServerVars.HTTPS, isSecure() );
		vars.put( ServerVars.DOCUMENT_ROOT, ServerLoader.getWebRoot() );
		vars.put( ServerVars.SESSION, this );

		if ( getAuth() != null )
		{
			// Implement authorization as an optional builtin manageable feature, e.g., .htdigest.

			if ( auth.isDigest() )
				vars.put( ServerVars.AUTH_DIGEST, getAuth().getDigest() );

			if ( auth.isBasic() )
			{
				vars.put( ServerVars.AUTH_USER, getAuth().getUsername() );
				vars.put( ServerVars.AUTH_PW, getAuth().getPassword() );
			}

			vars.put( ServerVars.AUTH_TYPE, getAuth().getType() );
		}
	}

	/**
	 * Tries to check the "X-requested-with" header.
	 * Not a guaranteed method to determine if a request was made with AJAX since this header is not always set.
	 *
	 * @return Was the request made with AJAX
	 */
	public boolean isAjaxRequest()
	{
		return getHeader( "X-requested-with" ) != null && getHeader( "X-requested-with" ).equals( "XMLHttpRequest" );
	}

	public boolean isCDN()
	{
		// TODO Implement additional CDN detection methods
		return http.headers().contains( "CF-Connecting-IP" );
	}

	public boolean isSecure()
	{
		// TODO Is this reliable or should we respond based on the ssl param?
		return channel.pipeline().get( SslHandler.class ) != null;
	}

	public boolean isWebsocketRequest()
	{
		return "/fw/websocket".equals( getUri() );
	}

	public HttpMethod method()
	{
		return http.method();
	}

	public String methodString()
	{
		return http.method().toString();
	}

	public boolean nonceProcessed()
	{
		return nonceProcessed;
	}

	void nonceProcessed( boolean processed )
	{
		nonceProcessed = processed;
	}

	private Map<String, Object> parseMapArrays( Map<String, String> origMap )
	{
		Map<String, Object> result = Maps.newLinkedHashMap();

		for ( Entry<String, String> e : origMap.entrySet() )
		{
			String var = null;
			String key = null;
			String val = e.getValue();

			if ( e.getKey().contains( "[" ) && e.getKey().endsWith( "]" ) )
			{
				var = e.getKey().substring( 0, e.getKey().indexOf( "[" ) );

				if ( e.getKey().length() - e.getKey().indexOf( "[" ) > 1 )
					key = e.getKey().substring( e.getKey().indexOf( "[" ) + 1, e.getKey().length() - 1 );
				else
					key = "";
			}
			else
				var = e.getKey();

			if ( result.containsKey( var ) )
			{
				Object o = result.get( var );
				if ( o instanceof String )
				{
					if ( key == null || key.isEmpty() )
						key = "1";

					Map<String, String> hash = Maps.newLinkedHashMap();
					hash.put( "0", ( String ) o );
					hash.put( key, val );
					result.put( var, hash );
				}
				else if ( o instanceof Map )
				{
					@SuppressWarnings( "unchecked" )
					Map<String, String> map = ( Map<String, String> ) o;

					if ( key == null || key.isEmpty() )
					{
						int cnt = 0;
						while ( map.containsKey( cnt ) )
							cnt++;
						key = "" + cnt;
					}

					map.put( key, val );
				}
				else if ( key == null )
					result.put( var, val );
				else
				{
					if ( key.isEmpty() )
						key = "0";

					Map<String, String> hash = Maps.newLinkedHashMap();
					hash.put( key, val );
					result.put( var, hash );
				}

			}
			else if ( key == null )
				result.put( e.getKey(), e.getValue() );
			else
			{
				if ( key.isEmpty() )
					key = "0";

				Map<String, String> hash = Maps.newLinkedHashMap();
				hash.put( key, val );
				result.put( var, hash );
			}
		}

		return result;
	}

	protected void putAllGetMap( Map<String, String> map )
	{
		getMap.putAll( map );
		getMap.replaceAll( ( k, v ) -> v.trim() );
	}

	protected void putAllPostMap( Map<String, String> map )
	{
		postMap.putAll( map );
		postMap.replaceAll( ( k, v ) -> v.trim() );
	}

	protected void putGetMap( String key, String value )
	{
		getMap.put( key, value.trim() );
	}

	protected void putPostMap( String key, String value )
	{
		postMap.put( key, value.trim() );
	}

	protected void putRewriteParam( String key, String val )
	{
		rewriteMap.put( key, val );
	}

	protected void putRewriteParams( Map<String, String> map )
	{
		rewriteMap.putAll( map );
	}

	protected void putUpload( String name, UploadedFile uploadedFile )
	{
		uploadedFiles.put( name, uploadedFile );
	}

	// XXX Better Implement
	public void requireLogin() throws IOException
	{
		requireLogin( null );
	}

	/**
	 * First checks in an account is present, sends to login page if not.
	 * Second checks if the present accounts has the specified permission.
	 *
	 * @param permission
	 * @throws IOException
	 */
	public void requireLogin( String permission ) throws IOException
	{
		if ( !getSession().hasLogin() )
			getResponse().sendLoginPage();

		if ( permission != null )
			if ( !getSession().checkPermission( permission ).isTrue() )
				getResponse().sendError( HttpCode.HTTP_FORBIDDEN, "You must have the permission `" + permission + "` in order to view this page!" );
	}

	@Override
	public void sendMessage( MessageSender sender, Object... objs )
	{
		// Do Nothing
	}

	@Override
	public void sendMessage( Object... objs )
	{
		// Do Nothing
	}

	@Override
	public void sessionStarted()
	{
		getBinding().setVariable( "request", this );
		getBinding().setVariable( "response", getResponse() );
	}

	void setDomainMapping( DomainMapping domainMapping )
	{
		UtilObjects.notNull( domainMapping );
		this.domainMapping = domainMapping;
	}

	void setUri( String uri )
	{
		this.uri = uri.startsWith( "/" ) ? uri : "/" + uri;
	}

	protected boolean validateLogins() throws HttpError
	{
		Session session = getSession();

		if ( getArgument( "logout" ) != null )
		{
			AccountResult result = getSession().logout();

			if ( result.isSuccess() )
			{
				getResponse().sendLoginPage( result.getMessage() );
				return true;
			}
		}

		// TODO Implement One Time Login Tokens

		String locId = session.getLocation().getId();
		String username = getArgument( "user" );
		String password = getArgument( "pass" );
		boolean remember = getArgumentBoolean( "remember" );
		String target = getArgument( "target" );

		String loginPost = target == null || target.isEmpty() ? getLocation().getConfig().getString( "scripts.login-post", "/" ) : target;

		if ( loginPost.isEmpty() )
			loginPost = "/";

		if ( username != null && password != null )
		{
			try
			{
				if ( !ssl )
					AccountManager.getLogger().warning( "It is highly recommended that account logins are submitted over SSL. Without SSL, passwords are at great risk." );

				if ( !nonceProcessed() && ConfigRegistry.i().getBoolean( "accounts.requireLoginWithNonce" ) )
					throw new AccountException( AccountDescriptiveReason.NONCE_REQUIRED, locId, username );

				AccountResult result = getSession().loginWithException( AccountAuthenticator.PASSWORD, locId, username, password );
				Account acct = result.getAccount();

				session.remember( remember );

				SessionModule.getLogger().info( EnumColor.GREEN + "Successful Login: [id='" + acct.getId() + "',siteId='" + ( acct.getLocation() == null ? null : acct.getLocation().getId() ) + "',authenticator='plaintext']" );

				if ( getSite().getLoginPost() != null )
					getResponse().sendRedirect( getSite().getLoginPost() );
				else
					getResponse().sendLoginPage( "Your have been successfully logged in!", "success" );
			}
			catch ( AccountException e )
			{
				AccountResult result = e.getResult();

				String msg = result.getFormattedMessage();

				if ( !result.isIgnorable() && result.hasCause() )
					msg = result.getCause().getMessage();

				if ( Versioning.isDevelopment() && result.getDescriptiveReason() == AccountDescriptiveReason.INTERNAL_ERROR )
					throw new HttpError( e, msg );

				AccountManager.getLogger().warning( EnumColor.RED + "Failed Login [id='" + username + "',hasPassword='" + !UtilObjects.isEmpty( password ) + "',authenticator='plaintext',reason='" + msg + "']" );
				getResponse().sendLoginPage( result.getMessage(), null, target );
			}
			catch ( Throwable t )
			{
				if ( Versioning.isDevelopment() )
					throw new HttpError( t, AccountDescriptiveReason.INTERNAL_ERROR.getMessage() );

				AccountManager.getLogger().severe( "Login has thrown an internal server error", t );
				getResponse().sendLoginPage( AccountDescriptiveReason.INTERNAL_ERROR.getMessage(), null, target );
			}
			return true;
		}
		else if ( session.hasLogin() )
		{
			// XXX Should we revalidate logins with each request? It could be something worth considering for extra security. Maybe a config option?

			/*
			 * Maybe make this a server configuration option, e.g., sessions.revalidateLogins
			 *
			 * try
			 * {
			 * session.currentAccount.reloadAndValidate(); // <- Is this being overly redundant?
			 * Loader.getLogger().info( ChatColor.GREEN + "Current Login `Username \"" + session.currentAccount.getName() + "\", Password \"" + session.currentAccount.getMetaData().getPassword() + "\", UserId \"" +
			 * session.currentAccount.getAccountId() + "\", Display Name \"" + session.currentAccount.getDisplayName() + "\"`" );
			 * }
			 * catch ( LoginException e )
			 * {
			 * session.currentAccount = null;
			 * Loader.getLogger().warning( ChatColor.GREEN + "Login Failed `There was a login present but it failed validation with error: " + e.getMessage() + "`" );
			 * }
			 */
		}

		// Will we ever be using a session on more than one domains?
		if ( !getRootDomain().isEmpty() && session.getSessionCookie() != null && !session.getSessionCookie().getDomain().isEmpty() )
			if ( !session.getSessionCookie().getDomain().endsWith( getRootDomain() ) )
				NetworkManager.getLogger().warning( "The site `" + getSite().getId() + "` specifies the session cookie domain as `" + session.getSessionCookie().getDomain() + "` but the request was made on domain `" + getRootDomain() + "`. The session will not remain persistent." );

		return false;
	}
}
