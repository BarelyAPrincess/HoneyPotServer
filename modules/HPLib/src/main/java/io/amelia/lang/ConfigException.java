/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2018 Amelia DeWitt <me@ameliadewitt.com>
 * Copyright (c) 2018 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.lang;

import io.amelia.foundation.ConfigMap;

public class ConfigException
{
	private ConfigException()
	{

	}

	public static class Error extends ParcelableException.Error
	{
		public Error( ConfigMap node )
		{
			super( node );
		}

		public Error( ConfigMap node, String message )
		{
			super( node, message );
		}

		public Error( ConfigMap node, String message, Throwable cause )
		{
			super( node, message, cause );
		}

		public Error( ConfigMap node, Throwable cause )
		{
			super( node, cause );
		}

		public ConfigMap getConfigNode()
		{
			return ( ConfigMap ) node;
		}
	}

	public static class Ignorable extends ParcelableException.Ignorable
	{
		public Ignorable( ConfigMap node )
		{
			super( node );
		}

		public Ignorable( ConfigMap node, String message )
		{
			super( node, message );
		}

		public Ignorable( ConfigMap node, String message, Throwable cause )
		{
			super( node, message, cause );
		}

		public Ignorable( ConfigMap node, Throwable cause )
		{
			super( node, cause );
		}

		public ConfigMap getConfigNode()
		{
			return ( ConfigMap ) node;
		}
	}
}
