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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.net.ssl.SSLException;

import io.amelia.data.apache.ApacheConfiguration;
import io.amelia.data.parcel.Parcel;
import io.amelia.data.parcel.ParcelLoader;
import io.amelia.database.Database;
import io.amelia.database.elegant.ElegantQueryTable;
import io.amelia.foundation.ConfigRegistry;
import io.amelia.foundation.Env;
import io.amelia.foundation.Kernel;
import io.amelia.http.localization.Localization;
import io.amelia.http.mappings.DomainMapping;
import io.amelia.http.mappings.DomainNode;
import io.amelia.http.routes.Routes;
import io.amelia.http.session.SessionPersistenceMethod;
import io.amelia.lang.WebrootException;
import io.amelia.scripting.ScriptBinding;
import io.amelia.scripting.ScriptingContext;
import io.amelia.scripting.ScriptingFactory;
import io.amelia.support.DateAndTime;
import io.amelia.support.EnumColor;
import io.amelia.support.IO;
import io.amelia.support.Lists;
import io.amelia.support.NIO;
import io.amelia.support.Objs;
import io.amelia.support.StoragePolicy;
import io.netty.handler.ssl.SslContext;

class BaseWebroot
{
	public static final String PATH_ARCHIVE = "__archive";
	// Storage Policy for Webroot Directories
	private final static StoragePolicy STORAGE_POLICY = new StoragePolicy();

	static
	{
		Kernel.setPath( PATH_ARCHIVE, Kernel.PATH_STORAGE, "archive" );

		// Language Files
		STORAGE_POLICY.setLayoutDirectory( "lang", StoragePolicy.Strategy.CREATE );
		// Public Files
		STORAGE_POLICY.setLayoutDirectory( "public", StoragePolicy.Strategy.OPTIONAL );
		// Resources
		STORAGE_POLICY.setLayoutDirectory( "resource", StoragePolicy.Strategy.CREATE );
		// SSL Certificates and Keys
		STORAGE_POLICY.setLayoutDirectory( "ssl", StoragePolicy.Strategy.CREATE );
		// .env
		STORAGE_POLICY.setLayoutFile( ".env", StoragePolicy.Strategy.CREATE );
		// config.parcel
		STORAGE_POLICY.setLayoutFile( "config.yaml", StoragePolicy.Strategy.CREATE );
		// .htaccess
		STORAGE_POLICY.setLayoutFile( "htaccess.json", StoragePolicy.Strategy.CREATE );
		// Routes file
		STORAGE_POLICY.setLayoutFile( "routes.json", StoragePolicy.Strategy.CREATE );
	}

	protected final ScriptBinding binding = new ScriptBinding();
	protected final List<String> cachePatterns = new ArrayList<>();
	protected final Path configFile;
	protected final Parcel configuration;
	protected final Path directory;
	protected final Env env;
	protected final ScriptingFactory factory = ScriptingFactory.create( binding );
	protected final List<String> ips = new ArrayList<>();
	protected final Localization localization;
	protected final List<DomainMapping> mappings = new ArrayList<>();
	protected final Routes routes;
	protected final String webrootId;
	protected Database database;
	protected String defDomain = null;
	protected SslContext defaultSslContext = null;
	protected SessionPersistenceMethod sessionPersistence = SessionPersistenceMethod.COOKIE;
	protected String webrootTitle;

	BaseWebroot( @Nonnull String webrootId ) throws WebrootException.Error
	{
		try
		{
			this.webrootId = webrootId;

			configFile = null;
			configuration = new Parcel();
			webrootTitle = Kernel.getDevMeta().getProductName();
			database = Database.getDatabase();

			directory = WebrootUtils.createWebrootDirectory( webrootId );
			localization = new Localization( getLangDirectory() );
			routes = new Routes( this );
			env = new Env( directory.resolve( ".env" ) );
		}
		catch ( IOException e )
		{
			throw new WebrootException.Error( e );
		}
	}

