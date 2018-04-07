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
import io.amelia.storage.file.FileStorageDriver;
import io.amelia.storage.methods.HomeContainerMethod;

public class WebrootManager
{
	public static final String PATH_WEBROOT = "__webroot";

	static
	{
		Kernel.setPath( PATH_WEBROOT, Kernel.PATH_STORAGE, "webroot" );

		FileStorageDriver driver = new FileStorageDriver( Kernel.getPath( PATH_WEBROOT ) );

		new HomeContainerMethod().getEntries( driver, "(.*)(?:\\\\|\\/)config.yaml" );
	}
}
