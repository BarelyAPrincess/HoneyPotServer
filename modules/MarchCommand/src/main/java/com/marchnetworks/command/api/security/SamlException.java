package com.marchnetworks.command.api.security;

public class SamlException extends Exception
{
	private static final long serialVersionUID = -2201507252897170513L;

	private SamlExceptionTypeEnum error;

	public SamlExceptionTypeEnum getError()
	{
		return error;
	}

	public static enum SamlExceptionTypeEnum
	{
		UNAUTHORIZED( 401 ),
		INTERNAL_SERVER_ERROR( 500 ),
		BAD_REQUEST( 400 );

		private int status;

		private SamlExceptionTypeEnum( int status )
		{
			this.status = status;
		}

		public int getStatus()
		{
			return status;
		}
	}

	public SamlException( SamlExceptionTypeEnum error )
	{
		this.error = error;
	}

	public SamlException( Throwable e, SamlExceptionTypeEnum error )
	{
		super( e );
		this.error = error;
	}

	public SamlException( String message, Throwable cause, SamlExceptionTypeEnum error )
	{
		super( message, cause );
		this.error = error;
	}

	public SamlException( String message, SamlExceptionTypeEnum error )
	{
		super( message );
		this.error = error;
	}

	public String toString()
	{
		return "SAMLException " + error.name() + " : " + getMessage();
	}
}
