/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.http.webroot;

import io.amelia.foundation.Kernel;
import io.amelia.storage.StorageContainerPolicy;
import io.amelia.storage.file.FileStorageBackend;
import io.amelia.storage.methods.HomeContainerMethod;

public class WebrootManager
{
	public static final String PATH_WEBROOT = "__webroot";
	public static Kernel.Logger L = Kernel.getLogger( WebrootManager.class );

	static
	{
		Kernel.setPath( PATH_WEBROOT, Kernel.PATH_STORAGE, "webroot" );

		FileStorageBackend driver = new FileStorageBackend( Kernel.getPath( PATH_WEBROOT ) );

		StorageContainerPolicy policy = new StorageContainerPolicy();

		// Language Files
		policy.setLayoutContainer( "lang", StorageContainerPolicy.Strategy.CREATE );
		// Public Files
		policy.setLayoutContainer( "public", StorageContainerPolicy.Strategy.OPTIONAL );
		// Resources
		policy.setLayoutContainer( "resource", StorageContainerPolicy.Strategy.CREATE );
		// SSL Certificates and Keys
		policy.setLayoutContainer( "ssl", StorageContainerPolicy.Strategy.CREATE );
		// .env
		policy.setLayoutObject( ".env", StorageContainerPolicy.Strategy.CREATE );
		// config.parcel
		policy.setLayoutObject( "config.yaml", StorageContainerPolicy.Strategy.CREATE );
		// .htaccess
		policy.setLayoutObject( "htaccess.json", StorageContainerPolicy.Strategy.CREATE );
		// Routes file
		policy.setLayoutObject( "routes.json", StorageContainerPolicy.Strategy.CREATE );

		new HomeContainerMethod( policy ).getEntries( driver, "(.*)(?:\\\\|\\/)config.yaml" );
	}
}
