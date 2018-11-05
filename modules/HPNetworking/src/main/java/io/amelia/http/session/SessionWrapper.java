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

import com.chiorichan.account.AccountAttachment;
import com.chiorichan.account.AccountInstance;
import com.chiorichan.account.AccountMeta;
import com.chiorichan.account.AccountPermissible;
import com.chiorichan.net.http.HttpCookie;
import com.chiorichan.permission.PermissibleEntity;
import com.chiorichan.site.Site;
import com.chiorichan.site.SiteModule;
import com.chiorichan.utils.UtilStrings;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import io.amelia.foundation.ConfigRegistry;
import io.amelia.http.HoneyCookie;
import io.amelia.http.webroot.Webroot;
import io.amelia.lang.ApplicationException;
import io.amelia.lang.SessionException;
import io.amelia.messaging.MessageSender;
import io.amelia.scripting.BindingProvider;
import io.amelia.scripting.ScriptBinding;
import io.amelia.scripting.ScriptingFactory;
import io.amelia.support.Strs;
import io.amelia.support.Voluntary;
import io.netty.handler.codec.http.cookie.Cookie;

/**
 * Acts as a bridge between the session and the end user.
 * TODO If Session is nullified, we need to start a new one
 */
public abstract class SessionWrapper implements BindingProvider, AccountAttachment
{
	/**
	 * The binding specific to this request
	 */
	private ScriptBinding binding = new ScriptBinding();

	/**
	 * The EvalFactory used to process scripts of this request
	 */
	private ScriptingFactory factory;

	/**
	 * The session associated with this request
	 */
	private Session session;

	/**
	 * Used to nullify a SessionWrapper and prepare it for collection by the GC
	 * something that should happen naturally but the simpler the better.
	 * <p>
	 * Sidenote: This is only for cleaning up a Session Wrapper, cleaning up an actual parent session is a whole different story.
	 */
	public void finish()
	{
		if ( session != null )
		{
			Map<String, Object> bindings = session.globals;
			Map<String, Object> variables = binding.getVariables();
			List<String> disallow = Arrays.asList( "out", "request", "response", "context" );

			/*
			 * We transfer any global variables back into our parent session like so.
			 * We also check to make sure keys like [out, _request, _response, _FILES, _REQUEST, etc...] are excluded.
			 */
			if ( variables != null )
				for ( Entry<String, Object> e : variables.entrySet() )
					if ( !disallow.contains( e.getKey() ) && !( e.getKey().startsWith( "_" ) && Strs.isUppercase( e.getKey() ) ) )
						bindings.put( e.getKey(), e.getValue() );

			/*
			 * Session Wrappers use a WeakReference but by doing this we are making sure we are GC'ed sooner rather than later
			 */
			session.removeWrapper( this );
		}

		/**
		 * Clearing references to these classes, again for easier GC cleanup.
		 */
		session = null;
		factory = null;
		binding = null;

		/**
		 * Active connections should be closed here
		 */
		finish0();
	}

	protected abstract void finish0();

	@Override
	public ScriptBinding getBinding()
	{
		return binding;
	}

	public abstract Voluntary<Cookie, ApplicationException.Error> getCookie( String key );

	public abstract Stream<Cookie> getCookies();

	@Override
	public String getDisplayName()
	{
		return getSession().getDisplayName();
	}

	public Object getGlobal( String key )
	{
		return binding.getVariable( key );
	}

	@Override
	public String getId()
	{
		return getSession().getId();
	}

	public abstract Webroot getWebroot();

	@Override
	public final AccountPermissible getPermissible()
	{
		return session;
	}

	@Override
	public PermissibleEntity getPermissibleEntity()
	{
		return getSession().getPermissibleEntity();
	}

	@Override
	public ScriptingFactory getScriptingFactory()
	{
		return factory;
	}

	protected abstract Voluntary<HoneyCookie, SessionException.Error> getServerCookie( String key );

	/**
	 * Gets the Session
	 *
	 * @return The session
	 */
	public final Session getSession()
	{
		if ( session == null )
			throw new IllegalStateException( "getSession() was called before startSession(). Possible race-condition programming bug." );
		return session;
	}

	@Override
	public String getVariable( String key )
	{
		return getSession().getVariable( key );
	}

	@Override
	public String getVariable( String key, String def )
	{
		return getSession().getVariable( key, def );
	}

	public final boolean hasSession()
	{
		return session != null;
	}

	@Override
	public AccountInstance i()
	{
		return session.i();
	}

	@Override
	public boolean isInitialized()
	{
		return session.isInitialized();
	}

	@Override
	public AccountMeta meta()
	{
		return session.meta();
	}

	@Override
	public void sendMessage( MessageSender sender, Object... objs )
	{
		// Do Nothing
	}

	@Override
	public void sendMessage( Object... objs )
	{
		// Do Nothing
	}

	protected abstract void sessionStarted();

	public void setGlobal( String key, Object val )
	{
		binding.setVariable( key, val );
	}

	@Override
	public void setVariable( String key, String value )
	{
		getSession().setVariable( key, value );
	}

	/**
	 * Starts the session
	 *
	 * @throws SessionException
	 */
	public Session startSession() throws SessionException.Error
	{
		session = SessionRegistry.i().startSession( this );
		/*
		 * Create our Binding
		 */
		binding = new ScriptBinding( new HashMap<String, Object>( session.getGlobals() ) );

		/*
		 * Create our EvalFactory
		 */
		factory = ScriptingFactory.create( this );

		/*
		 * Reference Session Variables
		 */
		binding.setVariable( "_SESSION", session.data.data );

		Site site = getWebroot();

		if ( site == null )
			site = SiteModule.i().getDefaultSite();

		session.setWebroot( site );

		for ( HttpCookie cookie : getCookies() )
			session.putSessionCookie( cookie.getKey(), cookie );

		// Reference Context
		binding.setVariable( "context", this );

		// Reset __FILE__ Variable
		binding.setVariable( "__FILE__", site.directoryPublic() );

		if ( ConfigRegistry.i().getBoolean( "sessions.rearmTimeoutWithEachRequest" ) )
			session.rearmTimeout();

		sessionStarted();

		return session;
	}

	// TODO: Future add of setDomain, setCookieName, setSecure (http verses https)
}