	BaseWebroot( @Nonnull Path directory, @Nonnull Parcel configuration, @Nonnull Env env ) throws WebrootException.Error
	{
		try
		{
			this.directory = directory;
			this.configFile = directory.resolve( "config.yaml" );
			this.configuration = configuration;
			this.env = env;

			// parcel.setEnvironmentVariables( env.getProperties() );

			webrootId = configuration.getString( "webroot.id" ).orElseThrow( () -> new WebrootException.Error( "Webroot ID is missing!" ) ).toLowerCase();
			webrootTitle = configuration.getString( "webroot.title" ).orElse( ConfigRegistry.config.getString( WebrootManager.Config.WEBROOTS_DEFAULT_TITLE ) );

			configuration.getList( "webroot.listen", ips );

			for ( String ip : ips )
				if ( !NIO.isValidIPv4( ip ) && !NIO.isValidIPv6( ip ) )
					WebrootManager.L.warning( String.format( "Webroot '%s' is set to listen on ip '%s', but the ip does not match the valid IPv4 or IPv6 regex formula.", webrootId, ip ) );
				else if ( !NIO.isAddressAssigned( ip ) )
					WebrootManager.L.warning( String.format( "Webroot '%s' is set to listen on ip '%s', but that address is not assigned to any network interfaces.", webrootId, ip ) );

			if ( ips.contains( "localhost" ) )
				throw new WebrootException.Error( "Webroots are not permitted to listen on hostname 'localhost', this hostname is reserved for the default webroot." );

			if ( WebrootManager.getWebrootById( webrootId ) != null )
				throw new WebrootException.Error( String.format( "There already exists a webroot by the provided webroot id '%s'", webrootId ) );

			WebrootManager.L.info( String.format( "Loading webroot '%s' with title '%s' from YAML file.", webrootId, webrootTitle ) );

			this.localization = new Localization( getLangDirectory() );

			if ( !configuration.hasValue( "webroot.web-allowed-origin" ) )
				configuration.setValue( "webroot.web-allowed-origin", "*" );

			mapDomain( configuration.getChildOrCreate( "webroot.domains" ) );

			Path ssl = getDirectory( "ssl" );
			IO.forceCreateDirectory( ssl );

			String sslCertFile = configuration.getString( "webroot.sslCert" ).orElse( null );
			String sslKeyFile = configuration.getString( "webroot.sslKey" ).orElse( null );
			String sslSecret = configuration.getString( "webroot.sslSecret" ).orElse( null );

			if ( sslCertFile != null && sslKeyFile != null )
			{
				File sslCert = new File( ssl.getAbsolutePath(), sslCertFile );
				File sslKey = new File( ssl.getAbsolutePath(), sslKeyFile );

				try
				{
					defaultSslContext = new CertificateWrapper( sslCert, sslKey, sslSecret ).context();
				}
				catch ( SSLException | FileNotFoundException | CertificateException e )
				{
					WebrootManager.L.severe( String.format( "Failed to load SslContext for webroot '%s' using cert '%s', key '%s', and hasSecret? %s", webrootId, UtilIO.relPath( sslCert ), UtilIO.relPath( sslKey ), sslSecret != null && !sslSecret.isEmpty() ), e );
				}
			}

			try
			{
				if ( EventDispatcher.i().callEventWithException( new WebrootLoadEvent( this ) ).isCancelled() )
					throw new WebrootException.Error( String.format( "Loading of webroot '%s' was cancelled by an internal event.", webrootId ) );
			}
			catch ( EventException e )
			{
				throw new WebrootException.Error( e );
			}

			if ( configuration.has( "database" ) && configuration.isConfigurationSection( "database" ) )
				database = new Database( StorageModule.i().init( configuration.getConfigurationSection( "database" ) ) );

			routes = new Routes( this );

			if ( configuration.has( "sessions.persistenceMethod" ) )
				for ( SessionPersistenceMethod method : SessionPersistenceMethod.values() )
					if ( method.name().equalsIgnoreCase( configuration.getString( "sessions.persistenceMethod" ) ) )
						sessionPersistence = method;

			List<String> onLoadScripts = configuration.getStringList( "scripts.on-load" );

			if ( onLoadScripts != null )
				for ( String script : onLoadScripts )
				{
					ScriptingResult result = factory.eval( ScriptingContext.fromFile( this, script ).shell( "groovy" ).Webroot( this ) );

					if ( result.hasExceptions() )
					{
						if ( result.hasException( FileNotFoundException.class ) )
							WebrootManager.L.severe( String.format( "Failed to eval onLoadScript '%s' for webroot '%s' because the file was not found.", script, webrootId ) );
						else
						{
							WebrootManager.L.severe( String.format( "Exception caught while evaluate onLoadScript '%s' for webroot '%s'", script, webrootId ) );
							ExceptionReport.printExceptions( result.getExceptions() );
						}
					}
					else
						WebrootManager.L.info( String.format( "Finished evaluate onLoadScript '%s' for webroot '%s' with result: %s", script, webrootId, result.getString( true ) ) );
				}

			Parcel archive = configuration.getChildOrCreate( "archive" );

			if ( !archive.hasValue( "enable" ) )
				archive.setValue( "enable", false );

			if ( !archive.hasValue( "interval" ) )
				archive.setValue( "interval", "24h" );

			if ( !archive.hasValue( "keep" ) )
				archive.setValue( "keep", "3" );

			if ( !archive.hasValue( "lastRun" ) )
				archive.setValue( "lastRun", "0" );

			if ( archive.getBoolean( "enable" ).orElse( false ) )
			{
				String interval = archive.getString( "interval" ).orElse( "24h" ).trim();
				if ( interval.matches( "[0-9]+[dhmsDHMS]?" ) )
				{
					interval = interval.toLowerCase();
					int multiply = 1;

					if ( interval.endsWith( "d" ) || interval.endsWith( "h" ) || interval.endsWith( "m" ) || interval.endsWith( "s" ) )
					{
						switch ( interval.substring( interval.length() - 1 ) )
						{
							case "d":
								multiply = 1728000;
								break;
							case "h":
								multiply = 72000;
								break;
							case "m":
								multiply = 1200;
								break;
							case "s":
								multiply = 20;
								break;
						}
						interval = interval.substring( 0, interval.length() - 1 );
					}

					long timer = Long.parseLong( interval ) * multiply;
					long lastRun = DateAndTime.epoch() - archive.getLong( "lastRun" ).orElse( 0L );
					long nextRun = archive.getLong( "lastRun" ).orElse( 0L ) < 1L ? 600L : lastRun > timer ? 600L : timer - lastRun;

					WebrootManager.L.info( String.format( "%s%sScheduled webroot archive for %s {nextRun: %s, interval: %s}", EnumColor.AQUA, EnumColor.NEGATIVE, webrootId, nextRun, timer ) );

					TaskManager.instance().scheduleSyncRepeatingTask( WebrootManager.i(), nextRun, timer, () -> {
						WebrootManager.L.info( String.format( "%s%sRunning archive for webroot %s...", EnumColor.AQUA, EnumColor.NEGATIVE, webrootId ) );

						WebrootManager.cleanupBackups( webrootId, ".zip", archive.getInteger( "keep" ).orElse( 3 ) );
						archive.setValue( "lastRun", DateAndTime.epoch() );

						Path dir = Kernel.getPath( PATH_ARCHIVE ).resolve( webrootId );
						IO.forceCreateDirectory( dir );

						Path zip = dir.resolve( new SimpleDateFormat( "yyyy-MM-dd_HH-mm-ss" ).format( new Date() ) + "-" + webrootId + ".zip" );

						try
						{
							IO.zipDir( BaseWebroot.this.getDirectory(), zip );
						}
						catch ( IOException e )
						{
							WebrootManager.L.severe( String.format( "%s%sFailed archiving webroot %s to %s", EnumColor.RED, EnumColor.NEGATIVE, webrootId, zip.getAbsolutePath() ), e );
							return;
						}

						WebrootManager.L.info( String.format( "%s%sFinished archiving webroot %s to %s", EnumColor.AQUA, EnumColor.NEGATIVE, webrootId, zip.getAbsolutePath() ) );
					} );
				}
				else
					WebrootManager.L.warning( String.format( "Failed to initialize webroot backup for webroot %s, interval did not match regex '[0-9]+[dhmsDHMS]?'.", webrootId ) );
			}
		}
		catch ( IOException e )
		{
			throw new WebrootException.Error( e );
		}
	}

