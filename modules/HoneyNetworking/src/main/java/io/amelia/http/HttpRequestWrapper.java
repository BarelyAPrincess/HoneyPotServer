/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

import io.amelia.foundation.DevMetaProvider;
import io.amelia.foundation.Kernel;
import io.amelia.http.mappings.DomainMapping;
import io.amelia.http.mappings.DomainTree;
import io.amelia.http.session.Session;
import io.amelia.http.session.SessionContext;
import io.amelia.http.session.SessionWrapper;
import io.amelia.http.webroot.Webroot;
import io.amelia.http.webroot.WebrootRegistry;
import io.amelia.lang.HttpCode;
import io.amelia.lang.HttpError;
import io.amelia.logging.LogEvent;
import io.amelia.net.Networking;
import io.amelia.net.wip.NetworkLoader;
import io.amelia.scripting.HttpScriptingRequest;
import io.amelia.support.DateAndTime;
import io.amelia.support.Http;
import io.amelia.support.HttpRequestContext;
import io.amelia.support.Maps;
import io.amelia.support.NIO;
import io.amelia.support.Objs;
import io.amelia.support.Strs;
import io.amelia.support.Voluntary;
import io.amelia.support.http.HttpServerKey;
import io.amelia.support.http.HttpVariableMap;
import io.amelia.users.UserResult;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.CharsetUtil;

/**
 * Wraps the Netty HttpRequest and provides shortcut methods
 */
public class HttpRequestWrapper extends SessionWrapper implements SessionContext, HttpScriptingRequest, HttpServletRequest
{
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
	final Set<HoneyCookie> serverCookies = new HashSet<>();
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
	private HttpAsyncContext asyncContext = null;
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

		HttpScriptingRequest.putHttpScriptingRequest( this );

		// Set Time of this Request
		requestTime = DateAndTime.epoch();

		// Create a matching HttpResponseWrapper
		response = new HttpResponseWrapper( this, log );

		String host = Http.hostnameNormalize( getHostDomain() );

		if ( Objs.isEmpty( host ) )
			this.domainMapping = WebrootRegistry.getDefaultWebroot().getDefaultMapping();
		else if ( NIO.isValidIPv4( host ) || NIO.isValidIPv6( host ) )
		{
			Stream<DomainMapping> domains = DomainTree.getDomainMappingByIp( host );
			domainMapping = domains.count() == 0 ? null : domains.findFirst().orElse( null );
		}
		else
		{
			if ( "localhost".equals( host ) && !getIpAddress().startsWith( "127" ) && !getIpAddress().equals( getLocalIpAddress() ) )
				throw new HttpError( 418 );

			this.domainMapping = DomainTree.parseDomain( host ).getDomainMapping();
		}

		if ( domainMapping == null )
			domainMapping = WebrootRegistry.getDefaultWebroot().getMappings( "" ).findFirst().orElse( null );

