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
 * Thrown when attempting to load an invalid PluginDescriptionFile
 */
public class PluginInformationException extends PluginException
{
	private static final long serialVersionUID = 5721389122281775896L;
	
	/**
	 * Constructs a new InvalidDescriptionException
	 */
	public PluginInformationException()
	{
		super( "Invalid plugin.yaml" );
	}
	
	/**
	 * Constructs a new InvalidDescriptionException with the given message
	 * 
	 * @param message
	 *            Brief message explaining the cause of the exception
	 */
	public PluginInformationException( final String message )
	{
		super( message );
	}
	
	/**
	 * Constructs a new InvalidDescriptionException based on the given Exception
	 * 
	 * @param cause
	 *            Exception that triggered this Exception
	 */
	public PluginInformationException( final Throwable cause )
	{
		super( "Invalid plugin.yaml", cause );
	}
	
	/**
	 * Constructs a new InvalidDescriptionException based on the given Exception
	 * 
	 * @param message
	 *            Brief message explaining the cause of the exception
	 * @param cause
	 *            Exception that triggered this Exception
	 */
	public PluginInformationException( final Throwable cause, final String message )
	{
		super( message, cause );
	}
}
