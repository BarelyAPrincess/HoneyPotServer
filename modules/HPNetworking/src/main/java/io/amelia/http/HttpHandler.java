/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.http;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import io.amelia.events.EventException;
import io.amelia.events.Events;
import io.amelia.foundation.ConfigRegistry;
import io.amelia.foundation.Kernel;
import io.amelia.http.apache.ApacheHandler;
import io.amelia.http.events.RequestEvent;
import io.amelia.http.mappings.DomainMapping;
import io.amelia.http.session.Session;
import io.amelia.http.webroot.BaseWebroot;
import io.amelia.lang.ExceptionReport;
import io.amelia.lang.MultipleException;
import io.amelia.lang.ReportingLevel;
import io.amelia.scripting.ScriptingContext;
import io.amelia.support.HttpRequestContext;
import io.amelia.support.Objs;
import io.amelia.support.TriEnum;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderUtil;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.DiskAttribute;
import io.netty.handler.codec.http.multipart.DiskFileUpload;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.EndOfDataDecoderException;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.ErrorDataDecoderException;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.IllegalReferenceCountException;

import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Handles both HTTP and HTTPS connections for Netty.
 */
public class HttpHandler extends SimpleChannelInboundHandler<Object>
{
	private static HttpDataFactory factory;

	static
	{
		/*
		 * Determines the minimum file size required to create a physical temporary file.
		 * See {@link DefaultHttpDataFactory#DefaultHttpDataFactory(boolean)} and {@link DefaultHttpDataFactory#DefaultHttpDataFactory(long)}
		 */
		long minsize = ConfigRegistry.config.getLong( "server.fileUploadMinInMemory" ).orElse( DefaultHttpDataFactory.MINSIZE );

		if ( minsize < 1 ) // Less then 1kb = always
			factory = new DefaultHttpDataFactory( true );
		if ( minsize > 102400 ) // Greater then 100mb = never
			factory = new DefaultHttpDataFactory( false );
		else
			factory = new DefaultHttpDataFactory( minsize );

		setTempDirectory( Kernel.getPath( Kernel.PATH_CACHE ) );
	}

	/**
	 * Sends the 100 continue response
	 *
	 * @param ctx the Channel
	 */
	private static void send100Continue( ChannelHandlerContext ctx )
	{
		FullHttpResponse response = new DefaultFullHttpResponse( HTTP_1_1, CONTINUE );
		ctx.write( response );
	}

	/**
	 * Updates the default temporary file directory
	 *
	 * @param tmpDir The temp directory
	 */
	public static void setTempDirectory( File tmpDir )
	{
		// TODO Config option to delete temporary files on exit?
		// DiskFileUpload.deleteOnExitTemporaryFile = true;
		// DiskAttribute.deleteOnExitTemporaryFile = true;

		DiskFileUpload.baseDirectory = tmpDir.getAbsolutePath();
		DiskAttribute.baseDirectory = tmpDir.getAbsolutePath();
	}

	/**
	 * Simple Time and Date formats
	 */
	final SimpleDateFormat dateFormat = new SimpleDateFormat( ConfigRegistry.config.getString( "console.dateFormat" ).orElse( "MM-dd" ) );
	final SimpleDateFormat timeFormat = new SimpleDateFormat( ConfigRegistry.config.getString( "console.timeFormat" ).orElse( "HH:mm:ss.SSS" ) );
	/**
	 * Is this handler used on secure connections
	 */
	private final boolean ssl;
	/**
	 * The selected Webroot
	 */
	private BaseWebroot currentWebroot;
	/**
	 * The POST body decoder
	 */
	private HttpPostRequestDecoder decoder;
	/**
	 * The {@link HttpRequestContext} used to parse annotations, file encoding, and etc.
	 */
	private HttpRequestContext fi;
	/**
	 * The WebSocket handshaker
	 */
	private WebSocketServerHandshaker handshaker = null;
	/**
	 * The simplified event logger
	 */
	private LogEvent log;
	/**
	 * The originating HTTP request
	 */
	private HttpRequestWrapper request;
	/**
	 * Has the request finished, used by {@link #exceptionCaught(ChannelHandlerContext, Throwable)}
	 */
	private boolean requestFinished = false;

	/**
	 * The raw originating Netty object
	 */
	private FullHttpRequest requestOrig;
	/**
	 * The destination HTTP response
	 */
	private HttpResponseWrapper response;

	/**
	 * Constructs a new HttpHandler, used within the Netty HTTP stream
	 *
	 * @param ssl Will this handler be used on a secure connection
	 */
	public HttpHandler( boolean ssl )
	{
		this.ssl = ssl;
		log = LogManager.logEvent( "" + hashCode() );
	}

	@Override
	public void channelInactive( ChannelHandlerContext ctx ) throws Exception
	{
		if ( decoder != null )
		{
			decoder.cleanFiles();
			decoder.destroy();
			decoder = null;
		}

		// Nullify references
		handshaker = null;
		response = null;
		requestOrig = null;
		request = null;
		log = null;
		requestFinished = false;
	}

