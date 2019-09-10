package com.marchnetworks.security.ldap;

public class LdapCommandException extends Exception
{
	private static final long serialVersionUID = 3180415485671613511L;

	public LdapCommandException()
	{
	}

	public LdapCommandException( String msg )
	{
		super( msg );
	}

	public LdapCommandException( Throwable throwable )
	{
		super( throwable );
	}

	public LdapCommandException( String msg, Throwable cause )
	{
		super( msg, cause );
	}
}

