package io.amelia.storage.method;

import io.amelia.storage.driver.StorageDriver;
import io.amelia.storage.driver.entries.BaseEntry;

public class StorageMethod<Driver extends StorageDriver, Ability extends BaseEntry>
{
	protected final Driver driver;

	public StorageMethod( Driver driver )
	{
		this.driver = driver;
	}
}
