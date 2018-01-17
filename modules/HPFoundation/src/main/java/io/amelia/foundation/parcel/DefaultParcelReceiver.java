package io.amelia.foundation.parcel;

import java.util.function.Consumer;

import io.amelia.lang.ParcelException;

public class DefaultParcelReceiver implements ParcelReceiver
{
	private final Consumer<ParcelCarrier> callback;

	public DefaultParcelReceiver( Consumer<ParcelCarrier> callback )
	{
		this.callback = callback;
	}

	@Override
	public void handleParcel( ParcelCarrier parcelCarrier ) throws ParcelException.Error
	{
		this.callback.accept( parcelCarrier );
	}
}
