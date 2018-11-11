/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.http.mappings;

import java.util.ArrayList;
import java.util.List;

import io.amelia.data.TypeBase;
import io.amelia.foundation.ConfigRegistry;
import io.amelia.support.Http;
import io.amelia.support.Namespace;
import io.amelia.support.Objs;

public class DomainParser
{
	public static final TypeBase.TypeStringList TLDS = new TypeBase.TypeStringList( ConfigRegistry.ConfigKeys.CONFIGURATION_BASE, "tlds", ArrayList::new );

	private static List<String> getTldList()
	{
		return ConfigRegistry.config.getValue( TLDS );
	}

	public static boolean isTld( String domain )
	{
		domain = Http.hostnameNormalize( domain );
		for ( String tld : getTldList() )
			if ( domain.matches( tld ) )
				return true;
		return false;
	}

	private final Namespace sub;
	private final Namespace tld;

	public DomainParser( String fullDomain )
	{
		fullDomain = Http.hostnameNormalize( fullDomain );

		if ( Objs.isEmpty( fullDomain ) )
		{
			tld = new Namespace();
			sub = new Namespace();
			return;
		}

		Namespace ns = Namespace.of( fullDomain );
		int parentNodePos = -1;

		for ( int n = 0; n < ns.getNodeCount(); n++ )
		{
			String sns = ns.subNodes( n ).getString();
			if ( isTld( sns ) )
			{
				parentNodePos = n;
				break;
			}
		}

		if ( parentNodePos > 0 )
		{
			tld = ns.subNodes( parentNodePos );
			sub = ns.subNodes( 0, parentNodePos );
		}
		else
		{
			tld = new Namespace();
			sub = ns;
		}
	}

	public void addTld( String tld )
	{
		getTldList().add( tld );
	}

	public Namespace getChildDomain()
	{
		return sub.getNodeCount() <= 1 ? new Namespace() : sub.subNodes( 1 );
	}

	public Namespace getFullDomain()
	{
		return sub.merge( tld );
	}

	public Namespace getRootDomain()
	{
		return Namespace.of( sub.getStringLast() + "." + tld.getString() );
	}

	public Namespace getSub()
	{
		return sub.clone();
	}

	public Namespace getTld()
	{
		return tld.clone();
	}
}
