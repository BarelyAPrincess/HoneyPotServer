package io.amelia.foundation;

import io.amelia.lang.ApplicationException;
import io.amelia.lang.Runlevel;

public class MinimalApplication extends ApplicationInterface
{
	@Override
	public void onRunlevelChange( Runlevel previousRunlevel, Runlevel currentRunlevel ) throws ApplicationException
	{

	}

	@Override
	public void onTick( int currentTick, float averageTick ) throws ApplicationException
	{

	}
}
