/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.support;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * Provides basic encryption and randomizing functions
 */
public class Encrypt
{
	/**
	 * The MD2 message digest algorithm defined in RFC 1319.
	 */
	public static final String MD2 = "MD2";

	/**
	 * The MD5 message digest algorithm defined in RFC 1321.
	 */
	public static final String MD5 = "MD5";

	/**
	 * The SHA-1 hash algorithm defined in the FIPS PUB 180-2.
	 */
	public static final String SHA_1 = "SHA-1";

	/**
	 * The SHA-256 hash algorithm defined in the FIPS PUB 180-2.
	 */
	public static final String SHA_256 = "SHA-256";

	/**
	 * The SHA-384 hash algorithm defined in the FIPS PUB 180-2.
	 */
	public static final String SHA_384 = "SHA-384";

	/**
	 * The SHA-512 hash algorithm defined in the FIPS PUB 180-2.
	 */
	public static final String SHA_512 = "SHA-512";

	private static final char[] allowedCharMap;
	private static final Random random = new Random();
	private static final char[] randomCharMap;

	static
	{
		Set<Character> newRandomCharMap = new HashSet<>();

		for ( int i = 33; i < 48; i++ )
			newRandomCharMap.add( ( char ) i );

		for ( int i = 58; i < 65; i++ )
			newRandomCharMap.add( ( char ) i );

		for ( int i = 91; i < 97; i++ )
			newRandomCharMap.add( ( char ) i );

		for ( int i = 123; i < 128; i++ )
			newRandomCharMap.add( ( char ) i );

		newRandomCharMap.addAll( new HashSet<>( Arrays.asList( new Character[] {128, 131, 134, 135, 138, 140, 142, 156, 158, 159, 161, 162, 163, 165, 167, 176, 181, 191} ) ) );

		for ( int i = 192; i < 256; i++ )
			newRandomCharMap.add( ( char ) i );

		randomCharMap = Arrs.toCharArray( newRandomCharMap );

		Set<Character> newAllowedCharMap = new HashSet<>();

		for ( int i = 33; i < 127; i++ )
			newAllowedCharMap.add( ( char ) i );

		newAllowedCharMap.addAll( new HashSet<>( Arrays.asList( new Character[] {128, 131, 134, 135, 138, 140, 142, 156, 158, 159, 161, 162, 163, 165, 167, 176, 181, 191} ) ) );

		for ( int i = 192; i < 256; i++ )
			newAllowedCharMap.add( ( char ) i );

		allowedCharMap = Arrs.toCharArray( newAllowedCharMap );
	}

	public static byte[] base64Decode( String str )
	{
		return Base64.getDecoder().decode( str );
	}

	public static String base64DecodeString( String str )
	{
		return new String( Base64.getDecoder().decode( str ) );
	}

	public static String base64Encode( byte[] bytes )
	{
		return Base64.getEncoder().encodeToString( bytes );
	}

	public static String base64Encode( String str )
	{
		return Base64.getEncoder().encodeToString( str.getBytes() );
	}

	/**
	 * Returns a <code>MessageDigest</code> for the given <code>algorithm</code>.
	 *
	 * @param algorithm the name of the algorithm requested. See <a
	 *                  href="http://java.sun.com/j2se/1.3/docs/guide/security/CryptoSpec.html#AppA">Appendix A in the Java
	 *                  Cryptography Architecture API Specification & Reference</a> for information about standard algorithm
	 *                  names.
	 *
	 * @return A digest instance.
	 *
	 * @throws IllegalArgumentException when a {@link NoSuchAlgorithmException} is caught.
	 * @see MessageDigest#getInstance(String)
	 */
	public static MessageDigest getDigest( final String algorithm )
	{
		try
		{
			return MessageDigest.getInstance( algorithm );
		}
		catch ( final NoSuchAlgorithmException e )
		{
			throw new IllegalArgumentException( e );
		}
	}

	public static Random getRandom()
	{
		return random;
	}

	public static String hash()
	{
		return hash( seed( 16 ) );
	}

	/**
	 * Creates a basic 16-bit hash string using the UUID and MD5 methods
	 */
	public static String hash( byte[] seed )
	{
		return md5Hex( uuid( seed ) );
	}

	public static byte[] md5( byte[] bytes )
	{
		return getDigest( MD5 ).digest( bytes );
	}

	public static byte[] md5( InputStream is ) throws IOException
	{
		return getDigest( MD5 ).digest( IO.readStreamToBytes( is ) );
	}

	public static byte[] md5( final String str )
	{
		return getDigest( MD5 ).digest( Strs.decodeUtf8( str ) );
	}

	public static String md5Hex( byte[] bytes )
	{
		return String.format( "%040x", new BigInteger( 1, md5( bytes ) ) );
	}

	public static String md5Hex( InputStream is ) throws IOException
	{
		return md5Hex( IO.readStreamToString( is ) );
	}

	public static String md5Hex( String str )
	{
		if ( str == null )
			return null;

		return String.format( "%040x", new BigInteger( 1, md5( Strs.decodeUtf8( str ) ) ) );
	}

	public static String rand()
	{
		return rand( 8, true, false, new String[0] );
	}

