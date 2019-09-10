package com.marchnetworks.command.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpUtils
{
	private static final Logger LOG = LoggerFactory.getLogger( HttpUtils.class );

	public static final String PROTOCOL_HTTP = "http";

	public static final String PROTOCOL_HTTPS = "https";

	public static final String HTTPS_SYSTEM_PROPERTY = "https.protocols";

	public static final HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier()
	{
		public boolean verify( String hostname, SSLSession session )
		{
			return true;
		}
	};

	public static TrustManager[] TRUST_ALL_MANAGER = {new X509TrustManager()
	{
		public X509Certificate[] getAcceptedIssuers()
		{
			return null;
		}

		public void checkClientTrusted( X509Certificate[] certs, String authType )
		{
		}

		public void checkServerTrusted( X509Certificate[] certs, String authType )
		{
		}
	}};

	public static SSLSocketFactory SOCKET_FACTORY;

	static
	{
		try
		{
			SSLContext sc = SSLContext.getInstance( "SSL" );
			sc.init( null, TRUST_ALL_MANAGER, new SecureRandom() );
			SOCKET_FACTORY = sc.getSocketFactory();
		}
		catch ( Exception e )
		{
			LOG.error( "Could not install the all trusting manager", e );
		}
	}

	public static boolean isLocalAddress( String address )
	{
		try
		{
			InetAddress addr = InetAddress.getByName( address );
			if ( ( addr.isAnyLocalAddress() ) || ( addr.isLoopbackAddress() ) )
			{
				return true;
			}
			if ( NetworkInterface.getByInetAddress( addr ) != null )
			{
				return true;
			}
		}
		catch ( IOException e )
		{
			return false;
		}
		return false;
	}

	public static boolean isIPv4Address( String address )
	{
		if ( ( address == null ) || ( address.isEmpty() ) || ( address.endsWith( "." ) ) )
		{
			return false;
		}

		String[] parts = address.split( "\\." );
		if ( parts.length != 4 )
		{
			return false;
		}

		for ( String s : parts )
		{
			for ( char c : s.toCharArray() )
			{
				if ( Character.isLetter( c ) )
				{
					return false;
				}
			}
		}

		return true;
	}

	public static String getAddressWithoutPort( String address )
	{
		if ( ( address == null ) || ( address.isEmpty() ) )
		{
			return address;
		}

		String[] parts = address.split( ":" );
		if ( parts.length > 1 )
		{
			return parts[0];
		}

		return address;
	}

	public static String getPortWithoutAddress( String address )
	{
		if ( ( address == null ) || ( address.isEmpty() ) )
		{
			return null;
		}

		String[] parts = address.split( ":" );
		if ( parts.length == 2 )
		{
			return parts[1];
		}

		return null;
	}

	public static String[] setPortOnAddresses( String[] addresses, int port )
	{
		for ( int i = 0; i < addresses.length; i++ )
		{
			addresses[i] = setPortOnAddress( addresses[i], Integer.toString( port ) );
		}
		return addresses;
	}

	public static String setPortOnAddress( String address, String port )
	{
		if ( ( address != null ) && ( address.indexOf( ":" ) < 0 ) )
		{
			return address + ":" + port;
		}
		return address;
	}

	public static Map<String, String> queryToMap( String query )
	{
		Map<String, String> result = new HashMap( 2 );
		if ( ( query == null ) || ( query.isEmpty() ) )
		{
			return result;
		}
		for ( String param : query.split( "&" ) )
		{
			String[] pair = param.split( "=" );
			if ( pair.length > 1 )
			{
				result.put( pair[0], pair[1] );
			}
			else
			{
				result.put( pair[0], "" );
			}
		}
		return result;
	}

	public static String[] getEnabledTLSVersions()
	{
		String enabledTLSProtocols = System.getProperty( "https.protocols" );
		List<String> protocols = CollectionUtils.stringToList( enabledTLSProtocols );
		return ( String[] ) protocols.toArray( new String[protocols.size()] );
	}

	public static String encodeUrlParam( String input )
	{
		try
		{
			return URLEncoder.encode( input, "UTF-8" );
		}
		catch ( UnsupportedEncodingException e )
		{
			throw new IllegalArgumentException( e );
		}
	}
}
