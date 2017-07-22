/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.utils;

import com.chiorichan.utils.UtilHttp;
import io.netty.channel.Channel;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class NIO
{
	private NIO()
	{

	}

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

	public static boolean isAddressAssigned( String address )
	{
		return getInterfaceAssigned( address ) != null;
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

	public static boolean isValidIPv4( String ip )
	{
		if ( ip == null )
			return false;

		return ip.matches( UtilHttp.REGEX_IPV4 );
	}

	public static boolean isValidIPv6( String ip )
	{
		if ( ip == null )
			return false;

		return ip.matches( UtilHttp.REGEX_IPV6 );
	}
}
