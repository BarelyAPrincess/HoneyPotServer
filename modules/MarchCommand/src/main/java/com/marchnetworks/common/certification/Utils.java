package com.marchnetworks.common.certification;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.util.encoders.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;

public class Utils
{
	public static final String BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----";
	public static final String END_CERTIFICATE = "-----END CERTIFICATE-----";

	public static boolean isExpired( X509Certificate cert )
	{
		Date current = new Date( System.currentTimeMillis() );

		if ( ( cert.getNotAfter().after( current ) ) && ( cert.getNotBefore().before( current ) ) )
			return false;
		return true;
	}

	public static String[] convertCertsToPEMStringArray( List<Certificate> list ) throws Exception
	{
		String[] array = new String[3];
		for ( int i = 0; i < list.size(); i++ )
		{
			Certificate cert = ( Certificate ) list.get( i );
			String certString = cert2PEM( cert );
			array[i] = certString;
		}
		return array;
	}

	private static String cert2PEM( Certificate cert ) throws CertificateException
	{
		StringBuffer sb = new StringBuffer();

		byte[] buf = cert.getEncoded();

		sb.append( "-----BEGIN CERTIFICATE-----" );
		sb.append( writeToString( buf ) );
		sb.append( "-----END CERTIFICATE-----" );

		return sb.toString();
	}

	private static String writeToString( byte[] out ) throws CertificateException
	{
		int lineLength = 64;
		ByteArrayOutputStream oStream = new ByteArrayOutputStream();
		byte[] outBytes = Base64.encode( out );

		int off = 0;
		while ( off < outBytes.length )
		{
			oStream.write( 10 );
			if ( off + lineLength < outBytes.length )
			{
				oStream.write( outBytes, off, lineLength );
			}
			else
				oStream.write( outBytes, off, outBytes.length - off );
			off += lineLength;
		}
		return oStream.toString();
	}

	public static byte[] convertDERObjectToByteArray( DERObject in ) throws IOException
	{
		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		DEROutputStream dOut = new DEROutputStream( bOut );
		Throwable localThrowable2 = null;
		try
		{
			dOut.writeObject( in );
		}
		catch ( Throwable localThrowable1 )
		{
			localThrowable2 = localThrowable1;
			throw localThrowable1;
		}
		finally
		{
			if ( dOut != null )
				if ( localThrowable2 != null )
					try
					{
						dOut.close();
					}
					catch ( Throwable x2 )
					{
						localThrowable2.addSuppressed( x2 );
					}
				else
					dOut.close();
		}
		return bOut.toByteArray();
	}

	public static DERObject convertByteArrayToDERObject( byte[] bArray ) throws IOException
	{
		ASN1InputStream a = new ASN1InputStream( bArray );
		Throwable localThrowable2 = null;
		DERObject result;

		try
		{
			result = a.readObject();
		}
		catch ( Throwable localThrowable1 )
		{
			localThrowable2 = localThrowable1;
			throw localThrowable1;
		}
		finally
		{
			if ( a != null )
				if ( localThrowable2 != null )
					try
					{
						a.close();
					}
					catch ( Throwable x2 )
					{
						localThrowable2.addSuppressed( x2 );
					}
				else
					a.close();
		}

		return result;
	}

	public static KeyPair generateRSAKeyPair( int keyLength ) throws Exception
	{
		KeyPairGenerator kpGen = KeyPairGenerator.getInstance( "RSA", "BC" );
		kpGen.initialize( keyLength, new SecureRandom() );
		return kpGen.generateKeyPair();
	}

	public static String ipOctets2String( byte[] ipOctets )
	{
		if ( ipOctets == null )
			return null;
		if ( ipOctets.length != 4 )
		{
			return null;
		}

		StringBuilder sb = new StringBuilder();

		int iOctetByte = 0xFF & ipOctets[0];
		short sOctetByte = ( short ) iOctetByte;
		sb.append( sOctetByte );
		sb.append( "." );
		iOctetByte = 0xFF & ipOctets[1];
		sOctetByte = ( short ) iOctetByte;
		sb.append( sOctetByte );
		sb.append( "." );
		iOctetByte = 0xFF & ipOctets[2];
		sOctetByte = ( short ) iOctetByte;
		sb.append( sOctetByte );
		sb.append( "." );
		iOctetByte = 0xFF & ipOctets[3];
		sOctetByte = ( short ) iOctetByte;
		sb.append( sOctetByte );

		return sb.toString();
	}

	public static byte[] ipString2Octets( String ipString )
	{
		String[] ss = ipString.split( "[.:]" );
		try
		{
			if ( ss.length == 4 )
			{
				byte[] Result = new byte[4];
				for ( int i = 0; i < 4; i++ )
				{
					int j = Integer.parseInt( ss[i] );
					if ( ( j < 0 ) || ( j > 255 ) )
						return null;
					Result[i] = ( ( byte ) j );
				}
				return Result;
			}
		}
		catch ( Exception e )
		{
			return null;
		}
		return null;
	}
}
