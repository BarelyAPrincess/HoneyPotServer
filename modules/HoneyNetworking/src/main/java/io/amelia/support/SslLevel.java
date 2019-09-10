/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.support;

public enum SslLevel
{
	Ignore,
	Deny,
	PostOnly,
	GetOnly,
	Preferred,
	Required;

	public static SslLevel parse( String level, SslLevel def )
	{
		try
		{
			return parse( level );
		}
		catch ( IllegalArgumentException e )
		{
			return def;
		}
	}

	public static SslLevel parse( String level )
	{
		if ( level == null || level.length() == 0 || level.equalsIgnoreCase( "ignore" ) )
			return Ignore;
		if ( level.equalsIgnoreCase( "deny" ) || level.equalsIgnoreCase( "disabled" ) )
			return Deny;
		if ( level.equalsIgnoreCase( "postonly" ) )
			return PostOnly;
		if ( level.equalsIgnoreCase( "getonly" ) )
			return GetOnly;
		if ( level.equalsIgnoreCase( "preferred" ) )
			return Preferred;
		if ( level.equalsIgnoreCase( "required" ) || level.equalsIgnoreCase( "require" ) )
			return Required;
		throw new IllegalArgumentException( String.format( "Ssl level %s is invalid, the available options are Deny, Ignore, PostOnly, Preferred, GetOnly, and Required.", level ) );
	}
}
