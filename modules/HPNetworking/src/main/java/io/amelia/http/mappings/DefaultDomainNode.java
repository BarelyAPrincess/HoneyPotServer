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

public class DefaultDomainNode extends DomainNode
{
	protected DefaultDomainNode()
	{
		super( "" );
		webroot = SiteModule.i().getDefaultSite();
	}

	@Override
	public DomainNode getChild( String domain, boolean create )
	{
		if ( create )
			throw new IllegalStateException( "Operation Not Permitted" );
		return null;
	}

	@Override
	protected DomainNode setWebroot( Site site, boolean override )
	{
		throw new IllegalStateException( "Operation Not Permitted" );
	}
}
