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
