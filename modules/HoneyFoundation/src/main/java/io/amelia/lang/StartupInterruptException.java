/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.lang;

/**
 * Used to gracefully interrupt startup.
 * Such as if --help or --version was specified.
 */
public class StartupInterruptException extends StartupException
{
	private static final long serialVersionUID = -4937198089020390887L;

	public StartupInterruptException()
	{
		super( "STARTUP INTERRUPT!" );
	}
}
