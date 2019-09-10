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

import com.google.common.base.Charsets;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import io.amelia.data.parcel.ParcelLoader;
import io.amelia.events.Events;
import io.amelia.foundation.Kernel;
import io.amelia.http.apache.ApacheHandler;
import io.amelia.http.events.HttpErrorEvent;
import io.amelia.http.events.HttpExceptionEvent;
import io.amelia.http.session.Session;
import io.amelia.http.webroot.WebrootRegistry;
import io.amelia.lang.HttpCode;
import io.amelia.lang.HttpError;
import io.amelia.lang.NetworkException;
import io.amelia.lang.SessionException;
import io.amelia.logging.LogEvent;
import io.amelia.net.Networking;
import io.amelia.net.web.WebService;
import io.amelia.scripting.HttpScriptingResponse;
import io.amelia.scripting.ScriptingContext;
import io.amelia.scripting.api.Web;
import io.amelia.support.EnumColor;
import io.amelia.support.Exceptions;
import io.amelia.support.Lists;
import io.amelia.support.Objs;
import io.amelia.support.Strs;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.stream.ChunkedStream;
import io.netty.util.AsciiString;

/**
 * Wraps the Netty HttpResponse to provide easy methods for manipulating the result of each request
 */
public class HttpResponseWrapper implements HttpScriptingResponse, HttpServletResponse
{
	final Map<AsciiString, String> annotations = new HashMap<>();
	final Map<AsciiString, Set<String>> headers = new HashMap<>();
	final LogEvent log;
	final HttpRequestWrapper request;
	Charset encoding = Charsets.UTF_8;
	ApacheHandler htaccess = null;
	HttpCode httpCode = HttpCode.HTTP_OK;
	String httpContentType = "text/html";
	ByteBuf output = Unpooled.buffer();
	HttpResponseStage stage = HttpResponseStage.READING;
	private Locale locale;

	protected HttpResponseWrapper( HttpRequestWrapper request, LogEvent log )
	{
		this.request = request;
		this.log = log;
	}

	public void addCookie( HoneyCookie cookie )
	{
		request.cookies.add( cookie );
	}

	@Override
	public void addCookie( Cookie cookie )
	{
		request.cookies.add( HoneyCookie.from( cookie ) );
	}

	@Override
	public void addDateHeader( String name, long date )
	{
		addHeader( HttpHeaderNames.DATE.toString(), Long.toString( date ) );
	}

	@Override
	public void addHeader( String name, String value )
	{
		headers.computeIfAbsent( AsciiString.of( name ), key -> new HashSet<>() ).add( value );
	}

	@Override
	public void addIntHeader( String name, int value )
	{
		headers.computeIfAbsent( AsciiString.of( name ), key -> new HashSet<>() ).add( Integer.toString( value ) );
	}

	@Override
	public void close()
	{
		request.getChannel().close();
		stage = HttpResponseStage.CLOSED;
	}

	@Override
	public boolean containsHeader( String name )
	{
		return headers.containsKey( name ) && headers.get( name ).size() > 0;
	}

	@Override
	public String encodeRedirectURL( String url )
	{
		// TODO HoneyPotServer does not presently support session ID in the URL but we should add it and then implement this method.
		return url;
	}

	@Override
	public String encodeRedirectUrl( String url )
	{
		// TODO HoneyPotServer does not presently support session ID in the URL but we should add it and then implement this method.
		return url;
	}

	@Override
	public String encodeURL( String url )
	{
		// TODO HoneyPotServer does not presently support session ID in the URL but we should add it and then implement this method.
		return url;
	}

	@Override
	public String encodeUrl( String url )
	{
		// TODO HoneyPotServer does not presently support session ID in the URL but we should add it and then implement this method.
		return url;
	}

