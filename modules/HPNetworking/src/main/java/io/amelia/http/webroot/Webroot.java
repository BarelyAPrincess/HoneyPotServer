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
import java.nio.file.Path;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.net.ssl.SSLException;

import io.amelia.data.apache.ApacheConfiguration;
import io.amelia.data.parcel.Parcel;
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
import io.amelia.scripting.ScriptingFactory;
import io.amelia.support.IO;
import io.amelia.support.Objs;
import io.amelia.support.StoragePolicy;
import io.netty.handler.ssl.SslContext;

public class Webroot
{
	// Storage Policy for Webroot Directories
	private final static StoragePolicy STORAGE_POLICY = new StoragePolicy();

	static
	{
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

	/* Domain Mappings */
	protected final List<DomainMapping> mappings = new ArrayList<>();
	/* Environment variables */
	final Env env;
	/* Language strings */
	final Localization localization;
	/* Loaded webroot configuration */
	final Parcel parcel;
	/* Scripting variables binding */
	private final ScriptBinding binding = new ScriptBinding();
	private final List<String> cachePatterns = new ArrayList<>();
	/* Configuration file */
	private final Path configFile;
	/* Root directory */
	private final Path directory;
	/* ScriptingFactory instance, for interpreting script files */
	private final ScriptingFactory factory = ScriptingFactory.create( binding );
	/* Listening IP addresses */
	private final List<String> ips;
	/* URL routes */
	private final Routes routes;
	/* Id */
	private final String webrootId;
	/* Site Database */
	private Database database;
	private String defDomain = null;
	/* Default webroot SSL context */
	private SslContext defaultSslContext = null;
	/* Session persistence methods */
	private SessionPersistenceMethod sessionPersistence = SessionPersistenceMethod.COOKIE;
	/* Title */
	private String webrootTitle;

	Webroot( @Nonnull String webrootId )
	{
		this.webrootId = webrootId;

		configFile = null;
		parcel = new Parcel();
		ips = new ArrayList<>();
		webrootTitle = Kernel.getDevMeta().getProductName();
		database = Database.getDatabase();

		directory = WebrootManager.checkSiteRoot( webrootId );
		localization = new Localization( getLangDirectory() );
		routes = new Routes( this );
		env = new Env();
	}

	Webroot( @Nonnull Path path, @Nonnull Parcel parcel, @Nonnull Env env ) throws WebrootException.Error
	{
		// parcel.setEnvironmentVariables( env.getProperties() );

		this.configFile = configFile;
		this.parcel = parcel;
		this.env = env;

		if ( !parcel.has( "webroot.id" ) )
			throw new WebrootException.Error( "Site id is missing!" );

		webrootId = parcel.getString( "webroot.id" ).toLowerCase();
		webrootTitle = parcel.getString( "webroot.title", ConfigRegistry.i().getString( "framework.sites.defaultTitle", "Unnamed Site" ) );

		ips = parcel.getAsList( "webroot.listen", new ArrayList<>() );

		for ( String ip : ips )
			if ( !UtilNet.isValidIPv4( ip ) && !UtilNet.isValidIPv6( ip ) )
				mgr.getLogger().warning( String.format( "Site '%s' is set to listen on ip '%s', but the ip does not match the valid IPv4 or IPv6 regex formula.", webrootId, ip ) );
			else if ( !UtilNet.isAddressAssigned( ip ) )
				mgr.getLogger().warning( String.format( "Site '%s' is set to listen on ip '%s', but that address is not assigned to any network interfaces.", webrootId, ip ) );

		if ( ips.contains( "localhost" ) )
			throw new SiteException( "Sites are not permitted to listen on hostname 'localhost', this hostname is reserved for the default webroot." );

		if ( WebrootManager.i().getSiteById( webrootId ) != null )
			throw new SiteException( String.format( "There already exists a webroot by the provided webroot id '%s'", webrootId ) );

		StorageModule.getLogger().info( String.format( "Loading webroot '%s' with title '%s' from YAML file.", webrootId, webrootTitle ) );

		directory = WebrootManager.checkSiteRoot( webrootId );

		this.localization = new Localization( getLangDirectory() );

		if ( !parcel.has( "webroot.web-allowed-origin" ) )
			parcel.set( "webroot.web-allowed-origin", "*" );

		mapDomain( parcel.getConfigurationSection( "webroot.domains", true ) );

		File ssl = getDirectory( "ssl" );
		IO.forceCreateDirectory( ssl );

		String sslCertFile = parcel.getString( "webroot.sslCert" );
		String sslKeyFile = parcel.getString( "webroot.sslKey" );
		String sslSecret = parcel.getString( "webroot.sslSecret" );

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
				mgr.getLogger().severe( String.format( "Failed to load SslContext for webroot '%s' using cert '%s', key '%s', and hasSecret? %s", webrootId, UtilIO.relPath( sslCert ), UtilIO.relPath( sslKey ), sslSecret != null && !sslSecret.isEmpty() ), e );
			}
		}

