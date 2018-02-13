/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.logging;

import io.amelia.foundation.Kernel;
import io.amelia.logcompat.LogBuilder;
import io.amelia.logcompat.Logger;

public class Log
{
	private static Logger L = LogBuilder.get( Kernel.class );

	public static void info( String tag, String s )
	{
		L.info( s );
	}
}
