/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <theameliadewitt@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.http.session;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.amelia.foundation.ConfigRegistry;
import io.amelia.lang.SessionException;
import io.amelia.support.DateAndTime;
import io.amelia.support.EnumColor;
import io.amelia.support.Objs;
import io.amelia.support.WeakReferenceList;
import sun.security.krb5.internal.crypto.Nonce;

/**
 * This class is used to carry data that is to be persistent from request to request.
 * If you need to sync data across requests then we recommend using Session Vars for Security.
 */
public final class Session extends AccountPermissible implements Kickable
{
	/**
	 * The underlying data for this session<br>
	 * Preserves access to the datastore and it's methods {@link SessionData#save()}, {@link SessionData#reload()}, {@link SessionData#destroy()}
	 */
	final SessionData data;
	/**
	 * Global session variables<br>
	 * Globals will not live outside of the session's life
	 */
	final Map<String, Object> globals = new LinkedHashMap<>();
	/**
	 * History of changes made to the variables since last {@link #save()}
	 */
	private final Set<String> dataChangeHistory = new HashSet<>();
	/**
	 * Holds a set of known IP Addresses
	 */
	private final Set<String> knownIps = new HashSet<>();
	/**
	 * The sessionId of this session
	 */
	private final String sessionId;
	/**
	 * Reference to each wrapper that is utilizing this session<br>
	 * We use a WeakReference so they can still be reclaimed by the GC
	 */
	private final WeakReferenceList<SessionWrapper> wrappers = new WeakReferenceList<>();
	// Indicates if the Session has been unloaded or destroyed!
	boolean isInvalidated = false;
	boolean newSession = false;
	private Nonce nonce = null;
	/**
	 * Number of times this session has been requested<br>
	 * More requests mean longer TTL
	 */
	private int requestCnt = 0;
	/**
	 * The Session Cookie
	 */
	private HttpCookie sessionCookie;
	/**
	 * Tracks session sessionCookies
	 */
	private Map<String, HttpCookie> sessionCookies = new LinkedHashMap<>();
	/**
	 * The sessionKey of this session
	 */
	private String sessionKey = Sessions.getDefaultSessionName();
	/**
	 * The site this session is bound to
	 */
	private Site site = SiteModule.i().getDefaultSite();
	/**
	 * The epoch for when this session is to be destroyed
	 */
	private long timeout = 0;

	Session( SessionData data ) throws SessionException
	{
		Validate.notNull( data );
		this.data = data;

		sessionId = data.sessionId;

		sessionKey = data.sessionName;
		timeout = data.timeout;
		knownIps.addAll( Splitter.on( "|" ).splitToList( data.ipAddress ) );
		site = SiteModule.i().getSiteById( data.site );

		if ( site == null )
		{
			site = SiteModule.i().getDefaultSite();
			data.site = site.getId();
		}

		timeout = data.timeout;


		if ( timeout > 0 && timeout < Timings.epoch() )
			throw new SessionException( String.format( "The session '%s' expired at epoch '%s', might have expired while offline or this is a bug!", sessionId, timeout ) );

		/*
		 * TODO Figure out how to track if a particular wrapper's IP changes
		 * Maybe check the original IP the wrapper was authenticated with
		 * TCP IP: ?
		 * HTTP IP: ?
		 *
		 * String origIpAddress = lastIpAddress;
		 *
		 * // Possible Session Hijacking! nullify!!!
		 * if ( lastIpAddress != null && !lastIpAddress.equals( origIpAddress ) && !Loader.getConfig().getBoolean( "sessions.allowIPChange" ) )
		 * {
		 * sessionCookie = null;
		 * lastIpAddress = origIpAddress;
		 * }
		 */

		// XXX New Session, Requested Session, Loaded Session

		if ( Sessions.isDebug() )
			Sessions.L.info( EnumColor.DARK_AQUA + "Session " + ( data.stale ? "Loaded" : "Created" ) + " `" + this + "`" );

		initialized();
	}