	@Override
	public void exceptionCaught( ChannelHandlerContext ctx, Throwable cause ) throws Exception
	{
		try
		{
			if ( request == null || response == null )
			{
				NetworkManager.getLogger().severe( EnumColor.NEGATIVE + "" + EnumColor.RED + "We got an unexpected exception before the connection was processed:", cause );

				StringBuilder sb = new StringBuilder();
				sb.append( "<h1>500 - Internal Server Error</h1>\n" );
				sb.append( "<p>The server had encountered an unexpected exception before it could fully process your request, so no extended debug information is or will be available.</p>\n" );
				sb.append( "<p>The exception has been logged to the console, so we can only hope the exception is noticed and resolved. We apologize for any inconvenience.</p>\n" );
				sb.append( "<p><i>You have a good day now and we will see you again soon. :)</i></p>\n" );
				sb.append( "<hr>\n" );
				sb.append( Versioning.getHTMLFooter() );

				FullHttpResponse response = new DefaultFullHttpResponse( HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf( 500 ), Unpooled.wrappedBuffer( sb.toString().getBytes() ) );
				ctx.write( response );

				return;
			}

			String ip = request.getIpAddress();

			if ( requestFinished && cause instanceof HttpError )
			{
				int code = ( ( HttpError ) cause ).getHttpCode();

				if ( code >= 400 && code <= 499 )
					NetworkSecurity.addStrikeToIp( ip, IpStrikeType.HTTP_ERROR_400 );
				if ( code >= 500 && code <= 599 )
					NetworkSecurity.addStrikeToIp( ip, IpStrikeType.HTTP_ERROR_500 );

				if ( response.getStage() != HttpResponseStage.CLOSED )
					response.sendError( ( HttpError ) cause );
				else
					NetworkManager.getLogger().severe( EnumColor.NEGATIVE + "" + EnumColor.RED + " [" + ip + "] For reasons unknown, we caught a HttpError but the connection was already closed.", cause );
				return;
			}

			if ( requestFinished && "Connection reset by peer".equals( cause.getMessage() ) )
			{
				NetworkManager.getLogger().warning( EnumColor.NEGATIVE + "" + EnumColor.RED + " [" + ip + "] The connection was closed before we could finish the request, if the IP continues to abuse the system it WILL BE BANNED!" );
				NetworkSecurity.addStrikeToIp( ip, IpStrikeType.CLOSED_EARLY );
				return;
			}

			ScriptingException evalOrig = null;

			/*
			 * Unpackage the EvalFactoryException.
			 * Not sure if exceptions from the EvalFactory should be handled differently or not.
			 * XXX Maybe skip generating exception pages for errors that were caused internally and report them to Chiori-chan unless the server is in development mode?
			 */
			if ( cause instanceof ScriptingException && cause.getCause() != null )
			{
				evalOrig = ( ScriptingException ) cause;
				cause = cause.getCause();
			}

			/*
			 * Presently we can only send one exception to the client
			 * So for now we only send the most severe one
			 *
			 * TODO Enhancement: Make it so each exception is printed out.
			 */
			if ( cause instanceof MultipleException )
			{
				IException most = null;

				// The lower the intValue() to more important it became
				for ( IException e : ( ( MultipleException ) cause ).getExceptions() )
					if ( e instanceof Throwable && ( most == null || most.reportingLevel().intValue() > e.reportingLevel().intValue() ) )
						most = e;

				if ( most instanceof ScriptingException )
				{
					evalOrig = ( ScriptingException ) most;
					cause = most.getCause();
				}
				else
					cause = ( Throwable ) most;
			}

			/*
			 * TODO Proper Exception Handling. Consider the ability to have these exceptions cached, then delivered by e-mail to chiori-chan and server administrator.
			 */
			if ( cause instanceof HttpError )
				response.sendError( ( HttpError ) cause );
			else if ( cause instanceof PermissionDeniedException )
			{
				PermissionDeniedException pde = ( PermissionDeniedException ) cause;

				if ( pde.getReason() == PermissionDeniedReason.LOGIN_PAGE )
					response.sendLoginPage( pde.getReason().getMessage() );
				else
					/*
					 * TODO generate a special permission denied page
					 */
					response.sendError( ( ( PermissionDeniedException ) cause ).getHttpCode(), cause.getMessage() );
			}
			else if ( cause instanceof OutOfMemoryError )
			{
				log.log( Level.SEVERE, EnumColor.NEGATIVE + "" + EnumColor.RED + "OutOfMemoryError! This is serious!!!" );
				response.sendError( 500, "We have encountered an internal server error" );

				if ( Versioning.isDevelopment() )
					cause.printStackTrace();
			}
			else if ( evalOrig == null )
			{
				// Was not caught by EvalFactory
				log.log( Level.SEVERE, "%s%sException %s thrown in file '%s' at line %s, message '%s'", EnumColor.NEGATIVE, EnumColor.RED, cause.getClass().getName(), cause.getStackTrace()[0].getFileName(), cause.getStackTrace()[0].getLineNumber(), cause.getMessage() );
				response.sendException( cause );

				if ( Versioning.isDevelopment() )
					cause.printStackTrace();
			}
			else
			{
				if ( evalOrig.isScriptingException() && !evalOrig.hasScriptTrace() )
				{
					log.log( Level.WARNING, "We caught an EvalException which was determined to be related to a scripting issue but the exception has no script trace, this might be a combined internal and external problem.", EnumColor.NEGATIVE, EnumColor.RED );
					log.log( Level.SEVERE, "%s%sException %s thrown in file '%s' at line %s, message '%s'", EnumColor.NEGATIVE, EnumColor.RED, cause.getClass().getName(), cause.getStackTrace()[0].getFileName(), cause.getStackTrace()[0].getLineNumber(), cause.getMessage() );
				}
				else if ( evalOrig.isScriptingException() )
				{
					ScriptTraceElement element = evalOrig.getScriptTrace()[0];
					log.log( Level.SEVERE, "%s%sException %s thrown in file '%s' at line %s:%s, message '%s'", EnumColor.NEGATIVE, EnumColor.RED, cause.getClass().getName(), element.context().filename(), element.getLineNumber(), element.getColumnNumber() > 0 ? element.getColumnNumber() : 0, cause.getMessage() );
				}
				else
					log.log( Level.SEVERE, "%s%sException %s thrown with message '%s'", EnumColor.NEGATIVE, EnumColor.RED, cause.getClass().getName(), cause.getMessage() );
				// log.log( Level.SEVERE, "%s%sException %s thrown in file '%s' at line %s, message '%s'", LogColor.NEGATIVE, LogColor.RED, cause.getClass().getName(), cause.getStackTrace()[0].getFileName(),
				// cause.getStackTrace()[0].getLineNumber(), cause.getMessage() );

				response.sendException( evalOrig );

				if ( Versioning.isDevelopment() )
					cause.printStackTrace();
			}

			finish();
		}
		catch ( Throwable t )
		{
			NetworkManager.getLogger().severe( EnumColor.NEGATIVE + "" + EnumColor.RED + "This is an uncaught exception from the exceptionCaught() method:", t );
			// ctx.fireExceptionCaught( t );
		}
	}