	public static String rand( int length )
	{
		return rand( length, true, false, new String[0] );
	}

	public static String rand( int length, boolean numbers )
	{
		return rand( length, numbers, false, new String[0] );
	}

	public static String rand( int length, boolean numbers, boolean letters )
	{
		return rand( length, numbers, letters, new String[0] );
	}

	public static String rand( int length, boolean numbers, boolean letters, String[] allowedChars )
	{
		if ( allowedChars == null )
			allowedChars = new String[0];

		if ( numbers )
			allowedChars = Arrs.concat( allowedChars, new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0"} );

		if ( letters )
			allowedChars = Arrs.concat( allowedChars, new String[] {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"} );

		StringBuilder rtn = new StringBuilder();
		for ( int i = 0; i < length; i++ )
			rtn.append( allowedChars[new Random().nextInt( allowedChars.length )] );

		return rtn.toString();
	}

	/**
	 * Creates a Random Instance using 4 bits from SecureRandom
	 */
	public static Random random()
	{
		String seed = Encrypt.md5Hex( Encrypt.seed( 4 ) ).replaceAll( "\\D", "" );
		return new Random( Long.parseLong( seed.length() > 12 ? seed.substring( 0, 12 ) : seed ) ^ System.nanoTime() );
	}

	/**
	 * Selects a random character within range 33-126, 128, 131, 134, 135, 138, 140, 142, 156, 158, 159, 161, 162, 163, 165, 167, 176, 181, 191, and 192-255
	 *
	 * @return The randomly selected character
	 */
	public static char randomize()
	{
		return randomize( new Random() );
	}

	/**
	 * Takes the input character and scrambles it
	 *
	 * @param chr Random base character<br>
	 *            <i>A-Z</i> will result in a random uppercase character<br>
	 *            <i>a-z</i> will result in a random lowercase character<br>
	 *            <i>0-9</i> will result in a random number character<br>
	 *            <i>All others will result in a random symbol or accented character</i>
	 *
	 * @return Randomized character based on the original
	 */
	public static char randomize( char chr )
	{
		return randomize( new Random(), chr );
	}

	/**
	 * Selects a random character between 0-255 using specified start and end arguments
	 *
	 * @param start The minimum character to select
	 * @param end   The maximum character to select
	 *
	 * @return The randomly selected character
	 */
	public static char randomize( int start, int end )
	{
		return randomize( new Random(), start, end );
	}

	public static char randomize( Random random )
	{
		return allowedCharMap[random.nextInt( allowedCharMap.length )];
	}

	public static char randomize( Random random, char chr )
	{
		if ( chr > 64 && chr < 91 ) // Uppercase
			return randomize( random, 65, 90 );

		if ( chr > 96 && chr < 123 ) // Lowercase
			return randomize( random, 97, 122 );

		if ( chr > 47 && chr < 58 ) // Numeric
			return randomize( random, 48, 57 );

		return randomCharMap[random.nextInt( randomCharMap.length )];
	}

	public static String randomize( Random rando, int length )
	{
		StringBuilder sb = new StringBuilder();

		for ( int i = 0; i < length; i++ )
			sb.append( randomize( rando ) );

		return sb.toString();
	}

	public static char randomize( Random rando, int start, int end )
	{
		if ( start > end )
			throw new RuntimeException( "Start can't be greater than end!" );

		return ( char ) ( start + rando.nextInt( end - start ) );
	}

	public static String randomize( Random rando, String base )
	{
		StringBuilder sb = new StringBuilder( base );

		for ( int i = 0; i < base.length(); i++ )
			sb.setCharAt( i, randomize( rando, sb.charAt( i ) ) );

		return sb.toString();
	}

	/**
	 * Takes each character of the provided string and scrambles it<br>
	 * Example: 0xx0000$X <i>could</i> result in 9at6342&Z
	 *
	 * @param base The base pattern to follow<br>
	 *             <i>A-Z</i> will result in a random uppercase character<br>
	 *             <i>a-z</i> will result in a random lowercase character<br>
	 *             <i>0-9</i> will result in a random number character<br>
	 *             <i>All others will result in a random symbol or accented character</i>
	 *
	 * @return String randomized using your original base string
	 */
	public static String randomize( String base )
	{
		return randomize( new Random(), base );
	}

	public static byte[] seed( int length )
	{
		try
		{
			return SecureRandom.getInstanceStrong().generateSeed( length );
		}
		catch ( NoSuchAlgorithmException e )
		{
			e.printStackTrace();
			return new byte[0];
		}
	}

	public static String uuid()
	{
		return uuid( seed( 16 ) );
	}

	public static String uuid( byte[] seed )
	{
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();

		try
		{
			for ( byte b : seed )
			{
				byte[] tbyte = new byte[2];
				new Random().nextBytes( tbyte );

				tbyte[0] = ( byte ) ( b + tbyte[0] );
				tbyte[1] = ( byte ) ( b + tbyte[1] );

				bytes.write( tbyte );
			}
		}
		catch ( IOException e )
		{
			// Ignore
		}

		return UUID.nameUUIDFromBytes( bytes.toByteArray() ).toString();
	}

	private Encrypt()
	{

	}
}