	public boolean changesMade()
	{
		return !isInvalidated && dataChangeHistory.size() > 0;
	}

	public void destroy() throws SessionException
	{
		destroy( Sessions.MANUAL );
	}

	public void destroy( int reasonCode ) throws SessionException
	{
		if ( Sessions.isDebug() )
			Sessions.L.info( EnumColor.DARK_AQUA + "Session Destroyed `" + this + "`" );

		EventDispatcher.i().callEvent( new SessionDestroyEvent( this, reasonCode ) );

		// Account Auth Section
		if ( "token".equals( getVariable( "auth" ) ) )
		{
			Validate.notNull( getVariable( "acctId" ) );
			Validate.notNull( getVariable( "token" ) );

			AccountAuthenticator.TOKEN.deleteToken( getVariable( "acctId" ), getVariable( "token" ) );
		}

		Sessions.sessions.remove( this );

		for ( SessionWrapper wrap : wrappers )
		{
			wrap.finish();
			unregisterAttachment( wrap );
		}
		wrappers.clear();

		timeout = DateAndTime.epoch();
		data.timeout = DateAndTime.epoch();

		if ( sessionCookie != null )
			sessionCookie.setMaxAge( 0 );

		data.destroy();
		isInvalidated = true;
	}

	public void destroyNonce()
	{
		nonce = null;
	}

	@Override
	protected void failedLogin( AccountResult result )
	{
		// Do Nothing
	}

	/**
	 * Get the present data change history
	 *
	 * @return A unmodifiable copy of dataChangeHistory.
	 */
	Set<String> getChangeHistory()
	{
		return Collections.unmodifiableSet( new HashSet<String>( dataChangeHistory ) );
	}

	/**
	 * Returns a sessionCookie if existent in the session.
	 *
	 * @param key
	 *
	 * @return Candy
	 */
	public HttpCookie getCookie( String key )
	{
		return sessionCookies.containsKey( key ) ? sessionCookies.get( key ) : new HttpCookie( key, null );
	}

	public Map<String, HttpCookie> getCookies()
	{
		return Collections.unmodifiableMap( sessionCookies );
	}

	public Map<String, String> getDataMap()
	{
		return data.data;
	}

	public Object getGlobal( String key )
	{
		return globals.get( key );
	}

	public Map<String, Object> getGlobals()
	{
		return Collections.unmodifiableMap( globals );
	}

	@Override
	public List<String> getIpAddresses()
	{
		List<String> ips = new ArrayList<>();
		for ( SessionWrapper sp : wrappers )
			if ( sp.hasSession() && sp.getSession() == this )
			{
				String ipAddress = sp.getIpAddress();
				if ( ipAddress != null && !ipAddress.isEmpty() && !ips.contains( ipAddress ) )
					ips.add( ipAddress );
			}
		return ips;
	}

	@Override
	public Site getLocation()
	{
		if ( site == null )
			return SiteModule.i().getDefaultSite();
		else
			return site;
	}

	public String getName()
	{
		return sessionKey;
	}

	public Nonce getNonce()
	{
		if ( nonce == null )
			regenNonce();
		return nonce;
	}

	public HttpCookie getSessionCookie()
	{
		return sessionCookie;
	}

	public String getSessionId()
	{
		return sessionId;
	}

	/**
	 * @return A set of active SessionProviders for this session.
	 */
	public Set<SessionWrapper> getSessionWrappers()
	{
		return wrappers.toSet();
	}

	public long getTimeout()
	{
		return timeout;
	}

	@Override
	public String getVariable( String key )
	{
		return getVariable( key, null );
	}

	@Override
	public String getVariable( String key, String def )
	{
		if ( !data.data.containsKey( key ) || data.data.get( key ) == null )
		{
			Sessions.L.fine( String.format( "%sGetting variable key `%s` which resulted in default value '%s'", EnumColor.GRAY, key, def ) );

			return def;
		}

		Sessions.L.fine( String.format( "%sGetting variable key `%s` with value '%s' and default value '%s'", EnumColor.GRAY, key, data.data.getString( key ), def ) );

		return data.data.get( key );
	}