	/**
	 * Sends the final response to the request
	 */
	private void finish()
	{
		try
		{
			log.log( Level.INFO, "%s {code=%s}", response.getHttpMsg(), response.getHttpCode() );

			if ( !response.isCommitted() )
				response.sendResponse();

			if ( request.hasSession() )
				request.getSession().save();

			request.finish();
			requestFinished = true;
		}
		catch ( Throwable t )
		{
			t.printStackTrace();
		}
	}

	@Override
	public void flush( ChannelHandlerContext ctx ) throws Exception
	{
		log.flushAndClose();
		ctx.flush();
	}

	/**
	 * Gets the origin HTTP request
	 *
	 * @return The HTTP request
	 */
	public HttpRequestWrapper getRequest()
	{
		return request;
	}

	/**
	 * Gets the {@link HttpRequestContext} used to parse annotations, file encoding, and etc.
	 *
	 * @return The active interpreter
	 */
	public HttpRequestContext getRequestContext()
	{
		return fi;
	}

	/**
	 * Gets the destination HTTP response
	 *
	 * @return The HTTP Response
	 */
	public HttpResponseWrapper getResponse()
	{
		return response;
	}

	/**
	 * Gets the currently selected Session for this request
	 *
	 * @return selected Session
	 */
	public Session getSession()
	{
		return request.getSession();
	}

	/**
	 * Gets the currently selected Webroot for this request
	 *
	 * @return selected Webroot
	 */
	public BaseWebroot getWebroot()
	{
		return currentWebroot;
	}

