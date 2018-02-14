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

import javax.annotation.Nonnull;

/**
 * Used by the internal HoneyPotLibrary carry context information about an exception to the {@link io.amelia.foundation.Kernel}.
 * You can either wrap the target exception with a subclass implementing this interface or implement this interface directly on
 * any subclass of {@link Throwable} (recommended).
 */
public interface ExceptionContext
{
	/**
	 * Returns the message associated with this {@link ExceptionContext}
	 *
	 * @return The string message.
	 */
	default String getMessage()
	{
		return getThrowable().getMessage();
	}

	/**
	 * Returns the {@link ReportingLevel} for this {@link ExceptionContext}
	 *
	 * @return The {@link ReportingLevel}
	 */
	ReportingLevel getReportingLevel();

	/**
	 * Returns the {@link Throwable} associated with this {@link ExceptionContext} or
	 * itself if this interface is implemented on an subclass of {@link Throwable}
	 *
	 * @return The associated throwable.
	 */
	@Nonnull
	Throwable getThrowable();

	/**
	 * Called to properly add exception information to the ExceptionReport which is then used to generate a script trace or {@link ApplicationCrashReport}
	 * <p/>
	 * Typically you would just add your exception to the report with {@code report.addException( this );} and provide some possible unique debug information, if any exists.
	 *
	 * @param report    The ExceptionReport to fill
	 * @param registrar The Exception Context
	 *
	 * @return The reporting level produced by this handler, returning {@link null} will cause the application to conclude it's own handling procedure, which is essentially the same as returning {@link ReportingLevel#E_UNHANDLED}.
	 */
	default ReportingLevel handle( ExceptionReport report, ExceptionRegistrar registrar )
	{
		return null;
	}

	default boolean isIgnorable()
	{
		return getReportingLevel().isIgnorable();
	}

	default void printStackTrace()
	{
		getThrowable().printStackTrace();
	}
}
