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
