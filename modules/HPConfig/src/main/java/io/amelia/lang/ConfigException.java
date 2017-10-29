package io.amelia.lang;

import io.amelia.config.ConfigMap;

public class ConfigException
{
	private ConfigException()
	{

	}

	public static class Error extends StackerException.Error
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

	public static class Ignorable extends StackerException.Ignorable
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