	@Override
	public void finishMultipart() throws IOException
	{
		if ( stage == HttpResponseStage.CLOSED )
			throw new IllegalStateException( "You can't access closeMultipart unless you start MULTIPART with sendMultipart." );

		stage = HttpResponseStage.CLOSED;

		// Write the end marker
		ChannelFuture lastContentFuture = request.getChannel().writeAndFlush( LastHttpContent.EMPTY_LAST_CONTENT );

		// Decide whether to close the connection or not.
		// if ( !isKeepAlive( request ) )
		{
			// Close the connection when the whole content is written out.
			lastContentFuture.addListener( ChannelFutureListener.CLOSE );
		}
	}

	@Override
	public void flushBuffer() throws IOException
	{
		// This method should write the output buffer to the stream but I don't think this is implemented yet.
	}

	@Override
	public String getAnnotation( String key )
	{
		return annotations.get( key );
	}

	@Override
	public int getBufferSize()
	{
		return output.capacity();
	}

	@Override
	public String getCharacterEncoding()
	{
		return request.getCharacterEncoding();
	}

	@Override
	public String getContentType()
	{
		return request.getContentType();
	}

	@Override
	public Charset getEncoding()
	{
		return encoding;
	}

	@Override
	public String getHeader( String name )
	{
		return Lists.first( getHeaders( name ) ).orElse( null );
	}

	@Override
	public Set<String> getHeaderNames()
	{
		return headers.keySet().stream().map( AsciiString::toString ).collect( Collectors.toSet() );
	}

	@Override
	public Set<String> getHeaders( String name )
	{
		return headers.computeIfAbsent( AsciiString.of( name ), key -> new HashSet<>() );
	}

	@Override
	public int getHttpCode()
	{
		return httpCode.getCode();
	}

	@Override
	public String getHttpReason()
	{
		return httpCode.getReason();
	}

	@Override
	public Locale getLocale()
	{
		return locale;
	}

	@Override
	public ByteBuf getOutput()
	{
		return output;
	}

