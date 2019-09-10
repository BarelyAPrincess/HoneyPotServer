/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.http;

import java.util.Map;
import java.util.Random;

import io.amelia.data.parcel.Parcel;
import io.amelia.http.session.Session;
import io.amelia.lang.NonceException;
import io.amelia.lang.ParcelableException;
import io.amelia.support.DateAndTime;
import io.amelia.support.Encrypt;
import io.amelia.support.http.HttpNonce;

/**
 * Provides NONCE persistence and checking
 */
public class Nonce implements HttpNonce
{
	private long created = DateAndTime.epoch();
	private String key;
	private Parcel data = Parcel.empty();
	private String sessionId;
	private String value;

	public Nonce( Session sess )
	{
		Random r = Encrypt.random();

		key = Encrypt.randomize( r, "Z1111Y2222" );
		value = Encrypt.base64Encode( sess.getSessionId() + created + Encrypt.randomize( r, 16 ) );
		sessionId = sess.getSessionId();
	}

	@Override
	public String key()
	{
		return key;
	}

	@Override
	public Parcel getData()
	{
		return data;
	}

	@Override
	public void putAll( Map<String, String> values )
	{
		for ( Map.Entry<String, String> entry : values.entrySet() )
		{
			String key = entry.getKey();
			if ( key.contains( data.getOptions().getSeparator() ) )
				key = key.replace( data.getOptions().getSeparator(), data.getOptions().getSeparatorReplacement() );
			put( key, entry.getValue() );
		}
	}

	@Override
	public void put( String key, String val )
	{
		try
		{
			data.setValue( key, value );
		}
		catch ( ParcelableException.Error e )
		{
			// Should never happen!
			e.printStackTrace();
		}
	}

	@Override
	public String query()
	{
		return key + "=" + value;
	}

	@Override
	public String toString()
	{
		return "<input type=\"hidden\" name=\"" + key + "\" value=\"" + value + "\" />";
	}

	@Override
	public boolean validate( String token )
	{
		try
		{
			validateWithException( token );
		}
		catch ( NonceException e )
		{
			return false;
		}
		return true;
	}

	public void validateWithException( String token ) throws NonceException
	{
		if ( !value.equals( token ) )
			throw new NonceException( "The NONCE token does not match" );

		String decoded = Encrypt.base64DecodeString( token );

		if ( !sessionId.equals( decoded.substring( 0, sessionId.length() ) ) )
			// This was generated for a different Session
			throw new NonceException( "The NONCE did not match the current session" );

		int epoch = Integer.parseInt( decoded.substring( sessionId.length(), decoded.length() - 16 ) );

		if ( epoch != created )
			throw new NonceException( "The NONCE has an invalid timestamp" );
	}

	@Override
	public String value()
	{
		return value;
	}

	public enum NonceLevel
	{
		Disabled,
		Flexible,
		PostOnly,
		GetOnly,
		Required;

		public static NonceLevel parse( String level )
		{
			if ( level == null || level.length() == 0 || level.equalsIgnoreCase( "flexible" ) )
				return Flexible;
			if ( level.equalsIgnoreCase( "postonly" ) )
				return PostOnly;
			if ( level.equalsIgnoreCase( "getonly" ) )
				return GetOnly;
			if ( level.equalsIgnoreCase( "disabled" ) )
				return Disabled;
			if ( level.equalsIgnoreCase( "required" ) || level.equalsIgnoreCase( "require" ) )
				return Required;
			throw new IllegalArgumentException( String.format( "Nonce level %s is not available, the available options are Disabled, Flexible, PostOnly, GetOnly, and Required.", level ) );
		}
	}
}
