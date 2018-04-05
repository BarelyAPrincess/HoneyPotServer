package io.amelia.storage.driver.entries;

import io.amelia.support.data.Parcel;
import io.amelia.support.data.ParcelLoader;

public class PropEntry extends StringEntry
{
	@Override
	public Parcel readToParcel()
	{
		return ParcelLoader.decodeProp( data );
	}
}
