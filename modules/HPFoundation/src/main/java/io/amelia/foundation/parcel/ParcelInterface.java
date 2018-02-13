/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation.parcel;

/**
 * Represents a point of contact for each {@link ApplicationRegistry.ApplicationRegistration}.
 * e.g., Network Connection (remote) or Application instance (local).
 */
public interface ParcelInterface
{
	/**
	 * Does this represent a channel that is remote from this JVM instance, e.g, over network?
	 *
	 * @return True if so, otherwise false.
	 */
	default boolean isRemote()
	{
		return true;
	}

	void sendToAll( ParcelCarrier parcel );
}