	/**
	 * Handles the HTTP request. Each HTTP subsystem will be explicitly activated until a resolve is determined.
	 *
	 * @throws IOException                                        Universal exception for all Input/Output errors
	 * @throws io.amelia.lang.HttpError                           for HTTP Errors
	 * @throws com.chiorichan.permission.lang.PermissionException for permission problems, like access denied
	 * @throws io.amelia.lang.MultipleException                   for multiple Scripting Factory Evaluation Exceptions
	 * @throws io.amelia.lang.ScriptingException                  for Scripting Factory Evaluation Exception
	 * @throws com.chiorichan.session.SessionException            for problems initializing a new or used session
	 */
	private void handleHttp() throws Exception // IOException, HttpError, WebrootException, PermissionException, MultipleException, ScriptingException, SessionException
	{
		log.log( Level.INFO, request.methodString() + " " + request.getFullUrl() );

		Session session = request.startSession();

		log.log( Level.FINE, "Session {id=%s,timeout=%s,new=%s}", session.getSessionId(), session.getTimeout(), session.isNew() );

		if ( response.getStage() == HttpResponseStage.CLOSED )
			throw new IOException( "Connection reset by peer" ); // This is not the only place 'Connection reset by peer' is thrown

		RequestEvent requestEvent = new RequestEvent( request );

		try
		{
			Events.callEventWithException( requestEvent );
		}
		catch ( EventException ex )
		{
			throw new IOException( "Exception encountered during request event call, most likely the fault of a plugin.", ex );
		}

		response.setStatus( requestEvent.getStatus() );

		if ( requestEvent.isCancelled() )
		{
			int status = requestEvent.getStatus();
			String reason = requestEvent.getReason();

			if ( status == 200 )
			{
				status = 502;
				reason = "Navigation Cancelled by Plugin Event";
			}

			NetworkManager.getLogger().warning( "Navigation was cancelled by a Plugin Event" );

			throw new HttpError( status, reason );
		}

		if ( response.isCommitted() )
			return;

		// Throws IOException and HttpError
		fi = new HttpRequestContext( request );

		response.annotations.putAll( fi.getAnnotations() );

		currentWebroot = request.getLocation();
		session.setWebroot( currentWebroot );

		DomainMapping mapping = request.getDomainMapping();

		if ( mapping == null )
		{
			if ( "www".equalsIgnoreCase( request.getChildDomain() ) || ConfigRegistry.i().getBoolean( "Webroots.redirectMissingSubDomains" ) )
			{
				log.log( Level.SEVERE, "Redirecting non-existent subdomain '%s' to root domain '%s'", request.getChildDomain(), request.getFullUrl( "" ) );
				response.sendRedirect( request.getFullUrl( "" ) );
			}
			else
			{
				log.log( Level.SEVERE, "The requested subdomain '%s' is non-existent.", request.getChildDomain(), request.getFullDomain( "" ) );
				response.sendError( HttpResponseStatus.NOT_FOUND, "Subdomain not found" );
			}
			return;
		}

		File docRoot = mapping.directory();
		Objs.notNull( docRoot );

		if ( !docRoot.exists() )
			NetworkManager.getLogger().warning( String.format( "The webroot directory [%s] was missing, it will be created.", UtilIO.relPath( docRoot ) ) );

		if ( docRoot.exists() && docRoot.isFile() )
			docRoot.delete();

		if ( !docRoot.exists() )
			docRoot.mkdirs();

		if ( session.hasLogin() )
			log.log( Level.FINE, "Account {id=%s,displayName=%s}", session.getId(), session.getDisplayName() );

		/* Check direct file access annotation: @disallowDirectAccess true */
		if ( Objs.castToBool( fi.get( "disallowDirectAccess" ) ) && new File( request.getDomainMapping().directory(), UtilStrings.trimAll( request.getUri(), '/' ) ).getAbsolutePath().equals( fi.getFile() ) )
			throw new HttpError( HttpResponseStatus.NOT_FOUND, "Accessing this file by exact file name is disallowed per annotation." );

		/*
		 * Start: Trailing slash enforcer
		 *
		 * Acts on the value of annotation 'trailingSlash.
		 *
		 * Options include:
		 *   Always: If the uri does NOT end with a trailing slash, a redirect will be made to force one.
		 *   Never: If the uri does end with a trailing slash, a redirect will be made to remove it.
		 *   Ignore: We don't care one way or other, do nothing! DEFAULT
		 */
		switch ( AlwaysNeverIgnore.parse( fi.getValue( "trailingSlash" ), TriEnum.Ignore ) )
		{
			case Always:
				request.enforceTrailingSlash();
				break;
			case Never:
				request.enforceNoTrailingSlash();
				break;
		}
		/*
		 * End: Trailing slash enforcer
		 */

		/*
		 * Start: SSL enforcer
		 *
		 * Acts on the value of annotation 'SSL'.
		 * REQUIRED means a forbidden error will be thrown is it can not be accomplished
		 *
		 * Options include:
		 *   Preferred: If SSL is available, we prefer to use it
		 *   PostOnly: SSL is REQUIRED if this is a POST request
		 *   GetOnly: SSL is REQUIRED if this is a GET request
		 *   Required: SSL is REQUIRED, no exceptions!
		 *   Deny: SSL is DENIED, no exceptions!
		 *   Ignore: We don't care one way or other, do nothing! DEFAULT
		 */
		SslLevel sslLevel = SslLevel.parse( fi.get( "ssl" ) );
		boolean required = false;

		switch ( sslLevel )
		{
			case Preferred:
				if ( HttpService.isHttpsRunning() )
					required = true;
				break;
			case PostOnly:
				if ( request.method() == HttpMethod.POST )
					required = true;
				break;
			case GetOnly:
				if ( request.method() == HttpMethod.GET )
					required = true;
				break;
			case Required:
				required = true;
				break;
			case Deny:
				if ( ssl )
				{
					if ( !response.switchToUnsecure() )
						response.sendError( HttpCode.HTTP_FORBIDDEN, "This page requires an unsecure connection." );
					return;
				}
				break;
			case Ignore:
				break;
		}

		if ( required && !ssl )
		{
			if ( !response.switchToSecure() )
				response.sendError( HttpCode.HTTP_FORBIDDEN, "This page requires a secure connection." );
			return;
		}
		/*
		 * End: SSL enforcer
		 */

		if ( fi.getStatus() != HttpResponseStatus.OK )
			throw new HttpError( fi.getStatus() );

		/*
		 * Start: Apache Configuration Section
		 *
		 * Loads a Apache configuration and .htaccess files into a common handler, then parsed for directives like access restrictions and basic auth
		 * TODO Load server-wide Apache Configuration then merge with Webroot Configuration
		 */
		ApacheHandler htaccess = new ApacheHandler();
		response.setApacheParser( htaccess );

		try
		{
			boolean result = htaccess.handleDirectives( currentWebroot.getApacheConfig(), this );

			if ( htaccess.overrideNone() || htaccess.overrideListNone() ) // Ignore .htaccess files
			{
				if ( fi.hasFile() )
					if ( !htaccess.handleDirectives( new ApacheConfiguration( fi.getFile().getParentFile() ), this ) )
						result = false;

				if ( !htaccess.handleDirectives( new ApacheConfiguration( docRoot ), this ) )
					result = false;
			}

			if ( !result )
			{
				if ( !response.isCommitted() )
					response.sendError( 500, "Your request was blocked by a .htaccess directive, exact details are unknown." );
				return;
			}
		}
		catch ( ApacheDirectiveException e )
		{
			log.log( Level.SEVERE, "Caught Apache directive exception: " + e.getMessage() );

			// TODO Throw 500 unless told not to
		}
		/*
		 * End: Apache Configuration Section
		 */

		if ( !fi.hasFile() ) // && !fi.hasHTML() )
			response.setStatus( HttpResponseStatus.NO_CONTENT );

		session.setGlobal( "__FILE__", fi.getFile() );

		request.putRewriteParams( fi.getRewriteParams() );
		response.setContentType( fi.getContentType() );
		response.setEncoding( fi.getEncoding() );

		request.getServer().put( HttpServerKey.DOCUMENT_ROOT, docRoot );

		request.setGlobal( "_SERVER", request.getServer() );
		request.setGlobal( "_POST", request.getPostMap() );
		request.setGlobal( "_GET", request.getGetMap() );
		request.setGlobal( "_REWRITE", request.getRewriteMap() );
		request.setGlobal( "_FILES", request.getUploadedFiles() );

		try
		{
			if ( request.getUploadedFiles().size() > 0 )
				log.log( Level.INFO, "Uploads {" + UtilStrings.limitLength( Joiner.on( "," ).skipNulls().join( request.getUploadedFiles().values() ), 255 ) + "}" );

			if ( request.getGetMap().size() > 0 )
				log.log( Level.INFO, "GetMap {" + UtilStrings.limitLength( Joiner.on( "," ).withKeyValueSeparator( "=" ).useForNull( "null" ).join( request.getGetMap() ), 255 ) + "}" );

			if ( request.getPostMap().size() > 0 )
				log.log( Level.INFO, "PostMap {" + UtilStrings.limitLength( Joiner.on( "," ).withKeyValueSeparator( "=" ).useForNull( "null" ).join( request.getPostMap() ), 255 ) + "}" );

			if ( request.getRewriteMap().size() > 0 )
				log.log( Level.INFO, "RewriteMap {" + UtilStrings.limitLength( Joiner.on( "," ).withKeyValueSeparator( "=" ).useForNull( "null" ).join( request.getRewriteMap() ), 255 ) + "}" );

			if ( fi.getAnnotations().size() > 0 )
				log.log( Level.INFO, "Annotations {" + UtilStrings.limitLength( Joiner.on( "," ).withKeyValueSeparator( "=" ).useForNull( "null" ).join( fi.getAnnotations() ), 255 ) + "}" );
		}
		catch ( Throwable t )
		{
			t.printStackTrace();
		}

		/*
		 * Nonce is separately verified by the login subroutine.
		 * So regardless what the nonce level is, nonce will have to be initialized by the login form.
		 */

		NonceLevel level = NonceLevel.parse( fi.get( "nonce" ) );
		Nonce nonce = session.nonce();

		boolean nonceProvided = nonce != null && request.getRequestMap().containsKey( nonce.key() );
		boolean processNonce = false;

		switch ( level )
		{
			case Required:
				processNonce = true;
				break;
			case GetOnly:
				processNonce = request.method() == HttpMethod.GET || nonceProvided;
				break;
			case PostOnly:
				processNonce = request.method() == HttpMethod.POST || nonceProvided;
				break;
			case Flexible:
				processNonce = nonceProvided;
				break;
			case Disabled:
			default:
				// Do Nothing
		}

		Map<String, String> nonceMap = new HashMap<>();

		if ( processNonce )
		{
			if ( !nonceProvided )
			{
				if ( !Objs.isNull( nonce ) )
					log.log( Level.SEVERE, String.format( "The expected nonce key was %s", nonce.key() ) );
				response.sendError( HttpResponseStatus.FORBIDDEN, "Request Failed NONCE Validation", "Nonce key is missing or the nonce was not initialized." );
				return;
			}

			if ( level == NonceLevel.Required )
				// Required nonce levels are of the highest protected state
				session.destroyNonce();

			try
			{
				if ( !( request.getRequestMap().get( nonce.key() ) instanceof String ) )
					throw new NonceException( "Nonce token is not a string" );
				nonce.validateWithException( ( String ) request.getRequestMap().get( nonce.key() ) );
			}
			catch ( NonceException e )
			{
				log.log( Level.SEVERE, "Request failed NONCE validation. \"" + e.getMessage() + "\"" );
				response.sendError( HttpResponseStatus.FORBIDDEN, "Request Failed NONCE Validation", e.getMessage() );
				session.destroyNonce();
				return;
			}
			finally
			{
				log.log( Level.INFO, "Request PASSED NONCE validation." );
				request.nonceProcessed( true );
				nonceMap = nonce.mapValues();
			}
		}

		if ( request.validateLogins() )
			return;

		if ( level != NonceLevel.Disabled )
			request.setGlobal( "_NONCE", nonceMap );

		if ( ConfigRegistry.i().getBoolean( "advanced.security.requestMapEnabled", true ) )
			request.setGlobal( "_REQUEST", request.getRequestMap() );

		ByteBuf rendered = Unpooled.buffer();

		ScriptingFactory factory = request.getScriptingFactory();
		factory.setEncoding( fi.getEncoding() );

		NetworkSecurity.isForbidden( htaccess, currentWebroot, fi );

		/* This annotation simply requests that the page requester must have a logged in account, the exact permission is not important. */
		boolean reqLogin = Objs.castToBool( fi.get( "reqLogin" ) );

		if ( reqLogin && !session.hasLogin() )
			throw new PermissionDeniedException( PermissionDeniedReason.LOGIN_PAGE );

		/* This annotation check the required permission to make the request and will also require a logged in account. */
		String reqPerm = fi.get( "reqPerm" );

		if ( reqPerm == null )
			reqPerm = "-1";

		session.requirePermission( reqPerm, currentWebroot.getId() );

		/* TODO Deprecated but removed for historical reasons
		if ( fi.hasHTML() )
		{
			ScriptingResult result = factory.eval( ScriptingContext.fromSource( fi.getHTML(), "<embedded>" ).request( request ).Webroot( currentWebroot ) );

			if ( result.hasExceptions() )
				// TODO Print notices to output like PHP does
				for ( IException e : result.getExceptions() )
				{
					ExceptionReport.throwExceptions( e );
					if ( e instanceof Throwable )
						log.exceptions( ( Throwable ) e );
					if ( e.reportingLevel().isEnabled() )
						rendered.writeBytes( e.getMessage().getBytes() );
				}

			if ( result.isSuccessful() )
			{
				rendered.writeBytes( result.content() );
				if ( result.getObject() != null && !( result.getObject() instanceof NullObject ) )
					try
					{
						rendered.writeBytes( ZObjects.castToStringWithException( result.getObject() ).getBytes() );
					}
					catch ( Exception e )
					{
						log.log( Level.SEVERE, "Exception Encountered: %s", e.getMessage() );
						if ( Versioning.isDevelopment() )
							log.log( Level.SEVERE, e.getStackTrace()[0].toString() );
					}
			}

			log.log( Level.INFO, "EvalHtml {timing=%sms,success=%s}", Timings.mark( this ), result.isSuccessful() );
		}
		*/

		if ( fi.hasFile() )
		{
			if ( fi.isDirectoryRequest() )
			{
				processDirectoryListing();
				return;
			}

			ScriptingContext scriptingContext = ScriptingContext.fromFile( fi ).request( request ).Webroot( currentWebroot );
			request.getArguments().forEach( arg -> {
				scriptingContext.addOption( arg.getKey(), arg.getValue() );
			} );
			ScriptingResult result = factory.eval( scriptingContext );

			if ( result.hasExceptions() )
				// TODO Print notices to output like PHP does
				for ( IException e : result.getExceptions() )
				{
					ExceptionReport.throwExceptions( e );
					if ( e instanceof Throwable )
						log.exceptions( ( Throwable ) e );
					if ( e.reportingLevel().isEnabled() && e.getMessage() != null )
						rendered.writeBytes( e.getMessage().getBytes() );
				}

			if ( result.isSuccessful() )
			{
				rendered.writeBytes( result.content() );
				if ( result.getObject() != null && !( result.getObject() instanceof NullObject ) )
					try
					{
						rendered.writeBytes( Objs.castToStringWithException( result.getObject() ).getBytes() );
					}
					catch ( Exception e )
					{
						rendered.writeBytes( result.getObject().toString().getBytes() );
						log.log( Level.SEVERE, "Exception encountered while writing returned object to output. %s", e.getMessage() );
						if ( Versioning.isDevelopment() )
							log.log( Level.SEVERE, e.getClass().getSimpleName() + ": " + e.getStackTrace()[0].toString() );
					}
			}

			log.log( Level.INFO, "EvalFile {file=%s,timing=%sms,success=%s}", fi.getFilePath(), Timings.mark( this ), result.isSuccessful() );
		}

		// if the connection was in a MultiPart mode, wait for the mode to change then return gracefully.
		if ( response.stage == HttpResponseStage.MULTIPART )
		{
			while ( response.stage == HttpResponseStage.MULTIPART )
				// I wonder if there is a better way to handle multipart responses.
				try
				{
					Thread.sleep( 100 );
				}
				catch ( InterruptedException e )
				{
					throw new HttpError( 500, "Internal Server Error encountered during multipart execution." );
				}

			return;
		}
		// If the connection was closed from page redirect, return gracefully.
		else if ( response.stage == HttpResponseStage.CLOSED || response.stage == HttpResponseStage.WRITTEN )
			return;

		// Allows scripts to directly override interpreter values. For example: Themes, Views, Titles
		for ( Entry<String, String> kv : response.annotations.entrySet() )
			fi.put( kv.getKey(), kv.getValue() );

		RenderEvent renderEvent = new RenderEvent( this, rendered, fi.getEncoding(), fi.getAnnotations() );

		try
		{
			EventDispatcher.i().callEventWithException( renderEvent );
			if ( renderEvent.getSource() != null )
				rendered = renderEvent.getSource();
		}
		catch ( EventException ex )
		{
			if ( ex.getCause() != null && ex.getCause() instanceof ScriptingException )
				throw ( ScriptingException ) ex.getCause();
			else
				throw new ScriptingException( ReportingLevel.E_ERROR, "Caught EventException while trying to fire the RenderEvent", ex.getCause() );
		}

		log.log( Level.INFO, "Written {bytes=%s,total_timing=%sms}", rendered.readableBytes(), Timings.finish( this ) );

		try
		{
			response.write( rendered );
		}
		catch ( IllegalReferenceCountException e )
		{
			log.log( Level.SEVERE, "Exception encountered while writing script object to output, %s", e.getMessage() );
		}
	}

