package com.marchnetworks.command.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.annotation.Nonnull;

public class Base64
{
	public static final int NO_OPTIONS = 0;
	public static final int ENCODE = 1;
	public static final int DECODE = 0;
	public static final int GZIP = 2;
	public static final int DONT_GUNZIP = 4;
	public static final int DO_BREAK_LINES = 8;
	public static final int URL_SAFE = 16;
	public static final int ORDERED = 32;
	private static final int MAX_LINE_LENGTH = 76;
	private static final byte EQUALS_SIGN = 61;
	private static final byte NEW_LINE = 10;
	private static final String PREFERRED_ENCODING = "UTF-8";
	private static final byte WHITE_SPACE_ENC = -5;
	private static final byte EQUALS_SIGN_ENC = -1;
	private static final byte[] _STANDARD_ALPHABET = {65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 43, 47};

	private static final byte[] _STANDARD_DECODABET = {-9, -9, -9, -9, -9, -9, -9, -9, -9, -5, -5, -9, -9, -5, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -5, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, 62, -9, -9, -9, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -9, -9, -9, -1, -9, -9, -9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -9, -9, -9, -9, -9, -9, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9};

	private static final byte[] _URL_SAFE_ALPHABET = {65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 45, 95};

	private static final byte[] _URL_SAFE_DECODABET = {-9, -9, -9, -9, -9, -9, -9, -9, -9, -5, -5, -9, -9, -5, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -5, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, 62, -9, -9, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -9, -9, -9, -1, -9, -9, -9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -9, -9, -9, -9, 63, -9, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9};

	private static final byte[] _ORDERED_ALPHABET = {45, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 95, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122};

	private static final byte[] _ORDERED_DECODABET = {-9, -9, -9, -9, -9, -9, -9, -9, -9, -5, -5, -9, -9, -5, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -5, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, 0, -9, -9, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, -9, -9, -9, -1, -9, -9, -9, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, -9, -9, -9, -9, 37, -9, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9};

	private static final byte[] getAlphabet( int options )
	{
		if ( ( options & 0x10 ) == 16 )
			return _URL_SAFE_ALPHABET;
		if ( ( options & 0x20 ) == 32 )
		{
			return _ORDERED_ALPHABET;
		}
		return _STANDARD_ALPHABET;
	}

	private static final byte[] getDecodabet( int options )
	{
		if ( ( options & 0x10 ) == 16 )
			return _URL_SAFE_DECODABET;
		if ( ( options & 0x20 ) == 32 )
		{
			return _ORDERED_DECODABET;
		}
		return _STANDARD_DECODABET;
	}

	private static byte[] encode3to4( byte[] b4, byte[] threeBytes, int numSigBytes, int options )
	{
		encode3to4( threeBytes, 0, numSigBytes, b4, 0, options );
		return b4;
	}

	private static byte[] encode3to4( byte[] source, int srcOffset, int numSigBytes, byte[] destination, int destOffset, int options )
	{
		byte[] ALPHABET = getAlphabet( options );

		int inBuff = ( numSigBytes > 0 ? source[srcOffset] << 24 >>> 8 : 0 ) | ( numSigBytes > 1 ? source[( srcOffset + 1 )] << 24 >>> 16 : 0 ) | ( numSigBytes > 2 ? source[( srcOffset + 2 )] << 24 >>> 24 : 0 );

		switch ( numSigBytes )
		{
			case 3:
				destination[destOffset] = ALPHABET[( inBuff >>> 18 )];
				destination[( destOffset + 1 )] = ALPHABET[( inBuff >>> 12 & 0x3F )];
				destination[( destOffset + 2 )] = ALPHABET[( inBuff >>> 6 & 0x3F )];
				destination[( destOffset + 3 )] = ALPHABET[( inBuff & 0x3F )];
				return destination;

			case 2:
				destination[destOffset] = ALPHABET[( inBuff >>> 18 )];
				destination[( destOffset + 1 )] = ALPHABET[( inBuff >>> 12 & 0x3F )];
				destination[( destOffset + 2 )] = ALPHABET[( inBuff >>> 6 & 0x3F )];
				destination[( destOffset + 3 )] = 61;
				return destination;

			case 1:
				destination[destOffset] = ALPHABET[( inBuff >>> 18 )];
				destination[( destOffset + 1 )] = ALPHABET[( inBuff >>> 12 & 0x3F )];
				destination[( destOffset + 2 )] = 61;
				destination[( destOffset + 3 )] = 61;
				return destination;
		}

		return destination;
	}