	@Override
	public byte[] getOutputBytes()
	{
		byte[] bytes = new byte[output.writerIndex()];
		int inx = output.readerIndex();
		output.readerIndex( 0 );
		output.readBytes( bytes );
		output.readerIndex( inx );
		return bytes;
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException
	{
		final ByteBufOutputStream wrappedOutputStream = new ByteBufOutputStream( output );

		return new ServletOutputStream()
		{
			private WriteListener writeListener;

			@Override
			public void close() throws IOException
			{
				wrappedOutputStream.close();
			}

			@Override
			public void flush() throws IOException
			{
				wrappedOutputStream.flush();
			}

			@Override
			public boolean isReady()
			{
				return true;
			}

			@Override
			public void setWriteListener( WriteListener writeListener )
			{
				this.writeListener = writeListener;
			}

			@Override
			public void write( int b ) throws IOException
			{
				try
				{
					wrappedOutputStream.write( b );
				}
				catch ( IOException e )
				{
					if ( writeListener != null )
						writeListener.onError( e );
					throw e;
				}
			}
		};
	}

	public HttpResponseStage getStage()
	{
		return stage;
	}

	@Override
	public int getStatus()
	{
		return getHttpCode();
	}

	@Override
	public PrintWriter getWriter() throws IOException
	{
		return new PrintWriter( getOutputStream(), true );
	}

	@Override
	public boolean isCommitted()
	{
		return stage == HttpResponseStage.CLOSED || stage == HttpResponseStage.WRITTEN;
	}

	@Override
	@Deprecated
	public void print( byte[] bytes ) throws IOException
	{
		write( bytes );
	}

	@Override
	public void print( String var ) throws IOException
	{
		if ( var != null && !var.isEmpty() )
			write( var.getBytes( encoding ) );
	}

	private void printMap( Map<String, ?> map ) throws IOException
	{
		ArrayList<Object> tbl = new ArrayList<Object>()
		{{
			if ( Objs.isEmpty( map ) )
				add( "(empty)" );
			else
				for ( Entry<String, ?> e : map.entrySet() )
					if ( !Objs.isNull( e.getKey() ) )
						add( new ArrayList<Object>()
						{{
							add( "<b>" + Strs.escapeHtml( e.getKey() ) + "</b>" );
							try
							{
								if ( Objs.isNull( e.getValue() ) )
									add( "(null)" );
								else if ( e.getKey().toLowerCase().contains( "password" ) )
									add( "(hidden)" );
								else
									add( Strs.escapeHtml( Objs.castToString( e.getValue() ) ) );
							}
							catch ( ClassCastException e )
							{
								add( "(non-string)" );
							}
						}} );
		}};

		println( Web.createTable( tbl, "debug-getTable" ) );
	}

	@Override
	public void println( String var ) throws IOException
	{
		if ( var != null && !var.isEmpty() )
			write( ( var + "\n" ).getBytes( encoding ) );
	}

	@Override
	public boolean redirectToSecure()
	{
		if ( !Networking.getService( WebService.class ).orElseThrow( NetworkException.Runtime::new ).isHttpsRunning() )
		{
			log.log( Level.SEVERE, "We were going to attempt to switch to a secure HTTPS connection and aborted due to the HTTPS server not running." );
			return false;
		}

		if ( request.isSecure() )
			return true;

		sendRedirectRepost( request.getFullUrl( true ) + request.getQuery() );
		return true;
	}

	@Override
	public boolean redirectToUnsecure()
	{
		if ( !Networking.getService( WebService.class ).orElseThrow( NetworkException.Runtime::new ).isHttpRunning() )
		{
			log.log( Level.SEVERE, "We were going to attempt to switch to an unsecure HTTP connection and aborted due to the HTTP server not running." );
			return false;
		}

		if ( !request.isSecure() )
			return true;

		sendRedirectRepost( request.getFullUrl( false ) + request.getQuery() );
		return true;
	}

	@Override
	public void reset()
	{

	}

	@Override
	public void resetBuffer()
	{
		output = Unpooled.buffer();
	}

	@Override
	public void sendError( Exception e ) throws IOException
	{
		Objs.notNull( e );

		if ( e instanceof HttpError && e.getCause() == null )
			sendError( ( ( HttpError ) e ).getHttpCode(), ( ( HttpError ) e ).getReason(), e.getMessage() );
		else if ( e instanceof HttpError )
			sendException( e.getCause() );
		else
			sendException( e );
	}

	@Override
	public void sendError( int code ) throws IOException
	{
		sendError( code, null );
	}

	@Override
	public void sendError( int code, String statusReason, String developerMessage ) throws IOException
	{
		sendError( HttpCode.getHttpCode( code ).orElse( HttpCode.HTTP_INTERNAL_SERVER_ERROR ), statusReason, developerMessage );
	}

	@Override
	public void sendError( int code, String developerMessage ) throws IOException
	{
		sendError( code, null, developerMessage );
	}

	@Override
	public void sendError( HttpResponseStatus status ) throws IOException
	{
		sendError( status.code(), status.reasonPhrase(), null );
	}

	@Override
	public void sendError( HttpResponseStatus status, String statusReason, String developerMessage ) throws IOException
	{
		sendError( status.code(), statusReason, developerMessage );
	}

	@Override
	public void sendError( HttpResponseStatus status, String developerMessage ) throws IOException
	{
		sendError( status.code(), status.reasonPhrase(), developerMessage );
	}

	@Override
	public void sendError( HttpCode httpCode ) throws IOException
	{
		sendError( httpCode, null );
	}

	@Override
	public void sendError( HttpCode httpCode, String developerMessage ) throws IOException
	{
		sendError( httpCode, null, developerMessage );
	}

	@Override
	public void sendError( @Nullable HttpCode httpCode, @Nullable String statusReason, @Nullable String developerMessage ) throws IOException
	{
		if ( stage == HttpResponseStage.CLOSED )
			throw new IllegalStateException( "You can't access sendError method within this HttpResponse because the connection has been closed." );

		if ( httpCode == null )
			httpCode = HttpCode.HTTP_INTERNAL_SERVER_ERROR;

		resetBuffer();

		// Trigger an internal Error Event to notify plugins of a possible problem.
		HttpErrorEvent event = new HttpErrorEvent( request, httpCode, statusReason, Kernel.isDevelopment() );
		Events.getInstance().callEvent( event );

		httpCode = event.getHttpCode();
		statusReason = event.getHttpReason();

		if ( statusReason == null )
			statusReason = httpCode.getReason();

		if ( statusReason.length() > 255 )
			log.log( Level.SEVERE, "%s {code=%s}", statusReason, httpCode.getCode() );
		else
			log.log( Level.SEVERE, "%s {code=%s,reason=%s}", statusReason, httpCode.getCode(), statusReason );

		if ( event.getErrorHtml() == null || event.getErrorHtml().length() == 0 )
		{
			boolean printHtml = true;

			if ( htaccess != null && htaccess.getErrorDocument( httpCode.getCode() ) != null )
			{
				String resp = htaccess.getErrorDocument( httpCode.getCode() ).getResponse();

				if ( resp.startsWith( "/" ) )
				{
					sendRedirect( request.getBaseUrl() + resp );
					printHtml = false;
				}
				else if ( resp.startsWith( "http" ) )
				{
					sendRedirect( resp );
					printHtml = false;
				}
				else
					statusReason = resp;
			}

			if ( printHtml )
			{
				println( "<html><head>" );
				println( "<title>" + httpCode.getCode() + " - " + statusReason + "</title>" );
				println( "<style>body { margin: 0; padding: 0; } h1, h2, h3, h4, h5, h6 { margin: 0; } .container { padding: 8px; } .debug-header { display: block; margin: 15px 0 0; font-size: 18px; color: #303030; font-weight: bold; } #debug-getTable { border: 1px solid; width: 100%; } #debug-getTable thead { background-color: #eee; } #debug-getTable #col_0 { width: 20%; min-width: 130px; overflow: hidden; font-weight: bold; color: #463C54; padding-right: 5px; } #debug-getTable #tblStringRow { color: rgba(0, 0, 0, .3); font-weight: 300; }</style>" );
				println( "</head><body>" );

				println( "<div class=\"container\" style=\" background-color: #eee; \">" );
				println( "<h1>" + httpCode.getCode() + " - " + statusReason + "</h1>" );
				println( "</div>" );
				println( "<div class=\"container\">" );

				if ( Kernel.isDevelopment() )
				{
					println( "<p>" + developerMessage + "</p>" );

					println( "<h3>Debug &amp; Environment Details:</h3>" );

					println( "<span class=\"debug-header\">GET Data</span>" );
					printMap( request.getGetMap() );

					println( "<span class=\"debug-header\">POST Data</span>" );
					printMap( request.getPostMap() );

					println( "<span class=\"debug-header\">Files</span>" );
					Collection<UploadedFile> files = request.getUploadedFiles().values();
					ArrayList<Object> tbl = new ArrayList<Object>()
					{{
						if ( files.size() == 0 )
							add( "empty" );
						else
							for ( UploadedFile file : files )
								add( new ArrayList<Object>()
								{{
									add( file.getFileName() );
									add( file.getFileSize() );
									add( file.getMimeType() );
									add( file.getMD5Hash() );
								}} );
					}};
					List<String> cols = new ArrayList<String>()
					{{
						add( "Filename" );
						add( "Mime Type" );
						add( "File Size" );
						add( "MD5 Hash" );
					}};
					println( Web.createTable( tbl, cols, "debug-getTable" ) );

					println( "<span class=\"debug-header\">Cookies</span>" );
					printMap( new HashMap<String, Object>()
					{{
						request.getHoneyCookies().forEach( cookie -> put( cookie.getName(), cookie.getValue() ) );
						request.getServerCookies().forEach( cookie -> put( cookie.getName() + " (Server Cookie)", cookie.getValue() ) );
						// TODO Implement a HTML color code parser, like a built-in BBCode or Markdown parser
						// put( cookie.getKey() + " <span style=\"color: #eee; font-weight: 300;\">(internal)</span>", cookie.getValue() );
					}} );

					if ( request.hasSession() )
					{
						println( "<span class=\"debug-header\">Session</span>" );
						printMap( ParcelLoader.encodeMap( request.getSession().getDataMap() ) );
					}

					println( "<span class=\"debug-header\">Server/Header Data</span>" );
					printMap( request.getServer().asMap() );

					// TODO Environment Variables
				}
				else
					Networking.L.severe( String.format( "%s%sHttpError Developer Message: %s", EnumColor.GOLD, EnumColor.NEGATIVE, developerMessage ) );

				println( "<hr>" );
				println( Kernel.getDevMeta().getHTMLFooter() );
				println( "</div>" );
				println( "</body></html>" );

				setContentType( "text/html" );
				setEncoding( Charsets.UTF_8 );
				this.httpCode = httpCode;
				sendResponse();
			}
		}
		else
		{
			print( event.getErrorHtml() );
			setContentType( "text/html" );
			setEncoding( Charsets.UTF_8 );
			this.httpCode = httpCode;
			sendResponse();
		}
	}

	@Override
	public void sendException( Throwable cause ) throws IOException
	{
		Objs.notNull( cause );

		if ( stage == HttpResponseStage.CLOSED )
			throw new IllegalStateException( "You can't access sendException method within this HttpResponse because the connection has been closed." );

		if ( cause instanceof HttpError && cause.getCause() == null )
		{
			sendError( ( HttpError ) cause );
			return;
		}

		HttpExceptionEvent event = new HttpExceptionEvent( request, cause, Kernel.isDevelopment() );
		Events.getInstance().callEvent( event );

		int httpCode = event.getHttpCode();

		if ( Kernel.isDevelopment() )
		{
			if ( Objs.isEmpty( event.getErrorHtml() ) )
			{
				if ( cause == null )
					sendError( httpCode, null, "No Stacktrace Available!" );

				String stackTrace = Exceptions.getStackTrace( cause );

				if ( request.getScriptingFactory() != null )
					for ( Entry<String, ScriptingContext> e : request.getScriptingFactory().getStack().getScriptTraceHistory().entrySet() )
						stackTrace = stackTrace.replace( e.getKey(), e.getValue().getFileName() );

				sendError( httpCode, null, "<pre>" + stackTrace + "</pre>" );
			}
			else
			{
				log.log( Level.SEVERE, "%s {code=500}", HttpCode.getReason( 500 ) );

				resetBuffer();
				print( event.getErrorHtml() );

				setContentType( "text/html" );
				setEncoding( Charsets.UTF_8 );
				sendResponse();
			}
		}
		else
		{
			StringBuilder sb = new StringBuilder();
			sb.append( "<p>The server encountered an exception and unfortunately the server is not in development mode, so no debug information will be made available.</p>\n" );
			sb.append( "<p>If you are the server owner or developer, you can turn development on by setting 'server.developmentMode' to true in the config file.</p>\n" );
			sendError( 500, null, sb.toString() );
		}
	}

	@Override
	public void sendLoginPage()
	{
		sendLoginPage( "You must be logged in to view this page" );
	}

	@Override
	public void sendLoginPage( String msg )
	{
		sendLoginPage( msg, null );
	}

	@Override
	public void sendLoginPage( String msg, String level )
	{
		sendLoginPage( msg, level, null );
	}

	@Override
	public void sendLoginPage( String msg, String level, String target )
	{
		Nonce nonce = request.getSession().getNonce();
		nonce.put( "msg", msg );
		nonce.put( "level", level == null || level.length() == 0 ? "danger" : level );
		nonce.put( "target", target == null || target.length() == 0 ? request.getFullUrl() : target );
		String loginForm = request.getWebroot().getLoginForm();
		if ( !loginForm.toLowerCase().startsWith( "http" ) )
			loginForm = ( request.isSecure() ? "https://" : "http://" ) + loginForm;
		sendRedirect( String.format( "%s?%s=%s", loginForm, nonce.key(), nonce.value() ) );
	}

	@Override
	public void sendMultipart( byte[] bytesToWrite ) throws IOException
	{
		if ( request.getHttpMethod() == HttpMethod.HEAD )
			throw new IllegalStateException( "You can't start MULTIPART mode on a HEAD Request." );

		if ( stage != HttpResponseStage.MULTIPART )
		{
			stage = HttpResponseStage.MULTIPART;
			HttpResponse response = new DefaultHttpResponse( HttpVersion.HTTP_1_1, HttpResponseStatus.OK );

			HttpHeaders h = response.headers();
			try
			{
				request.getSession().save();
			}
			catch ( SessionException.Error e )
			{
				e.printStackTrace();
			}

			request.getHoneyCookies().filter( HoneyCookie::needsUpdating ).forEach( cookie -> h.add( "Set-Cookie", cookie.toString() ) );

			if ( h.get( "Server" ) == null )
				h.add( "Server", Kernel.getDevMeta().getProductName() + " Version " + Kernel.getDevMeta().getVersionDescribe() );

			h.add( "Access-Control-Allow-Origin", request.getWebroot().getConfig().getValue( WebrootRegistry.Config.WEBROOTS_ALLOW_ORIGIN ) );
			h.add( "Connection", "close" );
			h.add( "Cache-Control", "no-execute" );
			h.add( "Cache-Control", "private" );
			h.add( "Pragma", "no-execute" );
			h.set( "Content-Type", "multipart/x-mixed-replace; boundary=--cwsframe" );

			// if ( isKeepAlive( request ) )
			{
				// response.headers().set( CONNECTION, HttpHeaders.Values.KEEP_ALIVE );
			}

			request.getChannel().write( response );
		}
		else
		{
			StringBuilder sb = new StringBuilder();

			sb.append( "--cwsframe\r\n" );
			sb.append( "Content-Type: " + httpContentType + "\r\n" );
			sb.append( "Content-Length: " + bytesToWrite.length + "\r\n\r\n" );

			ByteArrayOutputStream ba = new ByteArrayOutputStream();

			ba.write( sb.toString().getBytes( encoding ) );
			ba.write( bytesToWrite );
			ba.flush();

			ChannelFuture sendFuture = request.getChannel().write( new ChunkedStream( new ByteArrayInputStream( ba.toByteArray() ) ), request.getChannel().newProgressivePromise() );

			ba.close();

			sendFuture.addListener( new ChannelProgressiveFutureListener()
			{
				@Override
				public void operationComplete( ChannelProgressiveFuture future ) throws Exception
				{
					Networking.L.info( "Transfer complete." );
				}

				@Override
				public void operationProgressed( ChannelProgressiveFuture future, long progress, long total )
				{
					if ( total < 0 )
						Networking.L.info( "Transfer progress: " + progress );
					else
						Networking.L.info( "Transfer progress: " + progress + " / " + total );
				}
			} );
		}
	}

	@Override
	public void sendRedirect( String target )
	{
		sendRedirect( target, HttpCode.HTTP_MOVED_TEMP );
	}

	@Override
	public void sendRedirect( String target, HttpCode httpCode )
	{
		sendRedirect( target, httpCode, null );
	}

	@Override
	public void sendRedirect( String target, HttpCode httpCode, Map<String, String> nonceValues )
	{
		// NetworkManager.getLogger().info( ConsoleColor.DARK_GRAY + "Sending page redirect to `" + target + "` using httpCode `" + httpCode + " - " + HttpCode.msg( httpCode ) + "`" );
		log.log( Level.INFO, "Redirect {uri=%s,httpCode=%s,status=%s}", target, this.httpCode, httpCode );

		if ( stage == HttpResponseStage.CLOSED )
			throw new IllegalStateException( "You can't access sendRedirect method within this HttpResponse because the connection has been closed." );

		if ( nonceValues != null && nonceValues.size() > 0 )
		{
			target += ( target.contains( "?" ) ? "&" : "?" ) + request.getSession().getNonce().query();
			request.getSession().getNonce().putAll( nonceValues );
		}

		if ( !isCommitted() )
		{
			setStatus( this.httpCode );
			setHeader( "Location", target );
		}
		else
			try
			{
				sendError( 301, "The requested URL has been relocated to '" + target + "'" );
			}
			catch ( IOException e )
			{
				e.printStackTrace();
			}

		try
		{
			sendResponse();
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}
	}

	@Override
	public void sendRedirect( String target, Map<String, String> nonceValues )
	{
		sendRedirect( target, HttpCode.HTTP_MOVED_TEMP, nonceValues );
	}

	@Override
	public void sendRedirectRepost( String target )
	{
		sendRedirect( target, request.getHttpVersion() == HttpVersion.HTTP_1_0 ? HttpCode.HTTP_MOVED_TEMP : HttpCode.HTTP_TEMPORARY_REDIRECT );
	}

	@Override
	public void sendResponse() throws IOException
	{
		if ( stage == HttpResponseStage.CLOSED || stage == HttpResponseStage.WRITTEN )
			return;

		FullHttpResponse response = new DefaultFullHttpResponse( HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf( httpCode.getCode(), httpCode.getReason() ), output );
		HttpHeaders headers = response.headers();

		if ( request.hasSession() )
		{
			Session session = request.getSession();

			/**
			 * Initiate the Session Persistence Method.
			 * This is usually done with a cookie but we should make a param optional
			 */
			session.processSessionCookie( request.getRootDomain() );

			session.getCookies().filter( HoneyCookie::needsUpdating ).forEach( cookie -> headers.add( HttpHeaderNames.SET_COOKIE, cookie.toString() ) );

			if ( session.getSessionCookie().needsUpdating() )
				headers.add( HttpHeaderNames.SET_COOKIE, session.getSessionCookie().toString() );
		}

		if ( locale != null )
			headers.set( HttpHeaderNames.CONTENT_LANGUAGE, locale.toLanguageTag() );
		// TODO @See ServletResponse#setLocale

		// This might be a temporary measure - TODO Properly set the charset for each request.
		headers.set( HttpHeaderNames.CONTENT_TYPE, httpContentType + "; charset=" + encoding.name() );

		headers.set( HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE );
		headers.add( HttpHeaderNames.SERVER, Kernel.getDevMeta().getProductName() + " Version " + Kernel.getDevMeta().getVersionDescribe() );
		headers.setInt( HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes() );

		headers.add( HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, request.getWebroot().getConfig().getValue( WebrootRegistry.Config.WEBROOTS_ALLOW_ORIGIN ) );

		/*
		 * We define all header keys as lowercase to support HTTP/2 requirements while also not
		 * violating HTTP/1.x requirements.  New header names should always be lowercase.
		 * We apologize that there is currently no way to disable this behavior.
		 */

		for ( Entry<AsciiString, Set<String>> headerEntry : this.headers.entrySet() )
			for ( String header : headerEntry.getValue() )
				headers.add( headerEntry.getKey().toLowerCase(), header );

		// Expires: Wed, 08 Apr 2015 02:32:24 GMT
		// DateTimeFormatter formatter = DateTimeFormat.forPattern( "EE, dd-MMM-yyyy HH:mm:ss zz" );

		// headers.set( HttpHeaderNames.EXPIRES, formatter.print( DateTime.now( DateTimeZone.UTC ).plusDays( 1 ) ) );
		// headers.set( HttpHeaderNames.CACHE_CONTROL, "public, max-age=86400" );

		stage = HttpResponseStage.WRITTEN;

		request.getChannel().writeAndFlush( response );
	}

	@Override
	public void setAnnotation( String key, String val )
	{
		setAnnotation( AsciiString.of( key ), val );
	}

	@Override
	public void setAnnotation( AsciiString key, String val )
	{
		annotations.put( key, val );
	}

	public void setApacheParser( ApacheHandler htaccess )
	{
		this.htaccess = htaccess;
	}

	@Override
	public void setBufferSize( int size )
	{
		output = output.capacity( size );
	}

	@Override
	public void setCharacterEncoding( String charset )
	{
		request.setCharacterEncoding( charset );
	}

	@Override
	public void setContentLength( long length )
	{
		setLongHeader( HttpHeaderNames.CONTENT_LENGTH, length );
	}

	@Override
	public void setContentLength( int length )
	{
		setIntHeader( HttpHeaderNames.CONTENT_LENGTH, length );
	}

	@Override
	public void setContentLengthLong( long length )
	{
		setLongHeader( HttpHeaderNames.CONTENT_LENGTH, length );
	}

	@Override
	public void setContentType( String type )
	{
		if ( type == null || type.isEmpty() )
			type = "text/html";

		httpContentType = type;
	}

	@Override
	public void setDateHeader( String name, long date )
	{
		setLongHeader( name, date );
	}

	@Override
	public void setEncoding( String encoding )
	{
		this.encoding = Charset.forName( encoding );
	}

	@Override
	public void setEncoding( Charset encoding )
	{
		this.encoding = encoding;
	}

	public void setHeader( AsciiString name, String value )
	{
		headers.computeIfAbsent( name, key -> new HashSet<>() ).add( value );
	}

	@Override
	public void setHeader( String name, String value )
	{
		setHeader( AsciiString.of( name ), value );
	}

	public void setIntHeader( AsciiString name, int value )
	{
		headers.computeIfAbsent( name, key -> new HashSet<>() ).add( Integer.toString( value ) );
	}

	@Override
	public void setIntHeader( String name, int value )
	{
		setIntHeader( AsciiString.of( name ), value );
	}

	@Override
	public void setLocale( Locale locale )
	{
		this.locale = locale;
	}

	public void setLongHeader( String name, long value )
	{
		setLongHeader( AsciiString.of( name ), value );
	}

	public void setLongHeader( AsciiString name, long value )
	{
		Set<String> values = new HashSet<>();
		values.add( Long.toString( value ) );
		headers.put( name, values );
	}

	@Override
	public void setStatus( HttpCode httpCode )
	{
		if ( stage == HttpResponseStage.CLOSED )
			throw new IllegalStateException( "You can't access setStatus( status ) method within this HttpResponse because the connection is closed." );

		this.httpCode = httpCode;
	}

	@Override
	public void setStatus( int code )
	{
		setStatus( HttpCode.getHttpCode( code ).orElseThrow( IllegalArgumentException::new ) );
	}

	@Override
	public void setStatus( int code, String reason )
	{

	}

	@Override
	public void write( byte[] bytes ) throws IOException
	{
		if ( stage != HttpResponseStage.MULTIPART )
			stage = HttpResponseStage.WRITING;

		output.writeBytes( bytes );
	}

	@Override
	public void write( ByteBuf buf ) throws IOException
	{
		if ( stage != HttpResponseStage.MULTIPART )
			stage = HttpResponseStage.WRITING;

		output.writeBytes( buf.retain() );
	}
}
