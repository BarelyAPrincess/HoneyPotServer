/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation.events;

/**
 * Represents an event's priority in execution
 */
public enum EventPriority
{
	/**
	 * Event call is of very low importance and should be ran first, to allow other plugins to further customize the
	 * outcome
	 */
	LOWEST( 0 ),
	/**
	 * Event call is of low importance
	 */
	LOW( 1 ),
	/**
	 * Event call is neither important or unimportant, and may be ran normally
	 */
	NORMAL( 2 ),
	/**
	 * Event call is of high importance
	 */
	HIGH( 3 ),
	/**
	 * Event call is critical and must have the final say in what happens to the event
	 */
	HIGHEST( 4 ),
	/**
	 * Event is listened to purely for monitoring the outcome of an event.
	 * <p/>
	 * No modifications to the event should be made under this priority
	 */
	MONITOR( 5 );
	
	private final int slot;
	
	EventPriority( int slot )
	{
		this.slot = slot;
	}
	
	public int getSlot()
	{
		return slot;
	}
}
