/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.http.session;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import io.amelia.storage.HoneyStorageProvider;
import io.amelia.foundation.ConfigData;
import io.amelia.foundation.ConfigRegistry;
import io.amelia.foundation.Env;
import io.amelia.foundation.Foundation;
import io.amelia.foundation.Kernel;
import io.amelia.foundation.tasks.Tasks;
import io.amelia.foundation.tasks.Ticks;
import io.amelia.http.HoneyCookie;
import io.amelia.http.session.adapters.FileAdapter;
import io.amelia.http.session.adapters.SqlAdapter;
import io.amelia.http.webroot.Webroot;
import io.amelia.lang.SessionException;
import io.amelia.lang.StartupException;
import io.amelia.networking.Networking;
import io.amelia.support.DateAndTime;
import io.amelia.support.Encrypt;
import io.amelia.support.EnumColor;
import io.amelia.support.IO;
import io.amelia.support.StorageConversions;
import io.amelia.support.Streams;
import io.amelia.support.Strs;

/**
 * Persistence manager handles sessions kept in memory. It also manages when to unload the session to free memory.
 */
public class SessionRegistry
{
	public static final int MANUAL = 0;
	public static final int EXPIRED = 1;
	public static final int MAXPERIP = 2;
	public static final String PATH_SESSIONS = "__sessions";
	public final static Kernel.Logger L = Kernel.getLogger( SessionRegistry.class );

	private final static List<Session> sessions = new CopyOnWriteArrayList<>();
	private static SessionAdapterImpl datastore = null;
	private static boolean isCleanupRunning = false;

	static
	{
		Kernel.setPath( PATH_SESSIONS, Kernel.PATH_STORAGE, "sessions" );

		FileSystem backend;
		Path path;
		switch ( getDefaultBackend() )
		{
			case FILE:
				backend = FileSystems.getDefault();
				path = Kernel.getPath( PATH_SESSIONS );
				break;
			case SQL:
				backend = HoneyStorageProvider.newFileSystem();
				path = backend.getPath( "/" );
				break;
			default:
				throw new SessionException.Runtime( "The session backend is not set." );
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
			throw new SessionException.Runtime( e );
		}


		try
		{
			if ( "db".equalsIgnoreCase( datastoreType ) || "database".equalsIgnoreCase( datastoreType ) || "sql".equalsIgnoreCase( datastoreType ) )
				if ( StorageModule.i().getDatabase() == null )
					L.severe( "Session Manager's datastore is configured to use database but the server's database is unconfigured. Falling back to the file datastore." );
				else
					datastore = new SqlAdapter();

			if ( "file".equalsIgnoreCase( datastoreType ) || datastore == null )
				if ( !FileAdapter.getSessionsDirectory().canWrite() )
					L.severe( "Session Manager's datastore is configured to use the file system but we can't write to the directory `" + FileAdapter.getSessionsDirectory().getAbsolutePath() + "`. Falling back to the memory datastore, i.e., sessions will not be saved." );
				else
					datastore = new FileAdapter();

			if ( datastore == null )
				datastore = new MemoryDatastore();

			for ( SessionData data : datastore.getSessions() )
				try
				{
					sessions.add( new Session( data ) );
				}
				catch ( SessionException.Error e )
				{
					// If there is a problem with the session, make warning and destroy
					L.warning( e.getMessage() );
					data.destroy();
				}
				catch ( Throwable t )
				{
					t.printStackTrace();
					data.destroy();
				}
		}
		catch ( Throwable t )
		{
			throw new StartupException( "There was a problem initializing the Session Manager", t );
		}

		/*
		 * This schedules the Session Manager with the Scheduler to run every 5 minutes (by default) to cleanup sessions.
		 */
		Tasks.scheduleAsyncRepeatingTask( Foundation.getApplication(), 0L, Ticks.MINUTE * ConfigRegistry.config.getValue( Networking.Config.SESSION_CLEANUP_INTERVAL ), SessionRegistry::sessionCleanup );
	}

	/**
	 * Gets the Default Session Name
	 *
	 * @return Session Name as string
	 */
	public static String getDefaultSessionName()
	{
		return "_ws" + Strs.capitalizeWordsFully( ConfigRegistry.config.getValue( Networking.Config.SESSION_COOKIE_NAME ) ).replace( " ", "" );
	}

	/**
	 * Gets the Default Session Timeout in seconds.
	 *
	 * @return Session timeout in seconds
	 */
	public static int getDefaultTimeout()
	{
		return ConfigRegistry.config.getValue( Networking.Config.SESSION_TIMEOUT_DEFAULT );
	}

	/**
	 * Gets the Default Timeout in seconds with additional time added for a login being present
	 *
	 * @return Session timeout in seconds
	 */
	public static int getDefaultTimeoutWithLogin()
	{
		return ConfigRegistry.config.getValue( Networking.Config.SESSION_TIMEOUT_LOGIN );
	}

	/**
	 * Gets the Default Timeout in second with additional time added for a login being present and the user checking the "Remember Me" checkbox
	 *
	 * @return Session timeout in seconds
	 */
	public static int getDefaultTimeoutWithRememberMe()
	{
		return ConfigRegistry.config.getValue( Networking.Config.SESSION_TIMEOUT_EXTENDED );
	}

	/**
	 * Is the Session Manager is debug mode, i.e., mean more debug will output to the console
	 *
	 * @return True if we are
	 */
	public static boolean isDebug()
	{
		return ConfigRegistry.config.getValue( Networking.Config.SESSION_DEBUG );
	}

