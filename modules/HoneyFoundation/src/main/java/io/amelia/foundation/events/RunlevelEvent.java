/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation.events;

import io.amelia.support.Runlevel;

public class RunlevelEvent extends ApplicationEvent
{
	private final Runlevel currentRunlevel;
	private final Runlevel previousRunlevel;

	public RunlevelEvent( Runlevel previousRunlevel, Runlevel currentRunlevel )
	{
		this.previousRunlevel = previousRunlevel;
		this.currentRunlevel = currentRunlevel;
	}

	public Runlevel getLastRunLevel()
	{
		return previousRunlevel;
	}

	public Runlevel getRunLevel()
	{
		return currentRunlevel;
	}
}
