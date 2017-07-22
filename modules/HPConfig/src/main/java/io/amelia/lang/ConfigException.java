package io.amelia.lang;

import io.amelia.config.ConfigNode;

public class ConfigException
{
	private ConfigException()
	{

	}

	public static class Error extends ObjectStackerException.Error
	{
		public Error( ConfigNode node )
		{
			super( node );
		}

		public Error( ConfigNode node, String message )
		{
			super( node, message );
		}

		public Error( ConfigNode node, String message, Throwable cause )
		{
			super( node, message, cause );
		}

		public Error( ConfigNode node, Throwable cause )
		{
			super( node, cause );
		}

		public ConfigNode getConfigNode()
		{
			return ( ConfigNode ) node;
		}
	}

	public static class Ignorable extends ObjectStackerException.Ignorable
	{
		public Ignorable( ConfigNode node )
		{
			super( node );
		}

		public Ignorable( ConfigNode node, String message )
		{
			super( node, message );
		}

		public Ignorable( ConfigNode node, String message, Throwable cause )
		{
			super( node, message, cause );
		}

		public Ignorable( ConfigNode node, Throwable cause )
		{
			super( node, cause );
		}

		public ConfigNode getConfigNode()
		{
			return ( ConfigNode ) node;
		}
	}
}
