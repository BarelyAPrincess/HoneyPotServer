/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.http.localization;

import io.amelia.lang.ApplicationException;
import io.amelia.lang.ReportingLevel;

public class LocalizationException extends ApplicationException.Error
{
	public LocalizationException( String message )
	{
		super( ReportingLevel.E_WARNING, message );
	}

	public LocalizationException( String message, Throwable cause )
	{
		super( ReportingLevel.E_WARNING, message, cause );
	}
}
