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
 * Thrown when attempting to load an invalid Plugin file
 */
public class PluginDependencyUnknownException extends PluginException.Internal
{
	/**
	 * Constructs a new UnknownDependencyException
	 */
	public PluginDependencyUnknownException()
	{

	}

	/**
	 * Constructs a new UnknownDependencyException with the given message
	 *
	 * @param message Brief message explaining the cause of the exception
	 */
	public PluginDependencyUnknownException( final String message )
	{
		super( message );
	}

	/**
	 * Constructs a new UnknownDependencyException based on the given Exception
	 *
	 * @param throwable Exception that triggered this Exception
	 */
	public PluginDependencyUnknownException( final Throwable throwable )
	{
		super( throwable );
	}

	/**
	 * Constructs a new UnknownDependencyException based on the given Exception
	 *
	 * @param message   Brief message explaining the cause of the exception
	 * @param throwable Exception that triggered this Exception
	 */
	public PluginDependencyUnknownException( final Throwable throwable, final String message )
	{
		super( message, throwable );
	}
}
