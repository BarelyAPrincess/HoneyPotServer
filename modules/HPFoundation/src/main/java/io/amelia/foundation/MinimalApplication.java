package io.amelia.foundation;

import io.amelia.lang.ApplicationException;
import io.amelia.lang.ExceptionReport;
import io.amelia.lang.Runlevel;

public class MinimalApplication extends ApplicationInterface
{
	@Override
	public void fatalError( ExceptionReport report, boolean crashOnError )
	{

	}

	@Override
	public void onRunlevelChange( Runlevel previousRunlevel, Runlevel currentRunlevel ) throws ApplicationException
	{

	}
}