		if ( getUri().startsWith( "/~" ) && domainMapping.getWebroot() == WebrootRegistry.getDefaultWebroot() )
		{
			String uri = getUri();
			int inx = uri.indexOf( "/", 2 );
			String webrootId = uri.substring( 2, inx == -1 ? uri.length() - 2 : inx );
			String newUri = inx == -1 ? "/" : uri.substring( inx );

			if ( !webrootId.equals( "wisp" ) )
				WebrootRegistry.getWebrootById( webrootId ).ifPresent( webroot -> {
					// Get the declared default domain mapping, the first if otherwise
					domainMapping = webroot.getDefaultMapping();
					setUri( newUri );
				} );
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
				ServerCookieDecoder.LAX.decode( var1 ).stream().map( HoneyCookie::from ).forEach( cookie -> {
					if ( cookie.getName().startsWith( "_ws" ) )
						serverCookies.add( cookie );
					else
						cookies.add( cookie );
				} );
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

	@Override
	public boolean authenticate( HttpServletResponse response ) throws IOException, ServletException
	{
		return false;
	}

	@Override
	public String changeSessionId()
	{
		return getSession().changeSessionId();
	}

	@Override
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

	@Override
	public String getArgument( String key )
	{
		String val = getMap.get( key );

		if ( val == null && postMap != null )
			val = postMap.get( key );

		if ( val == null && rewriteMap != null )
			val = rewriteMap.get( key );

		return val;
	}

	@Override
	public String getArgument( String key, String def )
	{
		String val = getArgument( key );
		return val == null ? def : val;
	}

	@Override
	public boolean getArgumentBoolean( String key )
	{
		String rtn = getArgument( key, "0" ).toLowerCase();
		return Objs.isTrue( rtn );
	}

	@Override
	public double getArgumentDouble( String key )
	{
		Object obj = getArgument( key, "-1.0" );
		return Objs.castToDouble( obj );
	}

	@Override
	public int getArgumentInt( String key )
	{
		Object obj = getArgument( key, "-1" );
		return Objs.castToInt( obj );
	}

	@Override
	public Set<String> getArgumentKeys()
	{
		Set<String> keys = new HashSet<>();
		keys.addAll( getMap.keySet() );
		keys.addAll( postMap.keySet() );
		keys.addAll( rewriteMap.keySet() );
		return keys;
	}

	@Override
	public long getArgumentLong( String key )
	{
		Object obj = getArgument( key, "-1" );
		return Objs.castToLong( obj );
	}

	@Override
	public Stream<Entry<String, String>> getArguments()
	{
		return Stream.concat( Stream.concat( getMap.entrySet().stream(), postMap.entrySet().stream() ), rewriteMap.entrySet().stream() );
	}

	@Override
	public AsyncContext getAsyncContext()
	{
		return null;
	}

	@Override
	public Object getAttribute( String name )
	{
		return null;
	}

	@Override
	public Enumeration<String> getAttributeNames()
	{
		return null;
	}

	public HttpAuthenticator getAuth()
	{
		if ( auth == null )
			initAuthorization();
		return auth;
	}

	@Override
	public String getAuthType()
	{
		return auth.getType();
	}

	@Override
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

	@Override
	public String getCharacterEncoding()
	{
		return;
	}

	@Override
	public String getChildDomain()
	{
		return domainMapping.getChildDomain();
	}

	@Override
	public int getContentLength()
	{
		return contentSize;
	}

	@Override
	public long getContentLengthLong()
	{

	}

	@Override
	public String getContentType()
	{

	}

	@Override
	public String getContextPath()
	{

	}

	@Override
	public Voluntary<HoneyCookie> getCookie( String key )
	{
		return Voluntary.of( cookies.stream().filter( cookie -> key.equalsIgnoreCase( cookie.getName() ) ).findFirst() );
	}

	@Override
	public Cookie[] getCookies()
	{
		return cookies.stream().map( HoneyCookie::getServletCookie ).toArray( Cookie[]::new );
	}

	@Override
	public long getDateHeader( String name )
	{

	}

	@Override
	public DispatcherType getDispatcherType()
	{

	}

	public DomainMapping getDomainMapping()
	{
		return domainMapping;
	}

	@Override
	public String getFullDomain()
	{
		return getFullDomain( null, ssl );
	}

	@Override
	public String getFullDomain( boolean ssl )
	{
		return getFullDomain( null, ssl );
	}

	@Override
	public String getFullDomain( String subdomain )
	{
		return getFullDomain( subdomain, ssl );
	}

	@Override
	public String getFullDomain( String subdomain, boolean ssl )
	{
		return getFullDomain( subdomain, ssl ? "https://" : "http://" );
	}

	@Override
	public String getFullDomain( String subdomain, String prefix )
	{
		return prefix + ( Objs.isEmpty( subdomain ) ? getHostDomain() : subdomain + "." + getRootDomain() ) + "/";
	}

	@Override
	public String getFullUrl()
	{
		return getFullUrl( null, ssl );
	}

	@Override
	public String getFullUrl( boolean ssl )
	{
		return getFullUrl( null, ssl );
	}

	@Override
	public String getFullUrl( String subdomain )
	{
		return getFullUrl( subdomain, ssl );
	}

	@Override
	public String getFullUrl( String subdomain, boolean ssl )
	{
		return getFullDomain( subdomain, ssl ) + getUri();
	}

	@Override
	public String getFullUrl( String subdomain, String prefix )
	{
		return getFullDomain( subdomain, prefix ) + getUri().substring( 1 );
	}

	@Override
	public Map<String, Object> getGetMap()
	{
		return parseMapArrays( getGetMapRaw() );
	}

	@Override
	public Map<String, String> getGetMapRaw()
	{
		return getMap;
	}

	@Override
	public String getHeader( String name )
	{
		return null;
	}

	@Override
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

	@Override
	public Enumeration<String> getHeaderNames()
	{
		return null;
	}

	@Override
	public Enumeration<String> getHeaders( String name )
	{
		return null;
	}

	public HttpHeaders getHeaders()
	{
		return http.headers();
	}

	@Override
	public Stream<HoneyCookie> getHoneyCookies()
	{
		return cookies.stream();
	}

	@Override
	public String getHost()
	{
		return Http.hostnameNormalize( http.headers().get( "Host" ) );
	}

	@Override
	public String getHostDomain()
	{
		if ( http.headers().contains( "Host" ) )
			return http.headers().get( "Host" ).split( "\\:" )[0];
		return null;
	}

	public HttpRequestContext getHttpContext()
	{
		return handler.getHttpRequestContext();
	}

	@Override
	public HttpMethod getHttpMethod()
	{
		return http.method();
	}

	@Override
	public HttpVersion getHttpVersion()
	{
		return http.protocolVersion();
	}

	/**
	 * Similar to {@link #getInetAddr()}
	 *
	 * @return the remote connections IP address
	 */
	@Override
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
	@Override
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

	@Override
	public ServletInputStream getInputStream() throws IOException
	{
		return null;
	}

	@Override
	public int getIntHeader( String name )
	{
		return 0;
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
	@Override
	public String getIpAddress( boolean detectCDN )
	{
		// TODO Implement other CDNs
		if ( detectCDN && http.headers().contains( "CF-Connecting-IP" ) )
			return http.headers().get( "CF-Connecting-IP" );

		return ( ( InetSocketAddress ) channel.remoteAddress() ).getAddress().getHostAddress();
	}

	@Override
	public String getLocalAddr()
	{
		return null;
	}

	@Override
	public String getLocalHostName()
	{
		return ( ( InetSocketAddress ) channel.localAddress() ).getHostName();
	}

	@Override
	public String getLocalIpAddress()
	{
		return ( ( InetSocketAddress ) channel.localAddress() ).getAddress().getHostAddress();
	}

	@Override
	public String getLocalName()
	{
		return null;
	}

	@Override
	public int getLocalPort()
	{
		return ( ( InetSocketAddress ) channel.localAddress() ).getPort();
	}

	@Override
	public Locale getLocale()
	{
		return null;
	}

	@Override
	public Enumeration<Locale> getLocales()
	{
		return null;
	}

	@Override
	public String getMethod()
	{
		return http.method().toString();
	}

	public HttpRequest getOriginalRequest()
	{
		return http;
	}

	@Override
	public String getParameter( String key )
	{

	}

	@Override
	public Map<String, String[]> getParameterMap()
	{

	}

	@Override
	public Enumeration<String> getParameterNames()
	{

	}

	@Override
	public String[] getParameterValues( String name )
	{

	}

	@Override
	public Part getPart( String name ) throws IOException, ServletException
	{

	}

	@Override
	public Collection<Part> getParts() throws IOException, ServletException
	{
	}

	@Override
	public String getPathInfo()
	{
	}

	@Override
	public String getPathTranslated()
	{
	}

	@Override
	public Map<String, Object> getPostMap()
	{
		return parseMapArrays( getPostMapRaw() );
	}

	@Override
	public Map<String, String> getPostMapRaw()
	{
		return postMap;
	}

	@Override
	public String getProtocol()
	{
	}

	@Override
	public String getQuery()
	{
		if ( getMap.isEmpty() )
			return "";
		return "?" + Strs.join( getMap, "&", "=", "null" );
	}

	@Override
	public String getQueryString()
	{
	}

	@Override
	public BufferedReader getReader() throws IOException
	{
	}

	@Override
	public String getRealPath( String path )
	{
	}

	@Override
	public String getRemoteAddr()
	{
	}

	@Override
	public String getRemoteHost()
	{
	}

	@Override
	public String getRemoteHostname()
	{
		return ( ( InetSocketAddress ) channel.remoteAddress() ).getHostName();
	}

	@Override
	public int getRemotePort()
	{
		return ( ( InetSocketAddress ) channel.remoteAddress() ).getPort();
	}

	@Override
	public String getRemoteUser()
	{
	}

	public HttpRequestContext getRequestContext()
	{
		return handler.getHttpRequestContext();
	}

	@Override
	public RequestDispatcher getRequestDispatcher( String path )
	{
	}

	@Override
	public String getRequestHost()
	{
		return getHeader( "Host" );
	}

	@Override
	public Map<String, Object> getRequestMap() throws Exception
	{
		return parseMapArrays( getRequestMapRaw() );
	}

	@Override
	public Map<String, String> getRequestMapRaw() throws Exception
	{
		Map<String, String> requestMap = new TreeMap<>();
		Maps.joinMaps( requestMap, getMap );
		Maps.joinMaps( requestMap, postMap );
		Maps.joinMaps( requestMap, rewriteMap );
		return requestMap;
	}

	@Override
	public long getRequestTime()
	{
		return requestTime;
	}

	@Override
	public String getRequestURI()
	{
	}

	@Override
	public StringBuffer getRequestURL()
	{
	}

	@Override
	public String getRequestedSessionId()
	{
	}

	public HttpResponseWrapper getResponse()
	{
		return response;
	}

	@Override
	public Map<String, String> getRewriteMap()
	{
		return rewriteMap;
	}

	@Override
	public String getRootDomain()
	{
		return domainMapping.getRootDomain();
	}

	@Override
	public String getScheme()
	{
	}

	@Override
	public HttpVariableMap getServer()
	{
		return vars;
	}

	@Override
	public Voluntary<HoneyCookie> getServerCookie( String key )
	{
		return Voluntary.of( serverCookies.stream().filter( cookie -> key.equalsIgnoreCase( cookie.getName() ) ).findAny() );
	}

	@Override
	public Stream<HoneyCookie> getServerCookies()
	{
		return serverCookies.stream();
	}

	@Override
	public String getServerName()
	{
		return ( ( InetSocketAddress ) channel.localAddress() ).getHostName();
	}

	@Override
	public int getServerPort()
	{
		return ( ( InetSocketAddress ) channel.localAddress() ).getPort();
	}

	@Override
	public ServletContext getServletContext()
	{
	}

	@Override
	public String getServletPath()
	{
	}

	@Override
	public HttpSession getSession( boolean create )
	{
	}

	@Override
	public String getTopDomain()
	{
		return getFullDomain( null, ssl );
	}

	@Override
	public String getTopDomain( boolean ssl )
	{
		return getFullDomain( null, ssl );
	}

	@Override
	public String getTopDomain( String subdomain )
	{
		return getFullDomain( subdomain, ssl );
	}

	@Override
	public String getTopDomain( String subdomain, boolean ssl )
	{
		return getTopDomain( subdomain, ssl ? "https://" : "http://" );
	}

	@Override
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

	@Override
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

	@Override
	public String getUserAgent()
	{
		return getHeader( "User-Agent" );
	}

	@Override
	public Principal getUserPrincipal()
	{

	}

	public String getWebSocketLocation( HttpObject req )
	{
		return ( isSecure() ? "wss://" : "ws://" ) + getHost() + "/~wisp/ws";
	}

	@Override
	public Webroot getWebroot()
	{
		return domainMapping.getWebroot();
	}

	@Override
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
		DevMetaProvider meta = Kernel.getDevMeta();

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

			if ( auth.isTypeDigest() )
				vars.put( HttpServerKey.AUTH_DIGEST, auth.getDigest() );

			if ( auth.isTypeBasic() )
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
	@Override
	public boolean isAjaxRequest()
	{
		return getHeader( "X-requested-with" ) != null && getHeader( "X-requested-with" ).equals( "XMLHttpRequest" );
	}

	@Override
	public boolean isAsyncStarted()
	{
		return false;
	}

	@Override
	public boolean isAsyncSupported()
	{
		return false;
	}

	@Override
	public boolean isCDN()
	{
		// TODO Implement additional CDN detection methods
		return http.headers().contains( "CF-Connecting-IP" );
	}

	public boolean isNonceProcessed()
	{
		return nonceProcessed;
	}

	@Override
	public boolean isRequestedSessionIdFromCookie()
	{
		return true; // True for now
	}

	@Override
	public boolean isRequestedSessionIdFromURL()
	{
		return false; // False for now
	}

	@Override
	public boolean isRequestedSessionIdFromUrl()
	{
		return false; // False for now
	}

	@Override
	public boolean isRequestedSessionIdValid()
	{
		return true;
	}

	@Override
	public boolean isSecure()
	{
		// TODO Is this reliable or should we respond based on the ssl param?
		return channel.pipeline().get( SslHandler.class ) != null;
	}

	@Override
	public boolean isUserInRole( String role )
	{
		return false; // We don't support roles - presently
	}

	@Override
	public boolean isWebsocketRequest()
	{
		return "/~wisp/ws".equals( getUri() );
	}

	@Override
	public void login( String username, String password ) throws ServletException
	{
		throw new ServletException( "Not implemented" );
	}

	@Override
	public void logout() throws ServletException
	{
		throw new ServletException( "Not implemented" );
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

	@Override
	public void removeAttribute( String name )
	{
		setAttribute( name, null );
	}

	// XXX Better Implement
	@Override
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
	@Override
	public void requireLogin( String permission ) throws IOException
	{
		if ( !getSession().hasLogin() )
			getResponse().sendLoginPage();

		if ( permission != null )
			if ( !getSession().checkPermission( permission ).isTrue() )
				getResponse().sendError( HttpCode.HTTP_FORBIDDEN, "You must have the permission `" + permission + "` in order to view this page!" );
	}

	@Override
	public void sessionStarted()
	{
		getBinding().setVariable( "request", this );
		getBinding().setVariable( "response", getResponse() );
	}

	@Override
	public void setAttribute( String name, Object value )
	{

	}

	@Override
	public void setCharacterEncoding( String encoding )
	{
		Charset.forName( encoding );
	}

	public void setDomainMapping( @Nonnull DomainMapping domainMapping )
	{
		this.domainMapping = domainMapping;
	}

	void setNonceProcessed( boolean nonceProcessed )
	{
		this.nonceProcessed = nonceProcessed;
	}

	public void setUri( String uri )
	{
		this.uri = uri.startsWith( "/" ) ? uri : "/" + uri;
	}

	@Override
	public AsyncContext startAsync() throws IllegalStateException
	{
		if ( !isAsyncSupported() )
			throw new IllegalStateException( "Async is not supported!" );
		if ( asyncContext == null )
			asyncContext = new HttpAsyncContext( this, response, true );
		else if ( !asyncContext.isCompleted() )
			throw new IllegalStateException( "Previous AsyncContext#complete() was not yet called!" );
		else
			asyncContext.reset();
		return asyncContext;
	}

	@Override
	public AsyncContext startAsync( ServletRequest servletRequest, ServletResponse servletResponse ) throws IllegalStateException
	{
		if ( !isAsyncSupported() )
			throw new IllegalStateException( "Async is not supported!" );
		if ( asyncContext == null )
			asyncContext = new HttpAsyncContext( servletRequest, servletResponse, servletRequest == this && servletResponse == response );
		else if ( !asyncContext.isCompleted() )
			throw new IllegalStateException( "Previous AsyncContext#complete() was not yet called!" );
		else
			asyncContext.reset();
		return asyncContext;
	}

	@Override
	public <T extends HttpUpgradeHandler> T upgrade( Class<T> handlerClass ) throws IOException, ServletException
	{

	}

	protected boolean validateLogins() throws HttpError
	{
		Session session = getSession();

		if ( getArgument( "logout" ) != null )
		{
			UserResult result = getSession().logout();

			if ( result.getReportingLevel().isSuccess() )
			{
				getResponse().sendLoginPage( result.getDescriptiveReason().getReasonMessage() );
				return true;
			}
		}

		// TODO Implement One Time Login Tokens

		String webrootId = session.getWebroot().getWebrootId();
		String username = getArgument( "user" );
		String password = getArgument( "pass" );
		boolean remember = getArgumentBoolean( "remember" );
		String target = getArgument( "target" );

		String loginPost = target == null || target.isEmpty() ? getWebroot().getLoginPost() : target;

		if ( loginPost.isEmpty() )
			loginPost = "/";

		if ( username != null && password != null )
		{
			return false;
			// throw new UserException.Runtime( Foundation.getNullEntity(), DescriptiveReason.FEATURE_NOT_IMPLEMENTED );
			/*
			try
			{
				if ( !ssl )
					Users.L.warning( "It's highly recommended that logins are submitted over SSL. Without SSL, passwords are at great risk." );

				if ( !isNonceProcessed() && ConfigRegistry.config.getBoolean( "accounts.requireLoginWithNonce" ).orElse( true ) )
					throw new UserException.Error( new EntityPrincipal( username ), DescriptiveReason.NONCE_REQUIRED );

				UserResult result = getSession().loginWithException( UserAuthenticator.PASSWORD, username, password );
				UserContext acct = result.getUser();

				session.remember( remember );

				SessionRegistry.L.info( EnumColor.GREEN + "Successful Login: [uuid='" + acct.uuid().toString() + "',webrootId='" + getWebroot().getWebrootId() + "',authenticator='plaintext']" );

				if ( getWebroot().getLoginPost() != null )
					getResponse().sendRedirect( getWebroot().getLoginPost() );
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
			return true;*/
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
				NetworkLoader.L.warning( "The webroot `" + getWebroot().getWebrootId() + "` specifies the session cookie domain as `" + session.getSessionCookie().getDomain() + "` but the request was made on domain `" + getRootDomain() + "`. The session will not remain persistent." );

		return false;
	}
}