	/**
	 * Creates a fresh {@link Session} and saves it's reference.
	 *
	 * @param wrapper The {@link SessionWrapper} to reference
	 *
	 * @return The hot out of the oven Session
	 *
	 * @throws SessionException.Error If there was a problem - seriously!
	 */
	public static Session createSession( SessionWrapper wrapper ) throws SessionException.Error
	{
		Session session = new Session( datastore.createSession( sessionIdBaker(), wrapper ) );
		session.newSession = true;
		sessions.add( session );
		return session;
	}

	/**
	 * Gets an unmodifiable list of currently loaded {@link Session}s
	 *
	 * @return A unmodifiable list of sessions
	 */
	public static List<Session> getSessions()
	{
		return Collections.unmodifiableList( sessions );
	}

	/**
	 * Retrieves a list of {@link Session}s based on the Ip Address provided.
	 *
	 * @param ipAddress The Ip Address to check for
	 *
	 * @return A List of Sessions that matched
	 */
	public static List<Session> getSessionsByIp( String ipAddress )
	{
		return sessions.stream().filter( s -> s.getIpAddresses() != null && s.getIpAddresses().contains( ipAddress ) ).collect( Collectors.toList() );
	}

	/**
	 * Reloads the currently loaded sessions from their Datastore
	 *
	 * @throws SessionException.Error If there was problems
	 */
	public void reload() throws SessionException.Error
	{
		synchronized ( sessions )
		{
			// Run session cleanup before saving sessions
			sessionCleanup();

			// XXX Are we sure we want to override existing sessions without saving?
			for ( Session session : sessions )
				session.reload();
		}
	}

	public static void sessionCleanup()
	{
		if ( isCleanupRunning )
			return;
		isCleanupRunning = true;

		int cleanupCount = 0;

		Set<String> knownIps = new HashSet<>();

		for ( Session session : sessions )
			if ( session.getTimeout() > 0 && session.getTimeout() < DateAndTime.epoch() )
				try
				{
					cleanupCount++;
					session.destroy( SessionRegistry.EXPIRED );
				}
				catch ( SessionException.Error e )
				{
					L.severe( "SessionException: " + e.getMessage() );
				}
			else
				knownIps.addAll( session.getIpAddresses() );

		int maxPerIp = ConfigRegistry.config.getValue( Networking.Config.SESSION_MAX_PER_IP );

		for ( String ip : knownIps )
		{
			List<Session> sessions = getSessionsByIp( ip );
			if ( sessions.size() > maxPerIp )
			{
				Map<Long, Session> sorted = new TreeMap<>();

				for ( Session s : sessions )
				{
					long key = s.getTimeout();
					while ( sorted.containsKey( key ) )
						key++;
					sorted.put( key, s );
				}

				Session[] sortedArray = sorted.values().toArray( new Session[0] );

				for ( int i = 0; i < sortedArray.length - maxPerIp; i++ )
					try
					{
						cleanupCount++;
						sortedArray[i].destroy( SessionRegistry.MAXPERIP );
					}
					catch ( SessionException.Error e )
					{
						L.severe( "SessionException: " + e.getMessage() );
					}
			}
		}

		if ( cleanupCount > 0 )
			L.info( EnumColor.DARK_AQUA + "The cleanup task recycled " + cleanupCount + " session(s)." );

		isCleanupRunning = false;
	}

	/**
	 * Generates a random Session Id based on randomness.
	 *
	 * @return Random Session Id as a string
	 */
	public static String sessionIdBaker()
	{
		return Encrypt.md5Hex( Encrypt.randomize( Encrypt.random(), "$e$$i0n_R%ND0Mne$$" ) + DateAndTime.epoch() );
	}

	/**
	 * Finalizes the Session Manager for Shutdown
	 */
	public static void shutdown()
	{
		synchronized ( sessions )
		{
			for ( Session session : sessions )
				try
				{
					session.save();
					session.unload();
				}
				catch ( SessionException.Error e )
				{
					// Ignore
				}

			sessions.clear();
		}
	}

	public static Session startSession( SessionWrapper wrapper ) throws SessionException.Error
	{
		HoneyCookie cookie = wrapper.getServerCookie( wrapper.getWebroot().getSessionKey() ).ifAbsentGet( () -> wrapper.getServerCookie( getDefaultSessionName() ) ).orElse( null );
		Session session = null;

		if ( cookie != null )
			session = sessions.stream().filter( s -> s != null && cookie.value().equals( s.getSessionId() ) ).findFirst().orElse( null );

		if ( session == null )
			session = createSession( wrapper );

		session.registerWrapper( wrapper );

		// L.debug( "Debug: IpAddress " + wrapper.getIpAddress() + " | Loaded? " + session.data.stale + " | Expires " + ( session.getTimeout() - CommonFunc.getEpoch() ) );

		return session;
	}

	private static Backend getDefaultBackend()
	{
		return Backend.FILE;
	}

	// int defaultLife = ( getSite().getYaml() != null ) ? getSite().getYaml().getInt( "sessions.lifetimeDefault", 604800 ) : 604800;
	// timeout = CommonFunc.getEpoch() + AppConfig.get().getInt( "sessions.defaultTimeout", 3600 );

	private SessionRegistry()
	{
		// Static Access
	}

	public enum Backend
	{
		MEMORY,
		FILE,
		SQL
	}
}
