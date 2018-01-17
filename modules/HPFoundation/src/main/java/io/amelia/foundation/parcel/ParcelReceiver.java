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