	@Override
	protected void messageReceived( ChannelHandlerContext ctx, Object msg ) throws Exception
	{
		Timings.start( this );

		if ( msg instanceof FullHttpRequest )
		{
			if ( AppLoader.instances().get( 0 ).runLevel() != RunLevel.RUNNING )
			{
				// Outputs a very crude raw message if we are running in a low level mode a.k.a. Startup or Reload.
				// While in the mode, much of the server API is potentially unavailable, that is why we do this.

				StringBuilder sb = new StringBuilder();
				sb.append( "<h1>503 - Service Unavailable</h1>\n" );
				sb.append( "<p>I'm sorry to have to be the one to tell you this but the server is currently unavailable.</p>\n" );
				sb.append( "<p>This is most likely due to many possibilities, most commonly being it's currently booting up. Which would be great news because it means your request should succeed if you try again.</p>\n" );
				sb.append( "<p>But it is also possible that the server is actually running in a low level mode or could be offline for some other reason. If you feel this is a mistake, might I suggest you talk with the server admin.</p>\n" );
				sb.append( "<p><i>You have a good day now and we will see you again soon. :)</i></p>\n" );
				sb.append( "<hr>\n" );
				sb.append( "<small>Running <a href=\"https://github.com/ChioriGreene/ChioriWebServer\">" + Versioning.getProduct() + "</a> Version " + Versioning.getVersion() + " (Build #" + Versioning.getBuildNumber() + ")<br />" + Versioning.getCopyright() + "</small>" );

				FullHttpResponse response = new DefaultFullHttpResponse( HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf( 503 ), Unpooled.wrappedBuffer( sb.toString().getBytes() ) );
				ctx.write( response );

				return;
			}

			requestFinished = false;
			requestOrig = ( FullHttpRequest ) msg;
			request = new HttpRequestWrapper( ctx.channel(), requestOrig, this, ssl, log );
			response = request.getResponse();

			String threadName = Thread.currentThread().getName();

			if ( threadName.length() > 10 )
				threadName = threadName.substring( 0, 2 ) + ".." + threadName.substring( threadName.length() - 6 );
			else if ( threadName.length() < 10 )
				threadName = threadName + Strings.repeat( " ", 10 - threadName.length() );

			log.header( "&7[&d%s&7] %s %s &9[%s]:%s&7 -> &a[%s]:%s&7", threadName, dateFormat.format( Timings.millis() ), timeFormat.format( Timings.millis() ), request.getIpAddress(), request.getRemotePort(), request.getLocalIpAddress(), request.getLocalPort() );

			if ( HttpHeaderUtil.is100ContinueExpected( ( HttpRequest ) msg ) )
				send100Continue( ctx );

			if ( NetworkSecurity.isIpBanned( request.getIpAddress() ) )
			{
				response.sendError( 403 );
				return;
			}

			BaseWebroot currentWebroot = request.getLocation();

			File tmpFileDirectory = currentWebroot != null ? currentWebroot.getCacheDirectory() : ConfigRegistry.i().getDirectoryCache();

			setTempDirectory( tmpFileDirectory );

			if ( request.isWebsocketRequest() )
			{
				try
				{
					WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory( request.getWebSocketLocation( requestOrig ), null, true );
					handshaker = wsFactory.newHandshaker( requestOrig );
					if ( handshaker == null )
						WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse( ctx.channel() );
					else
						handshaker.handshake( ctx.channel(), requestOrig );
				}
				catch ( WebSocketHandshakeException e )
				{
					NetworkManager.getLogger().severe( "A request was made on the websocket uri '/fw/websocket' but it failed to handshake for reason '" + e.getMessage() + "'." );
					response.sendError( 500, null, "This URI is for websocket requests only<br />" + e.getMessage() );
				}
				return;
			}

			if ( request.method() != HttpMethod.GET )
				try
				{
					decoder = new HttpPostRequestDecoder( factory, requestOrig );
				}
				catch ( ErrorDataDecoderException e )
				{
					e.printStackTrace();
					response.sendException( e );
					return;
				}

			request.contentSize += requestOrig.content().readableBytes();

			if ( decoder != null )
			{
				try
				{
					decoder.offer( requestOrig );
				}
				catch ( ErrorDataDecoderException e )
				{
					e.printStackTrace();
					response.sendError( e );
					// ctx.channel().close();
					return;
				}
				catch ( IllegalArgumentException e )
				{
					// TODO Handle this further? maybe?
					// java.lang.IllegalArgumentException: empty name
				}
				readHttpDataChunkByChunk();
			}

			handleHttp();

			finish();
		}
		else if ( msg instanceof WebSocketFrame )
		{
			WebSocketFrame frame = ( WebSocketFrame ) msg;

			// Check for closing frame
			if ( frame instanceof CloseWebSocketFrame )
			{
				handshaker.close( ctx.channel(), ( CloseWebSocketFrame ) frame.retain() );
				return;
			}

			if ( frame instanceof PingWebSocketFrame )
			{
				ctx.channel().write( new PongWebSocketFrame( frame.content().retain() ) );
				return;
			}

			if ( !( frame instanceof TextWebSocketFrame ) )
				throw new UnsupportedOperationException( String.format( "%s frame types are not supported", frame.getClass().getName() ) );

			String request = ( ( TextWebSocketFrame ) frame ).text();
			NetworkManager.getLogger().fine( "Received '" + request + "' over WebSocket connection '" + ctx.channel() + "'" );
			ctx.channel().write( new TextWebSocketFrame( request.toUpperCase() ) );
		}
		else if ( msg instanceof DefaultHttpRequest )
		{
			// Do Nothing!
		}
		else
			NetworkManager.getLogger().warning( "Received Object '" + msg.getClass() + "' and had nothing to do with it, is this a bug?" );
	}

