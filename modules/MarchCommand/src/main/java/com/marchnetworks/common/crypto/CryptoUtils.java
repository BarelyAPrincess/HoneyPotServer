package com.marchnetworks.common.crypto;

import com.marchnetworks.command.common.Base64;
import com.marchnetworks.command.common.CollectionUtils;
import com.marchnetworks.command.common.CommonAppUtils;
import com.marchnetworks.command.common.user.data.MemberView;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtils
{
	public static final int HASH_LENGTH = 20;
	public static final String TRANSFORMATION = "AES/CFB/NoPadding";
	private static int ITERATION_NUMBER = 1000;
	private static final String CRYPT_SHA_1 = "SHA-1";
	private static final String CRYPT_MD5 = "MD5";
	private static final String DEFAULT_REALM = "Command";

	public static byte[] encrypt( byte[] input ) throws CryptoException
	{
		if ( input == null )
		{
			throw new CryptoException( "Input is null" );
		}
		byte[] iv = getIV();
		byte[] key = getKey();
		byte[] result = null;
		try
		{
			Cipher c = Cipher.getInstance( "AES/CFB/NoPadding", "BC" );
			SecretKeySpec k = new SecretKeySpec( key, "AES" );
			c.init( 1, k, new IvParameterSpec( iv ) );
			byte[] cipherText = c.doFinal( input );

			byte[] hash = sha1( input );

			result = CollectionUtils.concatenate( hash, cipherText );
		}
		catch ( GeneralSecurityException e )
		{
			throw new CryptoException( "Encryption failed", e );
		}

		return result;
	}

	public static String encrypt( String input ) throws CryptoException
	{
		if ( input == null )
			throw new CryptoException( "Input is null" );

		byte[] result;
		try
		{
			result = encrypt( input.getBytes( "UTF-8" ) );
		}
		catch ( UnsupportedEncodingException e )
		{
			throw new CryptoException( "String encoding failed", e );
		}

		return Base64.encodeBytes( result );
	}

	public static byte[] decrypt( byte[] cipherText ) throws CryptoException
	{
		if ( cipherText == null )
		{
			throw new CryptoException( "Input is null" );
		}
		byte[] hash = Arrays.copyOfRange( cipherText, 0, 20 );
		byte[] key = getKey();
		byte[] iv = getIV();
		byte[] result = null;

		try
		{
			Cipher c = Cipher.getInstance( "AES/CFB/NoPadding", "BC" );
			SecretKeySpec k = new SecretKeySpec( key, "AES" );
			c.init( 2, k, new IvParameterSpec( iv ) );

			result = c.doFinal( cipherText, 20, cipherText.length - 20 );

			byte[] computedHash = sha1( result );

			if ( !MessageDigest.isEqual( hash, computedHash ) )
			{
				throw new CryptoException( "Hash mismatch" );
			}
		}
		catch ( GeneralSecurityException e )
		{
			throw new CryptoException( "Decryption failed", e );
		}

		return result;
	}

	public static byte[] decrypt( String input ) throws CryptoException
	{
		if ( input == null )
			throw new CryptoException( "Input is null" );

		try
		{
			return decrypt( Base64.decode( input ) );
		}
		catch ( IOException e )
		{
			throw new CryptoException( "String decoding failed", e );
		}
	}

	private static byte[] getKey()
	{
		byte[] key = {-115, -1, 0, -12, -76, 126, 81, 8, -94, 58, 60, -34, 16, -55, Byte.MIN_VALUE, 4, -115, -46, -125, 19, -88, -23, -90, 5, -51, -26, -17, Byte.MAX_VALUE, -15, -5, 96, 51};

		return key;
	}

	private static byte[] getIV()
	{
		byte[] IV = {112, 118, -87, 26, -20, 95, 87, -37, 65, 1, 112, 71, 63, 77, Byte.MIN_VALUE, -32};
		return IV;
	}

	public static byte[] sha1( byte[] input ) throws CryptoException
	{
		try
		{
			MessageDigest cript = MessageDigest.getInstance( "SHA-1" );
			cript.reset();
			return cript.digest( input );
		}
		catch ( NoSuchAlgorithmException e )
		{
			throw new CryptoException( "Hashing failed", e );
		}
	}

	public static byte[] getSHA1Hash( String password, byte[] salt )
	{
		try
		{
			MessageDigest digest = MessageDigest.getInstance( "SHA-1" );
			digest.reset();
			digest.update( salt );
			byte[] input = digest.digest( password.getBytes( "UTF-8" ) );
			for ( int i = 0; i < ITERATION_NUMBER; i++ )
			{
				digest.reset();
				input = digest.digest( input );
			}
			return input;
		}
		catch ( Exception e )
		{
		}
		return null;
	}

	public static byte[] getSHA1HashESM( String password, byte[] salt )
	{
		try
		{
			MessageDigest digest = MessageDigest.getInstance( "SHA-1" );
			digest.reset();
			digest.update( salt );
			return digest.digest( password.getBytes( "UTF-8" ) );
		}
		catch ( Exception e )
		{
		}

		return null;
	}

	public static byte[] generateSalt()
	{
		Random ranGen = new SecureRandom();
		byte[] aesKey = new byte[16];
		ranGen.nextBytes( aesKey );
		return aesKey;
	}

	public static byte[] getMD5Hash( String userName, String password, String realm )
	{
		try
		{
			if ( CommonAppUtils.isNullOrEmptyString( realm ) )
			{
				realm = "Command";
			}

			MessageDigest digest = MessageDigest.getInstance( "MD5" );
			digest.reset();
			StringBuffer tmp = new StringBuffer( userName );
			return digest.digest( ( ":" + realm + ":" + password ).getBytes( "UTF-8" ) );
		}
		catch ( NoSuchAlgorithmException e )
		{
		}
		catch ( UnsupportedEncodingException e )
		{
		}

		return null;
	}

	public static byte[] getHash( MemberView member, String password, String function, String realm )
	{
		byte[] aHash = null;
		if ( ( function != null ) && ( function.equals( "MD5" ) ) )
		{
			aHash = getMD5Hash( member.getName(), password, realm );
		}
		else
		{
			aHash = getSHA1Hash( password, member.getSalt() );
			if ( !Arrays.equals( aHash, member.getHash() ) )
			{
				aHash = getSHA1HashESM( password, member.getSalt() );
			}
		}
		return aHash;
	}
}
