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
import com.google.common.collect.Maps;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import io.amelia.events.Events;
import io.amelia.foundation.ConfigRegistry;
import io.amelia.http.apache.ApacheHandler;
import io.amelia.logging.LogEvent;
import io.amelia.scripting.api.Web;
import io.amelia.support.Objs;
import io.amelia.support.Strs;
import io.netty.buffer.ByteBuf;
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

/**
 * Wraps the Netty HttpResponse to provide easy methods for manipulating the result of each request
 */
public class HttpResponseWrapper
{
	final Map<String, String> annotations = Maps.newHashMap();
	final Map<String, String> headers = Maps.newHashMap();
	final LogEvent log;
	final HttpRequestWrapper request;
	Charset encoding = Charsets.UTF_8;
	ApacheHandler htaccess = null;
	String httpContentType = "text/html";
	HttpResponseStatus httpStatus = HttpResponseStatus.OK;
	ByteBuf output = Unpooled.buffer();
	HttpResponseStage stage = HttpResponseStage.READING;

	protected HttpResponseWrapper( HttpRequestWrapper request, LogEvent log )
	{
		this.request = request;
		this.log = log;
	}

	public void close()
	{
		request.getChannel().close();
		stage = HttpResponseStage.CLOSED;
	}

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

	public String getAnnotation( String key )
	{
		return annotations.get( key );
	}

	public Charset getEncoding()
	{
		return encoding;
	}

	public void setEncoding( String encoding )
	{
		this.encoding = Charset.forName( encoding );
	}

	public int getHttpCode()
	{
		return httpStatus.code();
	}

	public String getHttpMsg()
	{
		return HttpCode.msg( httpStatus.code() );
	}

	public ByteBuf getOutput()
	{
		return output;
	}

	public byte[] getOutputBytes()
	{
		byte[] bytes = new byte[output.writerIndex()];
		int inx = output.readerIndex();
		output.readerIndex( 0 );
		output.readBytes( bytes );
		output.readerIndex( inx );
		return bytes;
	}

	/**
	 * @return HttpResponseStage
	 */
	public HttpResponseStage getStage()
	{
		return stage;
	}

	public boolean isCommitted()
	{
		return stage == HttpResponseStage.CLOSED || stage == HttpResponseStage.WRITTEN;
	}

	@Deprecated
	public void print( byte[] bytes ) throws IOException
	{
		write( bytes );
	}

	/**
	 * Prints a single string of text to the buffered output
	 *
	 * @param var string of text.
	 *
	 * @throws IOException if there was a problem with the output buffer.
	 */
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

	/**
	 * Prints a single string of text with a line return to the buffered output
	 *
	 * @param var string of text.
	 *
	 * @throws IOException if there was a problem with the output buffer.
	 */
	public void println( String var ) throws IOException
	{
		if ( var != null && !var.isEmpty() )
			write( ( var + "\n" ).getBytes( encoding ) );
	}

	public void resetBuffer()
	{
		output = Unpooled.buffer();
	}

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

	public void sendError( HttpResponseStatus status ) throws IOException
	{
		sendError( status.code(), status.reasonPhrase().toString(), null );
	}

	public void sendError( HttpResponseStatus status, String statusReason, String developerMessage ) throws IOException
	{
		sendError( status.code(), statusReason, developerMessage );
	}

	public void sendError( HttpResponseStatus status, String developerMessage ) throws IOException
	{
		sendError( status.code(), status.reasonPhrase().toString(), developerMessage );
	}

	public void sendError( int statusCode ) throws IOException
	{
		sendError( statusCode, null );
	}

	public void sendError( int statusCode, String statusReason ) throws IOException
	{
		sendError( statusCode, statusReason, null );
	}

