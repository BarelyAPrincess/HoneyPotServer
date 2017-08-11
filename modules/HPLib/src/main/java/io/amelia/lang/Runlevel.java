/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.lang;

public enum Runlevel
{
	/**
	 * Indicates the application has not done anything YET!
	 */
	INITIALIZATION,
	/**
	 * Indicates the application has begun startup procedures
	 */
	INITIALIZED,
	/**
	 * Indicates the application has completed all required startup procedures and started the main thread tick
	 */
	STARTUP,
	/**
	 * Indicates the application has started all and any networking
	 */
	POSTSTARTUP,
	/**
	 * Indicates the application is now ready to handle the main application loop
	 */
	RUNNING,
	/**
	 * Indicates the application is reloading
	 */
	RELOAD,
	/**
	 * Indicates the application is shutting down but the final state could be either CRASHED or DISPOSED
	 */
	SHUTDOWN,
	/**
	 * Indicates the application is preparing to shutdown
	 */
	DISPOSED,
	/**
	 * Indicates the application has crashed
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
			case INITIALIZED:
				return currentRunlevel == INITIALIZATION;
			case STARTUP:
				return currentRunlevel == INITIALIZED;
			case POSTSTARTUP:
				return currentRunlevel == STARTUP;
			case RUNNING:
				return currentRunlevel == POSTSTARTUP;
			case RELOAD:
			case SHUTDOWN:
				return currentRunlevel == RUNNING;
			case DISPOSED:
				return currentRunlevel == SHUTDOWN;
		}

		return true;
	}
}
