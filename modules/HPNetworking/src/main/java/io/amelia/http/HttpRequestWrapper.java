/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.http;

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Stream;

import io.amelia.foundation.ConfigRegistry;
import io.amelia.foundation.ImplDevMeta;
import io.amelia.foundation.Kernel;
import io.amelia.http.mappings.DomainMapping;
import io.amelia.http.session.Session;
import io.amelia.http.session.SessionContext;
import io.amelia.http.session.SessionWrapper;
import io.amelia.http.webroot.Webroot;
import io.amelia.http.webroot.WebrootRegistry;
import io.amelia.lang.ApplicationException;
import io.amelia.logging.LogEvent;
import io.amelia.networking.NetworkLoader;
import io.amelia.networking.Networking;
import io.amelia.support.DateAndTime;
import io.amelia.support.EnumColor;
import io.amelia.support.Http;
import io.amelia.support.HttpRequestContext;
import io.amelia.support.Maps;
import io.amelia.support.NIO;
import io.amelia.support.Objs;
import io.amelia.support.Strs;
import io.amelia.support.Voluntary;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.CharsetUtil;

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
	 * Cookie Cache
	 */
	final Set<HoneyCookie> cookies = new HashSet<>();
	/**
	 * The Get Map
	 */
	final Map<String, String> getMap = new TreeMap<>();
	/**
	 * The {@link HttpHandler} for this request
	 */
	final HttpHandler handler;
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
	final Set<Cookie> serverCookies = new HashSet<>();
	/**
	 * Is this a SSL request
	 */
	final boolean ssl;
	/**
	 * Files uploaded with this request
	 */
	final Map<String, UploadedFile> uploadedFiles = new HashMap<>();
	/**
	 * The original Netty Channel
	 */
	private final Channel channel;
	/**
	 * The original Netty Http Request
	 */
	private final HttpRequest http;
	/**
	 * The size of the posted content
	 */
	int contentSize = 0;
	/**
	 * Server Variables
	 */
	HttpVariableMap vars = new HttpVariableMap();
	private HttpAuthenticator auth = null;
	private DomainMapping domainMapping;
	private boolean nonceProcessed = false;
	/**
	 * The requested URI
	 */
	private String uri = null;

	HttpRequestWrapper( Channel channel, HttpRequest http, HttpHandler handler, boolean ssl, LogEvent log ) throws IOException, HttpError
	{
		this.channel = channel;
		this.http = http;
		this.handler = handler;
		this.ssl = ssl;
		this.log = log;

		putRequest( this );

		// Set Time of this Request
		requestTime = DateAndTime.epoch();

		// Create a matching HttpResponseWrapper
		response = new HttpResponseWrapper( this, log );

		String host = Http.hostnameNormalize( getHostDomain() );

		if ( Objs.isEmpty( host ) )
			this.domainMapping = WebrootRegistry.getDefaultWebroot().getDefaultMapping();
		else if ( NIO.isValidIPv4( host ) || NIO.isValidIPv6( host ) )
		{
			Stream<DomainMapping> domains = WebrootRegistry.getDomainMappingsByIp( host );
			domainMapping = domains.count() == 0 ? null : domains.findFirst().orElse( null );
		}
		else
		{
			if ( "localhost".equals( host ) && !getIpAddress().startsWith( "127" ) && !getIpAddress().equals( getLocalIpAddress() ) )
				throw new HttpError( 418 );

			this.domainMapping = WebrootRegistry.getDomainMapping( host );
		}

		if ( domainMapping == null )
			domainMapping = WebrootRegistry.getDefaultWebroot().getMappings( "" ).findFirst().orElse( null );

		if ( getUri().startsWith( "/~" ) && domainMapping.getWebroot() == WebrootRegistry.getDefaultWebroot() )
		{
			String uri = getUri();
			int inx = uri.indexOf( "/", 2 );
			String siteId = uri.substring( 2, inx == -1 ? uri.length() - 2 : inx );
			String newUri = inx == -1 ? "/" : uri.substring( inx );

			Webroot webroot = WebrootRegistry.getWebrootById( siteId );
			if ( !siteId.equals( "wisp" ) && webroot != null )
			{
				// Get the declared default domain mapping, the first if otherwise
				domainMapping = webroot.getDefaultMapping();
				setUri( newUri );
			}
		}

		// log.log( Level.INFO, "SiteId: " + webroot.getSiteId() + ", ParentDomain: " + rootDomain + ", ChildDomain: " + childDomain );

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
		// String var1 = URLDecoder.decode( http.headers().get( "Cookie" ), Charsets.UTF_8.displayName() );
		String var1 = http.headers().get( "Cookie" );

		// TODO fix missing invalid stuff

		if ( var1 != null )
			try
			{
				Set<Cookie> var2 = ServerCookieDecoder.LAX.decode( var1 );
				for ( Cookie cookie : var2 )
					if ( cookie.name().startsWith( "_ws" ) )
						serverCookies.add( cookie );
					else
						cookies.add( cookie );
			}
			catch ( IllegalArgumentException | NullPointerException e )
			{
				Networking.L.severe( "Failed to parse cookie for reason: " + e.getMessage() );
				// Networking.L.warning( "There was a problem decoding the request cookie.", e );
				// Networking.L.debug( "Cookie: " + var1 );
				// Networking.L.debug( "Headers: " + Joiner.on( "," ).withKeyValueSeparator( "=" ).join( http.headers() ) );
			}

		initServerVars();
	}

	public void enforceTrailingSlash( boolean enforce )
	{
		if ( enforce )
		{
			if ( !Objs.isEmpty( uri ) && !uri.endsWith( "/" ) )
			{
				NetworkLoader.L.fine( "Forcing URL redirect to have trailing slash." );
				getResponse().sendRedirect( getFullDomain() + uri + "/", HttpCode.HTTP_MOVED_PERM );
			}
		}
		else
		{
			if ( uri.endsWith( "/" ) )
			{
				NetworkLoader.L.fine( "Forcing URL redirect to have NO trailing slash." );
				getResponse().sendRedirect( getFullDomain() + uri.substring( uri.length() - 1 ), HttpCode.HTTP_MOVED_PERM );
			}
		}
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
		return Objs.isTrue( rtn );
	}

	public double getArgumentDouble( String key )
	{
		Object obj = getArgument( key, "-1.0" );
		return Objs.castToDouble( obj );
	}

	public int getArgumentInt( String key )
	{
		Object obj = getArgument( key, "-1" );
		return Objs.castToInt( obj );
	}

	public Stream<Entry<String, String>> getArguments()
	{
		return Stream.concat( Stream.concat( getMap.entrySet().stream(), postMap.entrySet().stream() ), rewriteMap.entrySet().stream() );
	}

	public Set<String> getArgumentKeys()
	{
		Set<String> keys = new HashSet<>();
		keys.addAll( getMap.keySet() );
		keys.addAll( postMap.keySet() );
		keys.addAll( rewriteMap.keySet() );
		return keys;
	}

	public long getArgumentLong( String key )
	{
		Object obj = getArgument( key, "-1" );
		return Objs.castToLong( obj );
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

	public String getChildDomain()
	{
		return domainMapping.getChildDomain();
	}

	public int getContentLength()
	{
		return contentSize;
	}

	@Override
	public Voluntary<Cookie, ApplicationException.Error> getCookie( String key )
	{
		for ( Cookie cookie : cookies )
			if ( key.equalsIgnoreCase( cookie.name() ) )
				return Voluntary.of( cookie );
		return Voluntary.empty();
	}

	@Override
	public Stream<HoneyCookie> getCookies()
	{
		return cookies.stream();
	}

	public DomainMapping getDomainMapping()
	{
		return domainMapping;
	}

	void setDomainMapping( DomainMapping domainMapping )
	{
		Objs.notNull( domainMapping );
		this.domainMapping = domainMapping;
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
		return prefix + ( Objs.isEmpty( subdomain ) ? getHostDomain() : subdomain + "." + getRootDomain() ) + "/";
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
			return http.headers().get( key );
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
		return Http.hostnameNormalize( http.headers().get( "Host" ) );
	}

	public String getHostDomain()
	{
		if ( http.headers().contains( "Host" ) )
			return http.headers().get( "Host" ).split( "\\:" )[0];
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
	 *
	 * @return the remote connections IP address
	 */
	public InetAddress getInetAddr( boolean detectCDN )
	{
		if ( detectCDN && http.headers().contains( "CF-Connecting-IP" ) )
			try
			{
				return InetAddress.getByName( http.headers().get( "CF-Connecting-IP" ) );
			}
			catch ( UnknownHostException e )
			{
				e.printStackTrace();
				return null;
			}

		return ( ( InetSocketAddress ) channel.remoteAddress() ).getAddress();
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
	 *
	 * @return the remote connections IP address as a string
	 */
	public String getIpAddress( boolean detectCDN )
	{
		// TODO Implement other CDNs
		if ( detectCDN && http.headers().contains( "CF-Connecting-IP" ) )
			return http.headers().get( "CF-Connecting-IP" );

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
		return "?" + Strs.join( getMap, "&", "=", "null" );
	}

	public String getRemoteHostname()
	{
		return ( ( InetSocketAddress ) channel.remoteAddress() ).getHostName();
	}

	public int getRemotePort()
	{
		return ( ( InetSocketAddress ) channel.remoteAddress() ).getPort();
	}

	public HttpRequestContext getRequestContext()
	{
		return handler.getRequestContext();
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
		Maps.joinMaps( requestMap, getMap );
		Maps.joinMaps( requestMap, postMap );
		Maps.joinMaps( requestMap, rewriteMap );
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

	public String getRootDomain()
	{
		return domainMapping.getRootDomain();
	}

	public HttpVariableMap getServer()
	{
		return vars;
	}

	@Override
	protected Voluntary<Cookie, ApplicationException.Error> getServerCookie( String key )
	{
		return Voluntary.of( serverCookies.stream().filter( cookie -> key.equalsIgnoreCase( cookie.name() ) ).findAny().orElse( null ) );
	}

	public Stream<Cookie> getServerCookies()
	{
		return serverCookies.stream();
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
		return prefix + ( Objs.isEmpty( subdomain ) ? "" : subdomain + "." ) + getRootDomain() + "/";
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
				uri = URLDecoder.decode( uri, CharsetUtil.UTF_8.name() );
			}
			catch ( UnsupportedEncodingException e )
			{
				try
				{
					uri = URLDecoder.decode( uri, CharsetUtil.ISO_8859_1.name() );
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

			if ( uri.startsWith( "/" ) )
				uri = uri.substring( 1 );
		}

		return uri;
	}

	void setUri( String uri )
	{
		this.uri = uri.startsWith( "/" ) ? uri : "/" + uri;
	}

	public String getUserAgent()
	{
		return getHeader( "User-Agent" );
	}

	public Webroot getWebroot()
	{
		return domainMapping.getWebroot();
	}

	public String getWebSocketLocation( HttpObject req )
	{
		return ( isSecure() ? "wss://" : "ws://" ) + getHost() + "/~wisp/ws";
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

	/**
	 * Initializes the serverVars with initial information from this request
	 */
	private void initServerVars()
	{
		ImplDevMeta meta = Kernel.getDevMeta();

		vars.put( HttpServerKey.SERVER_SOFTWARE, meta.getProductName() );
		vars.put( HttpServerKey.SERVER_VERSION, meta.getVersionString() );
		vars.put( HttpServerKey.SERVER_ADMIN, meta.getDeveloperEmail() );
		vars.put( HttpServerKey.SERVER_SIGNATURE, meta.getProductSignature() );
		vars.put( HttpServerKey.HTTP_VERSION, http.protocolVersion() );
		vars.put( HttpServerKey.HTTP_ACCEPT, getHeader( "Accept" ) );
		vars.put( HttpServerKey.HTTP_USER_AGENT, getUserAgent() );
		vars.put( HttpServerKey.HTTP_CONNECTION, getHeader( "Connection" ) );
		vars.put( HttpServerKey.HTTP_HOST, getLocalHostName() );
		vars.put( HttpServerKey.HTTP_ACCEPT_ENCODING, getHeader( "Accept-Encoding" ) );
		vars.put( HttpServerKey.HTTP_ACCEPT_LANGUAGE, getHeader( "Accept-Language" ) );
		vars.put( HttpServerKey.HTTP_X_REQUESTED_WITH, getHeader( "X-requested-with" ) );
		vars.put( HttpServerKey.REMOTE_HOST, getRemoteHostname() );
		vars.put( HttpServerKey.REMOTE_ADDR, getIpAddress() );
		vars.put( HttpServerKey.REMOTE_PORT, getRemotePort() );
		vars.put( HttpServerKey.REQUEST_TIME, getRequestTime() );
		vars.put( HttpServerKey.REQUEST_URI, getUri() );
		vars.put( HttpServerKey.CONTENT_LENGTH, getContentLength() );
		vars.put( HttpServerKey.SERVER_IP, getLocalIpAddress() );
		vars.put( HttpServerKey.SERVER_NAME, meta.getProductName() );
		vars.put( HttpServerKey.SERVER_PORT, getLocalPort() );
		vars.put( HttpServerKey.HTTPS, isSecure() );
		vars.put( HttpServerKey.DOCUMENT_ROOT, Kernel.getPath( WebrootRegistry.PATH_WEBROOT ) );
		vars.put( HttpServerKey.SESSION, this );

		if ( getAuth() != null )
		{
			// Implement authorization as an optional builtin manageable feature, e.g., .htdigest.

			if ( auth.isDigest() )
				vars.put( HttpServerKey.AUTH_DIGEST, auth.getDigest() );

			if ( auth.isBasic() )
			{
				vars.put( HttpServerKey.AUTH_USER, auth.getUsername() );
				vars.put( HttpServerKey.AUTH_PW, auth.getPassword() );
			}

			vars.put( HttpServerKey.AUTH_TYPE, auth.getType() );
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
		return "/~wisp/ws".equals( getUri() );
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
		Map<String, Object> result = new LinkedHashMap<>();

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

					Map<String, String> hash = new LinkedHashMap<>();
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
						key = Integer.toString( cnt );
					}

					map.put( key, val );
				}
				else if ( key == null )
					result.put( var, val );
				else
				{
					if ( key.isEmpty() )
						key = "0";

					Map<String, String> hash = new LinkedHashMap<>();
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

				Map<String, String> hash = new LinkedHashMap<>();
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
	 *
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

		String loginPost = target == null || target.isEmpty() ? this.getWebroot().getConfig().getString( "scripts.login-post", "/" ) : target;

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

				AccountManager.getLogger().warning( EnumColor.RED + "Failed Login [id='" + username + "',hasPassword='" + !Objs.isEmpty( password ) + "',authenticator='plaintext',reason='" + msg + "']" );
				getResponse().sendLoginPage( result.getMessage(), null, target );
			}
			catch ( Throwable t )
			{
				if ( Kernel.isDevelopment() )
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
			 * Maybe make this a server data option, e.g., sessions.revalidateLogins
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
				NetworkLoader.L.warning( "The webroot `" + getSite().getId() + "` specifies the session cookie domain as `" + session.getSessionCookie().getDomain() + "` but the request was made on domain `" + getRootDomain() + "`. The session will not remain persistent." );

		return false;
	}
}
