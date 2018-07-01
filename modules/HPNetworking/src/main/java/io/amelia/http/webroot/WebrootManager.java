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
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.amelia.data.TypeBase;
import io.amelia.filesystem.SQLFileSystemProvider;
import io.amelia.foundation.ConfigMap;
import io.amelia.foundation.ConfigRegistry;
import io.amelia.foundation.Env;
import io.amelia.foundation.Kernel;
import io.amelia.http.mappings.DomainNode;
import io.amelia.http.mappings.DomainTree;
import io.amelia.lang.WebrootException;
import io.amelia.support.IO;
import io.amelia.support.StorageConversions;
import io.amelia.support.Streams;

public class WebrootManager
{
	public static final String PATH_ARCHIVES = "__archives";
	public static final String PATH_WEBROOT = "__webroot";
	private static final List<Webroot> WEBROOTS = new CopyOnWriteArrayList<>();
	public static Kernel.Logger L = Kernel.getLogger( WebrootManager.class );

	static
	{
		WEBROOTS.add( new DefaultWebroot() );

		Kernel.setPath( PATH_ARCHIVES, Kernel.PATH_STORAGE, "archives" );
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

			Streams.forEachWithException( Files.list( path ).filter( directory -> Files.isDirectory( directory ) && Files.isRegularFile( directory.resolve( "config.yaml" ) ) ), directory -> {
				ConfigMap configuration = new ConfigMap();
				StorageConversions.loadToStacker( directory.resolve( "config.yaml" ), configuration );
				Env env = new Env( directory.resolve( ".env" ) );
				WEBROOTS.add( new Webroot( directory, configuration, env ) );
			} );
		}
		catch ( Exception e )
		{
			throw new WebrootException.Runtime( e );
		}
	}

	public static void cleanupBackups( final String webrootId, String suffix, int limit ) throws IOException
	{
		Path webrootArchive = Kernel.getPath( PATH_ARCHIVES ).resolve( webrootId );
		if ( !Files.exists( webrootArchive ) )
			return;
		Stream<Path> result = Files.list( webrootArchive ).filter( path -> path.toString().toLowerCase().endsWith( suffix.toLowerCase() ) );

		// Delete all logs, no archiving!
		if ( limit < 1 )
			Streams.forEachWithException( result, IO::deleteIfExists );
		else
			Streams.forEachWithException( result.sorted( new IO.PathComparatorByCreated() ).limit( limit ), IO::deleteIfExists );
	}

	private static String getDefaultBackend()
	{
		return "file";
	}

	public static Webroot getDefaultWebroot()
	{
		return getWebrootById( "default" );
	}

	public static DomainNode getDomain( String fullDomain )
	{
		return DomainTree.parseDomain( fullDomain );
	}

	public static Stream<DomainNode> getDomainsByWebroot( Webroot webroot )
	{
		return DomainTree.getChildren().filter( n -> n.getWebroot() == webroot );
	}

	public static Webroot getWebrootById( @Nonnull String id )
	{
		return null;
	}

	static void reload()
	{
		unload();
		// Reload
	}

	static void unload()
	{
		for ( Webroot webroot : WEBROOTS )
			try
			{
				webroot.save();
			}
			catch ( IOException e )
			{
				L.severe( e );
			}
		WEBROOTS.clear();
	}

	public static class Config
	{
		public static final TypeBase WEBROOTS = new TypeBase( ConfigRegistry.Config.APPLICATION_BASE, "webroots" );
		public static final TypeBase.TypeString WEBROOTS_DEFAULT_TITLE = new TypeBase.TypeString( WEBROOTS, "defaultTitle", "Unnamed Webroot" );

		private Config()
		{
			// Static Access
		}
	}
}