	public static void encode( ByteBuffer raw, ByteBuffer encoded )
	{
		byte[] raw3 = new byte[3];
		byte[] enc4 = new byte[4];

		while ( raw.hasRemaining() )
		{
			int rem = Math.min( 3, raw.remaining() );
			raw.get( raw3, 0, rem );
			encode3to4( enc4, raw3, rem, 0 );
			encoded.put( enc4 );
		}
	}

	public static void encode( ByteBuffer raw, CharBuffer encoded )
	{
		byte[] raw3 = new byte[3];
		byte[] enc4 = new byte[4];

		while ( raw.hasRemaining() )
		{
			int rem = Math.min( 3, raw.remaining() );
			raw.get( raw3, 0, rem );
			encode3to4( enc4, raw3, rem, 0 );
			for ( int i = 0; i < 4; i++ )
			{
				encoded.put( ( char ) ( enc4[i] & 0xFF ) );
			}
		}
	}

	public static String encodeObject( Serializable serializableObject ) throws IOException
	{
		return encodeObject( serializableObject, 0 );
	}

	public static String encodeObject( Serializable serializableObject, int options ) throws IOException
	{
		if ( serializableObject == null )
		{
			throw new NullPointerException( "Cannot serialize a null object." );
		}

		ByteArrayOutputStream baos = null;
		OutputStream b64os = null;
		GZIPOutputStream gzos = null;
		ObjectOutputStream oos = null;

		try
		{
			baos = new ByteArrayOutputStream();
			b64os = new OutputStream( baos, 0x1 | options );
			if ( ( options & 0x2 ) != 0 )
			{
				gzos = new GZIPOutputStream( b64os );
				oos = new ObjectOutputStream( gzos );
			}
			else
			{
				oos = new ObjectOutputStream( b64os );
			}
			oos.writeObject( serializableObject );

			try
			{
				oos.close();
			}
			catch ( Exception localException )
			{
			}
			try
			{
				gzos.close();
			}
			catch ( Exception localException1 )
			{
			}
			try
			{
				b64os.close();
			}
			catch ( Exception localException2 )
			{
			}
			try
			{
				baos.close();
			}
			catch ( Exception localException3 )
			{
			}
			try
			{
				return new String( baos.toByteArray(), "UTF-8" );
			}
			catch ( UnsupportedEncodingException uue )
			{
			}
		}
		catch ( IOException e )
		{
			throw e;
		}
		finally
		{
			try
			{
				oos.close();
			}
			catch ( Exception localException4 )
			{
			}
			try
			{
				gzos.close();
			}
			catch ( Exception localException5 )
			{
			}
			try
			{
				b64os.close();
			}
			catch ( Exception localException6 )
			{
			}
			try
			{
				baos.close();
			}
			catch ( Exception localException7 )
			{
			}
		}

		return new String( baos.toByteArray() );
	}

	public static String encodeBytes( byte[] source )
	{
		try
		{
			return encodeBytes( source, 0, source.length, 0 );
		}
		catch ( IOException ex )
		{
			throw new AssertionError( ex.getMessage() );
		}
	}

	public static String encodeBytes( byte[] source, int options ) throws IOException
	{
		return encodeBytes( source, 0, source.length, options );
	}

	public static String encodeBytes( byte[] source, int off, int len )
	{
		try
		{
			return encodeBytes( source, off, len, 0 );
		}
		catch ( IOException ex )
		{
			throw new AssertionError( ex.getMessage() );
		}
	}

