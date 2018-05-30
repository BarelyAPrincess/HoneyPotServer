/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <theameliadewitt@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.http.webroot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import io.amelia.foundation.Kernel;
import io.amelia.support.IO;
import io.amelia.support.Streams;

public class WebrootUtils
{
	public static void cleanupBackups( final String siteId, final String suffix, int limit )
	{
		try
		{
			Path dir = Kernel.getPath( BaseWebroot.PATH_ARCHIVE ).resolve( siteId );
			if ( !Files.isDirectory( dir ) )
				return;
			Stream<Path> files = Files.list( dir ).filter( path -> Files.isRegularFile( path ) && path.endsWith( suffix ) );

			// Delete all logs, no archiving!
			if ( limit < 1 )
			{
				Streams.forEachWithException( files, IO::deleteIfExists );
				return;
			}

			Streams.forEachWithException( files.sorted( new IO.PathComparatorByCreated( false ) ).skip( limit ), IO::deleteIfExists );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
			// Ignore
		}
	}

	public static Path createWebrootDirectory( String webrootId )
	{
		return Kernel.getPath( WebrootManager.PATH_WEBROOT ).resolve( webrootId );
	}

	private WebrootUtils()
	{
		// Static Access
	}
}
