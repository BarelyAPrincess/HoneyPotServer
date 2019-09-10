/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.http.mappings;

import io.amelia.http.webroot.Webroot;
import io.amelia.http.webroot.WebrootRegistry;

public class DefaultDomainNode extends DomainNode
{
	protected DefaultDomainNode()
	{
		super( "" );
		webroot = WebrootRegistry.getDefaultWebroot();
	}

	@Override
	public DomainNode getChild( String domain, boolean create )
	{
		if ( create )
			throw new IllegalStateException( "Operation Not Permitted" );
		return null;
	}

	@Override
	protected DomainNode setWebroot( Webroot site, boolean override )
	{
		throw new IllegalStateException( "Operation Not Permitted" );
	}
}
