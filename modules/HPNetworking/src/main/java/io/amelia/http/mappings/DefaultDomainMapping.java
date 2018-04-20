/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.http.mappings;

import io.amelia.lang.SiteConfigurationException;

import java.io.File;

public final class DefaultDomainMapping extends DomainMapping
{
	public DefaultDomainMapping( Site site )
	{
		super( site, "" );
	}

	@Override
	protected File directory0( boolean throwException ) throws SiteConfigurationException
	{
		return getSite().directoryPublic();
	}

	@Override
	public DomainMapping getParentMapping()
	{
		return null;
	}

	@Override
	public DomainMapping getChildMapping( String child )
	{
		return null;
	}

	@Override
	public boolean isMapped()
	{
		return true;
	}

	@Override
	public DomainNode map()
	{
		throw new IllegalStateException( "Operation Not Permitted" );
	}

	@Override
	public void unmap()
	{
		throw new IllegalStateException( "Operation Not Permitted" );
	}
}
