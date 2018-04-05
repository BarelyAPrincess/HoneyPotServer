package io.amelia.storage.driver;

import java.io.IOException;
import java.util.function.Supplier;
import java.util.stream.Stream;

import io.amelia.lang.StorageException;
import io.amelia.storage.driver.entries.BaseEntry;

public abstract class StorageDriver<Entry extends BaseEntry>
{
	protected final Supplier<Entry> entryMaker;

	public StorageDriver( Supplier<Entry> entryMaker )
	{
		this.entryMaker = entryMaker;
	}

	public abstract Entry getEntry( String localName ) throws StorageException.Error;

	public abstract Stream<Entry> streamEntries( String regexPattern );
}