	public static String encodeBytes( byte[] source, int off, int len, int options ) throws IOException
	{
		byte[] encoded = encodeBytesToBytes( source, off, len, options );

		try
		{
			return new String( encoded, "UTF-8" );
		}
		catch ( UnsupportedEncodingException uue )
		{
		}
		return new String( encoded );
	}

	public static byte[] encodeBytesToBytes( byte[] source )
	{
		try
		{
			return encodeBytesToBytes( source, 0, source.length, 0 );
		}
		catch ( IOException ex )
		{
			throw new AssertionError( "IOExceptions only come from GZipping, which is turned off: " + ex.getMessage() );
		}
	}

	public static byte[] encodeBytesToBytes( byte[] source, int off, int len, int options ) throws IOException
	{
		if ( source == null )
		{
			throw new NullPointerException( "Cannot serialize a null array." );
		}

		if ( off < 0 )
		{
			throw new IllegalArgumentException( "Cannot have negative offset: " + off );
		}

		if ( len < 0 )
		{
			throw new IllegalArgumentException( "Cannot have length offset: " + len );
		}

		if ( off + len > source.length )
		{
			throw new IllegalArgumentException( String.format( "Cannot have offset of %d and length of %d with array of length %d", new Object[] {Integer.valueOf( off ), Integer.valueOf( len ), Integer.valueOf( source.length )} ) );
		}

		ByteArrayOutputStream baos;

		if ( ( options & 0x2 ) != 0 )
		{
			baos = null;
			GZIPOutputStream gzos = null;
			OutputStream b64os = null;

			try
			{
				baos = new ByteArrayOutputStream();
				b64os = new OutputStream( baos, 0x1 | options );
				gzos = new GZIPOutputStream( b64os );

				gzos.write( source, off, len );
				gzos.close();

				return baos.toByteArray();
			}
			catch ( IOException e )
			{
				throw e;
			}
			finally
			{
				try
				{
					gzos.close();
				}
				catch ( Exception localException3 )
				{
				}
				try
				{
					b64os.close();
				}
				catch ( Exception localException4 )
				{
				}
				try
				{
					baos.close();
				}
				catch ( Exception localException5 )
				{
				}
			}
		}

		boolean breakLines = ( options & 0x8 ) != 0;

		int encLen = len / 3 * 4 + ( len % 3 > 0 ? 4 : 0 );
		if ( breakLines )
		{
			encLen += encLen / 76;
		}
		byte[] outBuff = new byte[encLen];

		int d = 0;
		int e = 0;
		int len2 = len - 2;
		int lineLength = 0;
		for ( ; d < len2; e += 4 )
		{
			encode3to4( source, d + off, 3, outBuff, e, options );

			lineLength += 4;
			if ( ( breakLines ) && ( lineLength >= 76 ) )
			{
				outBuff[( e + 4 )] = 10;
				e++;
				lineLength = 0;
			}
			d += 3;
		}

		if ( d < len )
		{
			encode3to4( source, d + off, len - d, outBuff, e, options );
			e += 4;
		}

		if ( e <= outBuff.length - 1 )
		{

			byte[] finalOut = new byte[e];
			System.arraycopy( outBuff, 0, finalOut, 0, e );

			return finalOut;
		}

		return outBuff;
	}

