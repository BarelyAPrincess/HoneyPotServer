package com.marchnetworks.server.communications.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;

import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

class BandwidthCappedSSLSocketFactory extends SSLSocketFactory
{
	public static BandwidthCappedSSLSocketFactory FACTORY_INSTANCE = new BandwidthCappedSSLSocketFactory();
	private SSLSocketFactory socketFactory;

	public BandwidthCappedSSLSocketFactory( SSLSocketFactory socketFactory )
	{
		this.socketFactory = socketFactory;
	}

	private BandwidthCappedSSLSocketFactory()
	{
		socketFactory = ( ( SSLSocketFactory ) SSLSocketFactory.getDefault() );
	}

	public Socket createSocket() throws IOException
	{
		SSLSocket socket = ( SSLSocket ) socketFactory.createSocket();
		return new BandwidthCappedSocket( socket );
	}

	public Socket createSocket( Socket s, String host, int port, boolean autoClose ) throws IOException
	{
		SSLSocket socket = ( SSLSocket ) socketFactory.createSocket( s, host, port, autoClose );
		return new BandwidthCappedSocket( socket );
	}

	public Socket createSocket( String host, int port ) throws IOException, UnknownHostException
	{
		SSLSocket socket = ( SSLSocket ) socketFactory.createSocket( host, port );
		return new BandwidthCappedSocket( socket );
	}

	public Socket createSocket( InetAddress host, int port ) throws IOException
	{
		SSLSocket socket = ( SSLSocket ) socketFactory.createSocket( host, port );
		return new BandwidthCappedSocket( socket );
	}

	public Socket createSocket( String host, int port, InetAddress localHost, int localPort ) throws IOException, UnknownHostException
	{
		SSLSocket socket = ( SSLSocket ) socketFactory.createSocket( host, port, localHost, localPort );
		return new BandwidthCappedSocket( socket );
	}

	public Socket createSocket( InetAddress address, int port, InetAddress localAddress, int localPort ) throws IOException
	{
		SSLSocket socket = ( SSLSocket ) socketFactory.createSocket( address, port, localAddress, localPort );
		return new BandwidthCappedSocket( socket );
	}

	public String[] getDefaultCipherSuites()
	{
		return socketFactory.getDefaultCipherSuites();
	}

	public String[] getSupportedCipherSuites()
	{
		return socketFactory.getSupportedCipherSuites();
	}

	private class BandwidthCappedSocket extends SSLSocket
	{
		private OutputStream managedStream;

		private SSLSocket wrappedSocket;

		public BandwidthCappedSocket( SSLSocket socket )
		{
			wrappedSocket = socket;
		}

		public OutputStream getOutputStream() throws IOException
		{
			if ( managedStream == null )
			{
				managedStream = new BandwidthCappedOutputStream( wrappedSocket.getOutputStream(), wrappedSocket.getInetAddress().getHostAddress() + ":" + wrappedSocket.getPort() );
			}
			return managedStream;
		}

		public void bind( SocketAddress arg0 ) throws IOException
		{
			wrappedSocket.bind( arg0 );
		}

		public synchronized void close() throws IOException
		{
			wrappedSocket.close();
		}

		public void connect( SocketAddress arg0, int arg1 ) throws IOException
		{
			wrappedSocket.connect( arg0, arg1 );
		}

		public void connect( SocketAddress arg0 ) throws IOException
		{
			wrappedSocket.connect( arg0 );
		}

		public SocketChannel getChannel()
		{
			return wrappedSocket.getChannel();
		}

		public InetAddress getInetAddress()
		{
			return wrappedSocket.getInetAddress();
		}

		public InputStream getInputStream() throws IOException
		{
			return wrappedSocket.getInputStream();
		}

		public boolean getKeepAlive() throws SocketException
		{
			return wrappedSocket.getKeepAlive();
		}

		public InetAddress getLocalAddress()
		{
			return wrappedSocket.getLocalAddress();
		}

		public int getLocalPort()
		{
			return wrappedSocket.getLocalPort();
		}

		public SocketAddress getLocalSocketAddress()
		{
			return wrappedSocket.getLocalSocketAddress();
		}

		public boolean getOOBInline() throws SocketException
		{
			return wrappedSocket.getOOBInline();
		}

		public int getPort()
		{
			return wrappedSocket.getPort();
		}

		public synchronized int getReceiveBufferSize() throws SocketException
		{
			return wrappedSocket.getReceiveBufferSize();
		}

		public SocketAddress getRemoteSocketAddress()
		{
			return wrappedSocket.getRemoteSocketAddress();
		}

		public boolean getReuseAddress() throws SocketException
		{
			return wrappedSocket.getReuseAddress();
		}

		public synchronized int getSendBufferSize() throws SocketException
		{
			return wrappedSocket.getSendBufferSize();
		}

		public int getSoLinger() throws SocketException
		{
			return wrappedSocket.getSoLinger();
		}

		public synchronized int getSoTimeout() throws SocketException
		{
			return wrappedSocket.getSoTimeout();
		}

