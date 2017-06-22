/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.lang;

/**
 */
public class PluginUnconfiguredException extends PluginException
{
	private static final long serialVersionUID = 4789128239905660393L;
	
	public PluginUnconfiguredException( String message )
	{
		super( message );
	}
}
