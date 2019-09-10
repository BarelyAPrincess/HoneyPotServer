package com.marchnetworks.license.serverId.criteria;

import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.license.Crypto;
import com.marchnetworks.license.exception.ServerIdGenerateException;
import com.marchnetworks.license.serverId.Criterion;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class UID extends Criterion
{
	public static final String NAME = "UUID";
	protected byte[] m_bHashValue;

	public UID()
	{
		super( "UUID" );
		m_bHashValue = null;
	}

	public void generate() throws ServerIdGenerateException
	{
		int NUM_UUIDS = 16;
		int BYTES_PER_UUID = 16;
		byte[] bRaw = new byte['Ā'];

		for ( int i = 0; i < 16; i++ )
		{
			UUID uid = UUID.randomUUID();
			long l1 = uid.getMostSignificantBits();
			long l2 = uid.getLeastSignificantBits();

			bRaw[( i * 16 )] = ( ( byte ) ( int ) ( l1 >>> 56 ) );
			bRaw[( i * 16 + 1 )] = ( ( byte ) ( int ) ( l1 >>> 48 ) );
			bRaw[( i * 16 + 2 )] = ( ( byte ) ( int ) ( l1 >>> 40 ) );
			bRaw[( i * 16 + 3 )] = ( ( byte ) ( int ) ( l1 >>> 32 ) );
			bRaw[( i * 16 + 4 )] = ( ( byte ) ( int ) ( l1 >>> 24 ) );
			bRaw[( i * 16 + 5 )] = ( ( byte ) ( int ) ( l1 >>> 16 ) );
			bRaw[( i * 16 + 6 )] = ( ( byte ) ( int ) ( l1 >>> 8 ) );
			bRaw[( i * 16 + 7 )] = ( ( byte ) ( int ) ( l1 >>> 0 ) );
			bRaw[( i * 16 + 8 )] = ( ( byte ) ( int ) ( l2 >>> 56 ) );
			bRaw[( i * 16 + 9 )] = ( ( byte ) ( int ) ( l2 >>> 48 ) );
			bRaw[( i * 16 + 10 )] = ( ( byte ) ( int ) ( l2 >>> 40 ) );
			bRaw[( i * 16 + 11 )] = ( ( byte ) ( int ) ( l2 >>> 32 ) );
			bRaw[( i * 16 + 12 )] = ( ( byte ) ( int ) ( l2 >>> 24 ) );
			bRaw[( i * 16 + 13 )] = ( ( byte ) ( int ) ( l2 >>> 16 ) );
			bRaw[( i * 16 + 14 )] = ( ( byte ) ( int ) ( l2 >>> 8 ) );
			bRaw[( i * 16 + 15 )] = ( ( byte ) ( int ) ( l2 >>> 0 ) );
		}
		try
		{
			m_bHashValue = Crypto.cmdHash( bRaw );
		}
		catch ( NoSuchAlgorithmException e )
		{
			throw new ServerIdGenerateException( "Error hashing data", e );
		}
	}

	protected String getValue()
	{
		return CommonAppUtils.byteToBase64( m_bHashValue );
	}

	public boolean fromValue( String value )
	{
		try
		{
			m_bHashValue = Crypto.stringBase64ToByte( value );
		}
		catch ( IOException e )
		{
			return false;
		}
		return true;
	}

	public boolean isLoaded()
	{
		return m_bHashValue != null;
	}

	public boolean isEqual( Criterion c )
	{
		if ( ( ( c instanceof UID ) ) && ( Crypto.isByteArrayEqual( m_bHashValue, m_bHashValue ) ) )
			return true;
		return false;
	}
}
