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

public interface IException
{
	static void check( IException t )
	{
		if ( !( t instanceof Throwable ) )
			throw new IllegalArgumentException( "IException interface can only be implemented by class that also extent java.lang.Throwable, this is a serious programming bug!" );
	}

	Throwable getCause();

	ReportingLevel reportingLevel();

	String getMessage();

	/**
	 * Called to properly add exception information to the ExceptionReport which is then used to generate a script trace or {@link ApplicationCrashReport}
	 * <p/>
	 * Typically you would just add your exception to the report with {@code report.addException( this );} and provide some possible unique debug information, if any exists.
	 *
	 * @param report  The ExceptionReport to fill
	 * @param context The Exception Context
	 * @return The reporting level produced by this handler, returning null will cause the application to conclude it's own handling procedure, which is essentially the same as returning {@link ReportingLevel#E_UNHANDLED}.
	 */
	ReportingLevel handle( ExceptionReport report, ExceptionContext context );

	boolean isIgnorable();

	void printStackTrace();
}
