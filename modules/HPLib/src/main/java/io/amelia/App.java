package io.amelia;

import java.awt.image.Kernel;
import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import io.amelia.lang.ApplicationException;
import io.amelia.support.Arrs;
import io.amelia.support.IO;
import io.amelia.support.Lists;
import io.amelia.support.Objs;
import io.amelia.support.Strs;

public class App
{
	public static final String PATH_APP = "__app";
	public static final String PATH_CACHE = "__cache";
	public static final String PATH_LOGS = "__logs";
	public static final String PATH_LIBS = "__libs";
	public static final String PATH_CONFIG = "__config";
	public static final String PATH_PLUGINS = "__plugins";
	public static final String PATH_UPDATES = "__updates";
	public static final String PATH_STORAGE = "__storage";

	private static final Map<String, List<String>> appPaths = new ConcurrentHashMap<>();
	private static File appPath;
	private static ImplLogHandler log;

	static
	{
		setPath( PATH_CACHE, PATH_STORAGE, "cache" );
		setPath( PATH_LOGS, PATH_STORAGE, "logs" );
		setPath( PATH_CONFIG, PATH_APP, "config" );
		setPath( PATH_PLUGINS, PATH_APP, "plugins" );
		setPath( PATH_UPDATES, PATH_APP, "updates" );
		setPath( PATH_STORAGE, PATH_APP, "storage" );
	}

	public static Logger getLogger( Class<?> source )
	{
		return new Logger( source );
	}

	public static File getPath( @Nonnull String slug )
	{
		return getPath( new String[] {slug} );
	}

	public static File getPath( @Nonnull String slug, boolean createPath )
	{
		return getPath( new String[] {slug}, createPath );
	}

	public static File getPath( @Nonnull String[] slugs )
	{
		return getPath( slugs, false );
	}

	/**
	 * Builds a directory based on the provided slugs.
	 * Key based paths MUST start with double underscores.
	 * <p>
	 * The options are as follows:
	 * __app
	 * __webroot
	 * __config
	 * __plugins
	 * __updates
	 * __database
	 * __storage
	 * __sessions
	 * __cache
	 * __logs
	 * <p>
	 * Slugs not starting with double underscores will be treated as either a relative
	 * or absolute path depending on if it starts with a single forward slash.
	 * <p>
	 * Examples:
	 * __app -> /usr/share/honeypot
	 * __sessions -> /usr/share/honeypot/storage/sessions
	 * relative -> /usr/share/honeypot/relative
	 * /absolute -> /absolute
	 * <p>
	 *
	 * @param slugs      The path slugs
	 * @param createPath Should we try creating the directory if it doesn't exist?
	 * @return The absolute File
	 * @throws ApplicationException.Ignorable
	 */
	public static File getPath( @Nonnull String[] slugs, boolean createPath )
	{
		Objs.notNull( slugs );

		if ( slugs.length == 0 )
			return getPath();

		if ( slugs[0].startsWith( "__" ) )
		{
			String key = slugs[0].substring( 2 );
			if ( key.equals( "app" ) )
				slugs[0] = getPath().toString();
			else if ( App.appPaths.containsKey( key ) )
				slugs = ( String[] ) Stream.concat( App.appPaths.get( key ).stream(), Arrays.stream( slugs ).skip( 1 ) ).toArray();
			else
				throw ApplicationException.ignorable( "Path " + key + " is not set!" );

			return getPath( slugs, createPath );
		}
		else if ( !slugs[0].startsWith( "/" ) )
			slugs = Arrs.prepend( slugs, getPath().toString() );

		File path = IO.buildFile( true, slugs );

		if ( createPath && !path.exists() )
			if ( !path.mkdirs() )
				throw ApplicationException.ignorable( "The path \"" + path.getAbsolutePath() + "\" does not exist and we failed to create it." );

		return path;
	}

	public static File getPath()
	{
		Objs.notNull( appPath, "appPath has yet to be set." );
		return appPath;
	}

	public static void setAppPath( @Nonnull File appPath )
	{
		Objs.notNull( appPath );
		App.appPath = appPath;
	}

	public static void setLogHandler( ImplLogHandler log )
	{
		App.log = log;
	}

	public static void setPath( @Nonnull String pathKey, @Nonnull String... paths )
	{
		Objs.notEmpty( pathKey );
		if ( pathKey.startsWith( "__" ) )
			pathKey = pathKey.substring( 2 );

		final String key = pathKey.toLowerCase();

		if ( "app".equals( key ) )
			throw new IllegalArgumentException( "App path is set using the setAppPath() method." );
		if ( !Paths.get( paths[0] ).isAbsolute() && !paths[0].startsWith( "__" ) )
			throw new IllegalArgumentException( "App paths must be absolute or reference another app path, i.e., __app. Paths: [" + Strs.join( paths ) + "]" );
		App.appPaths.put( key, Lists.newArrayList( paths ) );

		Kernel.getApplication().addStringArgument( "dir-" + pathKey, "Sets the " + pathKey + " directory." );
	}

	private App()
	{

	}

	public static class Logger
	{
		private Class<?> source;

		public Logger( Class<?> source )
		{
			Objs.notNull( log, "The LogHandler was never set." );

			this.source = source;
		}

		public void debug( String message, Object... args )
		{
			log.debug( source, message, args );
		}

		public void fine( String message, Object... args )
		{
			log.fine( source, message, args );
		}

		public void finest( String message, Object... args )
		{
			log.finest( source, message, args );
		}

		public void info( String message, Object... args )
		{
			log.info( message, args );
		}

		public void severe( String message, Object... args )
		{
			log.severe( source, message, args );
		}

		public void severe( String message, Throwable cause, Object... args )
		{
			log.severe( source, message, cause, args );
		}

		public void warning( String message, Throwable cause, Object... args )
		{
			log.warning( source, message, cause, args );
		}

		public void warning( String message, Object... args )
		{
			log.warning( source, message, args );
		}
	}
}
