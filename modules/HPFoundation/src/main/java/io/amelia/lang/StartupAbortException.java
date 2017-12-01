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
 * Used to gracefully abort a server startup, e.g., by user interaction.
 */
public class StartupAbortException extends StartupException
{
	private static final long serialVersionUID = -4937198089020390887L;

	public StartupAbortException()
	{
		super( "STARTUP ABORTED!" );
	}
}
