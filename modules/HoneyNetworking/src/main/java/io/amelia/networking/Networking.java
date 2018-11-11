/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia Sara Greene <barelyaprincess@gmail.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.networking;

import io.amelia.data.TypeBase;
import io.amelia.foundation.Kernel;
import io.amelia.foundation.bindings.Bindings;

public class Networking
{
	public static final Kernel.Logger L = Kernel.getLogger( Networking.class );

	public static NetworkingService i()
	{
		return Bindings.getSystemNamespace().getFacadeBinding( "", NetworkingService.class );
	}

	public static class HTTP
	{
		private HTTP()
		{
			// Static Access
		}


	}

	public static class TCP
	{
		private TCP()
		{
			// Static Access
		}


	}

	public static class UDP
	{
		private UDP()
		{
			// Static Access
		}


	}

	public static class Config
	{
		public final static TypeBase NET_BASE = new TypeBase( "net" );

		public final static TypeBase SESSION_BASE = new TypeBase( NET_BASE, "sessions" );
		public final static TypeBase.TypeBoolean SESSION_DEBUG = new TypeBase.TypeBoolean( SESSION_BASE, "debug", false );
		public final static TypeBase SESSION_TIMEOUT = new TypeBase( SESSION_BASE, "timeout" );
		public final static TypeBase.TypeInteger SESSION_TIMEOUT_DEFAULT = new TypeBase.TypeInteger( SESSION_TIMEOUT, "default", 3600 );
		public final static TypeBase.TypeInteger SESSION_TIMEOUT_LOGIN = new TypeBase.TypeInteger( SESSION_TIMEOUT, "login", 86400 );
		public final static TypeBase.TypeInteger SESSION_TIMEOUT_EXTENDED = new TypeBase.TypeInteger( SESSION_TIMEOUT, "extended", 604800 );
		public final static TypeBase.TypeString SESSION_COOKIE_NAME = new TypeBase.TypeString( SESSION_BASE, "defaultCookieName", "SessionId" );
		public final static TypeBase.TypeInteger SESSION_MAX_PER_IP = new TypeBase.TypeInteger( SESSION_BASE, "maxSessionsPerIP", 6 );
		public final static TypeBase.TypeInteger SESSION_CLEANUP_INTERVAL = new TypeBase.TypeInteger( SESSION_BASE, "cleanupInterval", 5 );

		public final static TypeBase SECURITY_BASE = new TypeBase( NET_BASE, "security" );
		public final static TypeBase.TypeBoolean SECURITY_DISABLE_REQUEST = new TypeBase.TypeBoolean( SECURITY_BASE, "disableRequestData", false );
	}
}