		try
		{
			if ( EventDispatcher.i().callEventWithException( new SiteLoadEvent( this ) ).isCancelled() )
				throw new SiteException( String.format( "Loading of webroot '%s' was cancelled by an internal event.", webrootId ) );
		}
		catch ( EventException e )
		{
			throw new SiteException( e );
		}

		if ( parcel.has( "database" ) && parcel.isConfigurationSection( "database" ) )
			database = new Database( StorageModule.i().init( parcel.getConfigurationSection( "database" ) ) );

		routes = new Routes( this );

		if ( parcel.has( "sessions.persistenceMethod" ) )
			for ( SessionPersistenceMethod method : SessionPersistenceMethod.values() )
				if ( method.name().equalsIgnoreCase( parcel.getString( "sessions.persistenceMethod" ) ) )
					sessionPersistence = method;

		List<String> onLoadScripts = parcel.getStringList( "scripts.on-load" );

		if ( onLoadScripts != null )
			for ( String script : onLoadScripts )
			{
				ScriptingResult result = factory.eval( ScriptingContext.fromFile( this, script ).shell( "groovy" ).site( this ) );

				if ( result.hasExceptions() )
				{
					if ( result.hasException( FileNotFoundException.class ) )
						mgr.getLogger().severe( String.format( "Failed to eval onLoadScript '%s' for webroot '%s' because the file was not found.", script, webrootId ) );
					else
					{
						mgr.getLogger().severe( String.format( "Exception caught while evaluate onLoadScript '%s' for webroot '%s'", script, webrootId ) );
						ExceptionReport.printExceptions( result.getExceptions() );
					}
				}
				else
					mgr.getLogger().info( String.format( "Finished evaluate onLoadScript '%s' for webroot '%s' with result: %s", script, webrootId, result.getString( true ) ) );
			}

		ConfigurationSection archive = parcel.getConfigurationSection( "archive", true );

		if ( !archive.has( "enable" ) )
			archive.set( "enable", false );

		if ( !archive.has( "interval" ) )
			archive.set( "interval", "24h" );

		if ( !archive.has( "keep" ) )
			archive.set( "keep", "3" );

		if ( !archive.has( "lastRun" ) )
			archive.set( "lastRun", "0" );

