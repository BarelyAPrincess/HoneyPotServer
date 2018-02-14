/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.scripting.event;

import io.amelia.foundation.events.AbstractEvent;
import io.amelia.foundation.events.Cancellable;

public class PostEvalEvent extends AbstractEvent implements Cancellable
{
	private boolean cancelled;
	private ScriptingContext context;

	public PostEvalEvent( ScriptingContext context )
	{
		this.context = context;
	}

	public ScriptingContext context()
	{
		return context;
	}

	@Override
	public boolean isCancelled()
	{
		return cancelled;
	}

	@Override
	public void setCancelled( boolean cancelled )
	{
		this.cancelled = cancelled;
	}
}
