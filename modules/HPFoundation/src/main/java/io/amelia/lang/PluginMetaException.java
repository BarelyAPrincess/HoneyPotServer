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
 * Thrown when attempting to load an invalid PluginDescriptionFile
 */
public class PluginMetaException extends PluginException.Error
{
	/**
	 * Constructs a new InvalidDescriptionException
	 */
	public PluginMetaException()
	{
		super( "Invalid plugin config" );
	}

	/**
	 * Constructs a new InvalidDescriptionException with the given message
	 *
	 * @param message Brief message explaining the cause of the exception
	 */
	public PluginMetaException( final String message )
	{
		super( message );
	}

	/**
	 * Constructs a new InvalidDescriptionException based on the given Exception
	 *
	 * @param cause Exception that triggered this Exception
	 */
	public PluginMetaException( final Throwable cause )
	{
		super( "Invalid plugin.yaml", cause );
	}

	/**
	 * Constructs a new InvalidDescriptionException based on the given Exception
	 *
	 * @param message Brief message explaining the cause of the exception
	 * @param cause   Exception that triggered this Exception
	 */
	public PluginMetaException( final Throwable cause, final String message )
	{
		super( message, cause );
	}
}
