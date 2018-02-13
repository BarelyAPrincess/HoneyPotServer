/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.support;

public enum Runlevel
{
	/**
	 * Indicates the application has not done anything. Variable initialization has only occurred.
	 */
	INITIALIZATION( 200 ),
	/**
	 * Indicates the application has started all managers and dispatchers then loads plugins.
	 */
	STARTUP( 400 ),
	/**
	 * Indicates the application has started the main loop. Expect heartbeat and task execution.
	 */
	MAINLOOP( 600 ),
	/**
	 * Indicates the application has started the networking (UDP in particular) and is waiting on the cluster.
	 */
	NETWORKING( 800 ),
	/**
	 * Indicates the application has started.
	 */
	STARTED( 1000 ),
	/**
	 * Indicates the application is reloading.
	 * TODO Not Implemented - Needs to disconnect from cluster, unload managers and dispatchers, then reload everything.
	 */
	RELOAD( 400 ),
	/**
	 * Indicates the application is preparing to shutdown. Best point to get the most critical shutdown logic done before managers, dispatchers, and plugins are disposed of.
	 */
	SHUTDOWN( 100 ),
	/**
	 * Indicates the application has been shutdown. The main loop will cease execution here. It's like no plugins will ever see this Runlevel as they have been unloaded.
	 */
	DISPOSED( 0 ),
	/**
	 * Indicates the application has CRASHED. Similar to DISPOSED with the exception that Plugins might see this one depending on the exception source.
	 * A crash report will soon be generated and dispatched to the server admin.
	 */
	CRASHED( 0 );

	private final int intValue;

	Runlevel( int intValue )
	{
		this.intValue = intValue;
	}

	/**
	 * Checks the RunLevel was called in proper order.
	 *
	 * @param currentRunlevel The current Runlevel
	 *
	 * @return Was the Runlevel called in proper level
	 */
	public boolean checkRunlevelOrder( Runlevel currentRunlevel )
	{
		switch ( this )
		{
			case INITIALIZATION:
				return false;
			case STARTUP:
				return currentRunlevel == INITIALIZATION;
			case MAINLOOP:
				return currentRunlevel == STARTUP || currentRunlevel == RELOAD;
			case NETWORKING:
				return currentRunlevel == MAINLOOP;
			case STARTED:
				// Going from MAINLOOP to STARTUP is only acceptable if application implements NetworkedApplication
				return currentRunlevel == NETWORKING || currentRunlevel == MAINLOOP;
			case RELOAD:
			case SHUTDOWN:
				return currentRunlevel == STARTED;
			case DISPOSED:
				return currentRunlevel == SHUTDOWN;
		}

		return true;
	}

	public int intValue()
	{
		return intValue;
	}
}
