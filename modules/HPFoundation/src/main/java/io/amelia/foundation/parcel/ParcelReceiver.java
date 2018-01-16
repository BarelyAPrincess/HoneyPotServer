package io.amelia.foundation.parcel;

import io.amelia.lang.ParcelException;

public interface ParcelReceiver
{
	void handleParcel( ParcelCarrier parcelCarrier ) throws ParcelException.Error;
}
