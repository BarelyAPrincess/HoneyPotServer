package com.marchnetworks.server.communications.http;

import com.marchnetworks.common.spring.ApplicationContextSupport;
import com.marchnetworks.server.network.settings.NetworkBandwidthService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

class BandwidthCappedOutputStream extends FilterOutputStream
{
	private static final Logger LOG = LoggerFactory.getLogger( BandwidthCappedOutputStream.class );
	private String host;

	public BandwidthCappedOutputStream( OutputStream outputStream, String host )
	{
		super( outputStream );
		this.host = host;
		getNetworkSettingsService().register( host );
	}

	public void write( int arg0 ) throws IOException
	{
		LOG.debug( "Intercepting traffic for 1 byte transfer" );
		getNetworkSettingsService().getPermit( host, 1 );
		out.write( arg0 );
	}

	public void write( byte[] b ) throws IOException
	{
		LOG.debug( "Intercepting traffic for {}  byte transfer", Integer.valueOf( b.length ) );
		getNetworkSettingsService().getPermit( host, b.length );
		out.write( b );
	}

	public void write( byte[] b, int off, int len ) throws IOException
	{
		LOG.debug( "Intercepting traffic for {}  byte transfer", Integer.valueOf( b.length ) );
		getNetworkSettingsService().getPermit( host, b.length );
		out.write( b, off, len );
	}

	public void close() throws IOException
	{
		try
		{
			flush();
		}
		catch ( IOException localIOException )
		{
		}
		try
		{
			out.close();
		}
		finally
		{
			getNetworkSettingsService().unregister( host );
		}
	}

	private static NetworkBandwidthService getNetworkSettingsService()
	{
		return ( NetworkBandwidthService ) ApplicationContextSupport.getBean( "networkBandwidthService" );
	}
}