	public boolean isInvalidated()
	{
		return isInvalidated;
	}

	public boolean isNew()
	{
		return newSession;
	}

	public boolean isSet( String key )
	{
		return data.data.containsKey( key );
	}

	@Override
	public AccountResult kick( String reason )
	{
		if ( isInvalidated )
			throw new IllegalStateException( "This session has been invalidated" );

		return logout();
	}

	/**
	 * Removes the session expiration and prevents the Session Manager from unloading or destroying sessions
	 */
	public void noTimeout()
	{
		if ( isInvalidated )
			throw new IllegalStateException( "This session has been invalidated" );

		timeout = 0;
		data.timeout = 0;
	}

	public Nonce nonce()
	{
		return nonce;
	}

	// TODO Sessions can outlive a login.
	// TODO Sessions can have an expiration in 7 days and a login can have an expiration of 24 hours.
	// TODO Remember should probably make it so logins last as long as the session does.

	@EventHandler( priority = EventPriority.NORMAL )
	public void onAccountMessageEvent( MessageEvent event )
	{

	}

	public void processSessionCookie( String domain )
	{
		if ( isInvalidated )
			throw new IllegalStateException( "This session has been invalidated" );

		// TODO Session Cookies and Session expire at the same time. - Basically, as long as a Session might become called, we keep the session in existence.
		// TODO Unload a session once it has but been used for a while but might still be called upon at anytime.

		/**
		 * Has a Session Cookie been produced yet?
		 * If not we try and create a new one from scratch
		 */
		if ( sessionCookie == null )
		{
			assert sessionId != null && !sessionId.isEmpty();

			sessionKey = getLocation().getSessionKey();
			sessionCookie = new HttpCookie( getLocation().getSessionKey(), sessionId ).setDomain( "." + domain ).setPath( "/" ).setHttpOnly( true );
			rearmTimeout();
		}

		/**
		 * Check if our current session cookie key does not match the key used by the Site.
		 * If so, we move the old session to the general cookie array and set it as expired.
		 * This usually forces the browser to delete the old session cookie.
		 */
		if ( !sessionCookie.getKey().equals( getLocation().getSessionKey() ) )
		{
			String oldKey = sessionCookie.getKey();
			sessionCookie.setKey( getLocation().getSessionKey() );
			sessionCookies.put( oldKey, new HttpCookie( oldKey, "" ).setExpiration( 0 ) );
		}
	}

	void putSessionCookie( String key, HttpCookie cookie )
	{
		if ( isInvalidated )
			throw new IllegalStateException( "This session has been invalidated" );

		sessionCookies.put( key, cookie );
	}

	public void rearmTimeout()
	{
		if ( isInvalidated )
			throw new IllegalStateException( "This session has been invalidated" );

		int defaultTimeout = Sessions.getDefaultTimeout();

		// Grant the timeout an additional 10 minutes per request, capped at one hour or 6 requests.
		requestCnt++;

		// Grant the timeout an additional 2 hours for having a user logged in.
		if ( hasLogin() )
		{
			defaultTimeout = Sessions.getDefaultTimeoutWithLogin();

			if ( UtilObjects.isTrue( getVariable( "remember", "false" ) ) )
				defaultTimeout = Sessions.getDefaultTimeoutWithRememberMe();

			if ( ConfigRegistry.i().getBoolean( "allowNoTimeoutPermission" ) && checkPermission( "com.chiorichan.noTimeout" ).isTrue() )
				defaultTimeout = Integer.MAX_VALUE;
		}

		timeout = Timings.epoch() + defaultTimeout + Math.min( requestCnt, 6 ) * 600;

		data.timeout = timeout;

		if ( sessionCookie != null )
			sessionCookie.setExpiration( timeout );
	}

	public void regenNonce()
	{
		nonce = new Nonce( this );
	}