	public void addToCachePatterns( String pattern )
	{
		if ( !cachePatterns.contains( pattern.toLowerCase() ) )
			cachePatterns.add( pattern.toLowerCase() );
	}

	public Path getAccountDirectory()
	{
		return null;
	}

	public List<String> getAccountFields()
	{
		return getConfig().getStringList( "accounts.fields" ).orElseGet( Lists::newArrayList );
	}

	public ElegantQueryTable getAccountTable()
	{
		return null;
	}

	public ApacheConfiguration getApacheConfig()
	{
		return new ApacheConfiguration();
	}

	protected ScriptBinding getBinding()
	{
		return binding;
	}

	public Path getCacheDirectory()
	{
		return Kernel.getPath( Kernel.PATH_CACHE ).resolve( getId() );
	}

	public List<String> getCachePatterns()
	{
		return cachePatterns;
	}

	public Parcel getConfig()
	{
		return configuration;
	}

	public Path getConfigFile()
	{
		return configFile;
	}

	public Database getDatabase()
	{
		return database;
	}

	public String getDefaultDomain()
	{
		if ( defDomain != null )
			return defDomain;
		Stream<DomainNode> domains = getDomains();
		if ( domains.count() > 0 )
			return domains.findFirst().get().getFullDomain();
		return null;
	}

