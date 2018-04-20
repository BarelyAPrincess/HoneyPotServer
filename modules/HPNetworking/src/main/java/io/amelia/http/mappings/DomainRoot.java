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

import io.amelia.support.Namespace;

public class DomainRoot extends DomainNode
{
	protected final String tld;

	public DomainRoot( String tld, String nodeName )
	{
		super( nodeName );
		this.tld = tld;
	}

	@Override
	public Namespace getNamespace()
	{
		return Namespace.parseString( tld ).reverseOrder().append( getNodeName() );
	}

	public String getTld()
	{
		return tld;
	}
}
