/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.permission.backend.file;

import io.amelia.permission.PermissionGuard;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FileConfig extends YamlConfiguration
{
	protected File file;

	public FileConfig( File file )
	{
		super();

		this.file = file;

		reload();
	}

	public File getFile()
	{
		return file;
	}

	public void reload()
	{

		try
		{
			this.load( file );
		}
		catch ( FileNotFoundException e )
		{
			// do nothing
		}
		catch ( Throwable e )
		{
			throw new IllegalStateException( "Error loading permissions file", e );
		}
	}

	public void save()
	{
		try
		{
			this.save( file );
		}
		catch ( IOException e )
		{
			PermissionGuard.L.severe( "Error during saving permissions file: " + e.getMessage() );
		}
	}
}
