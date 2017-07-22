package io.amelia.lang;

import io.amelia.support.ObjectStackerBase;

public class ObjectStackerException
{
	private ObjectStackerException()
	{

	}

	public static abstract class Error extends UtilException.Error
	{
		protected final ObjectStackerBase node;

		public <T extends ObjectStackerBase> Error( T node )
		{
			super();
			this.node = node;
		}

		public <T extends ObjectStackerBase> Error( T node, String message )
		{
			super( message );
			this.node = node;
		}

		public <T extends ObjectStackerBase> Error( T node, String message, Throwable cause )
		{
			super( message, cause );
			this.node = node;
		}

		public <T extends ObjectStackerBase> Error( T node, Throwable cause )
		{
			super( cause );
			this.node = node;
		}
	}

	public static abstract class Ignorable extends UtilException.Ignorable
	{
		protected final ObjectStackerBase node;

		public <T extends ObjectStackerBase> Ignorable( T node )
		{
			super();
			this.node = node;
		}

		public <T extends ObjectStackerBase> Ignorable( T node, String message )
		{
			super( message );
			this.node = node;
		}

		public <T extends ObjectStackerBase> Ignorable( T node, String message, Throwable cause )
		{
			super( message, cause );
			this.node = node;
		}

		public <T extends ObjectStackerBase> Ignorable( T node, Throwable cause )
		{
			super( cause );
			this.node = node;
		}
	}
}
