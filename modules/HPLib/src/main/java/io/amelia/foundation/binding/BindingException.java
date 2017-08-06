package io.amelia.foundation.binding;

import io.amelia.lang.ApplicationException;
import io.amelia.lang.IException;
import io.amelia.lang.ObjectStackerException;
import io.amelia.lang.ReportingLevel;

public class BindingException
{
	private BindingException()
	{

	}

	public static class Error extends ObjectStackerException.Error
	{
		public Error( BindingBase node )
		{
			super( node );
		}

		public Error( BindingBase node, String message )
		{
			super( node, message );
		}

		public Error( BindingBase node, String message, Throwable cause )
		{
			super( node, message, cause );
		}

		public Error( BindingBase node, Throwable cause )
		{
			super( node, cause );
		}

		public BindingBase getBindingNode()
		{
			return ( BindingBase ) node;
		}
	}

	public static class Ignorable extends ObjectStackerException.Ignorable
	{
		public Ignorable( BindingBase node )
		{
			super( node );
		}

		public Ignorable( BindingBase node, String message )
		{
			super( node, message );
		}

		public Ignorable( BindingBase node, String message, Throwable cause )
		{
			super( node, message, cause );
		}

		public Ignorable( BindingBase node, Throwable cause )
		{
			super( node, cause );
		}

		public BindingBase getBindingNode()
		{
			return ( BindingBase ) node;
		}
	}

	public static class Internal extends ApplicationException.Runtime implements IException
	{
		public Internal( String message )
		{
			super( ReportingLevel.E_ERROR, message );
		}

		public Internal( String message, Throwable cause )
		{
			super( ReportingLevel.E_ERROR, message, cause );
		}

		public Internal( Throwable cause )
		{
			super( ReportingLevel.E_ERROR, cause );
		}
	}
}
