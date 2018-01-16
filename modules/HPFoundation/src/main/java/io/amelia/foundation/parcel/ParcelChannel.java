package io.amelia.foundation.parcel;

/**
 * Represents a point of contact for each {@link ApplicationRegistry.ApplicationRegistration}.
 * e.g., Network Connection (remote) or Application instance (local).
 */
public interface ParcelChannel
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
