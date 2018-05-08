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

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Nonnull;

import io.amelia.filesystem.sql.SQLFileSystemProvider;
import io.amelia.foundation.Kernel;
import io.amelia.lang.StorageException;
import io.amelia.lang.WebrootException;
import io.amelia.support.IO;
import io.amelia.support.StoragePolicy;

public class WebrootManager
{
	public static final String PATH_WEBROOT = "__webroot";
	private static final List<Webroot> WEBROOTS = new CopyOnWriteArrayList<>();
	public static Kernel.Logger L = Kernel.getLogger( WebrootManager.class );

	static
	{
		WEBROOTS.add( new DefaultWebroot() );

		Kernel.setPath( PATH_WEBROOT, Kernel.PATH_STORAGE, "webroot" );

		FileSystem backend;
		Path path;
		switch ( getDefaultBackend() )
		{
			case "file":
				backend = FileSystems.getDefault();
				path = Kernel.getPath( PATH_WEBROOT );
				break;
			case "sql":
				backend = SQLFileSystemProvider.newFileSystem();
				path = backend.getPath( "/" );
				break;
			default:
				throw new WebrootException.Runtime( "WebrootManager has no set FileSystem backend." );
		}

		try
		{
			IO.forceCreateDirectory( path );



			Files.list( path ).filter( p -> Files.isDirectory( p ) && Files.isRegularFile( p.resolve( "config.yaml" ) ) ).forEach( p -> {
				if ( Files.isDirectory( p ) )
				{
					try
					{
						policy.enforcePolicy( p );

						WEBROOTS.add( new Webroot( p ) );
					}
					catch ( StorageException.Error error )
					{
						error.printStackTrace();
					}
				}
			} );
		}
		catch ( IOException e )
		{
			throw new WebrootException.Runtime( e );
		}
	}

	private static String getDefaultBackend()
	{
		return "file";
	}

	public static Webroot getDefaultWebroot()
	{
		return getWebrootById( "default" );
	}

	private static Webroot getWebrootById( @Nonnull String id )
	{
		return null;
	}
}
