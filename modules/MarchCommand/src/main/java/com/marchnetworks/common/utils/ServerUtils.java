package com.marchnetworks.common.utils;

import com.marchnetworks.command.common.HttpUtils;
import com.marchnetworks.common.config.ConfigProperty;
import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.common.system.ServerParameterStoreServiceIF;
import com.marchnetworks.shared.config.CommonConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class ServerUtils
{
	private static final Logger LOG = LoggerFactory.getLogger( ServerUtils.class );

	private static final String CES_PRODUCT_NAME = "Command Enterprise Server";

	public static String ADDRESS_CACHED;

	public static String HOSTNAME_CACHED;

	private static String serverVersion;

	static
	{
		getServerAddresses();
		getHostName();
	}

	public static List<String> getServerAddresses()
	{
		List<String> serverAddresses = new ArrayList();
		try
		{
			Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
			while ( nics.hasMoreElements() )
			{
				NetworkInterface aNic = ( NetworkInterface ) nics.nextElement();
				Enumeration<InetAddress> tempAddresses = aNic.getInetAddresses();
				while ( tempAddresses.hasMoreElements() )
				{
					InetAddress tempAddress = ( InetAddress ) tempAddresses.nextElement();

					if ( !tempAddress.isLoopbackAddress() )
					{

						String ipaddress = tempAddress.getHostAddress();
						if ( ipaddress.split( ":", 10 ).length <= 2 )
						{

							serverAddresses.add( tempAddress.getHostAddress() );
						}
					}
				}
			}
		}
		catch ( SocketException e )
		{
			LOG.error( "Failed to get server addresses, Exception:", e );
		}

		if ( serverAddresses.size() > 0 )
		{
			ADDRESS_CACHED = ( String ) serverAddresses.get( 0 );
		}
		return serverAddresses;
	}

	public static List<String> getServerAddressList()
	{
		CommonConfiguration commonConfig = ( CommonConfiguration ) ApplicationContextSupport.getBean( "commonConfiguration" );
		List<String> serverAddresses = commonConfig.getPropertyList( ConfigProperty.SERVER_ADDRESS_LIST );
		int httpPort = commonConfig.getServerPort();
		for ( int i = 0; i < serverAddresses.size(); i++ )
		{
			String address = ( String ) serverAddresses.get( i );
			if ( address.indexOf( ":" ) < 0 )
			{
				serverAddresses.set( i, address + ":" + httpPort );
			}
		}
		return serverAddresses;
	}

	public static List<String> getServerHostnames()
	{
		List<String> names = new ArrayList( 2 );
		names.add( getHostName() );
		try
		{
			String fqdn = InetAddress.getLocalHost().getCanonicalHostName();
			if ( ( !HttpUtils.isIPv4Address( fqdn ) ) && ( !names.contains( fqdn ) ) )
			{
				names.add( fqdn );
			}
		}
		catch ( UnknownHostException e )
		{
			LOG.warn( "Failed to lookup FQDN from DNS, error details:", e );
		}
		return names;
	}

	public static String getHostName()
	{
		String hostName = "";
		try
		{
			hostName = InetAddress.getLocalHost().getHostName();
		}
		catch ( UnknownHostException e )
		{
			LOG.error( "Failed to get server host name, Exception:", e );
		}
		HOSTNAME_CACHED = hostName;
		return hostName;
	}

	public static boolean isServerAddress( String address )
	{
		if ( address != null )
		{
			CommonConfiguration commonConfig = ( CommonConfiguration ) ApplicationContextSupport.getBean( "commonConfiguration" );

			List<String> serverAddresses = commonConfig.getPropertyList( ConfigProperty.CERT_ALL_IPS );
			if ( serverAddresses.contains( address ) )
			{
				return true;
			}

			List<String> serverHostnames = commonConfig.getPropertyList( ConfigProperty.CERT_ALL_HOSTNAMES );
			if ( serverHostnames.contains( address ) )
			{
				return true;
			}
		}
		return false;
	}

	public static String getServerInfo()
	{
		CommonConfiguration commonConfig = ( CommonConfiguration ) ApplicationContextSupport.getBean( "commonConfiguration" );

		StringBuffer sb = new StringBuffer();
		sb.append( "system.interface.httpPort: " );
		sb.append( commonConfig.getProperty( ConfigProperty.HTTP_PORT, "80" ) );
		sb.append( System.getProperty( "line.separator" ) );
		sb.append( "system.interface.httpsPort: " );
		sb.append( commonConfig.getProperty( ConfigProperty.HTTPS_PORT, "443" ) );
		sb.append( System.getProperty( "line.separator" ) );
		sb.append( "system.productname: " );
		sb.append( "Command Enterprise Server" );
		sb.append( System.getProperty( "line.separator" ) );
		sb.append( "system.interface.version: " );
		String packageVersion = commonConfig.getProperty( ConfigProperty.INTERFACE_VERSION );
		sb.append( packageVersion );
		sb.append( System.getProperty( "line.separator" ) );
		sb.append( "system.details.version: " );
		String serverVersion = commonConfig.getProperty( ConfigProperty.SERVER_VERSION );
		sb.append( serverVersion );
		sb.append( System.getProperty( "line.separator" ) );
		sb.append( "system.details.client.version: " );
		String clientVersion = commonConfig.getProperty( ConfigProperty.CLIENT_VERSION );
		sb.append( clientVersion );

		return sb.toString();
	}

	public static synchronized String getServerVersion()
	{
		if ( serverVersion == null )
		{
			ServerParameterStoreServiceIF serverParameterService = ( ServerParameterStoreServiceIF ) ApplicationContextSupport.getBean( "serverParameterStoreProxy" );
			serverVersion = serverParameterService.getParameterValue( "ServerManifest.Build-Number" );
			serverVersion = serverVersion.replaceAll( "\\$\\{env\\.SVN_REVISION\\}", "0" );
		}
		return serverVersion;
	}

	public static String getServerMajorMinorVersion()
	{
		String serverVersion = getServerVersion();
		return serverVersion.substring( 0, serverVersion.lastIndexOf( "." ) );
	}

	public static String getServerMajorVersion()
	{
		String serverVersion = getServerMajorMinorVersion();
		return serverVersion.substring( 0, serverVersion.lastIndexOf( "." ) );
	}

	public static String getInterfaceVersion()
	{
		CommonConfiguration commonConfig = ( CommonConfiguration ) ApplicationContextSupport.getBean( "commonConfiguration" );
		return commonConfig.getProperty( ConfigProperty.INTERFACE_VERSION );
	}
}

