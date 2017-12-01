/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.foundation.injection;

import com.chiorichan.utils.UtilHttp;
import com.chiorichan.utils.UtilIO;
import io.amelia.foundation.ConfigRegistry;
import io.amelia.foundation.Kernel;
import io.amelia.lang.EnumColor;
import io.amelia.lang.ReportingLevel;
import io.amelia.lang.UncaughtException;
import io.amelia.support.IO;
import io.amelia.logcompat.LogBuilder;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Used as a helper class for retrieving files from the central maven repository
 */
public class Libraries implements LibrarySource
{
	public static final String BASE_MAVEN_URL = "http://jcenter.bintray.com/";
	public static final String BASE_MAVEN_URL_ALT = "http://search.maven.org/remotecontent?filepath=";
	public static final Path INCLUDES_DIR;
	public static final Path LIBRARY_DIR;
	public static final Libraries SELF = new Libraries();
	private static final String LOGNAME = "Libs";
	public static Map<String, MavenReference> loadedLibraries = new HashMap<>();

	static
	{


		LIBRARY_DIR = Kernel.isDeployment() ? new File( "libraries" ) : ConfigRegistry.i().getDirectory( "lib", "libraries" );

		INCLUDES_DIR = new File( LIBRARY_DIR, "local" );

		if ( !IO.setDirectoryAccess( LIBRARY_DIR ) )
			throw new UncaughtException( ReportingLevel.E_ERROR, "This application experienced a problem setting read and write access to directory \"" + IO.relPath( LIBRARY_DIR ) + "\"!" );

		if ( !IO.setDirectoryAccess( INCLUDES_DIR ) )
			throw new UncaughtException( ReportingLevel.E_ERROR, "This application experienced a problem setting read and write access to directory \"" + IO.relPath( INCLUDES_DIR ) + "\"!" );

		Arrays.stream( INCLUDES_DIR.listFiles() ).filter(  )

		// Scans the 'libraries/local' directory for jar files that can be injected into the classpath
		FilenameFilter ff = new FilenameFilter()
		{
			@Override
			public boolean accept( File dir, String name )
			{
				return true;
				// return name != null && name.toLowerCase().endsWith( "jar" );
			}
		};
		for ( File f : INCLUDES_DIR.listFiles( ff ) )
			loadLibrary( f );
	}

	public static File getLibraryDir()
	{
		return LIBRARY_DIR;
	}

	public static List<MavenReference> getLoadedLibraries()
	{
		return new ArrayList<>( loadedLibraries.values() );
	}

	public static List<MavenReference> getLoadedLibrariesBySource( LibrarySource source )
	{
		List<MavenReference> references = new ArrayList<>();

		for ( MavenReference ref : loadedLibraries.values() )
			if ( ref.getSource() == source )
				references.add( ref );

		return references;
	}

	public static MavenReference getReferenceByGroup( String group )
	{
		Validate.notNull( group );
		for ( MavenReference ref : loadedLibraries.values() )
			if ( group.equalsIgnoreCase( ref.getGroup() ) )
				return ref;
		return null;
	}

	public static MavenReference getReferenceByName( String name )
	{
		Validate.notNull( name );
		for ( MavenReference ref : loadedLibraries.values() )
			if ( name.equalsIgnoreCase( ref.getName() ) )
				return ref;
		return null;
	}

	public static boolean isLoaded( MavenReference lib )
	{
		return loadedLibraries.containsKey( lib.getKey() );
	}

	public static boolean loadLibrary( File lib )
	{
		if ( lib == null || !lib.exists() )
			return false;

		LogBuilder.get( LOGNAME ).info( ( LogBuilder.useColor() ? EnumColor.GRAY : "" ) + "Loading the library `" + lib.getName() + "`" );

		try
		{
			LibraryClassLoader.addPath( lib );
		}
		catch ( Throwable t )
		{
			t.printStackTrace();
			return false;
		}

		try
		{
			UtilIO.extractNatives( lib, lib.getParentFile() );
		}
		catch ( IOException e )
		{
			LogBuilder.get( LOGNAME ).severe( "We had a problem trying to extract native libraries from jar file '" + lib.getAbsolutePath() + "'", e );
		}

		return true;
	}

	public static boolean loadLibrary( MavenReference lib )
	{
		String urlJar = lib.mavenUrl( "jar" );
		String urlPom = lib.mavenUrl( "pom" );

		File mavenLocalJar = lib.jarFile();
		File mavenLocalPom = lib.pomFile();

		if ( urlJar == null || urlJar.isEmpty() || urlPom == null || urlPom.isEmpty() )
			return false;

		try
		{
			if ( !mavenLocalPom.exists() || !mavenLocalJar.exists() )
			{
				LogBuilder.get( LOGNAME ).info( ( LogBuilder.useColor() ? EnumColor.GOLD : "" ) + "Downloading the library `" + lib.toString() + "` from url `" + urlJar + "`... Please Wait!" );

				// Try download from JCenter Bintray Maven Repository
				try
				{
					UtilHttp.downloadFile( urlPom, mavenLocalPom );
					UtilHttp.downloadFile( urlJar, mavenLocalJar );
				}
				catch ( IOException e )
				{
					// Try download from alternative Maven Central Repository
					String urlJarAlt = lib.mavenUrlAlt( "jar" );
					String urlPomAlt = lib.mavenUrlAlt( "pom" );

					LogBuilder.get( LOGNAME ).warning( "Primary download location failed, trying secondary location `" + urlJarAlt + "`... Please Wait!" );

					try
					{
						UtilHttp.downloadFile( urlPomAlt, mavenLocalPom );
						UtilHttp.downloadFile( urlJarAlt, mavenLocalJar );
					}
					catch ( IOException ee )
					{
						LogBuilder.get( LOGNAME ).severe( "Primary and secondary download location have FAILED!" );
						return false;
					}
				}
			}

			LogBuilder.get( LOGNAME ).info( ( LogBuilder.useColor() ? EnumColor.DARK_GRAY : "" ) + "Loading the library `" + lib.toString() + "` from file `" + mavenLocalJar + "`..." );

			LibraryClassLoader.addPath( mavenLocalJar );
		}
		catch ( Throwable t )
		{
			t.printStackTrace();
			return false;
		}

		loadedLibraries.put( lib.getKey(), lib );
		try
		{
			UtilIO.extractNatives( lib.jarFile(), lib.baseDir() );
		}
		catch ( IOException e )
		{
			LogBuilder.get( LOGNAME ).severe( "We had a problem trying to extract native libraries from jar file '" + lib.jarFile() + "'", e );
		}

		return true;
	}

	private Libraries()
	{

	}

	@Override
	public String getName()
	{
		return "builtin";
	}
}
