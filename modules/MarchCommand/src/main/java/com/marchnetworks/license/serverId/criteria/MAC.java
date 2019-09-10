package com.marchnetworks.license.serverId.criteria;

import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.license.Crypto;
import com.marchnetworks.license.LicenseUtils;
import com.marchnetworks.license.exception.ServerIdGenerateException;
import com.marchnetworks.license.serverId.Criterion;

import java.io.IOException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

public class MAC extends Criterion
{
	public static final String NAME = "MAC";
	List<byte[]> m_Data;

	public MAC()
	{
		super( "MAC" );
		m_Data = null;
	}

	public void generate() throws ServerIdGenerateException
	{
		List<byte[]> macs = new ArrayList();
		try
		{
			Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();

			while ( nics.hasMoreElements() )
			{
				NetworkInterface nic = ( NetworkInterface ) nics.nextElement();

				if ( !nic.isLoopback() )
				{

					byte[] hw = nic.getHardwareAddress();
					if ( isMACReal( hw ) )
						try
						{
							macs.add( Crypto.cmdHash( hw ) );

						}
						catch ( NoSuchAlgorithmException e )
						{

							throw new ServerIdGenerateException( "Crypto error" );
						}
				}
			}
		}
		catch ( SocketException e )
		{
			throw new ServerIdGenerateException();
		}

		if ( macs.isEmpty() )
		{
			throw new ServerIdGenerateException( "No mac addresses found" );
		}

		m_Data = macs;
	}

	protected String getValue()
	{
		StringBuilder sb = new StringBuilder( 256 );
		for ( byte[] mac : m_Data )
		{
			sb.append( "<mac>" );
			sb.append( CommonAppUtils.byteToBase64( mac ) );
			sb.append( "</mac>" );
		}
		return sb.toString();
	}

	public boolean fromValue( String value )
	{
		List<byte[]> macs = new ArrayList();

		int start = 0;

		LicenseUtils.XmlTag xt = LicenseUtils.findNextTag( value, "mac", start );
		while ( xt.a != -1 )
		{
			String s = value.substring( xt.b, xt.c );
			try
			{
				macs.add( Crypto.stringBase64ToByte( s ) );
			}
			catch ( IOException e )
			{
				return false;
			}

			start = xt.d;
			xt = LicenseUtils.findNextTag( value, "mac", start );
		}

		m_Data = macs;
		return true;
	}

	public boolean isLoaded()
	{
		return m_Data != null;
	}

	public boolean isEqual( Criterion c )
	{
		MAC B;
		Iterator i$;
		if ( ( c instanceof MAC ) )
		{
			B = ( MAC ) c;
			for ( i$ = m_Data.iterator(); i$.hasNext(); )
			{
				byte[] macA = ( byte[] ) i$.next();
				for ( byte[] macB : m_Data )
					if ( Crypto.isByteArrayEqual( macA, macB ) )
						return true;
			}
		}
		byte[] macA;
		return false;
	}

	protected boolean isMACReal( byte[] mac )
	{
		if ( mac == null )
			return false;
		if ( mac.length != 6 )
		{
			return false;
		}
		if ( ( mac[0] == 0 ) && ( mac[1] == 0 ) && ( mac[2] == 0 ) && ( mac[3] == 0 ) && ( mac[4] == 0 ) )
		{
			return false;
		}
		return true;
	}
}
