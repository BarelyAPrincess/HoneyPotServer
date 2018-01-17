package io.amelia.networking.ipc;

import io.amelia.foundation.parcel.ParcelCarrier;
import io.amelia.foundation.parcel.ParcelReceiver;
import io.amelia.lang.ParcelException;

public class RemoteReceiver implements ParcelReceiver
{
	@Override
	public void handleParcel( ParcelCarrier parcelCarrier ) throws ParcelException.Error
	{
		// Parcel parcel = Parcel.Factory.serialize( parcelCarrier );

		// TODO Transmit parcel over network to receiver
	}
}