	public DomainMapping getDefaultMapping()
	{
		// Prevent error if no mappings are mapped.
		Stream<DomainMapping> stream = mappings.stream().filter( DomainMapping::isDefault );
		return ( stream.count() == 0 ? getMappings() : stream ).findFirst().get();
	}

	public SslContext getDefaultSslContext()
	{
		return defaultSslContext;
	}

	/**
	 * @return The webroot main directory
	 */
	public Path getDirectory()
	{
		Objs.notNull( directory );
		return directory;
	}

	/**
	 * @param sub The subdirectory name
	 *
	 * @return The subdirectory of the webroot main directory
	 */
	public Path getDirectory( String sub )
	{
		return directory.resolve( sub );
	}

	/**
	 * Same as calling {@code WebrootManager.instance().getDomain( fullDomain ) } but instead checks the returned node belongs to this webroot.
	 *
	 * @param fullDomain The request domain
	 *
	 * @return The DomainNode
	 */
	public DomainNode getDomain( String fullDomain )
	{
		DomainNode node = WebrootManager.getDomain( fullDomain );
		return node != null && node.getWebroot() == this ? node : null;
	}

	public Stream<DomainNode> getDomains()
	{
		return WebrootManager.getDomainsByWebroot( this );
	}

	public ScriptingFactory getEvalFactory()
	{
		return factory;
	}

	public Object getGlobal( String key )
	{
		return binding.getVariable( key );
	}

	public Map<String, Object> getGlobals()
	{
		return binding.getVariables();
	}

	@Override
	public String getId()
	{
		return webrootId;
	}

	public List<String> getIps()
	{
		return ips;
	}

	public Path getLangDirectory()
	{
		return getDirectory( "lang" );
	}

	public Localization getLocalization()
	{
		return localization;
	}

	public String getLoginForm()
	{
		return getConfig().getString( "accounts.loginForm" ).orElse( "/~wisp/login" );
	}

	public String getLoginPost()
	{
		return getConfig().getString( "accounts.loginPost" ).orElse( "/" );
	}

	public Stream<DomainMapping> getMappings()
	{
		return mappings.stream();
	}

	public Stream<DomainMapping> getMappings( String fullDomain )
	{
		Objs.notEmpty( fullDomain );
		Supplier<Stream<DomainMapping>> stream = () -> mappings.stream().filter( d -> d.matches( fullDomain ) );
		return stream.get().count() == 0 ? Stream.of( new DomainMapping( this, fullDomain ) ) : stream.get();
	}

	public Path getPublicDirectory()
	{
		return getDirectory( "public" );
	}

	public Path getResourceDirectory()
	{
		return getDirectory( "resource" );
	}

	public Path getResourceFile( String file ) throws IOException
	{
		Objs.notNull( file, "File can't be null" );

		if ( file.length() == 0 )
			throw new FileNotFoundException( "File can't be empty!" );

		Path root = getResourceDirectory();

		Path packFile = root.resolve( file );

		if ( Files.isRegularFile( packFile ) )
			return packFile;

		root = packFile.getParent();

		if ( Files.isDirectory( root ) )
		{
			Map<String, Path> found = new LinkedHashMap<>();
			List<String> preferred = ScriptingContext.getPreferredExtensions();

			Files.list( root ).filter( child -> IO.getLocalName( child ).startsWith( IO.getLocalName( packFile ) + "." ) ).forEach( child -> found.put( IO.getLocalName( child ).substring( IO.getLocalName( packFile ).length() + 1 ), child ) );

			if ( found.size() > 0 )
			{
				if ( preferred.size() > 0 )
					for ( String ext : preferred )
						if ( found.containsKey( ext.toLowerCase() ) )
							return found.get( ext.toLowerCase() );

				return found.values().stream().findFirst().orElse( null );
			}
		}

		throw new FileNotFoundException( String.format( "Could not find the file '%s' file in webroot '%s' resource directory '%s'.", file, getId(), root.toString() ) );
	}