	private static int decode4to3( byte[] source, int srcOffset, byte[] destination, int destOffset, int options )
	{
		if ( source == null )
		{
			throw new NullPointerException( "Source array was null." );
		}
		if ( destination == null )
		{
			throw new NullPointerException( "Destination array was null." );
		}
		if ( ( srcOffset < 0 ) || ( srcOffset + 3 >= source.length ) )
		{
			throw new IllegalArgumentException( String.format( "Source array with length %d cannot have offset of %d and still process four bytes.", new Object[] {Integer.valueOf( source.length ), Integer.valueOf( srcOffset )} ) );
		}

		if ( ( destOffset < 0 ) || ( destOffset + 2 >= destination.length ) )
		{
			throw new IllegalArgumentException( String.format( "Destination array with length %d cannot have offset of %d and still store three bytes.", new Object[] {Integer.valueOf( destination.length ), Integer.valueOf( destOffset )} ) );
		}

		byte[] DECODABET = getDecodabet( options );

		if ( source[( srcOffset + 2 )] == 61 )
		{

			int outBuff = ( DECODABET[source[srcOffset]] & 0xFF ) << 18 | ( DECODABET[source[( srcOffset + 1 )]] & 0xFF ) << 12;

			destination[destOffset] = ( ( byte ) ( outBuff >>> 16 ) );
			return 1;
		}

		if ( source[( srcOffset + 3 )] == 61 )
		{

			int outBuff = ( DECODABET[source[srcOffset]] & 0xFF ) << 18 | ( DECODABET[source[( srcOffset + 1 )]] & 0xFF ) << 12 | ( DECODABET[source[( srcOffset + 2 )]] & 0xFF ) << 6;

			destination[destOffset] = ( ( byte ) ( outBuff >>> 16 ) );
			destination[( destOffset + 1 )] = ( ( byte ) ( outBuff >>> 8 ) );
			return 2;
		}

		int outBuff = ( DECODABET[source[srcOffset]] & 0xFF ) << 18 | ( DECODABET[source[( srcOffset + 1 )]] & 0xFF ) << 12 | ( DECODABET[source[( srcOffset + 2 )]] & 0xFF ) << 6 | DECODABET[source[( srcOffset + 3 )]] & 0xFF;

		destination[destOffset] = ( ( byte ) ( outBuff >> 16 ) );
		destination[( destOffset + 1 )] = ( ( byte ) ( outBuff >> 8 ) );
		destination[( destOffset + 2 )] = ( ( byte ) outBuff );

		return 3;
	}

	public static byte[] decode( byte[] source ) throws IOException
	{
		byte[] decoded = null;

		decoded = decode( source, 0, source.length, 0 );

		return decoded;
	}

	public static byte[] decode( byte[] source, int off, int len, int options ) throws IOException
	{
		if ( source == null )
		{
			throw new NullPointerException( "Cannot decode null source array." );
		}
		if ( ( off < 0 ) || ( off + len > source.length ) )
		{
			throw new IllegalArgumentException( String.format( "Source array with length %d cannot have offset of %d and process %d bytes.", new Object[] {Integer.valueOf( source.length ), Integer.valueOf( off ), Integer.valueOf( len )} ) );
		}

		if ( len == 0 )
			return new byte[0];
		if ( len < 4 )
		{
			throw new IllegalArgumentException( "Base64-encoded string must have at least four characters, but length specified was " + len );
		}

		byte[] DECODABET = getDecodabet( options );

		int len34 = len * 3 / 4;
		byte[] outBuff = new byte[len34];
		int outBuffPosn = 0;

		byte[] b4 = new byte[4];
		int b4Posn = 0;
		int i = 0;
		byte sbiDecode = 0;

		for ( i = off; i < off + len; i++ )
		{
			sbiDecode = DECODABET[( source[i] & 0xFF )];

			if ( sbiDecode >= -5 )
			{
				if ( sbiDecode >= -1 )
				{
					b4[( b4Posn++ )] = source[i];
					if ( b4Posn > 3 )
					{
						outBuffPosn += decode4to3( b4, 0, outBuff, outBuffPosn, options );
						b4Posn = 0;

						if ( source[i] == 61 )
						{
							break;
						}

					}
				}
			}
			else
			{
				throw new IOException( String.format( "Bad Base64 input character decimal %d in array position %d", new Object[] {Integer.valueOf( source[i] & 0xFF ), Integer.valueOf( i )} ) );
			}
		}

		byte[] out = new byte[outBuffPosn];
		System.arraycopy( outBuff, 0, out, 0, outBuffPosn );
		return out;
	}

	public static byte[] decode( String s ) throws IOException
	{
		return decode( s, 0 );
	}

