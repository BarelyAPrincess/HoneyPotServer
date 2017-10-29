package io.amelia.lang;

import io.amelia.support.data.StackerBase;

public class StackerException
{
	private StackerException()
	{

	}

	public static class Error extends UtilException.Error
	{
		protected final StackerBase node;

		public <T extends StackerBase> Error( T node )
		{
			super();
			this.node = node;
		}

		public <T extends StackerBase> Error( T node, String message )
		{
			super( message );
			this.node = node;
		}

		public <T extends StackerBase> Error( T node, String message, Throwable cause )
		{
			super( message, cause );
			this.node = node;
		}

		public <T extends StackerBase> Error( T node, Throwable cause )
		{
			super( cause );
			this.node = node;
		}
	}

	public static class Ignorable extends UtilException.Ignorable
	{
		protected final StackerBase node;

		public <T extends StackerBase> Ignorable( T node )
		{
			super();
			this.node = node;
		}

		public <T extends StackerBase> Ignorable( T node, String message )
		{
			super( message );
			this.node = node;
		}

		public <T extends StackerBase> Ignorable( T node, String message, Throwable cause )
		{
			super( message, cause );
			this.node = node;
		}

		public <T extends StackerBase> Ignorable( T node, Throwable cause )
		{
			super( cause );
			this.node = node;
		}
	}
}