	public void sendError( int statusCode, String statusReason, String developerMessage ) throws IOException
	{
		if ( stage == HttpResponseStage.CLOSED )
			throw new IllegalStateException( "You can't access sendError method within this HttpResponse because the connection has been closed." );

		if ( statusCode < 1 || statusCode > 600 )
			statusCode = 500;

		resetBuffer();

		// Trigger an internal Error Event to notify plugins of a possible problem.
		HttpErrorEvent event = new HttpErrorEvent( request, statusCode, statusReason, ConfigRegistry.i().getBoolean( "server.developmentMode" ) );
		Events.callEvent( event );

		statusCode = event.getHttpCode();
		statusReason = event.getHttpReason();

		if ( statusReason == null || statusReason.length() > 255 )
			log.log( Level.SEVERE, "%s {code=%s}", statusReason, statusCode );
		else
			log.log( Level.SEVERE, "%s {code=%s,reason=%s}", statusReason, statusCode, statusReason );

		if ( event.getErrorHtml() == null || event.getErrorHtml().length() == 0 )
		{
			boolean printHtml = true;

			if ( htaccess != null && htaccess.getErrorDocument( statusCode ) != null )
			{
				String resp = htaccess.getErrorDocument( statusCode ).getResponse();

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
				println( "<title>" + statusCode + " - " + statusReason + "</title>" );
				println( "<style>body { margin: 0; padding: 0; } h1, h2, h3, h4, h5, h6 { margin: 0; } .container { padding: 8px; } .debug-header { display: block; margin: 15px 0 0; font-size: 18px; color: #303030; font-weight: bold; } #debug-getTable { border: 1px solid; width: 100%; } #debug-getTable thead { background-color: #eee; } #debug-getTable #col_0 { width: 20%; min-width: 130px; overflow: hidden; font-weight: bold; color: #463C54; padding-right: 5px; } #debug-getTable #tblStringRow { color: rgba(0, 0, 0, .3); font-weight: 300; }</style>" );
				println( "</head><body>" );

				println( "<div class=\"container\" style=\" background-color: #eee; \">" );
				println( "<h1>" + statusCode + " - " + statusReason + "</h1>" );
				println( "</div>" );
				println( "<div class=\"container\">" );

				if ( Versioning.isDevelopment() )
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
					println( Builtin.createTable( tbl, cols, "debug-getTable" ) );

					println( "<span class=\"debug-header\">Cookies</span>" );
					printMap( new HashMap<String, Object>()
					{{
						for ( HttpCookie cookie : request.getCookies() )
							put( cookie.getKey(), cookie.getValue() );
						for ( HttpCookie cookie : request.getServerCookies() )
							put( cookie.getKey() + " (Server Cookie)", cookie.getValue() );
						// TODO Implement a HTML color code parser, like a built-in BBCode or Markdown parser
						// put( cookie.getKey() + " <span style=\"color: #eee; font-weight: 300;\">(internal)</span>", cookie.getValue() );
					}} );

					if ( request.hasSession() )
					{
						println( "<span class=\"debug-header\">Session</span>" );
						printMap( request.getSession().getDataMap() );
					}

					println( "<span class=\"debug-header\">Server/Header Data</span>" );
					printMap( request.getServer().asMap() );

					// TODO Environment Variables
				}
				else
					NetworkManager.getLogger().severe( String.format( "%s%sHttpError Developer Message: %s", EnumColor.GOLD, EnumColor.NEGATIVE, developerMessage ) );

				println( "<hr>" );
				println( "<small>Running <a href=\"https://github.com/ChioriGreene/ChioriWebServer\">" + Versioning.getProduct() + "</a> Version " + Versioning.getVersion() + " (Build #" + Versioning.getBuildNumber() + ")<br />" + Versioning.getCopyright() + "</small>" );
				println( "</div>" );
				println( "</body></html>" );

				setContentType( "text/html" );
				setEncoding( Charsets.UTF_8 );
				httpStatus = HttpResponseStatus.valueOf( statusCode );
				sendResponse();
			}
		}
		else
		{
			print( event.getErrorHtml() );
			setContentType( "text/html" );
			setEncoding( Charsets.UTF_8 );
			httpStatus = HttpResponseStatus.valueOf( statusCode );
			sendResponse();
		}
	}

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

		HttpExceptionEvent event = new HttpExceptionEvent( request, cause, Versioning.isDevelopment() );
		EventDispatcher.i().callEvent( event );

		int httpCode = event.getHttpCode();

		if ( Versioning.isDevelopment() )
		{
			if ( Objs.isEmpty( event.getErrorHtml() ) )
			{
				if ( cause == null )
					sendError( httpCode, null, "No Stacktrace Available!" );

				String stackTrace = ExceptionUtils.getStackTrace( cause );

				if ( request.getScriptingFactory() != null )
					for ( Entry<String, ScriptingContext> e : request.getScriptingFactory().stack().getScriptTraceHistory().entrySet() )
						stackTrace = stackTrace.replace( e.getKey(), e.getValue().filename() );

				sendError( httpCode, null, "<pre>" + stackTrace + "</pre>" );
			}
			else
			{
				log.log( Level.SEVERE, "%s {code=500}", HttpCode.msg( 500 ) );

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

	/**
	 * Sends the client to the site login page found in configuration and also sends a please login message along with it.
	 */
	public void sendLoginPage()
	{
		sendLoginPage( "You must be logged in to view this page" );
	}

	/**
	 * Sends the client to the site login page
	 *
	 * @param msg The message to pass to the login page
	 */
	public void sendLoginPage( String msg )
	{
		sendLoginPage( msg, null );
	}

	/**
	 * Sends the client to the site login page
	 *
	 * @param msg   The message to pass to the login page
	 * @param level The severity level of this login page redirect
	 */
	public void sendLoginPage( String msg, String level )
	{
		sendLoginPage( msg, level, null );
	}

	/**
	 * Sends the client to the site login page
	 *
	 * @param msg    The message to pass to the login page
	 * @param level  The severity level of this login page redirect
	 * @param target The target to redirect to once we receive a successful login
	 */
	public void sendLoginPage( String msg, String level, String target )
	{
		Nonce nonce = request.getSession().getNonce();
		nonce.mapValues( "msg", msg );
		nonce.mapValues( "level", level == null || level.length() == 0 ? "danger" : level );
		nonce.mapValues( "target", target == null || target.length() == 0 ? request.getFullUrl() : target );
		String loginForm = request.getLocation().getLoginForm();
		if ( !loginForm.toLowerCase().startsWith( "http" ) )
			loginForm = ( request.isSecure() ? "https://" : "http://" ) + loginForm;
		sendRedirect( String.format( "%s?%s=%s", loginForm, nonce.key(), nonce.value() ) );
	}

	public void sendMultipart( byte[] bytesToWrite ) throws IOException
	{
		if ( request.method() == HttpMethod.HEAD )
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
			catch ( SessionException e )
			{
				e.printStackTrace();
			}

			for ( HttpCookie c : request.getCookies() )
				if ( c.needsUpdating() )
					h.add( "Set-Cookie", c.toHeaderValue() );

			if ( h.get( "Server" ) == null )
				h.add( "Server", Versioning.getProduct() + " Version " + Versioning.getVersion() );

			h.add( "Access-Control-Allow-Origin", request.getLocation().getConfig().getString( "site.web-allowed-origin", "*" ) );
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
					NetworkManager.getLogger().info( "Transfer complete." );
				}

				@Override
				public void operationProgressed( ChannelProgressiveFuture future, long progress, long total )
				{
					if ( total < 0 )
						NetworkManager.getLogger().info( "Transfer progress: " + progress );
					else
						NetworkManager.getLogger().info( "Transfer progress: " + progress + " / " + total );
				}
			} );
		}
	}

	/**
	 * Send the client to a specified page with http code 302 automatically.
	 *
	 * @param target The destination URL. Can either be relative or absolute.
	 */
	public void sendRedirect( String target )
	{
		sendRedirect( target, 302 );
	}

	/**
	 * Sends the client to a specified page with specified http code but with the option to not automatically go.
	 *
	 * @param target     The destination url. Can be relative or absolute.
	 * @param httpStatus What http code to use.
	 */
	public void sendRedirect( String target, int httpStatus )
	{
		sendRedirect( target, httpStatus, null );
	}

	public void sendRedirect( String target, int httpStatus, Map<String, String> nonceValues )
	{
		// NetworkManager.getLogger().info( ConsoleColor.DARK_GRAY + "Sending page redirect to `" + target + "` using httpCode `" + httpStatus + " - " + HttpCode.msg( httpStatus ) + "`" );
		log.log( Level.INFO, "Redirect {uri=%s,httpCode=%s,status=%s}", target, httpStatus, HttpCode.msg( httpStatus ) );

		if ( stage == HttpResponseStage.CLOSED )
			throw new IllegalStateException( "You can't access sendRedirect method within this HttpResponse because the connection has been closed." );

		if ( nonceValues != null && nonceValues.size() > 0 )
		{
			target += ( target.contains( "?" ) ? "&" : "?" ) + request.getSession().getNonce().query();
			request.getSession().nonce().mapValues( nonceValues );
		}

		if ( !isCommitted() )
		{
			setStatus( httpStatus );
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

	public void sendRedirect( String target, Map<String, String> nonceValues )
	{
		sendRedirect( target, 302, nonceValues );
	}

	public void sendRedirectRepost( String target )
	{
		sendRedirect( target, request.getHttpVersion() == HttpVersion.HTTP_1_0 ? 302 : 307 );
	}

	/**
	 * Sends the data to the client. Internal Use.
	 *
	 * @throws IOException if there was a problem sending the data, like the connection was unexpectedly closed.
	 */
	public void sendResponse() throws IOException
	{
		if ( stage == HttpResponseStage.CLOSED || stage == HttpResponseStage.WRITTEN )
			return;

		FullHttpResponse response = new DefaultFullHttpResponse( HttpVersion.HTTP_1_1, httpStatus, output );
		HttpHeaders h = response.headers();

		if ( request.hasSession() )
		{
			Session session = request.getSession();

			/**
			 * Initiate the Session Persistence Method.
			 * This is usually done with a cookie but we should make a param optional
			 */
			session.processSessionCookie( request.getRootDomain() );

			for ( HttpCookie c : session.getCookies().values() )
				if ( c.needsUpdating() )
					h.add( HttpHeaderNames.SET_COOKIE, c.toHeaderValue() );

			if ( session.getSessionCookie().needsUpdating() )
				h.add( HttpHeaderNames.SET_COOKIE, session.getSessionCookie().toHeaderValue() );
		}

		/*
		 * We define all header keys as lowercase to support HTTP/2 requirements while also not
		 * violating HTTP/1.x requirements.  New header names should always be lowercase.
		 * We apologize that there is currently no way to disable this behavior.
		 */

		h.set( HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE );
		h.add( HttpHeaderNames.SERVER, Versioning.getProduct() + " Version " + Versioning.getVersion() );
		h.setInt( HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes() );

		// This might be a temporary measure - TODO Properly set the charset for each request.
		h.set( HttpHeaderNames.CONTENT_TYPE, httpContentType + "; charset=" + encoding.name() );

		h.add( HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, request.getLocation().getConfig().getString( "site.web-allowed-origin", "*" ) );

		for ( Entry<String, String> header : headers.entrySet() )
			h.add( header.getKey().toLowerCase(), header.getValue() );

		// Expires: Wed, 08 Apr 2015 02:32:24 GMT
		// DateTimeFormatter formatter = DateTimeFormat.forPattern( "EE, dd-MMM-yyyy HH:mm:ss zz" );

		// h.set( HttpHeaderNames.EXPIRES, formatter.print( DateTime.now( DateTimeZone.UTC ).plusDays( 1 ) ) );
		// h.set( HttpHeaderNames.CACHE_CONTROL, "public, max-age=86400" );

		stage = HttpResponseStage.WRITTEN;

		request.getChannel().writeAndFlush( response );
	}

	public void setAnnotation( String key, String val )
	{
		annotations.put( key, val );
	}

	public void setApacheParser( ApacheHandler htaccess )
	{
		this.htaccess = htaccess;
	}

	public void setContentLength( long length )
	{
		setHeader( "Content-Length", length );
	}

	/**
	 * Sets the ContentType header.
	 *
	 * @param type, e.g., text/html or application/xml
	 */
	public void setContentType( String type )
	{
		if ( type == null || type.isEmpty() )
			type = "text/html";

		httpContentType = type;
	}

	public void setEncoding( Charset encoding )
	{
		this.encoding = encoding;
	}

	public void setHeader( String key, Object val )
	{
		headers.put( key, Objs.castToStringWithException( val ) );
	}

	public void setStatus( HttpResponseStatus httpStatus )
	{
		if ( stage == HttpResponseStage.CLOSED )
			throw new IllegalStateException( "You can't access setStatus( status ) method within this HttpResponse because the connection has been closed." );

		this.httpStatus = httpStatus;
	}

	public void setStatus( int status )
	{
		setStatus( HttpResponseStatus.valueOf( status ) );
	}

	/**
	 * Redirects the current page load to a secure HTTPS connection
	 */
	public boolean switchToSecure()
	{
		if ( !NetworkManager.isHttpsRunning() )
		{
			log.log( Level.SEVERE, "We were going to attempt to switch to a secure HTTPS connection and aborted due to the HTTPS server not running." );
			return false;
		}

		if ( request.isSecure() )
			return true;

		sendRedirectRepost( request.getFullUrl( true ) + request.getQuery() );
		return true;
	}

	/**
	 * Redirects the current page load to an unsecure HTTP connection
	 */
	public boolean switchToUnsecure()
	{
		if ( !NetworkManager.isHttpRunning() )
		{
			log.log( Level.SEVERE, "We were going to attempt to switch to an unsecure HTTP connection and aborted due to the HTTP server not running." );
			return false;
		}

		if ( !request.isSecure() )
			return true;

		sendRedirectRepost( request.getFullUrl( false ) + request.getQuery() );
		return true;
	}

	/**
	 * Writes a byte array to the buffered output.
	 *
	 * @param bytes byte array to print
	 *
	 * @throws IOException if there was a problem with the output buffer.
	 */
	public void write( byte[] bytes ) throws IOException
	{
		if ( stage != HttpResponseStage.MULTIPART )
			stage = HttpResponseStage.WRITING;

		output.writeBytes( bytes );
	}

	/**
	 * Writes a ByteBuf to the buffered output
	 *
	 * @param buf byte buffer to print
	 *
	 * @throws IOException if there was a problem with the output buffer.
	 */
	public void write( ByteBuf buf ) throws IOException
	{
		if ( stage != HttpResponseStage.MULTIPART )
			stage = HttpResponseStage.WRITING;

		output.writeBytes( buf.retain() );
	}
}
