/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.http.session;

import com.chiorichan.net.http.HttpCookie;
import com.chiorichan.tasks.TaskManager;
import com.chiorichan.tasks.Ticks;
import com.chiorichan.tasks.Timings;
import com.chiorichan.utils.UtilEncryption;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import io.amelia.foundation.ConfigRegistry;
import io.amelia.foundation.Kernel;
import io.amelia.http.session.adapters.FileAdapter;
import io.amelia.http.session.adapters.SqlAdapter;
import io.amelia.lang.ApplicationException;
import io.amelia.lang.EnumColor;
import io.amelia.lang.SessionException;
import io.amelia.lang.StartupException;
import io.amelia.storage.StorageModule;
import io.amelia.support.Strs;

/**
 * Persistence manager handles sessions kept in memory. It also manages when to unload the session to free memory.
 */
public class Sessions
{
	public static final int MANUAL = 0;
	public static final int EXPIRED = 1;
	public static final int MAXPERIP = 2;
	public static final String PATH_SESSIONS = "__sessions";
	public final static Kernel.Logger L = Kernel.getLogger( Sessions.class );
	static boolean isDebug = false;
	static List<Session> sessions = new CopyOnWriteArrayList<>();

	static
	{
		Kernel.setPath( PATH_SESSIONS, Kernel.PATH_STORAGE, "sessions" );
	}

