package com.marchnetworks.license;

import com.marchnetworks.command.common.Base64;
import com.marchnetworks.command.common.CommonAppUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class Crypto
{
	protected static final String SYMCRYPT = "AES/CFB/NoPadding";
	protected static byte[] m_bSuperIodized;
	protected PublicKey m_PubKey;

	static
	{
		int sz = 21;
		sz += 46;
		sz *= 4;
		int h = 3;
		h *= 4;
		sz -= h;

		m_bSuperIodized = new byte[sz];

		for ( int i = 0; i < sz; i++ )
			m_bSuperIodized[i] = ( ( byte ) h );
		h = 613;
		int m = 45664881;

		int i = ( m - h - 14902 ) % ( h * sz );
		i = i * h % 78;

		int j = sz * h * i * m;
		j = j - m - h;
		j %= h;

		for ( int a = i; a < j; a++ )
		{
			long l = 261354912 * ( a & 0xFFFF ) + ( a + h >> 12 );
			if ( l < 0L )
			{
				l = -l;
			}
			long ll = a * m + 9L;
			ll /= h;
			ll -= a;
			if ( ll < 0L )
			{
				ll = -ll;
			}
			m = ( int ) ( l % sz );
			m_bSuperIodized[m] = ( ( byte ) ( int ) ( ll % 256L ) );
		}
	}

	public Crypto()
	{
		m_PubKey = loadMNPublicKey();
	}

	protected PublicKey loadMNPublicKey()
	{
		byte[] bRaw = {48, -126, 1, 34, 48, 13, 6, 9, 42, -122, 82, -122, -9, 13, 1, 1, 1, 5, 0, 3, -126, 1, 15, 0, 48, -126, 1, 10, 2, -126, 1, 1, 0, -61, -85, 36, 24, -62, 75, -28, 85, 77, -40, -25, -91, 114, 114, -30, -98, -46, -11, -34, 3, 46, 91, -6, -56, -97, -76, 4, 36, 104, 51, -123, 94, -91, -80, 101, 100, 92, 105, 103, -31, 37, -116, 125, 111, -20, 47, 101, 38, 113, 66, -20, 26, -25, -63, 23, -126, 59, 15, 70, 120, 59, 76, 90, 109, -48, 12, -101, 34, 82, 68, -68, -18, 101, -88, 43, -104, 91, -93, 2, -100, -101, 98, -32, Byte.MIN_VALUE, -99, -51, -60, 78, 17, -55, 87, 69, -125, -84, -59, -1, -72, -64, -45, Byte.MAX_VALUE, 30, 92, -22, -107, -39, 53, -37, 44, -113, 41, 55, -70, -104, -102, -74, -90, 67, 99, -12, -104, -74, -43, -5, -83, 18, -103, 34, -37, 121, 70, 99, 1, -15, 91, 122, -53, -113, -63, 30, 33, -29, -99, 97, 37, -78, -26, 122, -62, 58, -45, 50, 62, -102, -70, 23, 43, 12, -64, -123, -100, 109, 20, 58, 64, -90, 55, -72, -123, 107, 114, 14, 65, -66, -62, 19, 89, 107, -27, 97, 36, -97, -7, 73, -117, 2, -34, -64, 53, -69, 35, -56, 48, 67, -72, -112, 105, -11, 22, 1, 2, -108, 49, 51, -112, 126, 124, 123, -127, -11, 104, 27, -106, 73, -39, 39, -10, 4, -99, -91, 17, -67, -49, 15, 69, -117, -48, -29, 59, -17, -7, 107, 6, 89, 82, -12, -111, -45, -115, -87, 16, -89, -86, 50, -69, -30, 61, 55, -35, -101, 120, -100, 88, -25, -73, -51, 111, 2, 3, 1, 0, 1};

		byte[] bb = new byte[1];
		int a = 0;
		Random r = new Random( m_bSuperIodized[32] );
		for ( ; ; )
		{
			a = r.nextInt( 299 );
			r.nextBytes( bb );

			if ( a > 293 )
			{
				break;
			}
			bRaw[a] = ( ( byte ) ( bRaw[a] + bb[0] ) );
		}

		X509EncodedKeySpec spec = new X509EncodedKeySpec( bRaw );
		KeyFactory kf = null;
		try
		{
			kf = KeyFactory.getInstance( "RSA" );
		}
		catch ( NoSuchAlgorithmException localNoSuchAlgorithmException )
		{
		}
		try
		{
			return kf.generatePublic( spec );
		}
		catch ( InvalidKeySpecException localInvalidKeySpecException )
		{
		}
		return null;
	}

	public Crypto( PublicKey pk )
	{
		m_PubKey = pk;
	}

	public static byte[] cmdHash( String input ) throws NoSuchAlgorithmException, UnsupportedEncodingException
	{
		return cmdHash( input.getBytes( "UTF-8" ) );
	}

	public static byte[] cmdHash( byte[] input ) throws NoSuchAlgorithmException
	{
		MessageDigest digest = MessageDigest.getInstance( "SHA-1" );
		digest.reset();

		byte[] bIodized = new byte[m_bSuperIodized.length];
		System.arraycopy( m_bSuperIodized, 0, bIodized, 0, bIodized.length );

		for ( int i = 0; i < bIodized.length; i++ )
		{
			long l = 1465468 * bIodized[i] + ( i + 77 >> 12 ) + ( i & 0xFFFF );
			if ( l < 0L )
				l = -l;
			l %= 256L;
			bIodized[i] = ( ( byte ) ( int ) l );
		}

		digest.reset();
		digest.update( bIodized );
		byte[] bHashed = digest.digest( input );

		for ( int i = 0; i < 200; i++ )
		{
			digest.reset();
			bHashed = digest.digest( bHashed );
		}

		digest.reset();
		digest.update( bIodized );
		bHashed = digest.digest( bHashed );

		for ( int i = 0; i < 900; i++ )
		{
			digest.reset();
			bHashed = digest.digest( bHashed );
		}

		return bHashed;
	}

	public byte[] cmdDecryptPub( byte[] input ) throws Exception
	{
		Cipher c = getASymCipher();
		c.init( 2, m_PubKey );
		return c.doFinal( input );
	}

	protected static Cipher getASymCipher() throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException
	{
		return Cipher.getInstance( "RSA/ECB/PKCS1Padding", "BC" );
	}

	protected Cipher getSymCipher( boolean encrypt ) throws Exception
	{
		Cipher c = Cipher.getInstance( "AES/CFB/NoPadding" );

		byte[] bIV = new byte[16];

		int a = 9688241;
		int b = 163429;
		for ( int i = 0; i < 16; i++ )
		{
			a = 36969 * ( a & 0xFFFF ) + ( a >> 16 );
			b = 18000 * ( b & 0xFFFF ) + ( b >> 16 );
			bIV[i] = ( ( byte ) ( ( ( a << 16 ) + b ) % 256 ) );
		}

		IvParameterSpec iv = new IvParameterSpec( bIV );
		if ( encrypt )
		{
			c.init( 1, genKey(), iv );
		}
		else
			c.init( 2, genKey(), iv );
		return c;
	}

	public byte[] cmdSymEncrypt( byte[] input ) throws Exception
	{
		Cipher c = getSymCipher( true );
		return c.doFinal( input );
	}

	public byte[] cmdSymDecrypt( byte[] input ) throws Exception
	{
		Cipher c = getSymCipher( false );
		return c.doFinal( input );
	}

	protected SecretKey genKey() throws Exception
	{
		byte[] salt = m_PubKey.getEncoded();
		byte[] bPass = cmdHash( salt );
		String sPass = CommonAppUtils.byteToBase64( bPass );

		int iter = 46;
		int j = 27;
		iter = ( iter << 3 ) + 8;
		iter = iter % j-- * 4 << 4;
		iter = ( iter * j >> 2 ) - 1000;

		SecretKeyFactory factory = SecretKeyFactory.getInstance( "PBKDF2WithHmacSHA1" );
		KeySpec spec = new PBEKeySpec( sPass.toCharArray(), salt, iter, 128 );

		SecretKey tmp = factory.generateSecret( spec );
		return new SecretKeySpec( tmp.getEncoded(), "AES" );
	}

	public static byte[] stringBase64ToByte( String s ) throws IOException
	{
		return Base64.decode( s );
	}

	public static boolean isByteArrayEqual( byte[] A, byte[] B )
	{
		if ( ( A == null ) && ( B == null ) )
			return true;
		if ( ( A == null ) || ( B == null ) )
			return false;
		if ( A.length != B.length )
		{
			return false;
		}
		for ( int i = 0; i < A.length; i++ )
		{
			if ( A[i] != B[i] )
				return false;
		}
		return true;
	}

	public String exportEncryptInt( int input ) throws Exception
	{
		String s = "a;skldjhaiu<" + input + ">baliyzfevqouihdha";
		return CommonAppUtils.byteToBase64( cmdSymEncrypt( s.getBytes( "UTF-8" ) ) );
	}

	public int importDecryptInt( String input ) throws Exception
	{
		String s = new String( cmdSymDecrypt( stringBase64ToByte( input ) ), "UTF-8" );

		int a = s.indexOf( '<' );
		int b = s.indexOf( '>' );
		if ( ( a == -1 ) || ( b == -1 ) || ( a > b ) )
			throw new RuntimeException( "No integer found" );
		s = s.substring( a + 1, b );
		return Integer.parseInt( s );
	}
}