		if ( archive.getBoolean( "enable" ) )
		{
			String interval = archive.getString( "interval", "24h" ).trim();
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
				long lastRun = Timings.epoch() - archive.getLong( "lastRun" );
				long nextRun = archive.getLong( "lastRun" ) < 1L ? 600L : lastRun > timer ? 600L : timer - lastRun;
				final Site site = this;

				mgr.getLogger().info( String.format( "%s%sScheduled webroot archive for %s {nextRun: %s, interval: %s}", EnumColor.AQUA, EnumColor.NEGATIVE, webrootId, nextRun, timer ) );

				TaskManager.instance().scheduleSyncRepeatingTask( WebrootManager.i(), nextRun, timer, () -> {
					Logger l = mgr.getLogger();
					l.info( String.format( "%s%sRunning archive for webroot %s...", EnumColor.AQUA, EnumColor.NEGATIVE, webrootId ) );

					WebrootManager.cleanupBackups( webrootId, ".zip", archive.getInt( "keep", 3 ) );
					archive.set( "lastRun", Timings.epoch() );

					File dir = ConfigRegistry.i().getDirectory( "archive", "archive" );
					dir = new File( dir, webrootId );
					dir.mkdirs();

					File zip = new File( dir, new SimpleDateFormat( "yyyy-MM-dd_HH-mm-ss" ).format( new Date() ) + "-" + webrootId + ".zip" );

					try
					{
						UtilIO.zipDir( site.directory(), zip );
					}
					catch ( IOException e )
					{
						l.severe( String.format( "%s%sFailed archiving webroot %s to %s", EnumColor.RED, EnumColor.NEGATIVE, webrootId, zip.getAbsolutePath() ), e );
						return;
					}

					l.info( String.format( "%s%sFinished archiving webroot %s to %s", EnumColor.AQUA, EnumColor.NEGATIVE, webrootId, zip.getAbsolutePath() ) );
				} );
			}
			else
				mgr.getLogger().warning( String.format( "Failed to initialize webroot backup for webroot %s, interval did not match regex '[0-9]+[dhmsDHMS]?'.", webrootId ) );
		}
	}

	public void addToCachePatterns( String pattern )
	{
		if ( !cachePatterns.contains( pattern.toLowerCase() ) )
			cachePatterns.add( pattern.toLowerCase() );
	}

	@Override
	public Path getAccountDirectory()
	{
		return null;
	}

	@Override
	public Set<String> getAccountFields()
	{
		return new HashSet<>( getConfig().getAsList( "accounts.fields", new ArrayList<>() ) );
	}

	@Override
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

	public Path getCachePath()
	{

	}

	public List<String> getCachePatterns()
	{
		return cachePatterns;
	}

	public Parcel getConfig()
	{
		return parcel;
	}

	public Path getConfigFile()
	{
		return configFile == null ? parcel.loadedFrom() == null ? null : new File( parcel.loadedFrom() ) : configFile;
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
	 * Same as calling {@code SiteManager.instance().getDomain( fullDomain ) } but instead checks the returned node belongs to this webroot.
	 *
	 * @param fullDomain The request domain
	 *
	 * @return The DomainNode
	 */
	public DomainNode getDomain( String fullDomain )
	{
		DomainNode node = mgr.getDomain( fullDomain );
		return node != null && node.getSite() == this ? node : null;
	}

	public Stream<DomainNode> getDomains()
	{
		return mgr.getDomainsBySite( this );
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
		String key = parcel.getString( "sessions.keyName" );
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
		parcel.set( "webroot.title", title );
	}

	public Path getWebrootPath()
	{

	}

	public boolean hasDefaultSslContext()
	{
		return defaultSslContext != null;
	}

	private void mapDomain( ConfigurationSection domains ) throws SiteConfigurationException
	{
		mapDomain( domains, null, 0 );
	}

	private void mapDomain( @Nonnull ConfigurationSection domains, @Nonnull DomainMapping mapping, @Nonnegative int depth ) throws SiteConfigurationException
	{
		for ( String key : domains.getKeys() )
		{
			/* Replace underscore with dot, ignore escaped underscore. */
			String domainKey = key.replaceAll( "(?<!\\\\)_", "." ).replace( "\\_", "_" );

			if ( key.startsWith( "__" ) ) // Configuration Directive
			{
				if ( depth == 0 || mapping == null )
					throw new SiteConfigurationException( String.format( "Domain configuration directive [%s.%s] is not allowed here.", domains.getCurrentPath(), key ) );
				mapping.putConfig( key.substring( 2 ), domains.getString( key ) );

				if ( "__default".equals( key ) && domains.getBoolean( key ) )
				{
					if ( defDomain != null )
						throw new SiteConfigurationException( String.format( "Domain configuration at [%s] is invalid, the DEFAULT domain was previously set to [%s]", domains.getCurrentPath(), defDomain ) );
					defDomain = mapping.getFullDomain();
				}
			}
			else if ( domains.isConfigurationSection( key ) ) // Child Domain
				try
				{
					DomainMapping mappingNew = mapping == null ? getMappings( domainKey ).findFirst().get() : mapping.getChildMapping( domainKey );
					mappingNew.map();
					mapDomain( domains.getConfigurationSection( key ), mappingNew, depth + 1 );
				}
				catch ( IllegalStateException e )
				{
					/* Convert the IllegalStateException to a proper SiteConfigurationException */
					throw new SiteConfigurationException( e );
				}
			else /* Invalid Directive */
				WebrootManager.L.warning( String.format( "Site configuration path [%s.%s] is invalid, domain directives MUST start with a double underscore (e.g., __key) and child domains must be a (empty) YAML section (e.g., {}).", domains.getCurrentPath(), key ) );
		}
	}

	public File resourceFile( String file ) throws FileNotFoundException
	{
		Objs.notNull( file, "File can't be null" );

		if ( file.length() == 0 )
			throw new FileNotFoundException( "File can't be empty!" );

		File root = getResourceDirectory();

		File packFile = new File( root, file );

		if ( packFile.exists() )
			return packFile;

		root = packFile.getParentFile();

		if ( root.exists() && root.isDirectory() )
		{
			File[] files = root.listFiles();
			Map<String, File> found = Maps.newLinkedHashMap();
			List<String> preferred = ScriptingContext.getPreferredExtensions();

			for ( File child : files )
				if ( child.getName().startsWith( packFile.getName() + "." ) )
					found.put( child.getName().substring( packFile.getName().length() + 1 ).toLowerCase(), child );

			if ( found.size() > 0 )
			{
				if ( preferred.size() > 0 )
					for ( String ext : preferred )
						if ( found.containsKey( ext.toLowerCase() ) )
							return found.get( ext.toLowerCase() );

				return found.values().toArray( new File[0] )[0];
			}
		}

		throw new FileNotFoundException( String.format( "Could not find the file '%s' file in webroot '%s' resource directory '%s'.", file, getId(), root.getAbsolutePath() ) );
	}

	public File resourcePackage( @Nonnull String pack ) throws FileNotFoundException
	{
		if ( pack.length() == 0 )
			throw new FileNotFoundException( "Package can't be empty!" );

		return resourceFile( pack.replace( ".", IO.PATH_SEPERATOR ) );
	}

	public void save() throws IOException
	{
		save( false );
	}

	public void save( boolean force ) throws IOException
	{
		File file = getConfigFile();
		if ( file != null && ( file.exists() || force ) )
			parcel.save( file );
	}

	public void setGlobal( String key, Object val )
	{
		binding.setVariable( key, val );
	}

	@Override
	public String toString()
	{
		return "Site{id=" + getId() + ",title=" + getTitle() + ",domains=" + getDomains().map( n -> n.getFullDomain() ).collect( Collectors.joining( "," ) ) + ",ips=" + ips.stream().collect( Collectors.joining( "," ) ) + ",siteDir=" + directory.getAbsolutePath() + "}";
	}

	public void unload()
	{
		// Do Nothing
	}
}
