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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
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
import javax.annotation.Nullable;
import javax.net.ssl.SSLException;

import io.amelia.data.TypeBase;
import io.amelia.data.apache.ApacheConfiguration;
import io.amelia.database.Database;
import io.amelia.database.DatabaseManager;
import io.amelia.database.elegant.ElegantQueryTable;
import io.amelia.events.Events;
import io.amelia.foundation.ConfigData;
import io.amelia.foundation.ConfigRegistry;
import io.amelia.foundation.Env;
import io.amelia.foundation.Foundation;
import io.amelia.foundation.Kernel;
import io.amelia.http.events.WebrootLoadEvent;
import io.amelia.http.localization.Localization;
import io.amelia.http.mappings.DomainMapping;
import io.amelia.http.mappings.DomainNode;
import io.amelia.http.mappings.DomainTree;
import io.amelia.http.routes.Routes;
import io.amelia.http.session.SessionPersistenceMethod;
import io.amelia.http.session.SessionRegistry;
import io.amelia.http.ssl.CertificateWrapper;
import io.amelia.lang.ConfigException;
import io.amelia.lang.ExceptionReport;
import io.amelia.lang.WebrootException;
import io.amelia.scripting.ScriptBinding;
import io.amelia.scripting.ScriptingContext;
import io.amelia.scripting.ScriptingFactory;
import io.amelia.scripting.ScriptingResult;
import io.amelia.support.DateAndTime;
import io.amelia.support.EnumColor;
import io.amelia.support.Exceptions;
import io.amelia.support.IO;
import io.amelia.support.NIO;
import io.amelia.support.Namespace;
import io.amelia.support.Objs;
import io.amelia.support.StorageConversions;
import io.amelia.support.StoragePolicy;
import io.amelia.support.Strs;
import io.amelia.tasks.Tasks;
import io.amelia.users.UserRoot;
import io.amelia.users.Users;
import io.netty.handler.ssl.SslContext;

public class Webroot implements UserRoot
{
	public final static TypeBase.TypeString CONFIG_TITLE = new TypeBase.TypeString( "title", "" );
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
		// config directory
		STORAGE_POLICY.setLayoutDirectory( "config", StoragePolicy.Strategy.CREATE );
		// .env
		STORAGE_POLICY.setLayoutFile( ".env", StoragePolicy.Strategy.CREATE );
		// .htaccess
		STORAGE_POLICY.setLayoutFile( "htaccess.json", StoragePolicy.Strategy.CREATE );

