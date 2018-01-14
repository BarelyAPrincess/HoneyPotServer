/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.support;

public enum Runlevel
{
	/**
	 * Indicates the application has not done anything. Variable initialization has only occurred.
	 */
	INITIALIZATION,
	/**
	 * Indicates the application has started all managers and dispatchers then loads plugins.
	 */
	STARTUP,
	/**
	 * Indicates the application has started the main loop. Expect heartbeat and task execution.
	 */
	MAINLOOP,
	/**
	 * Indicates the application has started the networking (UDP in particular) and is waiting on the cluster.
	 */
	NETWORKING,
	/**
	 * Indicates the application has started.
	 */
	STARTED,
	/**
	 * Indicates the application is reloading.
	 * TODO Not Implemented - Needs to disconnect from cluster, unload managers and dispatchers, then reload everything.
	 */
	RELOAD,
	/**
	 * Indicates the application is preparing to shutdown. Best point to get the most critical shutdown logic done before managers, dispatchers, and plugins are disposed of.
	 */
	SHUTDOWN,
	/**
	 * Indicates the application has been shutdown. The main loop will cease execution here. It's like no plugins will ever see this Runlevel as they have been unloaded.
	 */
	DISPOSED,
	/**
	 * Indicates the application has CRASHED. Similar to DISPOSED with the exception that Plugins might see this one depending on the exception source.
	 * A crash report will soon be generated and dispatched to the server admin.
	 */
	CRASHED;

	/**
	 * Checks the RunLevel was called in proper order.
	 *
	 * @param currentRunlevel The current Runlevel
	 * @return Was the Runlevel called in proper level
	 */
	public boolean checkRunlevelOrder( Runlevel currentRunlevel )
	{
		switch ( this )
		{
			case INITIALIZATION:
				return false;
			case STARTUP:
				return currentRunlevel == INITIALIZATION || currentRunlevel == RELOAD;
			case MAINLOOP:
				return currentRunlevel == STARTUP;
			case NETWORKING:
				return currentRunlevel == MAINLOOP;
			case STARTED:
				return currentRunlevel == NETWORKING;
			case RELOAD:
			case SHUTDOWN:
				return currentRunlevel == STARTED;
			case DISPOSED:
				return currentRunlevel == SHUTDOWN;
		}

		return true;
	}
}
