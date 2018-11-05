/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.networking.packets;

import io.amelia.support.data.Parcel;

public abstract class DataPacket extends RawPacket
{
	private Parcel inbound = new Parcel();
	private Parcel outbound = new Parcel();



}
