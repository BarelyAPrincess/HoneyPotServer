package com.marchnetworks.security.ldap;

import com.marchnetworks.command.common.HttpUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class SSLTrustAllSocketFactory extends SSLSocketFactory
{
	SSLContext sslContext;
	private int socketReadTimeout;

	public SSLTrustAllSocketFactory() throws NoSuchAlgorithmException, KeyManagementException
	{
		sslContext = SSLContext.getInstance( "SSL" );
		sslContext.init( null, HttpUtils.TRUST_ALL_MANAGER, null );
	}

	public SSLTrustAllSocketFactory( int socketReadTimeout ) throws NoSuchAlgorithmException, KeyManagementException
	{
		sslContext = SSLContext.getInstance( "SSL" );
		sslContext.init( null, HttpUtils.TRUST_ALL_MANAGER, null );
		this.socketReadTimeout = socketReadTimeout;
	}

	public Socket createSocket() throws IOException
	{
		Socket sslSocket = sslContext.getSocketFactory().createSocket();
		setSocketSettings( ( SSLSocket ) sslSocket );
		return sslSocket;
	}

	public Socket createSocket( Socket s, String host, int port, boolean autoClose ) throws IOException
	{
		Socket sslSocket = sslContext.getSocketFactory().createSocket( s, host, port, autoClose );
		setSocketSettings( ( SSLSocket ) sslSocket );
		return sslSocket;
	}

	public String[] getDefaultCipherSuites()
	{
		return sslContext.getSocketFactory().getDefaultCipherSuites();
	}

	public String[] getSupportedCipherSuites()
	{
		return sslContext.getSocketFactory().getSupportedCipherSuites();
	}

	public Socket createSocket( String host, int port ) throws IOException, UnknownHostException
	{
		Socket sslSocket = sslContext.getSocketFactory().createSocket( host, port );
		setSocketSettings( ( SSLSocket ) sslSocket );
		return sslSocket;
	}

	public Socket createSocket( InetAddress host, int port ) throws IOException
	{
		Socket sslSocket = sslContext.getSocketFactory().createSocket( host, port );
		setSocketSettings( ( SSLSocket ) sslSocket );
		return sslSocket;
	}

	public Socket createSocket( String host, int port, InetAddress localHost, int localPort ) throws IOException, UnknownHostException
	{
		Socket sslSocket = sslContext.getSocketFactory().createSocket( host, port, localHost, localPort );
		setSocketSettings( ( SSLSocket ) sslSocket );
		return sslSocket;
	}

	public Socket createSocket( InetAddress address, int port, InetAddress localAddress, int localPort ) throws IOException
	{
		Socket sslSocket = sslContext.getSocketFactory().createSocket( address, port, localAddress, localPort );
		setSocketSettings( ( SSLSocket ) sslSocket );
		return sslSocket;
	}

	public static SocketFactory getDefault()
	{
		try
		{
			return new SSLTrustAllSocketFactory();
		}
		catch ( KeyManagementException e )
		{
			e.printStackTrace();
		}
		catch ( NoSuchAlgorithmException e )
		{
			e.printStackTrace();
		}
		return null;
	}

	private void setSocketSettings( SSLSocket socket ) throws IOException
	{
		socket.setEnabledProtocols( HttpUtils.getEnabledTLSVersions() );
		socket.setTcpNoDelay( true );
		if ( socketReadTimeout > 0 )
		{
			socket.setSoTimeout( socketReadTimeout );
		}
	}
}

