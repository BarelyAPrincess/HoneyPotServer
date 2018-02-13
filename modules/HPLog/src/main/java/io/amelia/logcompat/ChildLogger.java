/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.logcompat;

import java.util.logging.LogRecord;
import java.util.logging.Logger;

import io.amelia.foundation.ConfigRegistry;

public class ChildLogger extends Logger
{
	protected ChildLogger( String id )
	{
		super( id, null );
	}

	@Override
	public void log( LogRecord logRecord )
	{
		if ( ConfigRegistry.isConfigLoaded() && !ConfigRegistry.config.getBoolean( "console.hideLoggerName" ).orElse( false ) )
			logRecord.setMessage( "&7[" + getName() + "]&f " + logRecord.getMessage() );

		super.log( logRecord );
	}
}