	/**
	 * Registers a newly created wrapper with our session
	 *
	 * @param wrapper The newly created wrapper
	 */
	public void registerWrapper( SessionWrapper wrapper )
	{
		if ( isInvalidated )
			throw new IllegalStateException( "This session has been invalidated" );

		assert wrapper.getSession() == this : "SessionWrapper does not contain proper reference to this Session";

		registerAttachment( wrapper );
		wrappers.add( wrapper );
		knownIps.add( wrapper.getIpAddress() );
	}

	public void reload() throws SessionException
	{
		if ( isInvalidated )
			throw new IllegalStateException( "This session has been invalidated" );

		data.reload();
	}

	/**
	 * Sets if the user login should be remembered for a longer amount of time
	 *
	 * @param remember Should we?
	 */
	public void remember( boolean remember )
	{
		if ( isInvalidated )
			throw new IllegalStateException( "This session has been invalidated" );

		setVariable( "remember", remember ? "true" : "false" );
		rearmTimeout();
	}

	public void removeWrapper( SessionWrapper wrapper )
	{
		if ( isInvalidated )
			throw new IllegalStateException( "This session has been invalidated" );

		wrappers.remove( wrapper );
		unregisterAttachment( wrapper );
	}

	public void save() throws SessionException.Error
	{
		save( false );
	}

	public void save( boolean force ) throws SessionException.Error
	{
		if ( isInvalidated )
			throw new IllegalStateException( "This session has been invalidated" );

		if ( force || changesMade() )
		{
			data.sessionName = sessionKey;
			data.sessionId = sessionId;

			data.ipAddress = Joiner.on( "|" ).join( knownIps );

			data.save();
			dataChangeHistory.clear();
		}
	}

	public void saveWithoutException()
	{
		try
		{
			save();
		}
		catch ( SessionException.Error e )
		{
			Sessions.L.severe( "We had a problem saving the current session, changes were not saved to the datastore!", e );
		}
	}

	public void setGlobal( String key, Object val )
	{
		if ( isInvalidated )
			throw new IllegalStateException( "This session has been invalidated" );

		globals.put( key, val );
	}

	public void setSite( Site site )
	{
		if ( isInvalidated )
			throw new IllegalStateException( "This session has been invalidated" );

		Objs.notNull( site );
		this.site = site;
		data.site = site.getId();
	}

	@Override
	public void setVariable( String key, String value )
	{
		if ( isInvalidated )
			throw new IllegalStateException( "This session has been invalidated" );

		Sessions.L.info( String.format( "Setting session variable `%s` with value '%s'", key, value ) );

		if ( value == null )
			data.data.remove( key );

		data.data.put( key, value );
		dataChangeHistory.add( key );
	}

	@Override
	public void successfulLogin() throws AccountException
	{
		if ( isInvalidated )
			throw new IllegalStateException( "This session has been invalidated" );

		for ( SessionWrapper wrapper : wrappers )
			registerAttachment( wrapper );

		try
		{
			getAccount().meta().getContext().credentials().makeResumable( this );
		}
		catch ( AccountException e )
		{
			Sessions.L.severe( "We had a problem making the current login resumable!", e );
		}

		rearmTimeout();
		saveWithoutException();
	}

	@Override
	public String toString()
	{
		return "Session{key=" + sessionKey + ",id=" + sessionId + ",ipAddress=" + getIpAddresses() + ",timeout=" + timeout + ",isInvalidated=" + isInvalidated + ",data=" + data + ",requestCount=" + requestCnt + ",site=" + site + "}";
	}

	public void unload()
	{
		Sessions.L.fine( EnumColor.DARK_AQUA + "Session Unloaded `" + this + "`" );

		Sessions.sessions.remove( this );

		for ( SessionWrapper wrap : wrappers )
		{
			wrap.finish();
			unregisterAttachment( wrap );
		}
		wrappers.clear();

		isInvalidated = true;
	}
}
