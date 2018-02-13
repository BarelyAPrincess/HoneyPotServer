/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.lang;

/**
 * Provides a callback to when a registered exception is thrown
 */
@FunctionalInterface
public interface ExceptionCallback
{
	/**
	 * Called for each registered Exception Callback for handling.
	 *
	 * @param cause   The thrown exception
	 * @param context The thrown context
	 * @return The resulting ErrorReporting level. Returning NULL will, if possible, try the next best matching EvalCallback
	 */
	ReportingLevel callback( Throwable cause, ExceptionReport report, ExceptionRegistrar context );
}