	public static byte[] decode( String s, int options ) throws IOException
	{
		if ( s == null )
			throw new NullPointerException( "Input string was null." );

		byte[] bytes;

		try
		{
			bytes = s.getBytes( "UTF-8" );
		}
		catch ( UnsupportedEncodingException uee )
		{
			bytes = s.getBytes();
		}

		bytes = decode( bytes, 0, bytes.length, options );

		boolean dontGunzip = ( options & 0x4 ) != 0;
		if ( ( bytes != null ) && ( bytes.length >= 4 ) && ( !dontGunzip ) )
		{
			int head = bytes[0] & 0xFF | bytes[1] << 8 & 0xFF00;
			if ( 35615 == head )
			{
				ByteArrayInputStream bais = null;
				GZIPInputStream gzis = null;
				ByteArrayOutputStream baos = null;
				byte[] buffer = new byte['ࠀ'];
				int length = 0;
				try
				{
					baos = new ByteArrayOutputStream();
					bais = new ByteArrayInputStream( bytes );
					gzis = new GZIPInputStream( bais );

					while ( ( length = gzis.read( buffer ) ) >= 0 )
					{
						baos.write( buffer, 0, length );
					}

					return baos.toByteArray();
				}
				catch ( IOException e )
				{
					e.printStackTrace();
				}
				finally
				{
					try
					{
						baos.close();
					}
					catch ( Exception localException6 )
					{
					}
					try
					{
						gzis.close();
					}
					catch ( Exception localException7 )
					{
					}
					try
					{
						bais.close();
					}
					catch ( Exception localException8 )
					{
						// Ignore
					}
				}
			}
		}

		return bytes;
	}

	public static Object decodeToObject( String encodedObject ) throws IOException, ClassNotFoundException
	{
		return decodeToObject( encodedObject, 0, null );
	}

	public static Object decodeToObject( String encodedObject, int options, final ClassLoader loader ) throws IOException, ClassNotFoundException
	{
		byte[] objBytes = decode( encodedObject, options );

		ByteArrayInputStream bais = null;
		ObjectInputStream ois = null;

		try
		{
			bais = new ByteArrayInputStream( objBytes );

			if ( loader == null )
			{
				ois = new ObjectInputStream( bais );

			}
			else
			{

				ois = new ObjectInputStream( bais )
				{
					public Class<?> resolveClass( ObjectStreamClass streamClass ) throws IOException, ClassNotFoundException
					{
						Class<?> c = Class.forName( streamClass.getName(), false, loader );
						if ( c == null )
						{
							return super.resolveClass( streamClass );
						}
						return c;
					}
				};
			}

			return ois.readObject();
		}
		catch ( IOException e )
		{
			throw e;
		}
		catch ( ClassNotFoundException e )
		{
			throw e;
		}
		finally
		{
			try
			{
				bais.close();
			}
			catch ( Exception localException2 )
			{
			}
			try
			{
				ois.close();
			}
			catch ( Exception localException3 )
			{
			}
		}
	}

	public static void encodeToFile( byte[] dataToEncode, String filename ) throws IOException
	{
		if ( dataToEncode == null )
		{
			throw new NullPointerException( "Data to encode was null." );
		}

		OutputStream bos = null;
		try
		{
			bos = new OutputStream( new FileOutputStream( filename ), 1 );

			bos.write( dataToEncode );
			return;
		}
		catch ( IOException e )
		{
			throw e;
		}
		finally
		{
			try
			{
				bos.close();
			}
			catch ( Exception localException1 )
			{
			}
		}
	}

	public static void decodeToFile( String dataToDecode, String filename ) throws IOException
	{
		OutputStream bos = null;
		try
		{
			bos = new OutputStream( new FileOutputStream( filename ), 0 );

			bos.write( dataToDecode.getBytes( "UTF-8" ) );
			return;
		}
		catch ( IOException e )
		{
			throw e;
		}
		finally
		{
			try
			{
				bos.close();
			}
			catch ( Exception localException1 )
			{
			}
		}
	}

