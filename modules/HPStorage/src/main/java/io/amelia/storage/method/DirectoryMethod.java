package io.amelia.storage.method;

import io.amelia.lang.StorageException;
import io.amelia.storage.driver.StorageDriver;
import io.amelia.storage.driver.entries.BaseEntry;

public class DirectoryMethod<Driver extends StorageDriver<Entry>, Entry extends BaseEntry> extends StorageMethod<Driver, Entry>
{
	public DirectoryMethod( Driver driver )
	{
		super( driver );
	}

	public Entry getEntry( String localName ) throws StorageException.Error
	{
		return driver.getEntry( localName );
	}
}
