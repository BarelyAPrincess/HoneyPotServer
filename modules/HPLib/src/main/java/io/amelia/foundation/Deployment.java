package io.amelia.foundation;

import com.sun.istack.internal.NotNull;
import io.amelia.foundation.binding.BindingRegistry;
import io.amelia.foundation.injection.Libraries;
import io.amelia.foundation.injection.MavenReference;
import io.amelia.lang.ApplicationException;
import io.amelia.lang.StartupException;
import io.amelia.helpers.Arrs;
import io.amelia.helpers.LibIO;
import io.amelia.helpers.Strs;
import io.amelia.helpers.UtilLists;
import javafx.application.Application;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class Deployment
{
	private static final Map<String, List<String>> appPaths = new ConcurrentHashMap<>();

	static
	{
		path( "webroot", "__app", "webroot" );
		path( "config", "__app", "config" );
		path( "database", "__app", "database" );
		path( "storage", "__app", "storage" );
		path( "sessions", "__storage", "sessions" );
		path( "cache", "__storage", "cache" );
		path( "logs", "__storage", "logs" );
	}

	private static void panic( String msg )
	{
		LogBuilder.get().severe( msg );
		System.exit( 1 );
	}

	public static Path getAppPath()
	{
		return Paths.get( "" ).normalize();
	}

	public static Path getAppPath( @NotNull String[] slugs ) throws ApplicationException
	{
		if ( slugs.length == 0 )
			return getAppPath();

		if ( slugs[0].startsWith( "__" ) )
		{
			String key = slugs[0].substring( 2 );
			if ( key.equals( "app" ) )
				slugs[0] = getAppPath().toString();
			else if ( appPaths.containsKey( key ) )
				slugs = ( String[] ) Stream.concat( appPaths.get( key ).stream(), Arrays.stream( slugs ).skip( 1 ) ).toArray();
			else
				throw ApplicationException.fatal( "Path " + key + " is not set!" );

			return getAppPath( slugs );
		}
		else if ( !slugs[0].startsWith( "/" ) )
			slugs = Arrs.prepend( slugs, getAppPath().toString() );

		Path path = Paths.get( URI.create( "file://" + LibIO.buildPath( slugs ) ) ).normalize();

		if ( !path.toFile().exists() )
			if ( !path.toFile().mkdirs() )
				throw ApplicationException.fatal( "The path \"" + path.toString() + "\" does not exist and we failed to create it." );

		return path;
	}

	private static void path( @NotNull String key, @NotNull String... paths )
	{
		if ( !Paths.get( paths[0] ).isAbsolute() && !paths[0].startsWith( "__" ) )
			throw new IllegalArgumentException( "App paths must be absolute or reference another app path, i.e., __app. Paths: [" + Strs.join( paths ) + "]" );
		appPaths.put( key, UtilLists.newArrayList( paths ) );
	}

	/**
	 * Loads the built-in dependencies written by the gradle script
	 */
	public static void prepare()
	{
		try
		{
			for ( String depend : LibIO.resourceToString( "dependencies.txt" ).split( "\n" ) )
				Libraries.loadLibrary( new MavenReference( "builtin", depend ) );
		}
		catch ( IOException e )
		{
			throw new StartupException( "Failed to read the built-in dependencies file.", e );
		}

		// Base Libraries
		/*Libraries.loadLibrary( new MavenReference( "builtin", "org.yaml:snakeyaml:1.17" ) );
		Libraries.loadLibrary( new MavenReference( "builtin", "net.sf.jopt-simple:jopt-simple:5.0.1" ) );
		Libraries.loadLibrary( new MavenReference( "builtin", "org.fusesource.jansi:jansi:1.11" ) );

		// Extended Libraries
		Libraries.loadLibrary( new MavenReference( "builtin", "com.google.guava:guava:18.0" ) );
		Libraries.loadLibrary( new MavenReference( "builtin", "com.google.code.gson:gson:2.3" ) );
		Libraries.loadLibrary( new MavenReference( "builtin", "mysql:mysql-connector-java:5.1.32" ) );
		Libraries.loadLibrary( new MavenReference( "builtin", "org.xerial:sqlite-jdbc:3.8.11.2" ) );
		Libraries.loadLibrary( new MavenReference( "builtin", "com.h2database:h2:1.4.187" ) );
		Libraries.loadLibrary( new MavenReference( "builtin", "org.apache.commons:commons-lang3:3.3.2" ) );
		Libraries.loadLibrary( new MavenReference( "builtin", "commons-io:commons-io:2.4" ) );
		Libraries.loadLibrary( new MavenReference( "builtin", "commons-net:commons-net:3.3" ) );
		Libraries.loadLibrary( new MavenReference( "builtin", "commons-codec:commons-codec:1.9" ) );
		Libraries.loadLibrary( new MavenReference( "builtin", "joda-time:joda-time:2.7" ) );
		Libraries.loadLibrary( new MavenReference( "builtin", "org.ocpsoft.prettytime:prettytime:3.2.5.Final" ) );
		Libraries.loadLibrary( new MavenReference( "builtin", "com.googlecode.libphonenumber:libphonenumber:7.0.4" ) );
		Libraries.loadLibrary( new MavenReference( "builtin", "org.apache.httpcomponents:fluent-hc:4.3.5" ) );*/
	}

	public static void start( @NotNull Loader loader, String[] args )
	{
		try
		{
			if ( ConfigRegistry.i().populateOptions( loader, args ) )
			{
				LogBuilder.get().info( "Starting deployment of " + Versioning.getProduct() + " (" + Versioning.getVersion() + ")" );

				/* Load Deployment Libraries */
				LogBuilder.get().info( "Loading deployment libraries: " + Libraries.LIBRARY_DIR.getAbsolutePath() );
				List<String> libs = ConfigRegistry.i().getAsList( "deploy.libraries" );
				if ( libs != null )
				{
					LogBuilder.get().info( "Loading deployment libraries defined in deploy.yaml!" );
					for ( String lib : libs )
						Libraries.loadLibrary( new MavenReference( "builtin", lib ) );
				}

				LogBuilder.get().info( "Finished downloading deployment libraries, now launching application!" );

				try
				{


					kernel = BindingRegistry.resolveClass( Kernel.class );

					Kernel.initKernel( loader );
				}
				catch ( Throwable t )
				{
					panic( "Deployment failed! We encountered an exception while trying to initialize the main-class: " + t.getMessage() );
				}
			}
		}
		catch ( Throwable t )
		{
			throw new StartupException( t );
		}
	}

	public static Application application()
	{
		return null;
	}
}
