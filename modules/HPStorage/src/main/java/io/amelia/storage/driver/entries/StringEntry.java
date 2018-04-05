package io.amelia.storage.driver.entries;

import io.amelia.support.data.Parcel;

public abstract class StringEntry extends BaseEntry
{
	public String data;

	public abstract Parcel readToParcel();
}
