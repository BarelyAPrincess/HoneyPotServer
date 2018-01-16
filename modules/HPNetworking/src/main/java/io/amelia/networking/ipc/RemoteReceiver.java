package io.amelia.networking.ipc;

import io.amelia.foundation.parcel.ParcelCarrier;
import io.amelia.foundation.parcel.ParcelReceiver;
import io.amelia.lang.ParcelableException;
import io.amelia.support.data.Parcel;

public class RemoteReceiver implements ParcelReceiver
{
	@Override
	public void handleParcel( ParcelCarrier parcelCarrier ) throws ParcelableException.Error
	{
		Parcel parcel = Parcel.Factory.serialize( parcelCarrier );

		// TODO Transmit parcel over network to receiver
	}
}