	public static byte[] decodeFromFile( String filename ) throws IOException
	{
		byte[] decodedData = null;
		InputStream bis = null;

		try
		{
			File file = new File( filename );
			byte[] buffer = null;
			int length = 0;
			int numBytes = 0;

			if ( file.length() > 2147483647L )
			{
				throw new IOException( "File is too big for this convenience method (" + file.length() + " bytes)." );
			}
			buffer = new byte[( int ) file.length()];

			bis = new InputStream( new BufferedInputStream( new FileInputStream( file ) ), 0 );

			while ( ( numBytes = bis.read( buffer, length, 4096 ) ) >= 0 )
			{
				length += numBytes;
			}

			decodedData = new byte[length];
			System.arraycopy( buffer, 0, decodedData, 0, length );

			return decodedData;
		}
		catch ( IOException e )
		{
			throw e;
		}
		finally
		{
			try
			{
				bis.close();
			}
			catch ( Exception localException1 )
			{
			}
		}
	}

	public static String encodeFromFile( String filename ) throws IOException
	{
		InputStream bis = null;

		try
		{
			File file = new File( filename );
			byte[] buffer = new byte[Math.max( ( int ) ( file.length() * 1.4D + 1.0D ), 40 )];
			int length = 0;
			int numBytes = 0;

			bis = new InputStream( new BufferedInputStream( new FileInputStream( file ) ), 1 );

			while ( ( numBytes = bis.read( buffer, length, 4096 ) ) >= 0 )
			{
				length += numBytes;
			}

			return new String( buffer, 0, length, "UTF-8" );
		}
		catch ( IOException e )
		{
			throw e;
		}
		finally
		{
			try
			{
				bis.close();
			}
			catch ( Exception localException1 )
			{
			}
		}
	}

	public static void encodeFileToFile( String infile, String outfile ) throws IOException
	{
		String encoded = encodeFromFile( infile );
		java.io.OutputStream out = null;

		try
		{
			out = new BufferedOutputStream( new FileOutputStream( outfile ) );

			out.write( encoded.getBytes( "US-ASCII" ) );
			return;
		}
		catch ( IOException e )
		{
			throw e;
		}
		finally
		{
			try
			{
				out.close();
			}
			catch ( Exception localException1 )
			{
			}
		}
	}

	public static void decodeFileToFile( String infile, String outfile ) throws IOException
	{
		byte[] decoded = decodeFromFile( infile );
		java.io.OutputStream out = null;

		try
		{
			out = new BufferedOutputStream( new FileOutputStream( outfile ) );

			out.write( decoded );
			return;
		}
		catch ( IOException e )
		{
			throw e;
		}
		finally
		{
			try
			{
				out.close();
			}
			catch ( Exception localException1 )
			{
			}
		}
	}

	public static class InputStream extends FilterInputStream
	{
		private boolean encode;

		private int position;

		private byte[] buffer;

		private int bufferLength;

		private int numSigBytes;

		private int lineLength;

		private boolean breakLines;

		private int options;

		private byte[] decodabet;

		public InputStream( java.io.InputStream in )
		{
			this( in, 0 );
		}

		public InputStream( java.io.InputStream in, int options )
		{
			super( in );
			this.options = options;
			breakLines = ( ( options & 0x8 ) > 0 );
			encode = ( ( options & 0x1 ) > 0 );
			bufferLength = ( encode ? 4 : 3 );
			buffer = new byte[bufferLength];
			position = -1;
			lineLength = 0;
			decodabet = Base64.getDecodabet( options );
		}

		public int read() throws IOException
		{
			if ( position < 0 )
			{
				if ( encode )
				{
					byte[] b3 = new byte[3];
					int numBinaryBytes = 0;
					for ( int i = 0; i < 3; i++ )
					{
						int b = in.read();

						if ( b < 0 )
							break;
						b3[i] = ( ( byte ) b );
						numBinaryBytes++;
					}

					if ( numBinaryBytes > 0 )
					{
						Base64.encode3to4( b3, 0, numBinaryBytes, buffer, 0, options );
						position = 0;
						numSigBytes = 4;
					}
					else
					{
						return -1;
					}

				}
				else
				{
					byte[] b4 = new byte[4];
					int i = 0;
					for ( i = 0; i < 4; i++ )
					{
						int b = 0;
						do
						{
							b = in.read();
						}
						while ( ( b >= 0 ) && ( decodabet[( b & 0x7F )] <= -5 ) );

						if ( b < 0 )
						{
							break;
						}

						b4[i] = ( ( byte ) b );
					}

					if ( i == 4 )
					{
						numSigBytes = Base64.decode4to3( b4, 0, buffer, 0, options );
						position = 0;
					}
					else
					{
						if ( i == 0 )
						{
							return -1;
						}

						throw new IOException( "Improperly padded Base64 input." );
					}
				}
			}

			if ( position >= 0 )
			{
				if ( position >= numSigBytes )
				{
					return -1;
				}

				if ( ( encode ) && ( breakLines ) && ( lineLength >= 76 ) )
				{
					lineLength = 0;
					return 10;
				}

				lineLength += 1;

				int b = buffer[( position++ )];

				if ( position >= bufferLength )
				{
					position = -1;
				}

				return b & 0xFF;
			}

			throw new IOException( "Error in Base64 code reading stream." );
		}

