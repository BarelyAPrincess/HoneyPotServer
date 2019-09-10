package com.marchnetworks.command.common;

import com.marchnetworks.command.api.app.AppIds;
import com.marchnetworks.command.api.security.CommandAuthenticationDetails;
import com.marchnetworks.command.spring.security.SessionAuthenticationToken;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CommonAppUtils
{
	private static final Logger LOG = LoggerFactory.getLogger( CommonAppUtils.class );

	private static final int READ_BUFFER_SIZE = 8192;
	private static ThreadLocal<Map<String, Object>> authenticationObjects = new ThreadLocal()
	{
		protected Map<String, Object> initialValue()
		{
			return new HashMap();
		}
	};

	public static final String DEFAULT_AUTH = "_server_";

	public static boolean isNullOrEmptyString( String s )
	{
		return ( s == null ) || ( s.isEmpty() );
	}

	public static String stringValueOf( Object objectToConvert )
	{
		return objectToConvert == null ? null : objectToConvert.toString();
	}

	public static String encodeToUTF8String( byte[] data )
	{
		if ( data == null )
			return null;
		try
		{
			return new String( data, "UTF8" );
		}
		catch ( UnsupportedEncodingException e )
		{
			LOG.warn( "Error attempting to use UTF-8 encoding when converting byte to String." );
		}
		return null;
	}

	public static byte[] encodeStringToBytes( String input )
	{
		if ( isNullOrEmptyString( input ) )
		{
			return null;
		}
		try
		{
			return input.getBytes( "UTF-8" );
		}
		catch ( UnsupportedEncodingException e )
		{
			LOG.warn( "Error attempting to use UTF-8 encoding when converting String to byte." );
		}
		return null;
	}

	public static String byteToBase64( byte[] data )
	{
		return Base64.encodeBytes( data );
	}

	public static byte[] stringBase64ToByte( String s )
	{
		try
		{
			return Base64.decode( s );
		}
		catch ( IOException e )
		{
			LOG.warn( "Error attempting to use Base64 decode String to byte." );
		}
		return null;
	}

	public static String stringToBase64( String s )
	{
		byte[] data = encodeStringToBytes( s );
		return Base64.encodeBytes( data );
	}

	public static String stringBase64ToString( String s )
	{
		byte[] data = stringBase64ToByte( s );
		return encodeToUTF8String( data );
	}

	public static String getUsernameFromSecurityContext()
	{
		String authenticatedUser = null;
		SecurityContext context = SecurityContextHolder.getContext();
		if ( context != null )
		{
			Authentication authentication = context.getAuthentication();
			if ( ( authentication != null ) && ( !( authentication instanceof AnonymousAuthenticationToken ) ) )
			{
				authenticatedUser = authentication.getName();
			}
		}

		return authenticatedUser;
	}

	public static String getSessionIdFromSecurityContext()
	{
		String sessionId = null;
		SecurityContext context = SecurityContextHolder.getContext();
		if ( context != null )
		{
			Authentication authentication = context.getAuthentication();
			if ( ( authentication != null ) && ( ( authentication instanceof SessionAuthenticationToken ) ) )
			{
				SessionAuthenticationToken token = ( SessionAuthenticationToken ) authentication;
				sessionId = token.getSessionId();
			}
		}

		return sessionId;
	}

	public static String getRemoteIpAddressFromSecurityContext()
	{
		String remoteAddress = null;

		SecurityContext context = SecurityContextHolder.getContext();
		if ( ( context != null ) && ( context.getAuthentication() != null ) && ( context.getAuthentication().getDetails() != null ) )
		{
			System.setProperty( "java.net.preferIPv4Stack", "true" );
			WebAuthenticationDetails details = ( WebAuthenticationDetails ) context.getAuthentication().getDetails();
			remoteAddress = details.getRemoteAddress();
		}
		return remoteAddress;
	}

	public static boolean isCommandClientRequest()
	{
		SecurityContext context = SecurityContextHolder.getContext();
		if ( ( context != null ) && ( context.getAuthentication() != null ) && ( context.getAuthentication().getDetails() != null ) && ( ( context.getAuthentication().getDetails() instanceof CommandAuthenticationDetails ) ) )
		{
			CommandAuthenticationDetails sessionDetails = ( CommandAuthenticationDetails ) context.getAuthentication().getDetails();
			return AppIds.isCommandClient( sessionDetails.getAppId() );
		}

		return false;
	}

	public static Object getDetailParameter( String key )
	{
		Object obj = null;
		SecurityContext context = SecurityContextHolder.getContext();
		if ( ( context != null ) && ( context.getAuthentication() != null ) && ( context.getAuthentication().getDetails() != null ) )
		{
			CommandAuthenticationDetails details = ( CommandAuthenticationDetails ) context.getAuthentication().getDetails();
			obj = details.getParams().get( key );
		}
		return obj;
	}

	public static String readFileToStringInBundle( Class provider, String filename, String encoding )
	{
		String content = null;
		try
		{
			InputStream inputStream = getInputSteamInBundle( provider, filename );
			Throwable localThrowable2 = null;
			try
			{
				content = readInputStream( inputStream, encoding );
			}
			catch ( Throwable localThrowable1 )
			{
				localThrowable2 = localThrowable1;
				throw localThrowable1;
			}
			finally
			{
				if ( inputStream != null )
					if ( localThrowable2 != null )
						try
						{
							inputStream.close();
						}
						catch ( Throwable x2 )
						{
							localThrowable2.addSuppressed( x2 );
						}
					else
						inputStream.close();
			}
		}
		catch ( IOException e )
		{
			LOG.error( "email template cannot be loaded!", e );
			throw new RuntimeException( e );
		}
		return content;
	}

	public static InputStream getInputSteamInBundle( Class provider, String path ) throws IOException
	{
		Bundle bundle = FrameworkUtil.getBundle( provider );
		URL url = bundle.getEntry( path );
		InputStream template = url.openConnection().getInputStream();
		return template;
	}

	public static String readFileToString( String path )
	{
		try
		{
			FileInputStream fileInputStream = new FileInputStream( path );
			Throwable localThrowable2 = null;
			try
			{
				return readInputStream( fileInputStream, "UTF-8" );
			}
			catch ( Throwable localThrowable3 )
			{
				localThrowable2 = localThrowable3;
				throw localThrowable3;
			}
			finally
			{
				if ( fileInputStream != null )
					if ( localThrowable2 != null )
						try
						{
							fileInputStream.close();
						}
						catch ( Throwable x2 )
						{
							localThrowable2.addSuppressed( x2 );
						}
					else
					{
						fileInputStream.close();
					}
			}
		}
		catch ( IOException e )
		{
			LOG.warn( "File " + path + " could not be read" );
		}

		return null;
	}

	public static byte[] readFileToByteArray( Class provider, String path )
	{
		try
		{
			InputStream fileInputStream = getInputSteamInBundle( provider, path );
			Throwable localThrowable2 = null;
			try
			{
				return readInputStream( fileInputStream );
			}
			catch ( Throwable localThrowable3 )
			{
				localThrowable2 = localThrowable3;
				throw localThrowable3;
			}
			finally
			{
				if ( fileInputStream != null )
					if ( localThrowable2 != null )
						try
						{
							fileInputStream.close();
						}
						catch ( Throwable x2 )
						{
							localThrowable2.addSuppressed( x2 );
						}
					else
					{
						fileInputStream.close();
					}
			}
		}
		catch ( IOException e )
		{
			LOG.warn( "File " + path + " could not be read" );
		}
		return null;
	}

	public static void clearFile( String file )
	{
		try
		{
			PrintWriter out = new PrintWriter( file );
			out.close();
		}
		catch ( FileNotFoundException localFileNotFoundException )
		{
		}
	}

	private static void closeQuietly( Closeable io )
	{
		if ( io != null )
		{
			try
			{
				io.close();
			}
			catch ( IOException e )
			{
				LOG.warn( "Error closing file input stream" );
			}
		}
	}

	public static String getFileExtension( String file )
	{
		String result = "";
		int i = file.lastIndexOf( '.' );
		if ( i > 0 )
		{
			result = file.substring( i + 1 );
		}
		return result;
	}

	public static boolean getValue( Boolean b )
	{
		return ( b != null ) && ( b.booleanValue() );
	}

	public static boolean writeStringToFile( String path, String text )
	{
		try
		{
			PrintWriter out = new PrintWriter( path );
			Throwable localThrowable2 = null;
			try
			{
				out.println( text );
				return true;
			}
			catch ( Throwable localThrowable3 )
			{
				localThrowable2 = localThrowable3;
				throw localThrowable3;
			}
			finally
			{
				if ( out != null )
					if ( localThrowable2 != null )
						try
						{
							out.close();
						}
						catch ( Throwable x2 )
						{
							localThrowable2.addSuppressed( x2 );
						}
					else
					{
						out.close();
					}
			}
		}
		catch ( IOException e )
		{
			LOG.warn( "File " + path + " could not be written" );
		}
		return false;
	}

	public static String readInputStream( InputStream inputStream, String encoding ) throws IOException
	{
		byte[] readInputStream = readInputStream( inputStream );
		return new String( readInputStream, encoding );
	}

	public static byte[] readInputStream( InputStream inputStream ) throws IOException
	{
		return readInputStream( inputStream, 0 );
	}

	public static byte[] readInputStream( InputStream inputStream, int inputSize ) throws IOException
	{
		if ( inputStream == null )
		{
			return null;
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream( inputSize > 0 ? inputSize : 8192 );
		byte[] buffer = new byte[' '];
		int length = 0;
		while ( ( length = inputStream.read( buffer ) ) != -1 )
		{
			baos.write( buffer, 0, length );
		}
		return baos.toByteArray();
	}

	public static void writeToOutputStream( InputStream input, OutputStream output ) throws IOException
	{
		try
		{
			byte[] tmp = new byte[' '];
			int i = 0;
			while ( ( i = input.read( tmp ) ) >= 0 )
			{
				output.write( tmp, 0, i );
			}
		}
		finally
		{
			closeQuietly( input );
			closeQuietly( output );
		}
	}

	public static boolean appendStringToFile( String path, String text )
	{
		try
		{
			PrintWriter out = new PrintWriter( new BufferedWriter( new FileWriter( path, true ) ) );
			Throwable localThrowable2 = null;
			try
			{
				out.println( text );
				return true;
			}
			catch ( Throwable localThrowable3 )
			{
				localThrowable2 = localThrowable3;
				throw localThrowable3;
			}
			finally
			{
				if ( out != null )
					if ( localThrowable2 != null )
						try
						{
							out.close();
						}
						catch ( Throwable x2 )
						{
							localThrowable2.addSuppressed( x2 );
						}
					else
					{
						out.close();
					}
			}
		}
		catch ( IOException e )
		{
			LOG.error( "Error writing to " + path + ", Exception: " + e.getMessage() );
		}
		return false;
	}

	public static boolean fileExists( String path )
	{
		File f = new File( path );
		return ( f.exists() ) && ( !f.isDirectory() );
	}

	public static boolean equalsWithNull( Object object1, Object object2 )
	{
		if ( ( object1 == null ) && ( object2 == null ) )
		{
			return true;
		}
		if ( ( object1 == null ) && ( object2 != null ) )
		{
			return false;
		}
		if ( ( object1 != null ) && ( object2 == null ) )
		{
			return false;
		}
		return object1.equals( object2 );
	}

	public static void addAuthenticationObject( String key, Object value )
	{
		( ( Map ) authenticationObjects.get() ).put( key, value );
	}

	public static Object getAuthenticationObject( String key )
	{
		return ( ( Map ) authenticationObjects.get() ).get( key );
	}

	public static void clearAuthenticationObjects()
	{
		( ( Map ) authenticationObjects.get() ).clear();
	}

	public static <T> boolean isNullOrEmptyCollection( Collection<T> collection )
	{
		if ( ( collection == null ) || ( collection.isEmpty() ) )
		{
			return true;
		}
		return false;
	}
}

