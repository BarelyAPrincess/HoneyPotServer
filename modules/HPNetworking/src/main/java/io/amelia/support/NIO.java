/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.support;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Enumeration;

import io.amelia.foundation.Foundation;
import io.amelia.logcompat.LogBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

public class NIO
{
	public static final String REGEX_IPV4 = "^([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])\\.([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])\\.([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])\\.([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])$";
	public static final String REGEX_IPV6 = "^((?:[0-9A-Fa-f]{1,4}))((?::[0-9A-Fa-f]{1,4}))*::((?:[0-9A-Fa-f]{1,4}))((?::[0-9A-Fa-f]{1,4}))*|((?:[0-9A-Fa-f]{1,4}))((?::[0-9A-Fa-f]{1,4})){7}$";

	public static void closeQuietly( Channel channel )
	{
		try
		{
			if ( channel != null && channel.isOpen() )
				channel.close();
		}
		catch ( Throwable t )
		{
			// Ignore
		}
	}

	public static String decodeStringFromByteBuf( ByteBuf buf )
	{
		int length = buf.readInt();
		byte[] bytes = new byte[length];
		buf.readBytes( bytes );
		return Strs.encodeUtf8( bytes );
	}

	public static void encodeStringToByteBuf( ByteBuf buf, String str )
	{
		byte[] bytes = Strs.decodeUtf8( str );
		buf.writeInt( bytes.length );
		buf.writeBytes( bytes );
	}

	/* public static Date getNTPDate()
	{
		String[] hosts = new String[] {"pool.ntp.org"};

		NTPUDPClient client = new NTPUDPClient();
		// We want to timeout if a response takes longer than 5 seconds
		client.setDefaultTimeout( 5000 );

		for ( String host : hosts )
			try
			{
				InetAddress hostAddress = InetAddress.getByName( host );
				// System.out.println( "> " + hostAddress.getHostName() + "/" + hostAddress.getHostAddress() );
				TimeInfo info = client.getTime( hostAddress );
				Date date = new Date( info.getReturnTime() );
				return date;

			}
			catch ( IOException e )
			{
				e.printStackTrace();
			}

		client.close();

		return null;
	} */

	public static InetAddress firstAddr( NetworkInterface iface )
	{
		Enumeration<InetAddress> addresses = iface.getInetAddresses();
		while ( addresses.hasMoreElements() )
		{
			InetAddress address = addresses.nextElement();
			if ( address instanceof Inet4Address )
				return address;
		}
		return null;
	}

	public static NetworkInterface getInterfaceAssigned( String address )
	{
		try
		{
			return NetworkInterface.getByInetAddress( InetAddress.getByName( address ) );
		}
		catch ( UnknownHostException | SocketException e )
		{
			return null;
		}
	}

	public static boolean isAddressAssigned( String address )
	{
		return getInterfaceAssigned( address ) != null;
	}

	public static boolean isValidIPv4( String ip )
	{
		if ( ip == null )
			return false;

		return ip.matches( REGEX_IPV4 );
	}

	public static boolean isValidIPv6( String ip )
	{
		if ( ip == null )
			return false;

		return ip.matches( REGEX_IPV6 );
	}

	public static byte[] readByteBufToBytes( ByteBuf buffer, int numberOfBytes )
	{
		if ( buffer.readableBytes() < numberOfBytes )
			throw new IllegalArgumentException( "Would run out of bytes." );
		byte[] bytes = new byte[numberOfBytes];
		buffer.readBytes( bytes );
		return bytes;
	}

	public static byte[] readByteBufToBytes( ByteBuf buffer )
	{
		byte[] bytes = new byte[buffer.readableBytes()];
		buffer.readBytes( bytes );
		return bytes;
	}

	public static InputStream readByteBufToInputStream( ByteBuf buf, int expectedNumberOfBytes )
	{
		return new ByteArrayInputStream( readByteBufToBytes( buf, expectedNumberOfBytes ) );
	}

	public static InputStream readByteBufToInputStream( ByteBuf buf )
	{
		return new ByteArrayInputStream( readByteBufToBytes( buf ) );
	}

	public static String readByteBufToString( ByteBuf buffer, int numberOfBytes )
	{
		return Strs.bytesToStringUTF( readByteBufToBytes( buffer, numberOfBytes ) );
	}

	public static String readByteBufToString( ByteBuf buffer )
	{
		return Strs.bytesToStringUTF( readByteBufToBytes( buffer ) );
	}

	public static byte[] readByteBufferToBytes( ByteBuffer buffer, int numberOfBytes )
	{
		if ( buffer.remaining() < numberOfBytes )
			throw new IllegalArgumentException( "Would run out of bytes." );
		byte[] bytes = new byte[numberOfBytes];
		buffer.get( bytes );
		return bytes;
	}

	public static byte[] readByteBufferToBytes( ByteBuffer buffer )
	{
		byte[] bytes = new byte[buffer.remaining()];
		buffer.get( bytes );
		return bytes;
	}

	public static String readByteBufferToString( ByteBuffer buffer, int numberOfBytes )
	{
		return Strs.bytesToStringUTF( readByteBufferToBytes( buffer, numberOfBytes ) );
	}

	public static String readByteBufferToString( ByteBuffer buffer )
	{
		return Strs.bytesToStringUTF( readByteBufferToBytes( buffer ) );
	}

	/**
	 * TODO This was lagging the server! WHY???
	 * Maybe we should change our metrics system
	 */
	public static boolean sendTracking( String category, String action, String label )
	{
		try
		{
			String url = "http://www.google-analytics.com/collect";

			URL urlObj = new URL( url );
			HttpURLConnection con = ( HttpURLConnection ) urlObj.openConnection();
			con.setRequestMethod( "POST" );

			String urlParameters = "v=1&tid=UA-60405654-1&cid=" + Foundation.getApplication().getId() + "&t=event&ec=" + category + "&ea=" + action + "&el=" + label;

			con.setDoOutput( true );
			DataOutputStream wr = new DataOutputStream( con.getOutputStream() );
			wr.writeBytes( urlParameters );
			wr.flush();
			wr.close();

			int responseCode = con.getResponseCode();
			LogBuilder.get().fine( "Analytics Response [" + category + "]: " + responseCode );

			BufferedReader in = new BufferedReader( new InputStreamReader( con.getInputStream() ) );
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ( ( inputLine = in.readLine() ) != null )
				response.append( inputLine );
			in.close();

			return true;
		}
		catch ( IOException e )
		{
			return false;
		}
	}

	private NIO()
	{

	}
}
