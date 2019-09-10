package com.marchnetworks.common.diagnostics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

public class DiagnosticSettings
{
	private static final Logger LOG = LoggerFactory.getLogger( DiagnosticSettings.class );

	private static Long blockedDeviceId;

	private static Integer restPort;

	private static Integer soapPort;

	private static String transport;

	private static Integer maxRestConnectionsPerHost;

	public static void onDeviceSubscribe( Long deviceId )
	{
		if ( ( blockedDeviceId != null ) && ( blockedDeviceId.equals( deviceId ) ) )
		{
			LOG.info( "Blocking device {} from subscribing", blockedDeviceId );

			while ( blockedDeviceId != null )
			{
				try
				{
					Thread.sleep( 1000L );
				}
				catch ( InterruptedException e )
				{
					LOG.error( "Interrupted while blocking device " + e.getMessage() );
				}
			}
		}
	}

	public static String onGetSoapAddress( String deviceAddress )
	{
		if ( soapPort != null )
		{
			URI modified = UriBuilder.fromUri( deviceAddress ).port( soapPort.intValue() ).build( new Object[0] );
			return modified.toString();
		}
		return deviceAddress;
	}

	public static String onGetAddress( String deviceAddress )
	{
		if ( restPort != null )
		{
			String[] addressParts = deviceAddress.split( ":" );
			String baseAddress = addressParts[0];
			return baseAddress + ":" + restPort;
		}
		return deviceAddress;
	}

	public static String onGetTransport( String current )
	{
		if ( transport != null )
		{
			return transport;
		}
		return current;
	}

	public static int onGetMaxRestConnectionsPerHost( int current )
	{
		if ( maxRestConnectionsPerHost != null )
		{
			return maxRestConnectionsPerHost.intValue();
		}
		return current;
	}

	public static void setRestPort( Integer port )
	{
		restPort = port;
	}

	public static void setSoapPort( Integer port )
	{
		soapPort = port;
	}

	public static void setTransport( String trans )
	{
		transport = trans;
	}

	public static void setMaxRestConnectionsPerHost( Integer connections )
	{
		maxRestConnectionsPerHost = connections;
	}

	public static void blockDeviceSubscribe( Long deviceId )
	{
		blockedDeviceId = deviceId;
	}

	public static void unblockDeviceSubscribe()
	{
		blockedDeviceId = null;
	}
}
