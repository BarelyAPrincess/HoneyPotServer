/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.events.application;

import com.chiorichan.AppController;
import com.chiorichan.event.EventDispatcher;
import com.chiorichan.event.EventException;
import io.amelia.lang.RunLevel;
import io.amelia.logging.LogBuilder;

public class RunlevelEvent extends ApplicationEvent
{
	protected static RunLevel previousLevel;
	protected static RunLevel currentLevel;

	public RunlevelEvent()
	{
		currentLevel = RunLevel.INITIALIZATION;
	}

	public RunlevelEvent( RunLevel level )
	{
		currentLevel = level;
	}

	public RunLevel getLastRunLevel()
	{
		return previousLevel;
	}

	public RunLevel getRunLevel()
	{
		return currentLevel;
	}

	public void setRunLevel( RunLevel level )
	{
		previousLevel = currentLevel;
		currentLevel = level;

		LogBuilder.get().fine( "Application Runlevel has been changed to '" + level.name() + "'" );

		try
		{
			EventDispatcher.i().callEventWithException( this );
		}
		catch ( EventException e )
		{
			AppController.handleExceptions( e );
		}
	}
}
