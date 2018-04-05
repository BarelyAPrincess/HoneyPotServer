package io.amelia.webroot;

import io.amelia.foundation.Kernel;
import io.amelia.storage.driver.IODriver;
import io.amelia.storage.driver.entries.DirectoryEntry;
import io.amelia.storage.driver.entries.BaseEntry;
import io.amelia.storage.method.HomeDirectoryStorageMethod;

public class WebrootManager
{
	public static final String PATH_WEBROOT = "__webroot";
	private static volatile HomeDirectoryStorageMethod storage;

	static
	{
		Kernel.setPath( PATH_WEBROOT, Kernel.PATH_STORAGE, "webroot" );

		IODriver<DirectoryEntry> driver = new IODriver<>( Kernel.getPath( PATH_WEBROOT ), DirectoryEntry::new );
		storage = new HomeDirectoryStorageMethod<IODriver<BaseEntry>>( driver, "(.*)(?:\\\\|\\/)config.yaml" );
	}
}
