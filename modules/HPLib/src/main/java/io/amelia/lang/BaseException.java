package io.amelia.lang;

public class BaseException
{
	private BaseException()
	{

	}

	public static class Error extends ApplicationException.Error
	{
		public Error()
		{
			super( ReportingLevel.E_ERROR );
		}

		public Error( String message )
		{
			super( ReportingLevel.E_ERROR, message );
		}

		public Error( String message, Throwable cause )
		{
			super( ReportingLevel.E_ERROR, message, cause );
		}

		public Error( Throwable cause )
		{
			super( ReportingLevel.E_ERROR, cause );
		}
	}

	public static class Ignorable extends ApplicationException.Ignorable
	{
		public Ignorable()
		{
			super( ReportingLevel.E_IGNORABLE );
		}

		public Ignorable( String message )
		{
			super( ReportingLevel.E_IGNORABLE, message );
		}

		public Ignorable( String message, Throwable cause )
		{
			super( ReportingLevel.E_IGNORABLE, message, cause );
		}

		public Ignorable( Throwable cause )
		{
			super( ReportingLevel.E_IGNORABLE, cause );
		}
	}
}
