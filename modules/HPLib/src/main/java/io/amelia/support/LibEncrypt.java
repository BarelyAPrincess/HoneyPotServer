/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.support;

import com.chiorichan.tasks.Timings;
import com.google.common.collect.Sets;
import com.google.common.primitives.Chars;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * Provides basic encryption and randomizing functions
 */
public class LibEncrypt
{
	private LibEncrypt()
	{

	}

	private static final char[] randomCharMap;
	private static final char[] allowedCharMap;

	static
	{
		Set<Character> newRandomCharMap = Sets.newHashSet();

		for ( int i = 33; i < 48; i++ )
			newRandomCharMap.add( ( char ) i );

		for ( int i = 58; i < 65; i++ )
			newRandomCharMap.add( ( char ) i );

		for ( int i = 91; i < 97; i++ )
			newRandomCharMap.add( ( char ) i );

		for ( int i = 123; i < 128; i++ )
			newRandomCharMap.add( ( char ) i );

		newRandomCharMap.addAll( Sets.newHashSet( Arrays.asList( new Character[] {128, 131, 134, 135, 138, 140, 142, 156, 158, 159, 161, 162, 163, 165, 167, 176, 181, 191} ) ) );

		for ( int i = 192; i < 256; i++ )
			newRandomCharMap.add( ( char ) i );

		randomCharMap = Chars.toArray( newRandomCharMap );
		newRandomCharMap = null;

		Set<Character> newAllowedCharMap = Sets.newHashSet();

		for ( int i = 33; i < 127; i++ )
			newAllowedCharMap.add( ( char ) i );

		newAllowedCharMap.addAll( Sets.newHashSet( Arrays.asList( new Character[] {128, 131, 134, 135, 138, 140, 142, 156, 158, 159, 161, 162, 163, 165, 167, 176, 181, 191} ) ) );

		for ( int i = 192; i < 256; i++ )
			newAllowedCharMap.add( ( char ) i );

		allowedCharMap = Chars.toArray( newAllowedCharMap );
		newAllowedCharMap = null;
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

	public static String guid() throws UnsupportedEncodingException
	{
		return guid( Timings.epoch() + "-guid" );
	}

	public static String guid( String seed )
	{
		if ( seed == null )
			seed = "";

		byte[] bytes;
		try
		{
			bytes = seed.getBytes( "ISO-8859-1" );
		}
		catch ( UnsupportedEncodingException e )
		{
			bytes = new byte[0];
		}

		byte[] bytesScrambled = new byte[0];

		for ( byte b : bytes )
		{
			byte[] tbyte = new byte[2];
			new Random().nextBytes( bytes );

			tbyte[0] = ( byte ) ( b + tbyte[0] );
			tbyte[1] = ( byte ) ( b + tbyte[1] );

			bytesScrambled = ArrayUtils.addAll( bytesScrambled, tbyte );
		}

		return "{" + UUID.nameUUIDFromBytes( bytesScrambled ).toString() + "}";
	}

	public static String md5( byte[] bytes )
	{
		return DigestUtils.md5Hex( bytes );
	}

	public static String md5( File file )
	{
		if ( file == null || !file.exists() )
			return null;
		try
		{
			return md5( IOUtils.toByteArray( new FileInputStream( file ) ) );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
			return null;
		}
	}

	public static String md5( String str )
	{
		if ( str == null )
			return null;

		return DigestUtils.md5Hex( str );
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
			allowedChars = ArrayUtils.addAll( allowedChars, "1", "2", "3", "4", "5", "6", "7", "8", "9", "0" );

		if ( letters )
			allowedChars = ArrayUtils.addAll( allowedChars, "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" );

		String rtn = "";
		for ( int i = 0; i < length; i++ )
			rtn += allowedChars[new Random().nextInt( allowedChars.length )];

		return rtn;
	}

	/**
	 * Creates a Random Instance using 4 bits from SecureRandom
	 */
	public static Random random()
	{
		String seed = LibEncrypt.md5( LibEncrypt.seed( 4 ) ).replaceAll( "\\D", "" );
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

	public static String uuid() throws UnsupportedEncodingException
	{
		return uuid( Timings.epoch() + "-uuid" );
	}

	public static String uuid( String seed ) throws UnsupportedEncodingException
	{
		return DigestUtils.md5Hex( guid( seed ) );
	}
}