	/**
	 * Write a directory listing to the HTTP destination
	 *
	 * @throws HttpError   for HTTP errors
	 * @throws IOException for universal Input/Output problems
	 */
	public void processDirectoryListing() throws HttpError, IOException
	{
		File dir = fi.getFile();

		if ( !dir.exists() || !dir.isDirectory() )
			throw new HttpError( HttpResponseStatus.INTERNAL_SERVER_ERROR, "Directory does not exist!" );

		response.setContentType( "text/html" );
		response.setEncoding( Charsets.UTF_8 );

		File[] files = dir.listFiles();
		List<Object> tbl = Lists.newArrayList();
		StringBuilder sb = new StringBuilder();
		SimpleDateFormat sdf = new SimpleDateFormat( "dd-MMM-yyyy HH:mm:ss" );

		sb.append( "<style>.altrowstable { border-spacing: 12px; }</style>" );
		sb.append( "<h1>Index of " + request.getUri() + "</h1>" );

		for ( File f : files )
		{
			List<String> l = Lists.newArrayList();
			String type = ContentTypes.getContentType( f );
			String mainType = type.contains( "/" ) ? type.substring( 0, type.indexOf( "/" ) ) : type;

			l.add( "<img src=\"/wisp/icons/" + mainType + "\" />" );
			l.add( "<a href=\"" + request.getUri() + "/" + f.getName() + "\">" + f.getName() + "</a>" );
			l.add( sdf.format( f.lastModified() ) );

			if ( f.isDirectory() )
				l.add( "-" );
			else
			{
				InputStream stream = null;
				try
				{
					URL url = f.toURI().toURL();
					stream = url.openStream();
					l.add( String.valueOf( stream.available() ) + "kb" );
				}
				finally
				{
					if ( stream != null )
						stream.close();
				}
			}

			l.add( type );

			tbl.add( l );
		}

		sb.append( Builtin.createTable( tbl, Arrays.asList( "", "Name", "Last Modified", "Size", "Type" ) ) );
		sb.append( "<hr>" );
		sb.append( "<small>Running <a href=\"https://github.com/ChioriGreene/ChioriWebServer\">" + Versioning.getProduct() + "</a> Version " + Versioning.getVersion() + "<br />" + Versioning.getCopyright() + "</small>" );

		response.print( sb.toString() );
		response.sendResponse();

		// throw new HttpErrorException( 403, "Sorry, Directory Listing has not been implemented on this Server!" );
	}