		public boolean getTcpNoDelay() throws SocketException
		{
			return wrappedSocket.getTcpNoDelay();
		}

		public int getTrafficClass() throws SocketException
		{
			return wrappedSocket.getTrafficClass();
		}

		public boolean isBound()
		{
			return wrappedSocket.isBound();
		}

		public boolean isClosed()
		{
			return wrappedSocket.isClosed();
		}

		public boolean isConnected()
		{
			return wrappedSocket.isConnected();
		}

		public boolean isInputShutdown()
		{
			return wrappedSocket.isInputShutdown();
		}

		public boolean isOutputShutdown()
		{
			return wrappedSocket.isOutputShutdown();
		}

		public void sendUrgentData( int arg0 ) throws IOException
		{
			wrappedSocket.sendUrgentData( arg0 );
		}

		public void setKeepAlive( boolean arg0 ) throws SocketException
		{
			wrappedSocket.setKeepAlive( arg0 );
		}

		public void setOOBInline( boolean arg0 ) throws SocketException
		{
			wrappedSocket.setOOBInline( arg0 );
		}

		public void setPerformancePreferences( int arg0, int arg1, int arg2 )
		{
			wrappedSocket.setPerformancePreferences( arg0, arg1, arg2 );
		}

		public synchronized void setReceiveBufferSize( int arg0 ) throws SocketException
		{
			wrappedSocket.setReceiveBufferSize( arg0 );
		}

		public void setReuseAddress( boolean arg0 ) throws SocketException
		{
			wrappedSocket.setReuseAddress( arg0 );
		}

		public synchronized void setSendBufferSize( int arg0 ) throws SocketException
		{
			wrappedSocket.setSendBufferSize( arg0 );
		}

		public void setSoLinger( boolean arg0, int arg1 ) throws SocketException
		{
			wrappedSocket.setSoLinger( arg0, arg1 );
		}

		public synchronized void setSoTimeout( int arg0 ) throws SocketException
		{
			wrappedSocket.setSoTimeout( arg0 );
		}

		public void setTcpNoDelay( boolean arg0 ) throws SocketException
		{
			wrappedSocket.setTcpNoDelay( arg0 );
		}

		public void setTrafficClass( int arg0 ) throws SocketException
		{
			wrappedSocket.setTrafficClass( arg0 );
		}

		public void shutdownInput() throws IOException
		{
			wrappedSocket.shutdownInput();
		}

		public void shutdownOutput() throws IOException
		{
			wrappedSocket.shutdownOutput();
		}

		public String toString()
		{
			return wrappedSocket.toString();
		}

		public String[] getSupportedCipherSuites()
		{
			return wrappedSocket.getSupportedCipherSuites();
		}

		public String[] getEnabledCipherSuites()
		{
			return wrappedSocket.getEnabledCipherSuites();
		}

		public void setEnabledCipherSuites( String[] paramArrayOfString )
		{
			wrappedSocket.setEnabledCipherSuites( paramArrayOfString );
		}

		public String[] getSupportedProtocols()
		{
			return wrappedSocket.getSupportedProtocols();
		}

		public String[] getEnabledProtocols()
		{
			return wrappedSocket.getEnabledProtocols();
		}

		public void setEnabledProtocols( String[] paramArrayOfString )
		{
			wrappedSocket.setEnabledProtocols( paramArrayOfString );
		}

		public SSLSession getSession()
		{
			return wrappedSocket.getSession();
		}

		public SSLParameters getSSLParameters()
		{
			return wrappedSocket.getSSLParameters();
		}

		public void addHandshakeCompletedListener( HandshakeCompletedListener paramHandshakeCompletedListener )
		{
			wrappedSocket.addHandshakeCompletedListener( paramHandshakeCompletedListener );
		}

		public void removeHandshakeCompletedListener( HandshakeCompletedListener paramHandshakeCompletedListener )
		{
			wrappedSocket.removeHandshakeCompletedListener( paramHandshakeCompletedListener );
		}

		public void startHandshake() throws IOException
		{
			wrappedSocket.startHandshake();
		}

		public void setUseClientMode( boolean paramBoolean )
		{
			wrappedSocket.setUseClientMode( paramBoolean );
		}

		public boolean getUseClientMode()
		{
			return wrappedSocket.getUseClientMode();
		}

		public void setNeedClientAuth( boolean paramBoolean )
		{
			wrappedSocket.setNeedClientAuth( paramBoolean );
		}

		public boolean getNeedClientAuth()
		{
			return wrappedSocket.getNeedClientAuth();
		}

		public void setWantClientAuth( boolean paramBoolean )
		{
			wrappedSocket.setWantClientAuth( paramBoolean );
		}

		public boolean getWantClientAuth()
		{
			return wrappedSocket.getWantClientAuth();
		}

		public void setEnableSessionCreation( boolean paramBoolean )
		{
			wrappedSocket.setEnableSessionCreation( paramBoolean );
		}

		public boolean getEnableSessionCreation()
		{
			return wrappedSocket.getEnableSessionCreation();
		}
	}
}

