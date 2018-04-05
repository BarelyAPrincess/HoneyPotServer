package io.amelia.storage.method;

import java.util.List;
import java.util.stream.Collectors;

import io.amelia.storage.driver.StorageDriver;
import io.amelia.storage.driver.entries.DirectoryEntry;

public class HomeDirectoryStorageMethod<Driver extends StorageDriver<DirectoryEntry>> extends StorageMethod<Driver, DirectoryEntry>
{
	private List<DirectoryEntry> entries;

	public HomeDirectoryStorageMethod( Driver driver, String regexPattern )
	{
		super( driver );
		entries = driver.streamEntries( regexPattern ).collect( Collectors.toList() );
	}
}