	private void readHttpDataChunkByChunk() throws IOException
	{
		try
		{
			while ( decoder.hasNext() )
			{
				InterfaceHttpData data = decoder.next();
				if ( data != null )
					writeHttpData( data );
			}
		}
		catch ( EndOfDataDecoderException e )
		{
			// END OF CONTENT
		}
	}

	private void writeHttpData( InterfaceHttpData data ) throws IOException
	{
		if ( data.getHttpDataType() == HttpDataType.Attribute )
		{
			Attribute attribute = ( Attribute ) data;
			String value;
			try
			{
				value = attribute.getValue();
			}
			catch ( IOException e )
			{
				e.printStackTrace();
				response.sendException( e );
				return;
			}

			request.putPostMap( attribute.getName(), value );

			/*
			 * Should resolve the problem described in Issue #9 on our GitHub
			 */
			attribute.delete();
		}
		else if ( data.getHttpDataType() == HttpDataType.FileUpload )
		{
			FileUpload fileUpload = ( FileUpload ) data;
			if ( fileUpload.isCompleted() )
				try
				{
					request.putUpload( fileUpload.getName(), new UploadedFile( fileUpload ) );
				}
				catch ( IOException e )
				{
					e.printStackTrace();
					response.sendException( e );
				}
			else
				NetworkManager.getLogger().warning( "File to be continued but should not!" );
		}
	}
}
