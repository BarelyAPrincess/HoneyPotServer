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

import io.amelia.lang.ParcelException;
import io.amelia.looper.queue.AbstractEntry;

/**
 * When you have a parcel or signal to be transmitted to another part of the application,
 * you first need to find the {@link ParcelReceiver} intended for handling the parcel.
 * Receivers are a dime-a-dozen, they are instigated on their own or they are automatically
 * made available from sub-systems or plugins.
 */
public interface ParcelReceiver
{
	default String getId()
	{
		return null;
	}

	void handleParcel( ParcelCarrier parcelCarrier ) throws ParcelException.Error;

	/**
	 * Indicates if async loopers and tasks can execute this receiver asynchronously.
	 * Trumps both {@link io.amelia.looper.AbstractLooper#isAsync()} and {@link AbstractEntry#isAsync()}.
	 *
	 * @return True if so, false otherwise.
	 */
	default boolean isAsyncAllowed()
	{
		return true;
	}
}
