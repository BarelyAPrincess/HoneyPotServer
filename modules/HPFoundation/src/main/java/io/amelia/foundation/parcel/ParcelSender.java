package io.amelia.foundation.parcel;

public interface ParcelSender
{
	/**
	 * Used to reply to a parcel sent from this {@link ParcelSender}
	 * However, it's common for the ability to receive to not exist.
	 */
	default ParcelReceiver getReplyTo()
	{
		return null;
	}
}
