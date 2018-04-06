/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.webroot;

import io.amelia.foundation.Kernel;
import io.amelia.storage.driver.IODriver;
import io.amelia.storage.entry.DirectoryEntry;
import io.amelia.storage.entry.BaseEntry;
import io.amelia.storage.methods.HomeDirectoryStorageMethod;

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