	public Path getResourcePackage( @Nonnull String pack ) throws IOException
	{
		if ( pack.length() == 0 )
			throw new FileNotFoundException( "Package can't be empty!" );
		return getResourceFile( pack.replace( ".", IO.PATH_SEPERATOR ) );
	}

	public Path getResourcePath()
	{

	}

	public Routes getRoutes()
	{
		return routes;
	}

	/**
	 * Gets the webroot configured Session Key from configuration.
	 *
	 * @return The Session Key
	 */
	public String getSessionKey()
	{
		String key = configuration.getString( "sessions.keyName" );
		if ( key == null )
			return SessionModule.getDefaultSessionName();
		return "_ws" + WordUtils.capitalize( key );
	}

	public SessionPersistenceMethod getSessionPersistenceMethod()
	{
		return sessionPersistence;
	}

	public String getTitle()
	{
		return webrootTitle;
	}

	public void setTitle( String title )
	{
		webrootTitle = title;
		configuration.set( "webroot.title", title );
	}

	public Path getWebrootPath()
	{

	}

	public boolean hasDefaultSslContext()
	{
		return defaultSslContext != null;
	}

	private void mapDomain( @Nonnull Parcel domains ) throws WebrootException.Configuration
	{
		mapDomain( domains, null, 0 );
	}

	private void mapDomain( @Nonnull Parcel domains, @Nonnull DomainMapping mapping, @Nonnegative int depth ) throws WebrootException.Configuration
	{
		for ( String key : domains.getKeys() )
		{
			/* Replace underscore with dot, ignore escaped underscore. */
			String domainKey = key.replaceAll( "(?<!\\\\)_", "." ).replace( "\\_", "_" );

			if ( key.startsWith( "__" ) ) // Configuration Directive
			{
				if ( depth == 0 || mapping == null )
					throw new WebrootException.Configuration( String.format( "Domain configuration directive [%s.%s] is not allowed here.", domains.getCurrentPath(), key ) );
				mapping.putConfig( key.substring( 2 ), domains.getString( key ).orElse( null ) );

				if ( "__default".equals( key ) && domains.getBoolean( key ).orElse( false ) )
				{
					if ( defDomain != null )
						throw new WebrootException.Configuration( String.format( "Domain configuration at [%s] is invalid, the DEFAULT domain was previously set to [%s]", domains.getCurrentPath(), defDomain ) );
					defDomain = mapping.getFullDomain();
				}
			}
			else if ( domains.hasChild( key ) ) // Child Domain
				try
				{
					DomainMapping mappingNew = mapping == null ? getMappings( domainKey ).findFirst().get() : mapping.getChildMapping( domainKey );
					mappingNew.map();
					mapDomain( domains.getChildOrCreate( key ), mappingNew, depth + 1 );
				}
				catch ( IllegalStateException e )
				{
					/* Convert the IllegalStateException to a proper WebrootException.Configuration */
					throw new WebrootException.Configuration( e );
				}
			else /* Invalid Directive */
				WebrootManager.L.warning( String.format( "Webroot configuration path [%s.%s] is invalid, domain directives MUST start with a double underscore (e.g., __key) and child domains must be a (empty) YAML section (e.g., {}).", domains.getCurrentPath(), key ) );
		}
	}

	public void save() throws IOException
	{
		save( false );
	}

	public void save( boolean force ) throws IOException
	{
		if ( configFile != null && ( Files.isRegularFile( configFile ) || force ) )
			IO.writeStringToPath( ParcelLoader.encodeYaml( configuration ), configFile );
	}

	public void setGlobal( String key, Object val )
	{
		binding.setVariable( key, val );
	}

	@Override
	public String toString()
	{
		return "Webroot{id=" + getId() + ",title=" + getTitle() + ",domains=" + getDomains().map( n -> n.getFullDomain() ).collect( Collectors.joining( "," ) ) + ",ips=" + ips.stream().collect( Collectors.joining( "," ) ) + ",WebrootDir=" + directory.getAbsolutePath() + "}";
	}
}