	/**
	 * Gets the Default Session Name
	 *
	 * @return Session Name as string
	 */
	public static String getDefaultSessionName()
	{
		return "_ws" + Strs.capitalizeWordsFully( ConfigRegistry.config.getString( Sessions.ConfigKeys.DEFAULT_COOKIE_NAME ).orElse( "SessionId" ) ).replace( " ", "" ) );
	}

	/**
	 * Gets the Default Session Timeout in seconds.
	 *
	 * @return Session timeout in seconds
	 */
	public static int getDefaultTimeout()
	{
		return ConfigRegistry.i().getInt( "sessions.defaultTimeout", 3600 );
	}

	/**
	 * Gets the Default Timeout in seconds with additional time added for a login being present
	 *
	 * @return Session timeout in seconds
	 */
	public static int getDefaultTimeoutWithLogin()
	{
		return ConfigRegistry.i().getInt( "sessions.defaultTimeoutWithLogin", 86400 );
	}

	/**
	 * Gets the Default Timeout in second with additional time added for a login being present and the user checking the "Remember Me" checkbox
	 *
	 * @return Session timeout in seconds
	 */
	public static int getDefaultTimeoutWithRememberMe()
	{
		return ConfigRegistry.i().getInt( "sessions.defaultTimeoutRememberMe", 604800 );
	}

	public static Sessions i()
	{
		return Kernel.getServiceProvider( Sessions.class ).assemble();
	}

	/**
	 * Is the Session Manager is debug mode, i.e., mean more debug will output to the console
	 *
	 * @return True if we are
	 */
	public static boolean isDebug()
	{
		return isDebug;// || Versioning.isDevelopment();
	}

	SessionAdapterImpl datastore = null;
	private boolean isCleanupRunning = false;

	private Sessions()
	{

	}

	/**
	 * Creates a fresh {@link Session} and saves it's reference.
	 *
	 * @param wrapper The {@link SessionWrapper} to reference
	 *
	 * @return The hot out of the oven Session
	 *
	 * @throws SessionException If there was a problem - seriously!
	 */
	public Session createSession( SessionWrapper wrapper ) throws SessionException
	{
		Session session = new Session( datastore.createSession( sessionIdBaker(), wrapper ) );
		session.newSession = true;
		sessions.add( session );
		return session;
	}

	@Override
	public String getName()
	{
		return "SessionManager";
	}

	@Override
	public String getName()
	{
		return "SessionManager";
	}

	/**
	 * Gets an unmodifiable list of currently loaded {@link Session}s
	 *
	 * @return A unmodifiable list of sessions
	 */
	public List<Session> getSessions()
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
	public List<Session> getSessionsByIp( String ipAddress )
	{
		return sessions.stream().filter( s -> s.getIpAddresses() != null && s.getIpAddresses().contains( ipAddress ) ).collect( Collectors.toList() );
	}

	@Override
	public boolean isEnabled()
	{
		return true;
	}

	@Override
	public void onServiceDisable() throws ApplicationException
	{

	}

	@Override
	public void onServiceEnable() throws ApplicationException
	{

	}

	/**
	 * Initializes the Session Manager
	 *
	 * @throws StartupException If there was any problems
	 */
	@Override
	public void onServiceLoad() throws StartupException
	{
		try
		{
			isDebug = ConfigRegistry.i().getBoolean( "sessions.debug" );

			String datastoreType = ConfigRegistry.i().getString( "sessions.datastore", "file" );

			if ( "db".equalsIgnoreCase( datastoreType ) || "database".equalsIgnoreCase( datastoreType ) || "sql".equalsIgnoreCase( datastoreType ) )
				if ( StorageModule.i().getDatabase() == null )
					getLogger().severe( "Session Manager's datastore is configured to use database but the server's database is unconfigured. Falling back to the file datastore." );
				else
					datastore = new SqlAdapter();

			if ( "file".equalsIgnoreCase( datastoreType ) || datastore == null )
				if ( !FileAdapter.getSessionsDirectory().canWrite() )
					getLogger().severe( "Session Manager's datastore is configured to use the file system but we can't write to the directory `" + FileAdapter.getSessionsDirectory().getAbsolutePath() + "`. Falling back to the memory datastore, i.e., sessions will not be saved." );
				else
					datastore = new FileAdapter();

			if ( datastore == null )
				datastore = new MemoryDatastore();

			for ( SessionData data : datastore.getSessions() )
				try
				{
					sessions.add( new Session( data ) );
				}
				catch ( SessionException e )
				{
					// If there is a problem with the session, make warning and destroy
					getLogger().warning( e.getMessage() );
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
		TaskManager.instance().scheduleAsyncRepeatingTask( this, 0L, Ticks.MINUTE * ConfigRegistry.i().getInt( "sessions.cleanupInterval", 5 ), new Runnable()
		{
			@Override
			public void run()
			{
				sessionCleanup();
			}
		} );
	}

	/**
	 * Reloads the currently loaded sessions from their Datastore
	 *
	 * @throws SessionException If there was problems
	 */
	public void reload() throws SessionException
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

	public void sessionCleanup()
	{
		if ( isCleanupRunning )
			return;
		isCleanupRunning = true;

		int cleanupCount = 0;

		Set<String> knownIps = Sets.newHashSet();

		for ( Session session : sessions )
			if ( session.getTimeout() > 0 && session.getTimeout() < Timings.epoch() )
				try
				{
					cleanupCount++;
					session.destroy( Sessions.EXPIRED );
				}
				catch ( SessionException e )
				{
					getLogger().severe( "SessionException: " + e.getMessage() );
				}
			else
				knownIps.addAll( session.getIpAddresses() );

		int maxPerIp = ConfigRegistry.i().getInt( "sessions.maxSessionsPerIP", 6 );

		for ( String ip : knownIps )
		{
			List<Session> sessions = getSessionsByIp( ip );
			if ( sessions.size() > maxPerIp )
			{
				Map<Long, Session> sorted = Maps.newTreeMap();

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
						sortedArray[i].destroy( Sessions.MAXPERIP );
					}
					catch ( SessionException e )
					{
						getLogger().severe( "SessionException: " + e.getMessage() );
					}
			}
		}

		if ( cleanupCount > 0 )
			getLogger().info( EnumColor.DARK_AQUA + "The cleanup task recycled " + cleanupCount + " session(s)." );

		isCleanupRunning = false;
	}

	/**
	 * Generates a random Session Id based on randomness.
	 *
	 * @return Random Session Id as a string
	 */
	public String sessionIdBaker()
	{
		return UtilEncryption.md5( UtilEncryption.randomize( UtilEncryption.random(), "$e$$i0n_R%ND0Mne$$" ) + System.currentTimeMillis() );
	}

	/**
	 * Finalizes the Session Manager for Shutdown
	 */
	public void shutdown()
	{
		synchronized ( sessions )
		{
			for ( Session session : sessions )
				try
				{
					session.save();
					session.unload();
				}
				catch ( SessionException e )
				{
					// Ignore
				}

			sessions.clear();
		}
	}

	public Session startSession( SessionWrapper wrapper ) throws SessionException
	{
		HttpCookie cookie = wrapper.getServerCookie( wrapper.getLocation().getSessionKey(), getDefaultSessionName() );
		Session session = null;

		if ( cookie != null )
			session = sessions.stream().filter( s -> s != null && cookie.getValue().equals( s.getSessionId() ) ).findFirst().orElse( null );

		if ( session == null )
			session = createSession( wrapper );

		session.registerWrapper( wrapper );

		// getLogger().debug( "Debug: IpAddress " + wrapper.getIpAddress() + " | Loaded? " + session.data.stale + " | Expires " + ( session.getTimeout() - CommonFunc.getEpoch() ) );

		return session;
	}

	public enum SessionDestroyReason
	{

	}

	public static class ConfigKeys
	{
		public static final String DEFAULT_COOKIE_NAME = "networking.http.session.defaultCookieName";
	}

	// int defaultLife = ( getSite().getYaml() != null ) ? getSite().getYaml().getInt( "sessions.lifetimeDefault", 604800 ) : 604800;
	// timeout = CommonFunc.getEpoch() + AppConfig.get().getInt( "sessions.defaultTimeout", 3600 );
}
