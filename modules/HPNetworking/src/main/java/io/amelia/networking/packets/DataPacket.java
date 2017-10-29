package io.amelia.networking.packets;

import io.amelia.support.data.Parcel;

public abstract class DataPacket extends RawPacket
{
	private Parcel inbound = new Parcel();
	private Parcel outbound = new Parcel();



}
