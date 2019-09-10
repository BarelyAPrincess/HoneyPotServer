package com.marchnetworks.common.spring;

import io.amelia.foundation.Foundation;
import io.amelia.lang.ApplicationException;

/**
 * This class is a complete rewrite from the original to make March Command code compatible with Honey Pot Server
 */
public class ApplicationContextSupport
{
	public static Object getBean( String name )
	{
		try
		{
			return Foundation.make( name );
		}
		catch ( ApplicationException.Error e )
		{
			throw new ApplicationException.Runtime( e );
		}
	}
}
