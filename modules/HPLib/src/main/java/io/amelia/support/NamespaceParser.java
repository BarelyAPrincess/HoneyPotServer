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

import io.amelia.config.ConfigRegistry;
import io.amelia.foundation.Kernel;

import java.util.ArrayList;
import java.util.List;

public class NamespaceParser
{
	private static final List<String> tldMaps = new ArrayList<>();

	static
	{
		try
		{
			tldMaps.addAll( ConfigRegistry.getList( "conf.tlds" ) );
		}
		catch ( Exception e )
		{
			Kernel.L.severe( "Could not read TLD configuration. Check if config list `conf.tlds` exists.", e );
		}
	}

	public static boolean isTld( String domain )
	{
		domain = LibHttp.normalize( domain );
		for ( String tld : tldMaps )
			if ( domain.matches( tld ) )
				return true;
		return false;
	}

	private final Namespace sub;
	private final Namespace tld;

	public NamespaceParser( String fullDomain )
	{
		fullDomain = LibHttp.normalize( fullDomain );

		if ( Objs.isEmpty( fullDomain ) )
		{
			tld = new Namespace();
			sub = new Namespace();
			return;
		}

		Namespace ns = Namespace.parseString( fullDomain );
		int parentNodePos = -1;

		for ( int n = 0; n < ns.getNodeCount(); n++ )
		{
			String sns = ns.subNamespace( n ).getString();
			if ( isTld( sns ) )
			{
				parentNodePos = n;
				break;
			}
		}

		if ( parentNodePos > 0 )
		{
			tld = ns.subNamespace( parentNodePos );
			sub = ns.subNamespace( 0, parentNodePos );
		}
		else
		{
			tld = new Namespace();
			sub = ns;
		}
	}

	public Namespace getChildDomain()
	{
		return sub.getNodeCount() <= 1 ? new Namespace() : sub.subNamespace( 1 );
	}

	public Namespace getFullDomain()
	{
		return sub.merge( tld );
	}

	public Namespace getRootDomain()
	{
		return Namespace.parseString( sub.getLast() + "." + tld.getString() );
	}

	public Namespace getSub()
	{
		return sub;
	}

	public Namespace getTld()
	{
		return tld;
	}
}