		public int read( @Nonnull byte[] dest, int off, int len ) throws IOException
		{
			int i;

			for ( i = 0; i < len; i++ )
			{
				int b = read();

				if ( b >= 0 )
				{
					dest[( off + i )] = ( ( byte ) b );
				}
				else
				{
					if ( i != 0 )
						break;
					return -1;
				}
			}

			return i;
		}
	}

	public static class OutputStream extends FilterOutputStream
	{
		private boolean encode;

		private int position;

		private byte[] buffer;

		private int bufferLength;

		private int lineLength;

		private boolean breakLines;

		private byte[] b4;

		private boolean suspendEncoding;

		private int options;

		private byte[] decodabet;

		public OutputStream( java.io.OutputStream out )
		{
			this( out, 1 );
		}

		public OutputStream( java.io.OutputStream out, int options )
		{
			super( out );
			breakLines = ( ( options & 0x8 ) != 0 );
			encode = ( ( options & 0x1 ) != 0 );
			bufferLength = ( encode ? 3 : 4 );
			buffer = new byte[bufferLength];
			position = 0;
			lineLength = 0;
			suspendEncoding = false;
			b4 = new byte[4];
			this.options = options;
			decodabet = Base64.getDecodabet( options );
		}

		public void write( int theByte ) throws IOException
		{
			if ( suspendEncoding )
			{
				out.write( theByte );
				return;
			}

			if ( encode )
			{
				buffer[( position++ )] = ( ( byte ) theByte );
				if ( position >= bufferLength )
				{
					out.write( Base64.encode3to4( b4, buffer, bufferLength, options ) );

					lineLength += 4;
					if ( ( breakLines ) && ( lineLength >= 76 ) )
					{
						out.write( 10 );
						lineLength = 0;
					}

					position = 0;

				}

			}
			else if ( decodabet[( theByte & 0x7F )] > -5 )
			{
				buffer[( position++ )] = ( ( byte ) theByte );
				if ( position >= bufferLength )
				{
					int len = Base64.decode4to3( buffer, 0, b4, 0, options );
					out.write( b4, 0, len );
					position = 0;
				}
			}
			else if ( decodabet[( theByte & 0x7F )] != -5 )
			{
				throw new IOException( "Invalid character in Base64 data." );
			}
		}

		public void write( @Nonnull byte[] theBytes, int off, int len ) throws IOException
		{
			if ( suspendEncoding )
			{
				out.write( theBytes, off, len );
				return;
			}

			for ( int i = 0; i < len; i++ )
			{
				write( theBytes[( off + i )] );
			}
		}

		public void flushBase64() throws IOException
		{
			if ( position > 0 )
			{
				if ( encode )
				{
					out.write( Base64.encode3to4( b4, buffer, position, options ) );
					position = 0;
				}
				else
				{
					throw new IOException( "Base64 input not properly padded." );
				}
			}
		}

		public void close() throws IOException
		{
			flushBase64();

			super.close();

			buffer = null;
			out = null;
		}

		public void suspendEncoding() throws IOException
		{
			flushBase64();
			suspendEncoding = true;
		}

		public void resumeEncoding()
		{
			suspendEncoding = false;
		}
	}
}
