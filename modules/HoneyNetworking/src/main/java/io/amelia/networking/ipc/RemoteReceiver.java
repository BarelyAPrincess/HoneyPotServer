/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
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