		// Webroot Config File
		// STORAGE_POLICY.setLayoutFile( "config.yaml", StoragePolicy.Strategy.CREATE );
		// Routes File
		// STORAGE_POLICY.setLayoutFile( "routes.json", StoragePolicy.Strategy.CREATE );
	}

	protected final ScriptBinding binding = new ScriptBinding();
	protected final List<String> cachePatterns = new ArrayList<>();
	protected final Path configurationFile;
	protected final ConfigData data;
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

	Webroot( @Nonnull String webrootId ) throws WebrootException.Error
	{
		try
		{
			this.webrootId = webrootId;

			configurationFile = null;
			data = ConfigData.empty();
			webrootTitle = Kernel.getDevMeta().getProductName();
			database = DatabaseManager.getDefault().getDatabase();

			directory = WebrootUtils.createWebrootDirectory( webrootId ).toRealPath();
			localization = new Localization( getLangDirectory() );
			routes = new Routes( this );
			env = new Env( directory.resolve( ".env" ) );
		}
		catch ( IOException e )
		{
			throw new WebrootException.Error( e );
		}
	}

	Webroot( @Nonnull Path directory, @Nonnull ConfigData data, @Nonnull Env env ) throws WebrootException.Error
	{
		try
		{
			this.directory = directory.toRealPath();
			this.configurationFile = directory.resolve( "config" );
			this.data = data;
			this.env = env;

			StorageConversions.loadToStacker( configurationFile, data );
			data.setEnvironmentVariables( env.map() );

			webrootId = data.getString( "webroot.id" ).orElseThrow( () -> new WebrootException.Error( "Webroot Id is missing!" ) ).toLowerCase();
			webrootTitle = data.getString( "webroot.title" ).orElse( ConfigRegistry.config.getValue( WebrootRegistry.Config.WEBROOTS_DEFAULT_TITLE ) );

			data.getList( "webroot.listen", ips );

			for ( String ip : ips )
				if ( !NIO.isValidIPv4( ip ) && !NIO.isValidIPv6( ip ) )
					WebrootRegistry.L.warning( String.format( "Webroot '%s' is set to listen on ip '%s', but the ip does not match the valid IPv4 or IPv6 regex formula.", webrootId, ip ) );
				else if ( !NIO.isAddressAssigned( ip ) )
					WebrootRegistry.L.warning( String.format( "Webroot '%s' is set to listen on ip '%s', but that address is not assigned to any network interfaces.", webrootId, ip ) );

			if ( ips.contains( "localhost" ) )
				throw new WebrootException.Error( "Webroots are not permitted to listen on hostname 'localhost', it is reserved for internal use only." );

			if ( WebrootRegistry.getWebrootById( webrootId ) != null )
				throw new WebrootException.Error( String.format( "There already exists a webroot by the provided webroot id '%s'", webrootId ) );

			WebrootRegistry.L.info( String.format( "Loading webroot '%s' with title '%s' from YAML file.", webrootId, webrootTitle ) );

			this.localization = new Localization( getLangDirectory() );

			if ( !data.hasValue( "webroot.web-allowed-origin" ) )
				data.setValue( "webroot.web-allowed-origin", "*" );

			mapDomain( data.getChildOrCreate( "webroot.domains" ) );

			Path ssl = getDirectory( "ssl" );
			IO.forceCreateDirectory( ssl );

			String sslCertFile = data.getString( "webroot.sslCert" ).orElse( null );
			String sslKeyFile = data.getString( "webroot.sslKey" ).orElse( null );
			String sslSecret = data.getString( "webroot.sslSecret" ).orElse( null );

			if ( sslCertFile != null && sslKeyFile != null )
			{
				Path sslCert = ssl.resolve( sslCertFile );
				Path sslKey = ssl.resolve( sslKeyFile );

				try
				{
					defaultSslContext = new CertificateWrapper( sslCert, sslKey, sslSecret ).context();
				}
				catch ( SSLException | FileNotFoundException | CertificateException | InvalidKeySpecException e )
				{
					WebrootRegistry.L.severe( String.format( "Failed to load SslContext for webroot '%s' using cert '%s', key '%s', and hasSecret? %s", webrootId, IO.relPath( sslCert ), IO.relPath( sslKey ), sslSecret != null && !sslSecret.isEmpty() ), e );
				}
			}

			if ( Exceptions.tryCatch( () -> Events.getInstance().callEventWithException( new WebrootLoadEvent( this ) ), WebrootException.Error::new ).isCancelled() )
				throw new WebrootException.Error( String.format( "Webroot '%s' was prevented from loading by an internal event.", webrootId ) );

			if ( data.hasChild( "database" ) )
				database = DatabaseManager.getInstance( getWebrootId() ).init( data.getChild( "database" ) ).getDatabase();

			routes = new Routes( this );

			data.getString( "sessions.persistenceMethod" ).ifPresent( persistenceMethod -> sessionPersistence = SessionPersistenceMethod.valueOfIgnoreCase( persistenceMethod ).orElse( null ) );

			data.getStringList( "scripts.on-load" ).ifPresent( onLoadScripts -> {
				for ( String script : onLoadScripts )
				{
					ScriptingResult result = factory.eval( WebrootScriptingContext.fromWebrootResource( this, script ).setShell( "groovy" ).setWebroot( this ) );

					ExceptionReport exceptionReport = result.getExceptionReport();
					if ( exceptionReport.hasExceptions() )
					{
						if ( exceptionReport.hasException( FileNotFoundException.class ) )
							WebrootRegistry.L.severe( String.format( "Failed to eval onLoadScript '%s' for webroot '%s' because the file was not found.", script, webrootId ) );
						else
						{
							WebrootRegistry.L.severe( String.format( "Exception caught while evaluate onLoadScript '%s' for webroot '%s'", script, webrootId ) );
							exceptionReport.printToLog( WebrootRegistry.L );
						}
					}
					else
						WebrootRegistry.L.info( String.format( "Finished evaluate onLoadScript '%s' for webroot '%s' with result: %s", script, webrootId, result.getString() ) );
				}
			} );

			ConfigData archive = data.getChildOrCreate( "archive" );

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

					WebrootRegistry.L.info( String.format( "%s%sScheduled webroot archive for %s {nextRun: %s, interval: %s}", EnumColor.AQUA, EnumColor.NEGATIVE, webrootId, nextRun, timer ) );

					Tasks.scheduleSyncRepeatingTask( Foundation.getApplication(), nextRun, timer, () -> {
						WebrootRegistry.L.info( String.format( "%s%sRunning archive for webroot %s...", EnumColor.AQUA, EnumColor.NEGATIVE, webrootId ) );

						WebrootRegistry.cleanupBackups( webrootId, ".zip", archive.getInteger( "keep" ).orElse( 3 ) );
						archive.setValue( "lastRun", DateAndTime.epoch() );

						Path dir = Kernel.getPath( WebrootRegistry.PATH_ARCHIVES ).resolve( webrootId );
						IO.forceCreateDirectory( dir );

						Path zip = dir.resolve( new SimpleDateFormat( "yyyy-MM-dd_HH-mm-ss" ).format( new Date() ) + "-" + webrootId + ".zip" );

						try
						{
							IO.zipDir( Webroot.this.getDirectory(), zip );
						}
						catch ( IOException e )
						{
							WebrootRegistry.L.severe( String.format( "%s%sFailed archiving webroot %s to %s", EnumColor.RED, EnumColor.NEGATIVE, webrootId, IO.relPath( zip ) ), e );
							return;
						}

						WebrootRegistry.L.info( String.format( "%s%sFinished archiving webroot %s to %s", EnumColor.AQUA, EnumColor.NEGATIVE, webrootId, IO.relPath( zip ) ) );
					} );
				}
				else
					WebrootRegistry.L.warning( String.format( "Failed to initialize webroot backup for webroot %s, interval did not match regex '[0-9]+[dhmsDHMS]?'.", webrootId ) );
			}
		}
		catch ( Exception e )
		{
			throw new WebrootException.Error( e );
		}
	}

	public void addDomainMapping( DomainMapping domainMapping )
	{
		mappings.add( domainMapping );
	}

	public void addToCachePatterns( String pattern )
	{
		if ( !cachePatterns.contains( pattern.toLowerCase() ) )
			cachePatterns.add( pattern.toLowerCase() );
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
		return Kernel.getPath( Kernel.PATH_CACHE ).resolve( getWebrootId() );
	}

	public List<String> getCachePatterns()
	{
		return cachePatterns;
	}

	public ConfigData getConfig()
	{
		return data;
	}

	public Path getConfigFile()
	{
		return configurationFile;
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
		return ( stream.count() == 0 ? mappings.stream() : stream ).findFirst().get();
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
	 * Same as calling {@code WebrootRegistry.instance().getDomain( fullDomain ) } but instead checks the returned node belongs to this webroot.
	 *
	 * @param fullDomain The request domain
	 *
	 * @return The DomainNode
	 */
	public DomainNode getDomain( String fullDomain )
	{
		DomainNode node = DomainTree.parseDomain( fullDomain );
		return node != null && node.getWebroot() == this ? node : null;
	}

	public Stream<DomainNode> getDomains()
	{
		return DomainTree.getChildren().filter( n -> n.getWebroot() == this );
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

	public Path getResourceFile( @Nonnull String file ) throws IOException
	{
		Objs.notEmpty( file );

		Path packFile = getResourceDirectory().resolve( file );

		if ( Files.isRegularFile( packFile ) )
			return packFile;

		Path root = packFile.getParent();

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

		throw new FileNotFoundException( String.format( "Could not find the file '%s' file in webroot '%s' resource directory '%s'.", file, getWebrootId(), root.toString() ) );
	}

	public Path getResourcePackage( @Nonnull String pack ) throws IOException
	{
		if ( pack.length() == 0 )
			throw new FileNotFoundException( "Package can't be empty!" );
		return getResourceFile( pack.replace( ".", IO.PATH_SEPERATOR ) );
	}

	public Path getResourcePath()
	{
		return directory.resolve( "resource" );
	}

	public Routes getRoutes()
	{
		return routes;
	}

	/**
	 * Gets the webroot configured Session Key from data.
	 *
	 * @return The Session Key
	 */
	public String getSessionKey()
	{
		return "_ws" + Strs.capitalizeWords( data.getString( "sessions.keyName" ).orElse( SessionRegistry.getDefaultSessionName() ) );
	}

	public SessionPersistenceMethod getSessionPersistenceMethod()
	{
		return sessionPersistence;
	}

	public String getTitle()
	{
		return webrootTitle;
	}

	public Path getUsersDirectory()
	{
		return Kernel.getPath( Users.PATH_USERS );
	}

	public List<String> getUsersFields()
	{
		return getConfig().getStringList( "users.fields" ).orElseGet( ( Supplier ) ArrayList::new );
	}

	public ElegantQueryTable getUsersTable()
	{
		return null;
	}

	public String getWebrootId()
	{
		return webrootId;
	}

	public Path getWebrootPath()
	{
		return directory;
	}

	public boolean hasDefaultSslContext()
	{
		return defaultSslContext != null;
	}

	public boolean isProtectedFilePath( Path path )
	{
		// TODO One day sub-directories could be located outside of webroots or contain symlinks, implement a location directive type system.
		return !path.startsWith( getDirectory() );
	}

	private void mapDomain( @Nonnull ConfigData domains ) throws WebrootException.Configuration
	{
		mapDomain( domains, null, 0 );
	}

	private void mapDomain( @Nonnull ConfigData domains, @Nullable DomainMapping mapping, @Nonnegative int depth ) throws WebrootException.Configuration
	{
		for ( Namespace key : domains.getKeys() )
		{
			/* Replace underscore with dot, ignore escaped underscore. */
			// String domainKey = key.replaceAll( "(?<!\\\\)_", "." ).replace( "\\_", "_" );

			if ( key.startsWith( "__" ) ) // Configuration Directive
			{
				if ( depth == 0 || mapping == null )
					throw new WebrootException.Configuration( String.format( "Domain data directive [%s.%s] is not allowed here.", domains.getCurrentPath(), key ) );
				mapping.putConfig( key.getString().substring( 2 ), domains.getString( key ).orElse( null ) );

				if ( "__default".equals( key ) && domains.getBoolean( key ).orElse( false ) )
				{
					if ( defDomain != null )
						throw new WebrootException.Configuration( String.format( "Domain data at [%s] is invalid, the DEFAULT domain was previously set to [%s]", domains.getCurrentPath(), defDomain ) );
					defDomain = mapping.getFullDomain();
				}
			}
			else if ( domains.hasChild( key ) ) // Child Domain
				try
				{
					DomainMapping mappingNew = mapping == null ? getMappings( key.getString( "." ) ).findFirst().get() : mapping.getChildMapping( key.getString( "." ) );
					mappingNew.map();
					mapDomain( domains.getChildOrCreate( key ), mappingNew, depth + 1 );
				}
				catch ( IllegalStateException e )
				{
					/* Convert the IllegalStateException to a proper WebrootException.Configuration */
					throw new WebrootException.Configuration( e );
				}
			else /* Invalid Directive */
				WebrootRegistry.L.warning( String.format( "Webroot data path [%s.%s] is invalid, domain directives MUST start with a double underscore (e.g., __key) and child domains must be a (empty) YAML section (e.g., {}).", domains.getCurrentPath(), key ) );
		}
	}

	public void save() throws IOException
	{
		save( false );
	}

	public void save( boolean force ) throws IOException
	{
		// TODO Implement a similar save system to the ConfigRegistry.
		// if ( configFile != null && ( Files.isRegularFile( configFile ) || force ) )
		// IO.writeStringToPath( ConfigDataLoader.encodeYaml( data ), configFile );
	}

	public void setGlobal( String key, Object val )
	{
		binding.setVariable( key, val );
	}

	public void setTitle( String title ) throws ConfigException.Error
	{
		data.setValue( CONFIG_TITLE, title );
		webrootTitle = title;
	}

	@Override
	public String toString()
	{
		return "Webroot{id=" + getWebrootId() + ",title=" + getTitle() + ",domains=" + getDomains().map( DomainNode::getFullDomain ).collect( Collectors.joining( "," ) ) + ",ips=" + ips.stream().collect( Collectors.joining( "," ) ) + ",WebrootDir=" + IO.relPath( directory ) + "}";
	}
}
