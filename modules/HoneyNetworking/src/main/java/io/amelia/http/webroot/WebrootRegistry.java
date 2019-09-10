/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2019 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2019 Penoaks Publishing LLC <development@penoaks.com>
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
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.amelia.data.TypeBase;
import io.amelia.foundation.ConfigData;
import io.amelia.foundation.ConfigRegistry;
import io.amelia.foundation.Env;
import io.amelia.foundation.Kernel;
import io.amelia.lang.ApplicationException;
import io.amelia.lang.ExceptionReport;
import io.amelia.lang.WebrootException;
import io.amelia.storage.HoneyStorageProvider;
import io.amelia.support.IO;
import io.amelia.support.StorageConversions;
import io.amelia.support.Streams;

public class WebrootRegistry
{
	public static final String PATH_ARCHIVES = "__archives";
	public static final String PATH_WEBROOT = "__webroot";
	private static final List<Webroot> WEBROOTS = new CopyOnWriteArrayList<>();
	public static Kernel.Logger L = Kernel.getLogger( WebrootRegistry.class );

	static
	{
		try
		{
			WEBROOTS.add( new DefaultWebroot() );
		}
		catch ( WebrootException.Error e )
		{
			ExceptionReport.handleSingleException( e );
		}

		Kernel.setPath( PATH_ARCHIVES, Kernel.PATH_STORAGE, "archives" );
		Kernel.setPath( PATH_WEBROOT, Kernel.PATH_STORAGE, "webroot" );

		FileSystem backend;
		Path path;
		switch ( getDefaultBackend() )
		{
			case FILE:
				backend = FileSystems.getDefault();
				path = Kernel.getPath( PATH_WEBROOT );
				break;
			case SQL:
				backend = HoneyStorageProvider.newFileSystem();
				path = backend.getPath( "/" );
				break;
			default:
				throw new WebrootException.Runtime( "The webroot backend is not set." );
		}

		try
		{
			IO.forceCreateDirectory( path );

			Streams.forEachWithException( Files.list( path ).filter( directory -> Files.isDirectory( directory ) && Files.isRegularFile( directory.resolve( "config.yaml" ) ) ), directory -> {
				ConfigData data = ConfigData.empty();
				StorageConversions.loadToStacker( directory.resolve( "config.yaml" ), data );
				Env env = new Env( directory.resolve( ".env" ) );
				WEBROOTS.add( new Webroot( directory, data, env ) );
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

	private static Backend getDefaultBackend()
	{
		return Backend.FILE;
	}

	public static Webroot getDefaultWebroot()
	{
		return getWebrootById( "default" ).orElseThrow( ApplicationException.Runtime::new );
	}

	public static Optional<Webroot> getWebrootById( @Nonnull String id )
	{
		return WEBROOTS.stream().filter( webroot -> id.equalsIgnoreCase( webroot.getWebrootId() ) ).findFirst();
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

	public enum Backend
	{
		FILE,
		SQL
	}

	public static class Config
	{
		public static final TypeBase WEBROOTS = new TypeBase( ConfigRegistry.ConfigKeys.APPLICATION_BASE, "webroots" );
		public static final TypeBase.TypeString WEBROOTS_DEFAULT_TITLE = new TypeBase.TypeString( WEBROOTS, "defaultTitle", "Unnamed Webroot" );
		public static final TypeBase.TypeString WEBROOTS_ALLOW_ORIGIN = new TypeBase.TypeString( WEBROOTS, "web-allowed-origin", "*" );

		private Config()
		{
			// Static Access
		}
	}
}
