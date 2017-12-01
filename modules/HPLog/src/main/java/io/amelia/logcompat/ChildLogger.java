/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.logcompat;

import io.amelia.foundation.ConfigRegistry;

import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class ChildLogger extends Logger
{
	protected ChildLogger( String id )
	{
		super( id, null );
	}

	@Override
	public void log( LogRecord logRecord )
	{
		if ( ConfigRegistry.i().isConfigLoaded() && !ConfigRegistry.i().getBoolean( "console.hideLoggerName" ) )
			logRecord.setMessage( "&7[" + getName() + "]&f " + logRecord.getMessage() );

		super.log( logRecord );
	}
}
