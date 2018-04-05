package io.amelia.storage.driver.entries;

import io.amelia.support.data.Parcel;
import io.amelia.support.data.ParcelLoader;

public class YamlEntry extends StringEntry
{
	@Override
	public Parcel readToParcel()
	{
		return ParcelLoader.decodeYaml( data );
	}
}
