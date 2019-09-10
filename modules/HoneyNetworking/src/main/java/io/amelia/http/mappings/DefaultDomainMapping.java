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

import java.nio.file.Path;

import io.amelia.http.webroot.Webroot;

public final class DefaultDomainMapping extends DomainMapping
{
	public DefaultDomainMapping( Webroot webroot )
	{
		super( webroot, "" );
	}

	@Override
	protected Path directory0( boolean throwException )
	{
		return getWebroot().getPublicDirectory();
	}

	@Override
	public DomainMapping getChildMapping( String child )
	{
		return null;
	}

	@Override
	public DomainMapping getParentMapping()
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
